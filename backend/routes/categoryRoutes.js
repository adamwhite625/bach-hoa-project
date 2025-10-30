const express = require('express');
const router = express.Router();
const { getCategories, createCategory, deleteCategory, updateCategory, getCategoryProducts } = require('../controllers/categoryController');
const { protect, admin } = require('../middlewares/authMiddleware');
const upload = require('../middlewares/uploadMiddleware');

router.route('/').get(getCategories).post(protect, admin, createCategory);
router.route('/:id').delete(protect, admin, deleteCategory).put(protect, admin, updateCategory);
router.route('/:id/products').get(getCategoryProducts);
router.route('/:id').delete(protect, admin, deleteCategory);

module.exports = router;