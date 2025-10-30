const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Category = require('../models/categoryModel');
const Product = require('../models/productModel');
const User = require('../models/userModel');
const Order = require('../models/orderModel');

dotenv.config();

const categories = [
    {
        name: "Rau củ, trái cây",
        description: "Các loại rau củ và trái cây tươi",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/vegetables_fruits_nxftak.jpg"
    },
    {
        name: "Thịt, trứng, hải sản",
        description: "Các loại thịt, trứng và hải sản tươi sống",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/meat_fish_egg_rqgpzl.jpg"
    },
    {
        name: "Gia vị, thực phẩm khô",
        description: "Gia vị và các loại thực phẩm khô",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/spices_dry_food_kxhqtl.jpg"
    },
    {
        name: "Sữa, sản phẩm từ sữa",
        description: "Sữa và các sản phẩm được chế biến từ sữa",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/milk_dairy_products_pqxwvn.jpg"
    }
];

const products = [
    {
        name: "Dưa hấu đỏ",
        description: "Dưa hấu đỏ tươi ngon",
        price: 14900,
        unit: "Kg",
        countInStock: 100,
        sku: "WTM001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/watermelon_fqcr3l.jpg",
        category: "Rau củ, trái cây"
    },
    {
        name: "Cải ngồng Hưng Phát",
        description: "Cải ngồng gói 500g - Hưng Phát",
        price: 23500,
        unit: "Gói",
        countInStock: 50,
        sku: "VEG001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/cai_ngong_sxkpql.jpg",
        category: "Rau củ, trái cây"
    },
    {
        name: "Thanh Long ruột đỏ",
        description: "Thanh long ruột đỏ tươi ngon",
        price: 11900,
        unit: "Kg",
        countInStock: 80,
        sku: "DRG001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/thanh_long_ruot_do_yqgxpn.jpg",
        category: "Rau củ, trái cây"
    },
    {
        name: "Cà rốt Đà Lạt",
        description: "Cà rốt Đà Lạt tươi ngon",
        price: 32900,
        unit: "Kg",
        countInStock: 70,
        sku: "CRT001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/ca_rot_dalat_hqvpxk.jpg",
        category: "Rau củ, trái cây"
    },
    {
        name: "Nước chấm cá cơm 3 Miền",
        description: "Nước chấm cá cơm 3 Miền pet 800ml",
        price: 22000,
        unit: "Chai",
        countInStock: 200,
        sku: "SPC001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/nuoc_mam_3_mien_nwhpxk.jpg",
        category: "Gia vị, thực phẩm khô"
    },
    {
        name: "Cá bạc má",
        description: "Cá bạc má tươi ngon",
        price: 89000,
        unit: "Kg",
        countInStock: 30,
        sku: "FSH001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/ca_bac_ma_kxhqtl.jpg",
        category: "Thịt, trứng, hải sản"
    },
    {
        name: "Ba rọi heo CJ",
        description: "Ba rọi heo 400g - CJ",
        price: 78000,
        unit: "Vỉ",
        countInStock: 40,
        sku: "PRK001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/ba_roi_heo_cj_pqxwvn.jpg",
        category: "Thịt, trứng, hải sản"
    },
    {
        name: "File ức gà 3F",
        description: "File ức gà không da vỉ 500g - 3F",
        price: 50000,
        unit: "Vỉ",
        countInStock: 60,
        sku: "CHK001",
        image: "https://res.cloudinary.com/daeeumk1i/image/upload/v1761288815/products/uc_ga_3f_nxftak.jpg",
        category: "Thịt, trứng, hải sản"
    }
];

// Tạo dữ liệu người dùng mẫu
const sampleUser = {
    firstName: "Nguyễn",
    lastName: "Văn A",
    email: "nguyenvana4@example.com",
    password: "123",
    role: "Customer"
};

// Tạo dữ liệu đơn hàng mẫu (sẽ được cập nhật với ID thực sau khi tạo user và products)
const sampleOrders = [
    {
        items: [
            {
                quantity: 2,
                // product ID sẽ được cập nhật sau
            },
            {
                quantity: 1,
                // product ID sẽ được cập nhật sau
            }
        ],
        totalAmount: 53800, // 2 dưa hấu + 1 cải ngồng
        status: "Delivered",
        shippingAddress: {
            fullName: "Nguyễn Văn A",
            phone: "0123456789",
            address: "123 Đường ABC",
            city: "Quận 1, TP.HCM"
        },
        paymentMethod: "COD"
    },
    {
        items: [
            {
                quantity: 1,
                // product ID sẽ được cập nhật sau
            },
            {
                quantity: 2,
                // product ID sẽ được cập nhật sau
            }
        ],
        totalAmount: 161900, // 1 cá bạc má + 2 ba rọi heo
        status: "Processing",
        shippingAddress: {
            fullName: "Nguyễn Văn A",
            phone: "0123456789",
            address: "123 Đường ABC",
            city: "Quận 1, TP.HCM"
        },
        paymentMethod: "COD"
    }
];

async function seedData() {
    try {
        // Kết nối database
        await mongoose.connect(process.env.MONGO_URI);
        console.log('Connected to MongoDB');
        
        // Xóa toàn bộ collections
        await mongoose.connection.dropDatabase();
        console.log('Dropped old database');

        // Xóa dữ liệu cũ
        await Category.deleteMany({});
        await Product.deleteMany({});
        await User.deleteMany({ role: 'User' }); // Chỉ xóa user thường, giữ lại admin
        await Order.deleteMany({});

        // Thêm categories
        const createdCategories = await Category.insertMany(categories);
        console.log('Categories seeded');

        // Thêm products với category ID thích hợp
        const productsWithCategories = await Promise.all(products.map(async (product) => {
            const category = await Category.findOne({ name: product.category });
            return {
                ...product,
                category: category._id
            };
        }));
        const createdProducts = await Product.insertMany(productsWithCategories);
        console.log('Products seeded');

        // Tạo user mẫu
        const user = await User.create(sampleUser);
        console.log('Sample user created');

        // Tạo orders với product IDs thực
        const order1Products = [createdProducts[0], createdProducts[1]]; // dưa hấu và cải ngồng
        const order2Products = [createdProducts[5], createdProducts[6]]; // cá bạc má và ba rọi heo

        const ordersWithIds = [
            {
                ...sampleOrders[0],
                user: user._id,
                items: [
                    { product: order1Products[0]._id, quantity: 2, price: order1Products[0].price },
                    { product: order1Products[1]._id, quantity: 1, price: order1Products[1].price }
                ]
            },
            {
                ...sampleOrders[1],
                user: user._id,
                items: [
                    { product: order2Products[0]._id, quantity: 1, price: order2Products[0].price },
                    { product: order2Products[1]._id, quantity: 2, price: order2Products[1].price }
                ]
            }
        ];

        await Order.insertMany(ordersWithIds);
        console.log('Orders seeded');

        console.log('All data seeded successfully');
        process.exit();
    } catch (error) {
        console.error('Error seeding data:', error);
        process.exit(1);
    }
}

seedData();