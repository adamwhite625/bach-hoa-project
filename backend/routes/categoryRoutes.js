const express = require('express');
const router = express.Router();
const { getCategories, createCategory } = require('../controllers/categoryController');
const { protect, admin } = require('../middlewares/authMiddleware');

router.route('/').get(getCategories).post(protect, admin, createCategory);

module.exports = router;