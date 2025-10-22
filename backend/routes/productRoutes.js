const express = require('express');
const router = express.Router();
const { 
    getProducts, 
    getProductById, 
    createProduct, 
    createProductReview, 
    updateProduct,
    searchProducts 
} = require('../controllers/productController');
const { protect, admin } = require('../middlewares/authMiddleware');
const uploadMemory = require('../middlewares/multerMemory');

// Search route must be before /:id route to not treat 'search' as an ID
router.get('/search', searchProducts);
router.route('/').get(getProducts).post(protect, admin, uploadMemory.array('images', 8), createProduct);
router.route('/:id/reviews').post(protect, createProductReview);
router.route('/:id').get(getProductById).put(protect, admin, uploadMemory.array('images', 8), updateProduct);

module.exports = router;