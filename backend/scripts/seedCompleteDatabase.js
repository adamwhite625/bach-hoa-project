/**
 * Complete Database Seed Script
 * Seeds all collections with complete database data
 * Usage: node scripts/seedCompleteDatabase.js
 */

require("dotenv").config();
const mongoose = require("mongoose");
const fs = require("fs");
const path = require("path");

// Import all models
const User = require("../models/userModel");
const Product = require("../models/productModel");
const Category = require("../models/categoryModel");
const Order = require("../models/orderModel");
const Cart = require("../models/cartModel");
const Discount = require("../models/discountModel");
const Notification = require("../models/notificationModel");
const DiscountUsage = require("../models/discountUsageModel");

const SEED_FILE = path.join(__dirname, "../exports/fullDatabase.json");

/**
 * Seed a collection
 */
const seedCollection = async (Model, collectionName, data) => {
  try {
    if (!data || data.length === 0) {
      console.log(`⚠️  No data for ${collectionName}`);
      return 0;
    }

    // Delete existing data
    await Model.deleteMany({});
    console.log(`🗑️  Cleared ${collectionName}`);

    // Insert new data
    const result = await Model.insertMany(data, { ordered: false });
    console.log(`✅ ${collectionName}: ${result.length} documents seeded`);
    return result.length;
  } catch (error) {
    console.error(`❌ Error seeding ${collectionName}:`, error.message);
    return 0;
  }
};

/**
 * Main seed function
 */
const seedDatabase = async () => {
  try {
    // Check if seed file exists
    if (!fs.existsSync(SEED_FILE)) {
      console.error(`❌ Seed file not found: ${SEED_FILE}`);
      console.log(
        "💡 Run 'node scripts/exportDatabase.js' first to create seed data"
      );
      process.exit(1);
    }

    console.log("🔌 Connecting to MongoDB...");
    await mongoose.connect(process.env.MONGO_URI);
    console.log("✅ Connected to MongoDB\n");

    console.log("📖 Reading seed file...");
    const seedData = JSON.parse(fs.readFileSync(SEED_FILE, "utf8"));
    console.log("✅ Seed file loaded\n");

    // Seed all collections
    let totalSeeded = 0;

    console.log("🌱 Seeding collections...\n");

    // Order matters for foreign keys
    totalSeeded += await seedCollection(
      Category,
      "categories",
      seedData.categories
    );
    totalSeeded += await seedCollection(Product, "products", seedData.products);
    totalSeeded += await seedCollection(
      Discount,
      "discounts",
      seedData.discounts
    );
    totalSeeded += await seedCollection(User, "users", seedData.users);
    totalSeeded += await seedCollection(Order, "orders", seedData.orders);
    totalSeeded += await seedCollection(Cart, "carts", seedData.carts);
    totalSeeded += await seedCollection(
      Notification,
      "notifications",
      seedData.notifications
    );
    totalSeeded += await seedCollection(
      DiscountUsage,
      "discountusages",
      seedData.discountusages
    );

    // Summary
    console.log("\n" + "=".repeat(50));
    console.log("✅ DATABASE SEEDING COMPLETE");
    console.log("=".repeat(50));
    console.log(`Total documents seeded: ${totalSeeded}`);

    if (seedData.categories)
      console.log(`  • Categories: ${seedData.categories.length}`);
    if (seedData.products)
      console.log(`  • Products: ${seedData.products.length}`);
    if (seedData.discounts)
      console.log(`  • Discounts: ${seedData.discounts.length}`);
    if (seedData.users) console.log(`  • Users: ${seedData.users.length}`);
    if (seedData.orders) console.log(`  • Orders: ${seedData.orders.length}`);
    if (seedData.carts) console.log(`  • Carts: ${seedData.carts.length}`);
    if (seedData.notifications)
      console.log(`  • Notifications: ${seedData.notifications.length}`);
    if (seedData.discountusages)
      console.log(`  • Discount Usages: ${seedData.discountusages.length}`);

    console.log("=".repeat(50) + "\n");

    await mongoose.disconnect();
    console.log("🔌 Disconnected from MongoDB");
    process.exit(0);
  } catch (error) {
    console.error("❌ Seeding failed:", error);
    await mongoose.disconnect();
    process.exit(1);
  }
};

// Run seed
seedDatabase();
