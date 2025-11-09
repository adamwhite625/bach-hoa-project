const mongoose = require("mongoose");
const dotenv = require("dotenv");
const Product = require("../models/productModel");
const User = require("../models/userModel");
const Order = require("../models/orderModel");

dotenv.config();

async function seedOrders() {
  try {
    // Kết nối database
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB");

    // Tìm user mẫu
    const user = await User.findOne({ email: "nguyenvana4@example.com" });
    if (!user) {
      console.log("User not found");
      process.exit(1);
    }

    // Tìm các sản phẩm cần thiết
    const watermelon = await Product.findOne({ sku: "WTM001" }); // Dưa hấu
    const vegetable = await Product.findOne({ sku: "VEG001" }); // Cải ngồng
    const fish = await Product.findOne({ sku: "FSH001" }); // Cá bạc má
    const pork = await Product.findOne({ sku: "PRK001" }); // Ba rọi heo

    if (!watermelon || !vegetable || !fish || !pork) {
      console.log("Some products not found");
      process.exit(1);
    }

    // Xóa đơn hàng cũ của user này
    await Order.deleteMany({ user: user._id });

    // Tính toán giá trị cho đơn hàng 1
    const order1ItemsPrice = watermelon.price * 2 + vegetable.price * 1;
    const order1ShippingPrice = 20000;
    const order1TaxPrice = Math.round(order1ItemsPrice * 0.1);
    const order1TotalPrice =
      order1ItemsPrice + order1ShippingPrice + order1TaxPrice;

    // Tính toán giá trị cho đơn hàng 2
    const order2ItemsPrice = fish.price * 1 + pork.price * 2;
    const order2ShippingPrice = 20000;
    const order2TaxPrice = Math.round(order2ItemsPrice * 0.1);
    const order2TotalPrice =
      order2ItemsPrice + order2ShippingPrice + order2TaxPrice;

    const orders = [
      {
        user: user._id,
        orderItems: [
          {
            product: watermelon._id,
            name: watermelon.name,
            image: watermelon.image,
            price: watermelon.price,
            quantity: 2,
          },
          {
            product: vegetable._id,
            name: vegetable.name,
            image: vegetable.image,
            price: vegetable.price,
            quantity: 1,
          },
        ],
        shippingAddress: {
          fullName: "Nguyễn Văn A",
          phone: "0123456789",
          address: "123 Đường ABC",
          city: "Quận 1, TP.HCM",
        },
        paymentMethod: "COD",
        itemsPrice: order1ItemsPrice,
        taxPrice: order1TaxPrice,
        shippingPrice: order1ShippingPrice,
        totalPrice: order1TotalPrice,
        status: "Delivered",
        isPaid: true,
        paidAt: new Date(),
        isDelivered: true,
        deliveredAt: new Date(),
      },
      {
        user: user._id,
        orderItems: [
          {
            product: fish._id,
            name: fish.name,
            image: fish.image,
            price: fish.price,
            quantity: 1,
          },
          {
            product: pork._id,
            name: pork.name,
            image: pork.image,
            price: pork.price,
            quantity: 2,
          },
        ],
        shippingAddress: {
          fullName: "Nguyễn Văn A",
          phone: "0123456789",
          address: "123 Đường ABC",
          city: "Quận 1, TP.HCM",
        },
        paymentMethod: "COD",
        itemsPrice: order2ItemsPrice,
        taxPrice: order2TaxPrice,
        shippingPrice: order2ShippingPrice,
        totalPrice: order2TotalPrice,
        status: "Processing",
        isPaid: false,
        isDelivered: false,
      },
    ];

    await Order.insertMany(orders);
    console.log("Orders seeded successfully");

    process.exit();
  } catch (error) {
    console.error("Error seeding orders:", error);
    process.exit(1);
  }
}

seedOrders();
