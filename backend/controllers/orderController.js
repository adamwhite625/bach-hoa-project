const Order = require('../models/orderModel');
const Product = require('../models/productModel');
const Notification = require('../models/notificationModel');

// const addOrderItems = async (req, res) => {
//     const { orderItems, shippingAddress, paymentMethod, itemsPrice, taxPrice, shippingPrice, totalPrice } = req.body;

//     console.log(orderItems, shippingAddress, paymentMethod, itemsPrice, taxPrice, shippingPrice, totalPrice, req.user._id);
//     if (orderItems && orderItems.length === 0) {
//         return res.status(400).json({ message: 'No order items' });
//     }
    
//     // Ở đây cần thêm logic trừ số lượng sản phẩm trong kho
//     const order = new Order({
//         orderItems: orderItems.map(item => ({...item, product: item._id, _id: undefined})),
//         user: req.user._id,
//         shippingAddress, paymentMethod, itemsPrice, taxPrice, shippingPrice, totalPrice
//     });

//     const createdOrder = await order.save();
//     res.status(201).json(createdOrder);
// };

const addOrderItems = async (req, res) => {
  try {
    const { 
      orderItems, 
      shippingAddress, 
      paymentMethod, 
      itemsPrice, 
      taxPrice, 
      shippingPrice, 
      totalPrice 
    } = req.body;

    if (!orderItems || orderItems.length === 0) {
      return res.status(400).json({ message: 'Không có sản phẩm trong đơn hàng' });
    }

    const validatedItems = [];
    
    for (const item of orderItems) {
      const product = await Product.findById(item.product);
      
      if (!product) {
        return res.status(404).json({ 
          message: `Sản phẩm ${item.name} không tồn tại` 
        });
      }

      if (!product.isActive) {
        return res.status(400).json({ 
          message: `Sản phẩm ${item.name} đã ngưng kinh doanh` 
        });
      }

      if (product.quantity < item.quantity) {
        return res.status(400).json({ 
          message: `Sản phẩm ${item.name} chỉ còn ${product.quantity} sản phẩm trong kho` 
        });
      }

      validatedItems.push({
        product: item.product,
        name: item.name,
        quantity: item.quantity,
        price: item.price,
        image: item.image,
      });

      product.quantity -= item.quantity;
      await product.save();
    }

    const order = new Order({
      orderItems: validatedItems,
      user: req.user._id,
      shippingAddress,
      paymentMethod,
      itemsPrice,
      taxPrice,
      shippingPrice,
      totalPrice,
    });

    const createdOrder = await order.save();
    
    // Create notification
    try {
      await Notification.create({
        user: req.user._id,
        type: 'order_created',
        title: 'Đơn hàng đã được tạo',
        message: `Đơn hàng của bạn đã được tạo thành công với tổng giá trị ${totalPrice.toLocaleString('vi-VN')}₫`,
        data: {
          orderId: createdOrder._id,
          orderCode: createdOrder._id.toString().slice(-8).toUpperCase(),
          totalPrice,
          itemCount: orderItems.length,
        }
      });
    } catch (notifError) {
      // Notification error should not fail the order creation
    }
    
    res.status(201).json({
      _id: createdOrder._id,
      orderItems: createdOrder.orderItems,
      shippingAddress: createdOrder.shippingAddress,
      paymentMethod: createdOrder.paymentMethod,
      itemsPrice: createdOrder.itemsPrice,
      taxPrice: createdOrder.taxPrice,
      shippingPrice: createdOrder.shippingPrice,
      totalPrice: createdOrder.totalPrice,
      status: createdOrder.status,
      isPaid: createdOrder.isPaid,
      isDelivered: createdOrder.isDelivered,
      createdAt: createdOrder.createdAt,
      message: 'Đơn hàng đã được tạo thành công'
    });
  } catch (error) {
    res.status(500).json({ 
      message: 'Lỗi tạo đơn hàng', 
      error: error.message 
    });
  }
};

const getOrderById = async (req, res) => {
    const order = await Order.findById(req.params.id).populate('user', 'firstName lastName email');
    if (order) {
        res.json(order);
    } else {
        res.status(404).json({ message: 'Order not found' });
    }
};

const getMyOrders = async (req, res) => {
    const orders = await Order.find({ user: req.user._id });
    res.json(orders);
};

const getOrders = async (req, res) => {
  try {
    const orders = await Order.find({})
      .populate('user', 'firstName lastName email')
      .populate('orderItems.product', 'name image')
      .sort({ createdAt: -1 });
    
    res.json({ EC: 0, DT: orders, EM: 'Lấy danh sách đơn hàng thành công' });
  } catch (error) {
    console.error('Get orders error:', error);
    res.status(500).json({ EC: -1, DT: null, EM: 'Lỗi server' });
  }
};

const updateOrderStatus = async (req, res) => {
  try {
    const { id } = req.params;
    const { status } = req.body;

    const order = await Order.findById(id)
      .populate('user', 'firstName lastName email');
      
    if (!order) {
      return res.status(404).json({ EC: -1, DT: null, EM: 'Không tìm thấy đơn hàng' });
    }

    const oldStatus = order.status;
    order.status = status;

    // Update delivery status
    if (status === 'Delivered' && !order.isDelivered) {
      order.isDelivered = true;
      order.deliveredAt = Date.now();
      
      // For COD orders, mark as paid when delivered
      if (order.paymentMethod === 'COD' && !order.isPaid) {
        order.isPaid = true;
        order.paidAt = Date.now();
        console.log('💰 COD order marked as paid on delivery');
      }
    }

    await order.save();

    // Tạo notification với message phù hợp (lưu vào DB để user xem sau)
    try {
      const statusMessages = {
        'Pending': 'Đơn hàng của bạn đang chờ xác nhận',
        'Processing': 'Đơn hàng của bạn đang được xử lý',
        'Shipped': 'Đơn hàng của bạn đang được vận chuyển',
        'Delivered': 'Đơn hàng của bạn đã được giao thành công',
        'Cancelled': 'Đơn hàng của bạn đã bị hủy',
      };

      await Notification.create({
        user: order.user._id,
        type: 'order_status',
        title: 'Cập nhật đơn hàng',
        message: statusMessages[status] || `Trạng thái đơn hàng: ${status}`,
        data: {
          orderId: order._id,
          orderCode: order._id.toString().slice(-8).toUpperCase(),
          oldStatus,
          newStatus: status,
        }
      });
      console.log('✅ Status update notification saved to database');
    } catch (notifError) {
      console.error('Failed to save notification:', notifError);
    }

    // Populate lại để trả về đầy đủ dữ liệu
    const updatedOrder = await Order.findById(id)
      .populate('user', 'firstName lastName email')
      .populate('orderItems.product', 'name image');

    res.json({ EC: 0, DT: updatedOrder, EM: 'Cập nhật trạng thái thành công' });
  } catch (error) {
    console.error('Update order status error:', error);
    res.status(500).json({ EC: -1, DT: null, EM: 'Lỗi cập nhật trạng thái' });
  }
};

const deleteOrder = async (req, res) => {
  try {
    const { id } = req.params;

    const order = await Order.findById(id);
    if (!order) {
      return res.status(404).json({ EC: -1, DT: null, EM: 'Không tìm thấy đơn hàng' });
    }

    await Order.findByIdAndDelete(id);
    res.json({ EC: 0, DT: { id }, EM: 'Xóa đơn hàng thành công' });
  } catch (error) {
    console.error('Delete order error:', error);
    res.status(500).json({ EC: -1, DT: null, EM: 'Lỗi xóa đơn hàng' });
  }
};

module.exports = { addOrderItems, getOrderById, getMyOrders, getOrders, updateOrderStatus, deleteOrder };