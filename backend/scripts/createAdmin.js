import mongoose from 'mongoose';
import dotenv from 'dotenv';
import User from '../models/userModel.js';

dotenv.config();

async function createAdmin() {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    const adminEmail = 'BachHoa@shop.com';

    // Kiểm tra xem admin đã tồn tại chưa
    const existing = await User.findOne({ email: adminEmail });
    if (existing) {
      console.log('⚠️ Admin đã tồn tại:', existing.email);
      process.exit();
    }

    const admin = await User.create({
      firstName: 'Admin',
      lastName: 'Bach Hoa',
      email: adminEmail,
      password: '12345678',
      role: 'admin'
    });

    console.log('✅ Tạo tài khoản admin thành công:', admin.email);
    process.exit();
  } catch (err) {
    console.error(err);
    process.exit(1);
  }
}

createAdmin();
