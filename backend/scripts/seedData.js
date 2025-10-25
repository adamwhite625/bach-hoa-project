

const mongoose = require('mongoose');
const dotenv = require('dotenv');
const Category = require('../models/categoryModel');
const Product = require('../models/productModel');
const cloudinary = require('../config/cloudinary');
const fs = require('fs');
const path = require('path');

dotenv.config();

// Data mẫu với URL Cloudinary trực tiếp
const sampleData = [
    {
        category: {
            name: "Thịt tươi",
            description: "Các loại thịt tươi sống",
            imageUrl: "https://res.cloudinary.com/daeeumk1i/image/upload/v1/categories/thit-tuoi.jpg" // URL trực tiếp từ Cloudinary
        },
        products: [
            {
                name: "Thịt bò Úc",
                price: 280000,
                description: "Thịt bò Úc tươi ngon, thích hợp cho bít tết",
                brand: "Meat Master",
                countInStock: 50,
                imageUrl: "https://res.cloudinary.com/daeeumk1i/image/upload/v1/products/thit-bo.jpg" // URL trực tiếp từ Cloudinary
            },
            {
                name: "Thịt heo ba rọi",
                price: 150000,
                description: "Thịt heo ba rọi tươi ngon",
                brand: "Meat Master",
                countInStock: 100,
                imagePath: "sample_images/pork.jpg"
            }
        ]
    },
    {
        category: {
            name: "Rau củ",
            description: "Rau củ tươi sạch",
            imagePath: "sample_images/vegetables.jpg"
        },
        products: [
            {
                name: "Cải thìa",
                price: 15000,
                description: "Cải thìa tươi sạch",
                brand: "Organic Farm",
                countInStock: 200,
                imagePath: "sample_images/bokchoy.jpg"
            },
            {
                name: "Cà rốt",
                price: 12000,
                description: "Cà rốt tươi ngon",
                brand: "Organic Farm",
                countInStock: 150,
                imagePath: "sample_images/carrot.jpg"
            }
        ]
    },
    {
        category: {
            name: "Gia vị",
            description: "Các loại gia vị nấu ăn",
            imagePath: "sample_images/spices.jpg"
        },
        products: [
            {
                name: "Bột ớt Hàn Quốc",
                price: 45000,
                description: "Bột ớt cay nồng Hàn Quốc",
                brand: "Korean Spice",
                countInStock: 80,
                imagePath: "sample_images/chili.jpg"
            },
            {
                name: "Tiêu đen xay",
                price: 35000,
                description: "Tiêu đen xay nhuyễn",
                brand: "VietSpice",
                countInStock: 100,
                imagePath: "sample_images/pepper.jpg"
            }
        ]
    }
];

// Không cần function upload nữa vì dùng URL trực tiếp
// Format của Cloudinary URL:
// https://res.cloudinary.com/daeeumk1i/image/upload/v1/folder_name/image_name.jpg
// Ví dụ:
// https://res.cloudinary.com/daeeumk1i/image/upload/v1/categories/thit-tuoi.jpg
// https://res.cloudinary.com/daeeumk1i/image/upload/v1/products/thit-bo.jpg

// Function chính để seed data
const seedData = async () => {
    try {
        await mongoose.connect(process.env.MONGO_URI);
        console.log('Connected to MongoDB...');

        // Kiểm tra và thêm dữ liệu mới
        console.log('Checking and adding new data...');

        // Thêm categories và products mới
        for (const item of sampleData) {
            // Kiểm tra xem category đã tồn tại chưa
            let category = await Category.findOne({ name: item.category.name });
            
            if (!category) {
                // Tạo category mới nếu chưa tồn tại
                category = await Category.create({
                    name: item.category.name,
                    description: item.category.description,
                    image: item.category.imageUrl // Sử dụng URL trực tiếp
                });
                console.log(`Created new category: ${item.category.name}`);
            } else {
                console.log(`Category already exists: ${item.category.name}`);
            }

            // Thêm products cho category
            for (const productData of item.products) {
                const productImageUrl = await uploadImage(productData.imagePath);
                
                // Kiểm tra xem sản phẩm đã tồn tại chưa
                const existingProduct = await Product.findOne({ 
                    name: productData.name,
                    category: category._id 
                });

                if (!existingProduct) {
                    // Tạo sản phẩm mới nếu chưa tồn tại
                    await Product.create({
                        ...productData,
                        category: category._id,
                        image: productImageUrl
                    });
                    console.log(`Created new product: ${productData.name}`);
                } else {
                    console.log(`Product already exists: ${productData.name}`);
                }
            }
        }

        console.log('Data seeded successfully!');
        process.exit();
    } catch (error) {
        console.error('Error seeding data:', error);
        process.exit(1);
    }
};

seedData();