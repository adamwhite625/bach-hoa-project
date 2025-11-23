// const express = require('express');
// const router = express.Router();
// const { getDiscounts, createDiscount, updateDiscount, deleteDiscount } = require('../controllers/discountController');
// const { protect, admin } = require('../middlewares/authMiddleware');

// router.route('/')
//   .get(protect, admin, getDiscounts)
//   .post(protect, admin, createDiscount);

// router.route('/:id')
//   .put(protect, admin, updateDiscount)
//   .delete(protect, admin, deleteDiscount);

// module.exports = router;


const express = require('express');
const router = express.Router();
const {
  getDiscounts,
  createDiscount,
  updateDiscount,
  deleteDiscount,
  previewDiscount,
} = require('../controllers/discountController');
const { protect, admin } = require('../middlewares/authMiddleware');

// List + create (admin)
router.route('/')
  .get(protect, admin, getDiscounts)
  .post(protect, admin, createDiscount);

// Preview a discount on a cart (logged in user)
router.post('/preview', protect, previewDiscount);

// Update / delete (admin)
router.route('/:id')
  .put(protect, admin, updateDiscount)
  .delete(protect, admin, deleteDiscount);

module.exports = router;