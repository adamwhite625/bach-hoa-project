const User = require('../models/userModel');
const jwt = require('jsonwebtoken');
const bcrypt = require('bcryptjs');

const generateToken = (user) => {
    // include small user payload to be compatible with frontend expecting token contains `user`
    const payload = {
        user: {
            id: user._id,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            role: user.role
        }
    };
    return jwt.sign(payload, process.env.JWT_SECRET, { expiresIn: '30d' });
};

const adminLogin = async (req, res) => {
    try {
        const { email, password } = req.body;
        
        // Tìm user với email và role Admin
        const admin = await User.findOne({ email, role: 'Admin' });
        
        if (!admin) {
            return res.status(401).json({ 
                message: 'Không tìm thấy tài khoản admin',
                EC: 1
            });
        }

        // Kiểm tra password
        try {
            const isMatch = await bcrypt.compare(password, admin.password);
            if (!isMatch) {
                return res.status(401).json({ 
                    message: 'Mật khẩu không đúng',
                    EC: 1
                });
            }
        } catch (err) {
            console.error('Lỗi so sánh password:', err);
            return res.status(500).json({ 
                message: 'Lỗi xác thực mật khẩu',
                EC: 1
            });
        }

        // Tạo token và trả về thông tin
        const token = generateToken(admin);
        res.json({
            EC: 0,
            access_token: token,
            user: {
                _id: admin._id,
                email: admin.email,
                role: admin.role,
                firstName: admin.firstName,
                lastName: admin.lastName
            }
        });
    } catch (error) {
        console.error('Admin login error:', error);
        res.status(500).json({ 
            message: 'Lỗi server',
            EC: 1
        });
    }
};

const registerUser = async (req, res) => {
    const { firstName, lastName, email, password } = req.body;
    try {
        const userExists = await User.findOne({ email });
        if (userExists) {
            return res.status(400).json({ message: 'Email này đã được đăng ký' });
        }
        
        const user = await User.create({
            firstName, 
            lastName, 
            email, 
            password,
            role: 'Customer' // Mặc định role là Customer
        });

        if (user) {
            res.status(201).json({
                _id: user._id,
                email: user.email,
                role: user.role,
                token: generateToken(user),
            });
        } else {
            res.status(400).json({ message: 'Invalid user data' });
        }
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const loginUser = async (req, res) => {
    const { email, password } = req.body;
    try {
        const user = await User.findOne({ email });
        if (user && (await user.matchPassword(password))) {
            res.json({
                _id: user._id,
                firstName: user.firstName,
                lastName: user.lastName,
                email: user.email,
                role: user.role,
                token: generateToken(user),
            });
        } else {
            res.status(401).json({ message: 'Invalid email or password' });
        }
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const resetPassword = async (req, res) => {
  const { email, newPassword } = req.body;
  try {
    const user = await User.findOne({ email });
    if (!user) {
      return res.status(404).json({ message: 'Email not found' });
    }

    const salt = await bcrypt.genSalt(10);
    user.password = await bcrypt.hash(newPassword, salt);
    await user.save();

    res.json({ message: 'Password updated successfully' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = { registerUser, loginUser, resetPassword, adminLogin };
