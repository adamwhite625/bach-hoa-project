const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Product = require('../models/productModel');
const User = require('../models/userModel');

dotenv.config();

const products = [
    {
        name: "Dưa hấu đỏ",
        description: "Dưa hấu đỏ tươi ngon",
        price: 14900,
        unit: "Kg",
        countInStock: 100,
        sku: "WTM001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/dua_hau_i4cw8r.webp",
        category: "68ff7d680f92094310959e37" // ID của category "Rau củ, trái cây"
    },
    {
        name: "Cải ngồng Hưng Phát",
        description: "Cải ngồng gói 500g - Hưng Phát",
        price: 23500,
        unit: "Gói",
        countInStock: 50,
        sku: "VEG001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/cai_sah0ae.webp",
        category: "68ff7d680f92094310959e37" // ID của category "Rau củ, trái cây"
    },
    {
        name: "Thanh Long ruột đỏ",
        description: "Thanh long ruột đỏ tươi ngon",
        price: 11900,
        unit: "Kg",
        countInStock: 80,
        sku: "DRG001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/thanh_long_o5jo83.webp",
        category: "68ff7d680f92094310959e37" // ID của category "Rau củ, trái cây"
    },
    {
        name: "Cà rốt Đà Lạt",
        description: "Cà rốt Đà Lạt tươi ngon",
        price: 32900,
        unit: "Kg",
        countInStock: 70,
        sku: "CRT001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/ca_rot_vcjtqu.webp",
        category: "68ff7d680f92094310959e37" // ID của category "Rau củ, trái cây"
    },
    {
        name: "Nước chấm cá cơm 3 Miền",
        description: "Nước chấm cá cơm 3 Miền pet 800ml",
        price: 22000,
        unit: "Chai",
        countInStock: 200,
        sku: "SPC001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/nuoc_mam_3mien_uypln5.webp",
        category: "68ff7d680f92094310959e39" // ID của category "Gia vị, thực phẩm khô"
    },
    {
        name: "Cá bạc má",
        description: "Cá bạc má tươi ngon",
        price: 89000,
        unit: "Kg",
        countInStock: 30,
        sku: "FSH001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/ca_bac_ma_ai3qjh.webp",
        category: "68ff7d680f92094310959e38" // ID của category "Thịt, trứng, hải sản"
    },
    {
        name: "Ba rọi heo CJ",
        description: "Ba rọi heo 400g - CJ",
        price: 78000,
        unit: "Vỉ",
        countInStock: 40,
        sku: "PRK001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484205/ba_roi_tknpsa.webp",
        category: "68ff7d680f92094310959e38" // ID của category "Thịt, trứng, hải sản"
    },
    {
        name: "File ức gà 3F",
        description: "File ức gà không da vỉ 500g - 3F",
        price: 50000,
        unit: "Vỉ",
        countInStock: 60,
        sku: "CHK001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/uc_ga_k6a17p.webp",
        category: "68ff7d680f92094310959e38" // ID của category "Thịt, trứng, hải sản"
    }
];

// Tạo dữ liệu người dùng mới có avatar
const newUser = {
    firstName: "Trần",
    lastName: "Văn B",
    email: "tranvanb@example.com",
    password: "123",
    role: "Customer",
    avatar: "https://avatar.iran.liara.run/public/48",
    phone: "0987654321",
    addresses: [
        {
            fullName: "Trần Văn B",
            phone: "0987654321",
            street: "456 Đường XYZ",
            city: "Quận 2, TP.HCM",
            isDefault: true
        }
    ]
};

async function seedProductsAndUser() {
    try {
        // Kết nối database
        await mongoose.connect(process.env.MONGO_URI);
        console.log('Connected to MongoDB');

        // Xóa products cũ và thêm products mới
        await Product.deleteMany({});
        const createdProducts = await Product.insertMany(products);
        console.log('Products seeded successfully');

        // Tạo user mới
        const user = await User.create(newUser);
        console.log('New user created:', user.email);

        console.log('All data seeded successfully');
        process.exit();
    } catch (error) {
        console.error('Error seeding data:', error);
        process.exit(1);
    }
}

seedProductsAndUser();