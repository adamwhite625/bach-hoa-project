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
  getAvailableDiscounts,
  validateDiscount,
  previewDiscount,
} = require('../controllers/discountController');
const { protect, admin } = require('../middlewares/authMiddleware');

// User routes (protected but not admin)
router.get('/available', protect, getAvailableDiscounts);
router.post('/validate', protect, validateDiscount);
router.post('/preview', protect, previewDiscount);

// Admin routes
router.route('/')
  .get(protect, admin, getDiscounts)
  .post(protect, admin, createDiscount);

router.route('/:id')
  .put(protect, admin, updateDiscount)
  .delete(protect, admin, deleteDiscount);

module.exports = router;