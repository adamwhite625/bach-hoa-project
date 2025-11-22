const mongoose = require("mongoose");
const dotenv = require("dotenv");
const Discount = require("../models/discountModel");

dotenv.config();

const discountData = [
  {
    code: "WELCOME10",
    description: "Giảm 10% cho khách hàng mới",
    type: "percentage",
    value: 10,
    minOrderValue: 50000,
    maxDiscountAmount: 50000,
    usageLimit: 1000,
    usedCount: 0,
    startDate: new Date("2025-11-01"),
    endDate: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "SAVE50K",
    description: "Giảm 50.000đ cho đơn hàng từ 200.000đ",
    type: "fixed",
    value: 50000,
    minOrderValue: 200000,
    maxDiscountAmount: 50000,
    usageLimit: 500,
    usedCount: 120,
    startDate: new Date("2025-11-15"),
    endDate: new Date("2025-12-15"),
    isActive: true,
  },
  {
    code: "FLASH20",
    description: "Flash sale - Giảm 20% (Có hạn)",
    type: "percentage",
    value: 20,
    minOrderValue: 100000,
    maxDiscountAmount: 100000,
    usageLimit: 300,
    usedCount: 250,
    startDate: new Date("2025-11-20"),
    endDate: new Date("2025-11-22"),
    isActive: true,
  },
  {
    code: "LOYALTY15",
    description: "Giảm 15% cho khách hàng thân thiết",
    type: "percentage",
    value: 15,
    minOrderValue: 150000,
    maxDiscountAmount: 80000,
    usageLimit: 2000,
    usedCount: 350,
    startDate: new Date("2025-10-01"),
    endDate: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "FREESHIP",
    description: "Miễn phí vận chuyển cho đơn hàng trên 300.000đ",
    type: "fixed",
    value: 30000,
    minOrderValue: 300000,
    maxDiscountAmount: 30000,
    usageLimit: 800,
    usedCount: 650,
    startDate: new Date("2025-11-01"),
    endDate: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "HOLIDAY25",
    description: "Giảm 25% cuối năm",
    type: "percentage",
    value: 25,
    minOrderValue: 250000,
    maxDiscountAmount: 150000,
    usageLimit: 600,
    usedCount: 420,
    startDate: new Date("2025-11-20"),
    endDate: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "VIP30",
    description: "Giảm 30% dành cho VIP members",
    type: "percentage",
    value: 30,
    minOrderValue: 300000,
    maxDiscountAmount: 200000,
    usageLimit: 200,
    usedCount: 85,
    startDate: new Date("2025-09-01"),
    endDate: new Date("2026-02-28"),
    isActive: true,
  },
  {
    code: "NEWYEAR100",
    description: "Giảm 100.000đ đầu năm mới",
    type: "fixed",
    value: 100000,
    minOrderValue: 500000,
    maxDiscountAmount: 100000,
    usageLimit: 300,
    usedCount: 0,
    startDate: new Date("2026-01-01"),
    endDate: new Date("2026-01-10"),
    isActive: false,
  },
  {
    code: "SUMMER5",
    description: "Giảm 5% cho sản phẩm mùa hè",
    type: "percentage",
    value: 5,
    minOrderValue: 75000,
    maxDiscountAmount: 25000,
    usageLimit: 2000,
    usedCount: 1250,
    startDate: new Date("2025-05-01"),
    endDate: new Date("2025-08-31"),
    isActive: false,
  },
  {
    code: "BULK30K",
    description: "Giảm 30.000đ cho đơn hàng trên 400.000đ",
    type: "fixed",
    value: 30000,
    minOrderValue: 400000,
    maxDiscountAmount: 30000,
    usageLimit: 400,
    usedCount: 210,
    startDate: new Date("2025-11-01"),
    endDate: new Date("2025-12-31"),
    isActive: true,
  },
];

const seedDiscounts = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB...");

    // Xóa các discount cũ (tùy chọn)
    // await Discount.deleteMany({});
    // console.log("Cleared existing discounts");

    for (const discountInfo of discountData) {
      // Kiểm tra discount đã tồn tại chưa
      const existingDiscount = await Discount.findOne({
        code: discountInfo.code,
      });

      if (!existingDiscount) {
        await Discount.create(discountInfo);
        console.log(`✓ Created discount: ${discountInfo.code}`);
      } else {
        console.log(`→ Discount already exists: ${discountInfo.code}`);
      }
    }

    console.log("\n✓ Discounts seeded successfully!");
    process.exit();
  } catch (error) {
    console.error("Error seeding discounts:", error);
    process.exit(1);
  }
};

seedDiscounts();
