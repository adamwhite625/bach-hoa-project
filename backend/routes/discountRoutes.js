const express = require('express');
const router = express.Router();
const { getDiscounts, createDiscount, updateDiscount, deleteDiscount } = require('../controllers/discountController');
const { protect, admin } = require('../middlewares/authMiddleware');

router.route('/')
  .get(protect, admin, getDiscounts)
  .post(protect, admin, createDiscount);

router.route('/:id')
  .put(protect, admin, updateDiscount)
  .delete(protect, admin, deleteDiscount);

module.exports = router;
