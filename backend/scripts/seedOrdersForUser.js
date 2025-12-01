const mongoose = require("mongoose");
const dotenv = require("dotenv");
const User = require("../models/userModel");
const Order = require("../models/orderModel");
const Product = require("../models/productModel");

dotenv.config();

/**
 * Seed 2 orders for user with email thienmocay1235@gmail.com
 * Orders will be marked as delivered to add spending to user's totalSpent
 */
const seedOrdersForUser = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB...");

    // Find user by email
    const user = await User.findOne({ email: "thienmocay1235@gmail.com" });
    if (!user) {
      console.error("❌ User with email thienmocay1235@gmail.com not found!");
      process.exit(1);
    }

    console.log(`✓ Found user: ${user.firstName} ${user.lastName}`);

    // Get some products to add to orders
    const products = await Product.find({ isActive: true }).limit(5);
    if (products.length < 2) {
      console.error("❌ Not enough products in database!");
      process.exit(1);
    }

    // Order 1: 250,000 VND
    const order1Items = [
      {
        product: products[0]._id,
        name: products[0].name,
        quantity: 2,
        price: 75000,
        image: products[0].image,
      },
    ];

    const order1 = new Order({
      user: user._id,
      orderItems: order1Items,
      shippingAddress: {
        fullName: `${user.firstName} ${user.lastName}`,
        phone: user.phone || "0123456789",
        address: user.shippingAddress?.address || "123 Main Street",
        city: user.shippingAddress?.city || "Ho Chi Minh",
      },
      paymentMethod: "Credit Card",
      paymentResult: {
        id: "txn_001",
        status: "COMPLETED",
        update_time: new Date().toISOString(),
        email_address: user.email,
      },
      itemsPrice: 150000,
      taxPrice: 0,
      shippingPrice: 0,
      totalPrice: 150000,
      status: "Delivered",
      isPaid: true,
      paidAt: new Date(Date.now() - 7 * 24 * 60 * 60 * 1000), // 7 days ago
      isDelivered: true,
      deliveredAt: new Date(Date.now() - 5 * 24 * 60 * 60 * 1000), // 5 days ago
    });

    const createdOrder1 = await order1.save();
    console.log(`✓ Created order 1: ${createdOrder1._id} (150,000 VND)`);

    // Order 2: 350,000 VND
    const order2Items = [
      {
        product: products[1]._id,
        name: products[1].name,
        quantity: 1,
        price: 200000,
        image: products[1].image,
      },
      {
        product: products[2]._id,
        name: products[2].name,
        quantity: 2,
        price: 75000,
        image: products[2].image,
      },
    ];

    const order2 = new Order({
      user: user._id,
      orderItems: order2Items,
      shippingAddress: {
        fullName: `${user.firstName} ${user.lastName}`,
        phone: user.phone || "0123456789",
        address: user.shippingAddress?.address || "123 Main Street",
        city: user.shippingAddress?.city || "Ho Chi Minh",
      },
      paymentMethod: "Credit Card",
      paymentResult: {
        id: "txn_002",
        status: "COMPLETED",
        update_time: new Date().toISOString(),
        email_address: user.email,
      },
      itemsPrice: 350000,
      taxPrice: 0,
      shippingPrice: 0,
      totalPrice: 350000,
      status: "Delivered",
      isPaid: true,
      paidAt: new Date(Date.now() - 3 * 24 * 60 * 60 * 1000), // 3 days ago
      isDelivered: true,
      deliveredAt: new Date(Date.now() - 1 * 24 * 60 * 60 * 1000), // 1 day ago
    });

    const createdOrder2 = await order2.save();
    console.log(`✓ Created order 2: ${createdOrder2._id} (350,000 VND)`);

    // Update user spending
    const { addSpending } = require("../services/loyaltyService");

    console.log("\n📊 Updating user loyalty tier...");
    await addSpending(user._id, 150000);
    const result = await addSpending(user._id, 350000);

    console.log(`\n✓ User loyalty updated:`);
    console.log(`  - Old tier: ${result.oldTier}`);
    console.log(`  - New tier: ${result.newTier}`);
    console.log(`  - Total spent: ${result.loyaltyInfo.totalSpent} VND`);
    console.log(`  - Tier changed: ${result.tierChanged}`);

    console.log("\n✅ Orders seeded successfully!");
    process.exit();
  } catch (error) {
    console.error("❌ Error seeding orders:", error.message);
    process.exit(1);
  }
};

seedOrdersForUser();
