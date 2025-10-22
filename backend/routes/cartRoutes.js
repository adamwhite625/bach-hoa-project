const express = require('express');
const router = express.Router();
const { addToCart, getCart, updateCartItem, removeFromCart } = require('../controllers/cartController');
const { protect } = require('../middlewares/authMiddleware');

router.route('/')
    .post(protect, addToCart)
    .get(protect, getCart);

router.route('/:itemId')
    .put(protect, updateCartItem)
    .delete(protect, removeFromCart);

module.exports = router;