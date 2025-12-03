/**
 * Loyalty Service
 * Handles loyalty tier calculations and updates
 */

const User = require("../models/userModel");
const {
  calculateTier,
  getTierInfo,
  getNextTierInfo,
  formatCurrencyExact,
} = require("../config/loyaltyTiers");
const {
  sendTierUpgradeEmail,
  sendTierDowngradeEmail,
} = require("./emailService");

/**
 * Update user loyalty tier based on total spending
 * Sends notification email if tier changed
 * @param {string} userId - User ID
 * @returns {object} { oldTier, newTier, tierChanged }
 */
const updateUserLoyaltyTier = async (userId) => {
  try {
    const user = await User.findById(userId);
    if (!user) {
      throw new Error("User not found");
    }

    const oldTier = user.loyaltyTier;
    const newTier = calculateTier(user.totalSpent);
    const tierChanged = oldTier !== newTier;

    if (tierChanged) {
      user.loyaltyTier = newTier;
      user.lastTierUpdateAt = new Date();
      await user.save();

      // Send email notification based on tier change
      try {
        if (oldTier === "bronze" && newTier === "silver") {
          await sendTierUpgradeEmail(user, newTier, oldTier);
        } else if (oldTier === "silver" && newTier === "gold") {
          await sendTierUpgradeEmail(user, newTier, oldTier);
        } else if (oldTier === "bronze" && newTier === "gold") {
          await sendTierUpgradeEmail(user, newTier, oldTier);
        } else if (oldTier === "silver" && newTier === "bronze") {
          await sendTierDowngradeEmail(user, newTier, oldTier);
        } else if (
          oldTier === "gold" &&
          (newTier === "bronze" || newTier === "silver")
        ) {
          await sendTierDowngradeEmail(user, newTier, oldTier);
        }
      } catch (emailError) {
        console.error("Email notification failed but continuing:", emailError);
        // Don't throw - email failure should not block tier update
      }

      console.log(
        `User ${user.email} tier changed from ${oldTier} to ${newTier}`
      );
    }

    return {
      oldTier,
      newTier,
      tierChanged,
      user,
    };
  } catch (error) {
    console.error("Error updating loyalty tier:", error);
    throw error;
  }
};

/**
 * Get user loyalty information
 * @param {string} userId - User ID
 * @returns {object} Loyalty information including tier, spending, next tier info
 */
const getUserLoyaltyInfo = async (userId) => {
  try {
    const user = await User.findById(userId).select(
      "loyaltyTier totalSpent lastTierUpdateAt email firstName lastName"
    );
    if (!user) {
      throw new Error("User not found");
    }

    const tierInfo = getTierInfo(user.loyaltyTier);
    const nextTierInfo = getNextTierInfo(user.loyaltyTier, user.totalSpent);

    return {
      userId: user._id,
      email: user.email,
      firstName: user.firstName,
      lastName: user.lastName,
      currentTier: user.loyaltyTier,
      totalSpent: user.totalSpent,
      formattedTotal: formatCurrencyExact(user.totalSpent),
      lastTierUpdateAt: user.lastTierUpdateAt,
      tierInfo,
      nextTier: nextTierInfo.nextTier,
      amountToNextTier: nextTierInfo.amountNeeded,
      formattedAmountToNextTier: formatCurrencyExact(nextTierInfo.amountNeeded),
    };
  } catch (error) {
    console.error("Error getting loyalty info:", error);
    throw error;
  }
};

/**
 * Add spending to user and update tier
 * @param {string} userId - User ID
 * @param {number} amount - Amount spent in VND
 * @returns {object} Updated loyalty info
 */
const addSpending = async (userId, amount) => {
  try {
    if (amount <= 0) {
      throw new Error("Amount must be greater than 0");
    }

    const user = await User.findByIdAndUpdate(
      userId,
      { $inc: { totalSpent: amount } },
      { new: true }
    );

    if (!user) {
      throw new Error("User not found");
    }

    const tierUpdate = await updateUserLoyaltyTier(userId);
    const loyaltyInfo = await getUserLoyaltyInfo(userId);

    return {
      ...tierUpdate,
      loyaltyInfo,
    };
  } catch (error) {
    console.error("Error adding spending:", error);
    throw error;
  }
};

/**
 * Check if discount is applicable for user based on tier requirement
 * @param {string} userTier - User's current loyalty tier
 * @param {string|array} tierRequired - Required tier(s) for discount
 * @returns {boolean} True if user tier meets requirement
 */
const isUserEligibleForDiscount = (userTier, tierRequired) => {
  // If no tier requirement, all users eligible
  if (!tierRequired || tierRequired === "all") {
    return true;
  }

  // If tierRequired is array, check if user tier is in array
  if (Array.isArray(tierRequired)) {
    return tierRequired.includes(userTier);
  }

  // If tierRequired is string, check exact match
  return userTier === tierRequired;
};

module.exports = {
  updateUserLoyaltyTier,
  getUserLoyaltyInfo,
  addSpending,
  isUserEligibleForDiscount,
};
