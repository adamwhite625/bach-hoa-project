/**
 * Loyalty Tier Configuration
 * Defines the tiers and spending thresholds for customer loyalty program
 */

const LOYALTY_TIERS = {
  BRONZE: {
    id: "bronze",
    name: "Bronze",
    minSpending: 0,
    maxSpending: 499999,
    benefits: "Basic member benefits",
    discount: 0,
  },
  SILVER: {
    id: "silver",
    name: "Silver",
    minSpending: 500000,
    maxSpending: 999999,
    benefits: "Special discounts and priority support",
    discount: 5,
  },
  GOLD: {
    id: "gold",
    name: "Gold",
    minSpending: 1000000,
    maxSpending: Infinity,
    benefits: "Exclusive offers, priority support, and special events",
    discount: 10,
  },
};

/**
 * Calculate loyalty tier based on total spending
 * @param {number} totalSpent - Total amount spent by user in VND
 * @returns {string} Tier ID (bronze, silver, gold)
 */
const calculateTier = (totalSpent) => {
  if (totalSpent >= LOYALTY_TIERS.GOLD.minSpending) {
    return LOYALTY_TIERS.GOLD.id;
  }
  if (totalSpent >= LOYALTY_TIERS.SILVER.minSpending) {
    return LOYALTY_TIERS.SILVER.id;
  }
  return LOYALTY_TIERS.BRONZE.id;
};

/**
 * Get tier information by tier ID
 * @param {string} tierId - Tier ID
 * @returns {object} Tier information
 */
const getTierInfo = (tierId) => {
  return (
    Object.values(LOYALTY_TIERS).find((tier) => tier.id === tierId) ||
    LOYALTY_TIERS.BRONZE
  );
};

/**
 * Get spending required to reach next tier
 * @param {string} currentTier - Current tier ID
 * @param {number} currentSpending - Current spending amount
 * @returns {object} { nextTier, amountNeeded }
 */
const getNextTierInfo = (currentTier, currentSpending) => {
  if (currentTier === LOYALTY_TIERS.GOLD.id) {
    return { nextTier: null, amountNeeded: 0 };
  }

  if (currentTier === LOYALTY_TIERS.SILVER.id) {
    const amountNeeded = LOYALTY_TIERS.GOLD.minSpending - currentSpending;
    return {
      nextTier: LOYALTY_TIERS.GOLD.id,
      amountNeeded: Math.max(0, amountNeeded),
    };
  }

  const amountNeeded = LOYALTY_TIERS.SILVER.minSpending - currentSpending;
  return {
    nextTier: LOYALTY_TIERS.SILVER.id,
    amountNeeded: Math.max(0, amountNeeded),
  };
};

/**
 * Format currency without rounding
 * Example: 525123 → "525.123₫"
 * @param {number} amount - Amount in VND
 * @returns {string} Formatted currency string
 */
const formatCurrencyExact = (amount) => {
  if (typeof amount !== "number" || isNaN(amount)) {
    return "0₫";
  }

  // Convert to integer and format with thousand separators
  const intAmount = Math.floor(amount);
  const formatted = intAmount.toString().replace(/\B(?=(\d{3})+(?!\d))/g, ".");
  return `${formatted}₫`;
};

module.exports = {
  LOYALTY_TIERS,
  calculateTier,
  getTierInfo,
  getNextTierInfo,
  formatCurrencyExact,
};
