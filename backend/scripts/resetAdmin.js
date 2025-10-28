const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('../models/userModel');

dotenv.config();

async function resetAdmin() {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        const adminEmail = 'nguyenhaithien2k5@gmail.com';

        // Xóa admin cũ nếu có
        await User.deleteOne({ email: adminEmail });

        // Tạo admin mới với role = 'Admin'
        const admin = await User.create({
            firstName: 'Nguyen',
            lastName: 'Hai Thien',
            email: adminEmail,
            password: '12345678',
            role: 'Admin'
        });

        console.log('✅ Đã tạo lại tài khoản admin thành công:', admin.email);
        process.exit();
    } catch (err) {
        console.error('❌ Lỗi:', err);
        process.exit(1);
    }
}

resetAdmin();