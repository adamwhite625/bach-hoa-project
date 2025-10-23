const express = require('express');
const dotenv = require('dotenv');
const cors = require('cors');
const path = require('path');
const connectDB = require('./config/db');

// Tải các biến môi trường
dotenv.config();

// Kết nối tới MongoDB
connectDB();

const app = express();

// Sử dụng các middleware
app.use(cors());
app.use(express.json()); // Để phân tích cú pháp body của request dưới dạng JSON
app.use('/uploads', express.static(path.join(__dirname, 'uploads')));

// Route chính
app.get('/', (req, res) => {
    res.send('E-commerce API is up and running!');
});

// Sử dụng các routes đã định nghĩa
app.use('/api/auth', require('./routes/authRoutes'));
app.use('/api/users', require('./routes/userRoutes'));
app.use('/api/categories', require('./routes/categoryRoutes'));
app.use('/api/products', require('./routes/productRoutes'));
app.use('/api/orders', require('./routes/orderRoutes'));

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => console.log(`Server is running on port ${PORT}`));
