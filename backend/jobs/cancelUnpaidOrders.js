const cron = require('node-cron');
const mongoose = require('mongoose');
const Order = require('../models/orderModel');
const Product = require('../models/productModel');
const Notification = require('../models/notificationModel');

// Chạy mỗi 5 phút để kiểm tra unpaid orders
const startUnpaidOrdersCronJob = () => {
    cron.schedule('*/5 * * * *', async () => {
        try {
            const fifteenMinutesAgo = new Date(Date.now() - 15 * 60 * 1000);            // Tìm orders:
            // - Chưa thanh toán (isPaid = false)
            // - Có paymentResult.id (đã tạo payment)
            // - Tạo từ 15 phút trước
            const unpaidOrders = await Order.find({
                isPaid: false,
                'paymentResult.id': { $exists: true, $ne: null },
                createdAt: { $lt: fifteenMinutesAgo }
            }).populate('user', 'firstName lastName email');

            if (unpaidOrders.length === 0) {
                return;
            }

            const session = await mongoose.startSession();

            for (const order of unpaidOrders) {
                try {
                    await session.startTransaction();

                    // Restore product quantities
                    for (const item of order.orderItems) {
                        await Product.findByIdAndUpdate(
                            item.product,
                            { $inc: { quantity: item.qty } },
                            { session }
                        );
                    }

                    // Delete order
                    await Order.findByIdAndDelete(order._id).session(session);

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
                        } catch (notifError) {
                            // Ignore notification errors
                        }
                    }

                } catch (error) {
                    await session.abortTransaction();
                }
            }

            session.endSession();

        } catch (error) {
            // Ignore cron job errors
        }
    });
};

module.exports = { startUnpaidOrdersCronJob };
