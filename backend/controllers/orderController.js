const Order = require('../models/orderModel');
const Product = require('../models/productModel');

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

    // Validate và chuẩn bị orderItems
    const validatedItems = [];
    
    for (const item of orderItems) {
      // Kiểm tra product tồn tại
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

      // Kiểm tra tồn kho
      if (product.quantity < item.quantity) {
        return res.status(400).json({ 
          message: `Sản phẩm ${item.name} chỉ còn ${product.quantity} sản phẩm trong kho` 
        });
      }

      validatedItems.push({
        product: item.product, // ← Đúng: dùng item.product, không phải item._id
        name: item.name,
        quantity: item.quantity,
        price: item.price,
        image: item.image,
      });

      // Trừ số lượng trong kho
      product.quantity -= item.quantity;
      await product.save();
    }

    // Tạo order
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
    
    res.status(201).json(createdOrder);
  } catch (error) {
    console.error('Create order error:', error);
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

    const order = await Order.findById(id);
    if (!order) {
      return res.status(404).json({ EC: -1, DT: null, EM: 'Không tìm thấy đơn hàng' });
    }

    order.status = status;
    await order.save();

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