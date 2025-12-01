const mongoose = require("mongoose");
const dotenv = require("dotenv");
const Discount = require("../models/discountModel");

dotenv.config();

// Mapping của các discount codes hiện có với tier requirement
const discountTierMapping = {
  WELCOME10: "all",
  SAVE50K: "all",
  FLASH20: "all",
  LOYALTY15: "silver",
  FREESHIP: "all",
  HOLIDAY25: "all",
  VIP30: "gold",
  NEWYEAR100: "gold",
  SUMMER5: "all",
  BULK30K: "all",
};

const updateDiscountTiers = async () => {
  try {
    await mongoose.connect(process.env.MONGO_URI);
    console.log("Connected to MongoDB...");

    let updatedCount = 0;

    for (const [code, tier] of Object.entries(discountTierMapping)) {
      const result = await Discount.findOneAndUpdate(
        { code: code },
        { $set: { tierRequired: tier } },
        { new: true }
      );

      if (result) {
        console.log(`✓ Updated ${code} with tier: ${tier}`);
        updatedCount++;
      } else {
        console.log(`→ Discount not found: ${code}`);
      }
    }

    console.log(
      `\n✓ Updated ${updatedCount} discount(s) with loyalty tier support!`
    );
    process.exit();
  } catch (error) {
    console.error("Error updating discount tiers:", error);
    process.exit(1);
  }
};

updateDiscountTiers();
