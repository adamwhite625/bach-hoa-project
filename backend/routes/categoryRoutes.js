const express = require('express');
const router = express.Router();
const { 
    getCategories, 
    createCategory,
    updateCategory,
    deleteCategory,
    getCategoryProducts
} = require('../controllers/categoryController');
const { protect, admin } = require('../middlewares/authMiddleware');
const upload = require('../middlewares/uploadMiddleware');

router.route('/')
    .get(getCategories)
    .post(protect, admin, upload.single('image'), createCategory);

router.route('/:id')
    .put(protect, admin, updateCategory)
    .delete(protect, admin, deleteCategory);

router.get('/:id/products', getCategoryProducts);

module.exports = router;