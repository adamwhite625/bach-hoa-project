const express = require('express');
const router = express.Router();
const { registerUser, loginUser, adminLogin } = require('../controllers/authController');
const { forgotPassword, resetPassword } = require('../controllers/passwordController');
const rateLimit = require('express-rate-limit');

// Limit forgot-password to prevent abuse
const forgotLimiter = rateLimit({
	windowMs: 60 * 60 * 1000, // 1 hour window
	max: 5, // start blocking after 5 requests
	message: { message: 'Too many password reset attempts from this IP, please try again after an hour' }
});

router.post('/register', registerUser);
router.post('/login', loginUser);
router.post('/admin/login', adminLogin);
router.post('/forgot-password', forgotLimiter, forgotPassword);
router.post('/reset-password', resetPassword);

module.exports = router;