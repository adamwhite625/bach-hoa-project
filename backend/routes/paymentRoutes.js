const express = require('express');
const router = express.Router();
const { protect } = require('../middlewares/authMiddleware');
const { 
    createZaloPayPayment,
    zaloPayCallback,
    queryZaloPayStatus
} = require('../controllers/paymentController.js');

// ZaloPay routes
router.post('/create-zalopay', protect, createZaloPayPayment);
router.post('/zalopay-callback', zaloPayCallback);
router.get('/zalopay-status/:app_trans_id', queryZaloPayStatus);
router.get('/zalopay-status-by-order/:order_id', protect, queryZaloPayStatus); // Query by orderId

module.exports = router;