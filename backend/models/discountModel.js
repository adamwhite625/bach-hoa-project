const mongoose = require("mongoose");

// const discountSchema = new mongoose.Schema({
//   code: {
//     type: String,
//     required: true,
//     unique: true,
//     trim: true,
//     uppercase: true
//   },
//   description: {
//     type: String,
//     trim: true
//   },
//   type: {
//     type: String,
//     enum: ['percentage', 'fixed'],
//     default: 'percentage'
//   },
//   value: {
//     type: Number,
//     required: true,
//     min: 0
//   },
//   minOrderValue: {
//     type: Number,
//     default: 0,
//     min: 0
//   },
//   maxDiscountAmount: {
//     type: Number,
//     default: null,
//     min: 0
//   },
//   usageLimit: {
//     type: Number,
//     default: null,
//     min: 0
//   },
//   usedCount: {
//     type: Number,
//     default: 0,
//     min: 0
//   },
//   startDate: {
//     type: Date,
//     default: null
//   },
//   endDate: {
//     type: Date,
//     default: null
//   },
//   isActive: {
//     type: Boolean,
//     default: true
//   }
// }, {
//   timestamps: true
// });

// discountSchema.index({ code: 1 });

// chinh sua cho phu hop
const discountSchema = new mongoose.Schema(
  {
    code: {
      type: String,
      required: true,
      unique: true,
      uppercase: true,
      trim: true,
    },
    type: { type: String, enum: ["percent", "fixed"], required: true },
    value: { type: Number, required: true }, // percent: 0-100; fixed: VND
    minOrder: { type: Number, default: 0 },
    maxDiscount: { type: Number, default: 0 }, // 0 = no cap
    startAt: { type: Date },
    endAt: { type: Date },
    usageLimit: { type: Number, default: 0 }, // 0 = unlimited
    perUserLimit: { type: Number, default: 0 }, // 0 = unlimited per user
    userType: {
      type: String,
      enum: ["all", "new", "vip", "specific"],
      default: "all",
    },
    tierRequired: {
      type: String,
      enum: ["all", "bronze", "silver", "gold"],
      default: "all",
    },
    allowedUsers: [{ type: mongoose.Schema.Types.ObjectId, ref: "User" }],
    applicableProducts: [
      { type: mongoose.Schema.Types.ObjectId, ref: "Product" },
    ],
    applicableCategories: [
      { type: mongoose.Schema.Types.ObjectId, ref: "Category" },
    ],
    excludeProducts: [{ type: mongoose.Schema.Types.ObjectId, ref: "Product" }],
    excludeCategories: [
      { type: mongoose.Schema.Types.ObjectId, ref: "Category" },
    ],
    isActive: { type: Boolean, default: true },
    createdBy: { type: mongoose.Schema.Types.ObjectId, ref: "User" },
    usedCount: { type: Number, default: 0 },
  },
  { timestamps: true }
);

// Validation
discountSchema.pre("save", function (next) {
  if (this.type === "percent" && (this.value < 0 || this.value > 100)) {
    return next(new Error("Percent discount must be between 0 and 100"));
  }
  if (this.startAt && this.endAt && this.startAt >= this.endAt) {
    return next(new Error("Start date must be before end date"));
  }
  if (
    this.userType === "specific" &&
    (!this.allowedUsers || this.allowedUsers.length === 0)
  ) {
    return next(
      new Error("Specific user type requires at least one allowed user")
    );
  }
  next();
});

module.exports = mongoose.model("Discount", discountSchema);
