const mongoose = require("mongoose");
const dotenv = require("dotenv");
const Discount = require("../models/discountModel");

dotenv.config();

const discountData = [
  {
    code: "WELCOME10",
    description: "Giảm 10% cho khách hàng mới",
    type: "percent",
    value: 10,
    minOrder: 50000,
    maxDiscount: 50000,
    usageLimit: 1000,
    perUserLimit: 1,
    tierRequired: "all",
    startAt: new Date("2025-11-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "SAVE50K",
    description: "Giảm 50.000đ cho đơn hàng từ 200.000đ",
    type: "fixed",
    value: 50000,
    minOrder: 200000,
    maxDiscount: 50000,
    usageLimit: 500,
    perUserLimit: 3,
    tierRequired: "all",
    startAt: new Date("2025-11-15"),
    endAt: new Date("2025-12-15"),
    isActive: true,
  },
  {
    code: "FLASH20",
    description: "Flash sale - Giảm 20% (Có hạn)",
    type: "percent",
    value: 20,
    minOrder: 100000,
    maxDiscount: 100000,
    usageLimit: 300,
    perUserLimit: 1,
    tierRequired: "all",
    startAt: new Date("2025-11-20"),
    endAt: new Date("2025-11-22"),
    isActive: true,
  },
  {
    code: "LOYALTY15",
    description: "Giảm 15% cho khách hàng Silver",
    type: "percent",
    value: 15,
    minOrder: 150000,
    maxDiscount: 80000,
    usageLimit: 2000,
    perUserLimit: 5,
    tierRequired: "silver",
    startAt: new Date("2025-10-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "FREESHIP",
    description: "Miễn phí vận chuyển cho đơn hàng trên 300.000đ",
    type: "fixed",
    value: 30000,
    minOrder: 300000,
    maxDiscount: 30000,
    usageLimit: 800,
    perUserLimit: 5,
    tierRequired: "all",
    startAt: new Date("2025-11-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "HOLIDAY25",
    description: "Giảm 25% cuối năm",
    type: "percent",
    value: 25,
    minOrder: 250000,
    maxDiscount: 150000,
    usageLimit: 600,
    perUserLimit: 3,
    tierRequired: "all",
    startAt: new Date("2025-11-20"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "VIP30",
    description: "Giảm 30% dành cho Gold members",
    type: "percent",
    value: 30,
    minOrder: 300000,
    maxDiscount: 200000,
    usageLimit: 200,
    perUserLimit: 10,
    tierRequired: "gold",
    startAt: new Date("2025-09-01"),
    endAt: new Date("2026-02-28"),
    isActive: true,
  },
  {
    code: "NEWYEAR100",
    description: "Giảm 100.000đ đầu năm mới",
    type: "fixed",
    value: 100000,
    minOrder: 500000,
    maxDiscount: 100000,
    usageLimit: 300,
    perUserLimit: 1,
    tierRequired: "gold",
    startAt: new Date("2026-01-01"),
    endAt: new Date("2026-01-10"),
    isActive: false,
  },
  {
    code: "SUMMER5",
    description: "Giảm 5% cho sản phẩm mùa hè",
    type: "percent",
    value: 5,
    minOrder: 75000,
    maxDiscount: 25000,
    usageLimit: 2000,
    perUserLimit: 10,
    tierRequired: "all",
    startAt: new Date("2025-05-01"),
    endAt: new Date("2025-08-31"),
    isActive: false,
  },
  {
    code: "BULK30K",
    description: "Giảm 30.000đ cho đơn hàng trên 400.000đ",
    type: "fixed",
    value: 30000,
    minOrder: 400000,
    maxDiscount: 30000,
    usageLimit: 400,
    perUserLimit: 3,
    tierRequired: "all",
    startAt: new Date("2025-11-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "SILVER20",
    description: "Giảm 20% cho Silver members",
    type: "percent",
    value: 20,
    minOrder: 150000,
    maxDiscount: 100000,
    usageLimit: 500,
    perUserLimit: 7,
    tierRequired: "silver",
    startAt: new Date("2025-11-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
  {
    code: "GOLDEXTRA",
    description: "Thêm 5% cho Gold members trên các discount khác",
    type: "percent",
    value: 5,
    minOrder: 100000,
    maxDiscount: 50000,
    usageLimit: 1000,
    perUserLimit: 15,
    tierRequired: "gold",
    startAt: new Date("2025-11-01"),
    endAt: new Date("2025-12-31"),
    isActive: true,
  },
];

const seedDiscounts = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB...");

    for (const discountInfo of discountData) {
      const existingDiscount = await Discount.findOne({
        code: discountInfo.code,
      });

      if (!existingDiscount) {
        await Discount.create(discountInfo);
        console.log(
          `✓ Created discount: ${discountInfo.code} (Tier: ${discountInfo.tierRequired})`
        );
      } else {
        console.log(`→ Discount already exists: ${discountInfo.code}`);
      }
    }

    console.log("\n✓ Discounts seeded successfully with loyalty tier support!");
    process.exit();
  } catch (error) {
    console.error("Error seeding discounts:", error);
    process.exit(1);
  }
};

seedDiscounts();
