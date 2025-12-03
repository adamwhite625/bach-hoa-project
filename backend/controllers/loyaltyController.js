/**
 * Loyalty Controller
 * Handles loyalty tier and spending related endpoints
 */

const {
  getUserLoyaltyInfo,
  isUserEligibleForDiscount,
} = require("../services/loyaltyService");
const User = require("../models/userModel");
const { getTierInfo, formatCurrencyExact } = require("../config/loyaltyTiers");

/**
 * Get user's loyalty information
 * GET /api/users/loyalty/info
 * @protected
 */
const getLoyaltyInfo = async (req, res) => {
  try {
    const loyaltyInfo = await getUserLoyaltyInfo(req.user._id);

    res.status(200).json({
      EC: 0,
      DT: loyaltyInfo,
      EM: "Lấy thông tin cấp độ thân thiết thành công",
    });
  } catch (error) {
    console.error("Get loyalty info error:", error);
    res.status(500).json({
      EC: -1,
      DT: null,
      EM: "Lỗi lấy thông tin cấp độ thân thiết",
    });
  }
};

/**
 * Check current loyalty status
 * GET /api/users/loyalty/status
 * @protected
 */
const checkLoyaltyStatus = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select(
      "loyaltyTier totalSpent lastTierUpdateAt"
    );

    if (!user) {
      return res.status(404).json({
        EC: -1,
        DT: null,
        EM: "Không tìm thấy người dùng",
      });
    }

    const tierInfo = getTierInfo(user.loyaltyTier);

    res.status(200).json({
      EC: 0,
      DT: {
        currentTier: user.loyaltyTier,
        tierName: tierInfo.name,
        totalSpent: user.totalSpent,
        formattedTotal: formatCurrencyExact(user.totalSpent),
        benefits: tierInfo.benefits,
        discount: tierInfo.discount,
        lastTierUpdateAt: user.lastTierUpdateAt,
      },
      EM: "Kiểm tra trạng thái thành công",
    });
  } catch (error) {
    console.error("Check loyalty status error:", error);
    res.status(500).json({
      EC: -1,
      DT: null,
      EM: "Lỗi kiểm tra trạng thái",
    });
  }
};

/**
 * Get total spending amount
 * GET /api/users/loyalty/total-spent
 * @protected
 */
const getTotalSpent = async (req, res) => {
  try {
    const user = await User.findById(req.user._id).select(
      "totalSpent loyaltyTier"
    );

    if (!user) {
      return res.status(404).json({
        EC: -1,
        DT: null,
        EM: "Không tìm thấy người dùng",
      });
    }

    const tierInfo = getTierInfo(user.loyaltyTier);

    res.status(200).json({
      EC: 0,
      DT: {
        totalSpent: user.totalSpent,
        currentTier: user.loyaltyTier,
        tierName: tierInfo.name,
        formattedTotal: formatCurrencyExact(user.totalSpent),
      },
      EM: "Lấy tổng chi tiêu thành công",
    });
  } catch (error) {
    console.error("Get total spent error:", error);
    res.status(500).json({
      EC: -1,
      DT: null,
      EM: "Lỗi lấy tổng chi tiêu",
    });
  }
};

module.exports = {
  getLoyaltyInfo,
  checkLoyaltyStatus,
  getTotalSpent,
};
