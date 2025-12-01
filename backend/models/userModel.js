const mongoose = require("mongoose");
const bcrypt = require("bcryptjs");

const shippingAddressSchema = mongoose.Schema({
  fullName: { type: String, required: true },
  phone: { type: String, required: true },
  address: { type: String, required: true },
  city: { type: String, required: true },
});

const userSchema = mongoose.Schema(
  {
    firstName: { type: String, required: true },
    lastName: { type: String, required: true },
    email: { type: String, required: true, unique: true },
    gender: {
      type: String,
      enum: ["Male", "Female", "Other"],
      default: "Other",
    },
    password: { type: String, required: true },
    phone: { type: String },
    avatar: { type: String },
    shippingAddress: shippingAddressSchema,
    resetPasswordToken: { type: String },
    resetPasswordExpires: { type: Date },
    role: {
      type: String,
      enum: ["Customer", "Admin", "Sales", "Warehouse"],
      default: "Customer",
    },
    loyaltyTier: {
      type: String,
      enum: ["bronze", "silver", "gold"],
      default: "bronze",
    },
    totalSpent: {
      type: Number,
      default: 0,
      min: 0,
    },
    lastTierUpdateAt: {
      type: Date,
      default: null,
    },
    isActive: { type: Boolean, default: true },
  },
  { timestamps: true }
);

userSchema.pre("save", async function (next) {
  if (!this.isModified("password")) {
    next();
  }
  const salt = await bcrypt.genSalt(10);

  this.password = await bcrypt.hash(this.password, salt);
});

userSchema.methods.matchPassword = async function (enteredPassword) {
  return await bcrypt.compare(enteredPassword, this.password);
};

const User = mongoose.model("User", userSchema);
module.exports = User;
