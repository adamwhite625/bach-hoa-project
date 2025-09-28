const Order = require('../models/orderModel');

const addOrderItems = async (req, res) => {
    const { orderItems, shippingAddress, paymentMethod, itemsPrice, taxPrice, shippingPrice, totalPrice } = req.body;

    if (orderItems && orderItems.length === 0) {
        return res.status(400).json({ message: 'No order items' });
    }
    
    // Ở đây cần thêm logic trừ số lượng sản phẩm trong kho
    const order = new Order({
        orderItems: orderItems.map(item => ({...item, product: item._id, _id: undefined})),
        user: req.user._id,
        shippingAddress, paymentMethod, itemsPrice, taxPrice, shippingPrice, totalPrice
    });

    const createdOrder = await order.save();
    res.status(201).json(createdOrder);
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
    const orders = await Order.find({}).populate('user', 'id firstName');
    res.json(orders);
};

module.exports = { addOrderItems, getOrderById, getMyOrders, getOrders };