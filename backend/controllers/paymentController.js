const axios = require('axios');
const crypto = require('crypto');
const moment = require('moment');
const mongoose = require('mongoose');
const Order = require('../models/orderModel');
const Product = require('../models/productModel');
const Notification = require('../models/notificationModel');

// ZaloPay Sandbox Config
const config = {
    app_id: "2553",
    key1: "PcY4iZIKFCIdgZvA6ueMcMHHUbRLYjPL",
    key2: "kLtgPl8HHhfvMuDHPwKfgfsY4Ydm9eIz",
    endpoint: "https://sb-openapi.zalopay.vn/v2/create"
};

const createZaloPayPayment = async (req, res) => {
    let orderToRollback = null;
    const session = await mongoose.startSession();
    
    try {
        const { amount, orderInfo, orderId } = req.body;

        console.log('📝 Creating ZaloPay payment:', { orderId, amount });

        if (!amount || amount <= 0) {
            return res.status(400).json({ message: 'Số tiền không hợp lệ' });
        }

        if (!orderId) {
            return res.status(400).json({ message: 'Thiếu mã đơn hàng' });
        }

        // Kiểm tra order tồn tại
        const order = await Order.findById(orderId);
        if (!order) {
            console.error('❌ Order not found:', orderId);
            return res.status(404).json({ message: 'Không tìm thấy đơn hàng' });
        }

        if (order.isPaid) {
            console.error('❌ Order already paid:', orderId);
            return res.status(400).json({ message: 'Đơn hàng đã được thanh toán' });
        }

        // Kiểm tra nếu order đã có paymentTransId (đang xử lý thanh toán)
        if (order.paymentTransId) {
            console.error('❌ Payment already in progress:', order.paymentTransId);
            return res.status(400).json({ 
                message: 'Đơn hàng đang xử lý thanh toán. Vui lòng không gửi lại yêu cầu.' 
            });
        }

        // Lưu order để rollback nếu cần
        orderToRollback = order;

        const embed_data = {
            redirecturl: "bachhoa://payment-result",
            orderId: orderId
        };

        const items = [{
            itemid: orderId,
            itemname: orderInfo || `Thanh toan don hang ${orderId}`,
            itemprice: amount,
            itemquantity: 1
        }];

        const transID = Math.floor(Math.random() * 1000000);
        const app_trans_id = `${moment().format('YYMMDD')}_${transID}`;

        const order_data = {
            app_id: config.app_id,
            app_trans_id: app_trans_id,
            app_user: order.user.toString(),
            app_time: Date.now(),
            item: JSON.stringify(items),
            embed_data: JSON.stringify(embed_data),
            amount: amount,
            description: `Bach Hoa - ${orderInfo || 'Thanh toan don hang'}`,
            bank_code: "",
            callback_url: `${process.env.BACKEND_URL || 'http://localhost:5000'}/api/payment/zalopay-callback`,
        };

        // Create MAC
        const data = config.app_id + "|" + order_data.app_trans_id + "|" + order_data.app_user + "|" + order_data.amount 
            + "|" + order_data.app_time + "|" + order_data.embed_data + "|" + order_data.item;
        order_data.mac = crypto.createHmac('sha256', config.key1).update(data).digest('hex');

        console.log('🔄 Calling ZaloPay API...');
        const response = await axios.post(config.endpoint, null, { params: order_data });

        // ⚠️ UNCOMMENT DÒNG DƯỚI ĐỂ TEST ROLLBACK
        // response.data.return_code = 2;
        // response.data.return_message = 'TEST: Force payment failure';

        console.log('📬 ZaloPay response:', {
            return_code: response.data.return_code,
            return_message: response.data.return_message
        });

        if (response.data.return_code === 1) {
            // ✅ Lưu app_trans_id vào paymentResult.id
            order.paymentResult = {
                id: app_trans_id,
                status: 'pending',
                update_time: new Date().toISOString()
            };
            await order.save();

            console.log('✅ Payment created successfully:', app_trans_id);
            console.log('✅ Order updated with paymentResult.id:', order.paymentResult.id);

            res.json({
                paymentUrl: response.data.order_url,
                zp_trans_token: response.data.zp_trans_token,
                app_trans_id: app_trans_id,
                message: 'Tạo thanh toán ZaloPay thành công'
            });
        } else {
            // ❌ ZaloPay trả về lỗi → Rollback order
            console.error('❌ ZaloPay payment creation failed:', response.data);
            
            await session.startTransaction();
            try {
                await rollbackOrder(order, session);
                await session.commitTransaction();
                console.log('✅ Rollback completed successfully');
            } catch (rollbackError) {
                await session.abortTransaction();
                throw rollbackError;
            }
            
            res.status(400).json({
                message: 'Lỗi tạo thanh toán ZaloPay',
                error: response.data.return_message || 'Vui lòng thử lại sau'
            });
        }
    } catch (error) {
        console.error('❌ ZaloPay error:', error.response?.data || error.message);
        
        // Rollback order nếu có lỗi
        if (orderToRollback) {
            try {
                await session.startTransaction();
                await rollbackOrder(orderToRollback, session);
                await session.commitTransaction();
                console.log('✅ Rollback completed after exception');
            } catch (rollbackError) {
                await session.abortTransaction();
                console.error('❌ Rollback error:', rollbackError.message);
            }
        }
        
        res.status(500).json({
            message: 'Lỗi tạo thanh toán ZaloPay',
            error: error.message
        });
    } finally {
        session.endSession();
    }
};

// Helper function: Rollback order và restore product quantities
async function rollbackOrder(order, session = null) {
    try {
        console.log(`🔄 [ROLLBACK] Starting rollback for order ${order._id}...`);
        
        // Kiểm tra xem order có tồn tại không
        const existingOrder = await Order.findById(order._id).session(session);
        if (!existingOrder) {
            console.log(`⚠️ [ROLLBACK] Order ${order._id} already deleted, skipping...`);
            return;
        }

        // Kiểm tra xem order đã thanh toán chưa (tránh rollback order đã thanh toán)
        if (existingOrder.isPaid) {
            console.log(`⚠️ [ROLLBACK] Order ${order._id} is already paid, CANNOT rollback!`);
            throw new Error('Cannot rollback a paid order');
        }

        // Restore product quantities
        for (const item of order.orderItems) {
            const product = await Product.findByIdAndUpdate(
                item.product,
                { $inc: { quantity: item.qty } },
                { new: true, session }
            );
            
            if (product) {
                console.log(`✅ [ROLLBACK] Restored ${item.qty} items to product "${item.name}" (ID: ${item.product})`);
            } else {
                console.log(`⚠️ [ROLLBACK] Product ${item.product} not found, skipping...`);
            }
        }
        
        // Lưu user ID trước khi xóa order
        const userId = order.user;
        const orderIdStr = order._id.toString();
        
        // Delete order
        const deleteResult = await Order.findByIdAndDelete(order._id).session(session);
        
        if (deleteResult) {
            console.log(`✅ [ROLLBACK] Order ${order._id} deleted successfully`);
        } else {
            console.error(`❌ [ROLLBACK] Failed to delete order ${order._id}`);
            throw new Error('Failed to delete order');
        }
        
        // Tạo notification cho user (SAU KHI commit transaction)
        // NOTE: Notification sẽ được tạo bên ngoài transaction để tránh conflict
        if (session && !session.inTransaction()) {
            try {
                await Notification.create({
                    user: userId,
                    type: 'order_cancelled',
                    title: 'Đơn hàng đã bị hủy',
                    message: `Đơn hàng #${orderIdStr.slice(-8).toUpperCase()} đã bị hủy do lỗi thanh toán`,
                    data: {
                        orderId: orderIdStr,
                        reason: 'payment_failed'
                    }
                });
                console.log(`✅ [ROLLBACK] Cancellation notification sent to user ${userId}`);
            } catch (notifError) {
                console.log(`⚠️ [ROLLBACK] Failed to send notification:`, notifError.message);
            }
        }
        
    } catch (error) {
        console.error(`❌ [ROLLBACK] Failed for order ${order._id}:`, error.message);
        throw error;
    }
}

// Callback từ ZaloPay
const zaloPayCallback = async (req, res) => {
    const session = await mongoose.startSession();
    
    try {
        let result = {};
        console.log('📬 [CALLBACK] ZaloPay callback:', req.body);

        const dataStr = req.body.data;
        const reqMac = req.body.mac;

        const mac = crypto
            .createHmac('sha256', config.key2)
            .update(dataStr)
            .digest('hex');

        // Kiểm tra callback hợp lệ
        if (reqMac !== mac) {
            result.return_code = -1;
            result.return_message = 'mac not equal';
        } else {
            const dataJson = JSON.parse(dataStr);
            const app_trans_id = dataJson.app_trans_id;

            // Tìm order bằng app_trans_id
            const order = await Order.findOne({ 'paymentResult.id': app_trans_id })
                .populate('user', 'firstName lastName email');

            if (!order) {
                console.error('❌ [CALLBACK] Order not found for app_trans_id:', app_trans_id);
                result.return_code = 2;
                result.return_message = 'Order not found';
            } else if (dataJson.return_code === 1) {
                // ✅ Thanh toán thành công
                if (!order.isPaid) {
                    order.isPaid = true;
                    order.paidAt = Date.now();
                    order.paymentResult.status = 'completed';
                    order.paymentResult.update_time = new Date().toISOString();
                    await order.save();

                    console.log(`✅ [CALLBACK] Payment SUCCESS - Order ${order._id} → isPaid=true`);

                    // Tạo notification
                    try {
                        await Notification.create({
                            user: order.user._id,
                            type: 'order_status',
                            title: 'Thanh toán thành công',
                            message: `Đơn hàng của bạn đã được thanh toán thành công với số tiền ${dataJson.amount.toLocaleString('vi-VN')}₫`,
                            data: {
                                orderId: order._id,
                                orderCode: order._id.toString().slice(-8).toUpperCase(),
                                amount: dataJson.amount,
                                transId: app_trans_id,
                            }
                        });
                    } catch (notifError) {
                        console.error('❌ Notification error:', notifError.message);
                    }
                } else {
                    console.log(`⚠️ [CALLBACK] Order ${order._id} already paid, skipping...`);
                }

                result.return_code = 1;
                result.return_message = 'success';
            } else {
                // ❌ Thanh toán thất bại → ROLLBACK
                console.log(`❌ [CALLBACK] Payment FAILED - Order ${order._id}, Code: ${dataJson.return_code}`);
                
                await session.startTransaction();
                try {
                    await rollbackOrder(order, session);
                    await session.commitTransaction();
                    console.log(`✅ [CALLBACK] Rollback completed for order ${order._id}`);
                } catch (rollbackError) {
                    await session.abortTransaction();
                    console.error(`❌ [CALLBACK] Rollback failed:`, rollbackError.message);
                }
                
                result.return_code = 1;
                result.return_message = 'success';
            }
        }

        res.json(result);
    } catch (error) {
        console.error('❌ ZaloPay callback error:', error);
        res.status(500).json({
            return_code: 0,
            return_message: error.message
        });
    } finally {
        session.endSession();
    }
};

// Query trạng thái thanh toán
const queryZaloPayStatus = async (req, res) => {
    try {
        const { app_trans_id, order_id } = req.params;

        let order = null;
        let app_trans_id_to_query = app_trans_id;

        console.log('🔍 [QUERY] Request params:', { app_trans_id, order_id });

        // Tìm order bằng orderId hoặc app_trans_id
        if (order_id) {
            // Query bằng orderId
            order = await Order.findById(order_id);
            if (order && order.paymentResult?.id) {
                app_trans_id_to_query = order.paymentResult.id;
                console.log('🔍 [QUERY] Found order by orderId, app_trans_id:', app_trans_id_to_query);
            } else if (!order) {
                console.error('❌ [QUERY] Order not found for orderId:', order_id);
                return res.status(404).json({
                    success: false,
                    message: 'Không tìm thấy đơn hàng'
                });
            } else {
                console.error('❌ [QUERY] Order found but no payment transaction ID:', order_id);
                return res.status(400).json({
                    success: false,
                    message: 'Đơn hàng chưa có thông tin thanh toán'
                });
            }
        } else if (app_trans_id) {
            // Query bằng app_trans_id
            app_trans_id_to_query = app_trans_id;
            console.log('🔍 [QUERY] Checking payment status for app_trans_id:', app_trans_id);
        } else {
            return res.status(400).json({
                success: false,
                message: 'Thiếu thông tin đơn hàng hoặc mã giao dịch'
            });
        }

        const postData = {
            app_id: config.app_id,
            app_trans_id: app_trans_id_to_query,
        };

        const data = postData.app_id + "|" + postData.app_trans_id + "|" + config.key1;
        postData.mac = crypto.createHmac('sha256', config.key1).update(data).digest('hex');

        const response = await axios.post('https://sb-openapi.zalopay.vn/v2/query', null, {
            params: postData
        });

        console.log('📬 [QUERY] ZaloPay response:', response.data);

        // Tìm order nếu chưa có (trong trường hợp query bằng app_trans_id)
        if (!order) {
            order = await Order.findOne({ 'paymentResult.id': app_trans_id_to_query });
            
            if (!order) {
                console.error('❌ [QUERY] Order not found for app_trans_id:', app_trans_id_to_query);
                return res.status(404).json({
                    success: false,
                    message: 'Không tìm thấy đơn hàng',
                    data: response.data
                });
            }
        }

        if (response.data.return_code === 1) {
            // ✅ Thanh toán thành công
            if (!order.isPaid) {
                order.isPaid = true;
                order.paidAt = Date.now();
                order.paymentResult.status = 'completed';
                order.paymentResult.update_time = new Date().toISOString();
                await order.save();
                
                console.log(`✅ [QUERY] Order ${order._id} updated → isPaid=true`);

                // Tạo notification
                try {
                    await Notification.create({
                        user: order.user,
                        type: 'order_status',
                        title: 'Thanh toán thành công',
                        message: `Đơn hàng của bạn đã được thanh toán thành công với số tiền ${response.data.amount.toLocaleString('vi-VN')}₫`,
                        data: {
                            orderId: order._id,
                            orderCode: order._id.toString().slice(-8).toUpperCase(),
                            amount: response.data.amount,
                            transId: app_trans_id_to_query,
                        }
                    });
                    console.log(`✅ [QUERY] Notification sent to user ${order.user}`);
                } catch (notifError) {
                    console.error('❌ [QUERY] Notification error:', notifError.message);
                }
            } else {
                console.log(`⚠️ [QUERY] Order ${order._id} already marked as paid`);
            }

            res.json({
                success: true,
                message: 'Thanh toán thành công',
                orderId: order._id,
                isPaid: order.isPaid,
                data: response.data
            });
        } else if (response.data.return_code === 2) {
            // ❌ Thanh toán thất bại
            console.log(`❌ [QUERY] Payment FAILED for order ${order._id}`);
            
            res.json({
                success: false,
                message: 'Thanh toán thất bại',
                orderId: order._id,
                isPaid: order.isPaid,
                data: response.data
            });
        } else {
            // ⏳ Đang xử lý
            console.log(`⏳ [QUERY] Payment PROCESSING for order ${order._id}`);
            
            res.json({
                success: false,
                message: 'Đơn hàng đang được xử lý',
                orderId: order._id,
                isPaid: order.isPaid,
                data: response.data
            });
        }
    } catch (error) {
        console.error('❌ [QUERY] Error:', error.message);
        res.status(500).json({
            success: false,
            message: 'Lỗi truy vấn trạng thái thanh toán',
            error: error.message
        });
    }
};

module.exports = {
    createZaloPayPayment,
    zaloPayCallback,
    queryZaloPayStatus,
};