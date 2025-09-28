const express = require('express');
const router = express.Router();
const { getProducts, getProductById, createProduct, createProductReview } = require('../controllers/productController');
const { protect, admin } = require('../middlewares/authMiddleware');

router.route('/').get(getProducts).post(protect, admin, createProduct);
router.route('/:id/reviews').post(protect, createProductReview);
router.route('/:id').get(getProductById);

module.exports = router;