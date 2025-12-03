const cron = require('node-cron');
const mongoose = require('mongoose');
const Order = require('../models/orderModel');
const Product = require('../models/productModel');
const Notification = require('../models/notificationModel');

// Chạy mỗi 5 phút để kiểm tra unpaid orders
const startUnpaidOrdersCronJob = () => {
    cron.schedule('*/5 * * * *', async () => {
        try {
            console.log('⏰ [CRON] Checking for unpaid orders...');

            const fifteenMinutesAgo = new Date(Date.now() - 15 * 60 * 1000);

            // Tìm orders:
            // - Chưa thanh toán (isPaid = false)
            // - Có paymentResult.id (đã tạo payment)
            // - Tạo từ 15 phút trước
            const unpaidOrders = await Order.find({
                isPaid: false,
                'paymentResult.id': { $exists: true, $ne: null },
                createdAt: { $lt: fifteenMinutesAgo }
            }).populate('user', 'firstName lastName email');

            if (unpaidOrders.length === 0) {
                console.log('✅ [CRON] No unpaid orders to cancel');
                return;
            }

            console.log(`⚠️ [CRON] Found ${unpaidOrders.length} unpaid orders to cancel`);

            const session = await mongoose.startSession();

            for (const order of unpaidOrders) {
                try {
                    await session.startTransaction();

                    console.log(`  🔄 [CRON] Processing order ${order._id}...`);

                    // Restore product quantities
                    for (const item of order.orderItems) {
                        await Product.findByIdAndUpdate(
                            item.product,
                            { $inc: { quantity: item.qty } },
                            { session }
                        );
                        console.log(`    ✅ Restored ${item.qty} items to product ${item.product}`);
                    }

                    // Delete order
                    await Order.findByIdAndDelete(order._id).session(session);
                    console.log(`    ✅ Deleted unpaid order ${order._id}`);

                    await session.commitTransaction();

                    // Send notification
                    if (order.user) {
                        try {
                            await Notification.create({
                                user: order.user._id,
                                type: 'order_cancelled',
                                title: 'Đơn hàng đã bị hủy',
                                message: `Đơn hàng #${order._id.toString().slice(-8).toUpperCase()} đã bị hủy do không thanh toán trong 15 phút`,
                                data: {
                                    orderId: order._id.toString(),
                                    reason: 'payment_timeout'
                                }
                            });
                            console.log(`    ✅ Notification sent to user ${order.user._id}`);
                        } catch (notifError) {
                            console.error(`    ⚠️ Failed to send notification:`, notifError.message);
                        }
                    }

                } catch (error) {
                    await session.abortTransaction();
                    console.error(`  ❌ Failed to cancel order ${order._id}:`, error.message);
                }
            }

            session.endSession();
            console.log(`✅ [CRON] Cancelled ${unpaidOrders.length} unpaid orders`);

        } catch (error) {
            console.error('❌ [CRON] Error in unpaid orders job:', error.message);
        }
    });

    console.log('✅ Unpaid orders cron job started (runs every 5 minutes)');
};

module.exports = { startUnpaidOrdersCronJob };
