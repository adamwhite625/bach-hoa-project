const mongoose = require('mongoose');
const dotenv = require('dotenv');
const User = require('../models/userModel');

dotenv.config();

async function createAdmin() {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    const adminEmail = 'nguyenhaithien2k5@gmail.com';

    // Kiểm tra xem admin đã tồn tại chưa
    const existing = await User.findOne({ email: adminEmail });
    if (existing) {
      console.log('⚠️ Admin đã tồn tại:', existing.email);
      process.exit();
    }

    const admin = await User.create({
      firstName: 'Nguyen',
      lastName: 'Hai Thien',
      email: adminEmail,
      password: '12345678',
      role: 'Admin',
      avatar: 'https://res.cloudinary.com/root/image/upload/v1/avatars/default-avatar.png'
    });

    console.log('✅ Tạo tài khoản admin thành công:', admin.email);
    process.exit();
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

createAdmin();
