const crypto = require('crypto');
const User = require('../models/userModel');
const nodemailer = require('nodemailer');
const { promisify } = require('util');

// Util to hash token for storage
const hashToken = (token) => {
    return crypto.createHash('sha256').update(token).digest('hex');
};

// Configure nodemailer transporter using env vars
// If you prefer SendGrid or another provider, replace this transporter config
const createTransporter = () => {
    if (!process.env.GMAIL_USER || !process.env.GMAIL_APP_PASSWORD) {
        console.warn('GMAIL_USER or GMAIL_APP_PASSWORD not set. Emails will fail to send.');
    }
    return nodemailer.createTransport({
        service: 'gmail',
        auth: {
            user: process.env.GMAIL_USER,
            pass: process.env.GMAIL_APP_PASSWORD,
        },
    });
};

// POST /api/auth/forgot-password
const forgotPassword = async (req, res) => {
    const { email } = req.body;
    if (!email) return res.status(400).json({ message: 'Email is required' });

    const user = await User.findOne({ email });
    // Always respond with generic message to avoid enumeration
    const genericMessage = 'If an account with that email exists, you will receive an email with reset instructions.';

    if (!user) {
        return res.status(200).json({ message: genericMessage });
    }

    // Create 6-digit OTP
    const otp = Math.floor(100000 + Math.random() * 900000).toString();
    const otpHash = hashToken(otp);
    const expiry = Date.now() + 15 * 60 * 1000; // 15 minutes

    user.resetPasswordToken = otpHash;
    user.resetPasswordExpires = new Date(expiry);
    await user.save();

    const message = `
Xin chào,

Bạn vừa yêu cầu đặt lại mật khẩu cho tài khoản của mình.

Mã xác thực (OTP) của bạn là: ${otp}

Mã này có hiệu lực trong 15 phút.
Nếu bạn không yêu cầu đặt lại mật khẩu, vui lòng bỏ qua email này.

Trân trọng,
${process.env.APP_NAME || 'Bach Hoa Online'}`;

    try {
        const transporter = createTransporter();
        await transporter.sendMail({
            from: process.env.GMAIL_USER,
            to: email,
            subject: 'Mã xác thực đặt lại mật khẩu',
            text: message,
        });
    } catch (err) {
        console.error('Error sending reset email:', err);
        // Do not reveal error to client
    }

    return res.status(200).json({ message: genericMessage });
};

// POST /api/auth/reset-password
const resetPassword = async (req, res) => {
    const { email, token, password } = req.body;
    if (!email || !token || !password) return res.status(400).json({ message: 'Vui lòng nhập đầy đủ email, mã OTP và mật khẩu mới' });

    const tokenHash = hashToken(token);
    const user = await User.findOne({ email, resetPasswordToken: tokenHash, resetPasswordExpires: { $gt: new Date() } });

    if (!user) {
        return res.status(400).json({ message: 'Mã OTP không đúng hoặc đã hết hạn' });
    }

    // Update password
    user.password = password; // userSchema pre-save will hash it
    user.resetPasswordToken = undefined;
    user.resetPasswordExpires = undefined;
    await user.save();

    // Optionally, send confirmation email
    try {
        const transporter = createTransporter();
        await transporter.sendMail({
            from: process.env.GMAIL_USER,
            to: email,
            subject: 'Your password has been changed',
            text: 'This is a confirmation that the password for your account has just been changed.'
        });
    } catch (err) {
        console.error('Error sending confirmation email:', err);
    }

    return res.status(200).json({ message: 'Mật khẩu đã được cập nhật thành công' });
};

module.exports = { forgotPassword, resetPassword };
