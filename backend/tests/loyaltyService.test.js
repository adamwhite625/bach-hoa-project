/**
 * Loyalty Service Tests
 * Unit tests for loyalty tier calculations and updates
 */

const assert = require("assert");
const {
  calculateTier,
  getTierInfo,
  getNextTierInfo,
  LOYALTY_TIERS,
} = require("../config/loyaltyTiers");
const { isUserEligibleForDiscount } = require("../services/loyaltyService");

// Test Suite: Loyalty Tiers
describe("Loyalty Tiers", () => {
  describe("calculateTier", () => {
    it("should return bronze tier for spending < 500,000", () => {
      assert.strictEqual(calculateTier(0), "bronze");
      assert.strictEqual(calculateTier(250000), "bronze");
      assert.strictEqual(calculateTier(499999), "bronze");
    });

    it("should return silver tier for spending >= 500,000 and < 1,000,000", () => {
      assert.strictEqual(calculateTier(500000), "silver");
      assert.strictEqual(calculateTier(750000), "silver");
      assert.strictEqual(calculateTier(999999), "silver");
    });

    it("should return gold tier for spending >= 1,000,000", () => {
      assert.strictEqual(calculateTier(1000000), "gold");
      assert.strictEqual(calculateTier(5000000), "gold");
      assert.strictEqual(calculateTier(100000000), "gold");
    });
  });

  describe("getTierInfo", () => {
    it("should return correct tier information for bronze", () => {
      const tierInfo = getTierInfo("bronze");
      assert.strictEqual(tierInfo.id, "bronze");
      assert.strictEqual(tierInfo.name, "Bronze");
      assert.strictEqual(tierInfo.minSpending, 0);
      assert.strictEqual(tierInfo.discount, 0);
    });

    it("should return correct tier information for silver", () => {
      const tierInfo = getTierInfo("silver");
      assert.strictEqual(tierInfo.id, "silver");
      assert.strictEqual(tierInfo.name, "Silver");
      assert.strictEqual(tierInfo.minSpending, 5000000);
      assert.strictEqual(tierInfo.discount, 5);
    });

    it("should return correct tier information for gold", () => {
      const tierInfo = getTierInfo("gold");
      assert.strictEqual(tierInfo.id, "gold");
      assert.strictEqual(tierInfo.name, "Gold");
      assert.strictEqual(tierInfo.minSpending, 10000000);
      assert.strictEqual(tierInfo.discount, 10);
    });

    it("should return bronze tier info for invalid tier ID", () => {
      const tierInfo = getTierInfo("invalid");
      assert.strictEqual(tierInfo.id, "bronze");
    });
  });

  describe("getNextTierInfo", () => {
    it("should return no next tier for gold tier", () => {
      const nextTier = getNextTierInfo("gold", 1000000);
      assert.strictEqual(nextTier.nextTier, null);
      assert.strictEqual(nextTier.amountNeeded, 0);
    });

    it("should return silver tier for bronze users", () => {
      const nextTier = getNextTierInfo("bronze", 200000);
      assert.strictEqual(nextTier.nextTier, "silver");
      assert.strictEqual(nextTier.amountNeeded, 300000);
    });

    it("should return gold tier for silver users", () => {
      const nextTier = getNextTierInfo("silver", 600000);
      assert.strictEqual(nextTier.nextTier, "gold");
      assert.strictEqual(nextTier.amountNeeded, 400000);
    });

    it("should calculate correct amount needed to reach next tier", () => {
      // Bronze user needs 500,000 to reach silver
      const nextTier1 = getNextTierInfo("bronze", 0);
      assert.strictEqual(nextTier1.amountNeeded, 500000);

      // Silver user at 500,000 needs 500,000 more for gold
      const nextTier2 = getNextTierInfo("silver", 500000);
      assert.strictEqual(nextTier2.amountNeeded, 500000);
    });
  });
});

// Test Suite: Discount Eligibility
describe("Discount Eligibility", () => {
  describe("isUserEligibleForDiscount", () => {
    it('should allow all tiers for "all" requirement', () => {
      assert.strictEqual(isUserEligibleForDiscount("bronze", "all"), true);
      assert.strictEqual(isUserEligibleForDiscount("silver", "all"), true);
      assert.strictEqual(isUserEligibleForDiscount("gold", "all"), true);
    });

    it("should allow no requirement to mean all tiers", () => {
      assert.strictEqual(isUserEligibleForDiscount("bronze", null), true);
      assert.strictEqual(isUserEligibleForDiscount("bronze", undefined), true);
    });

    it("should check exact tier match for string requirement", () => {
      assert.strictEqual(isUserEligibleForDiscount("silver", "silver"), true);
      assert.strictEqual(isUserEligibleForDiscount("bronze", "silver"), false);
      assert.strictEqual(isUserEligibleForDiscount("gold", "silver"), false);
    });

    it("should check array membership for array requirement", () => {
      assert.strictEqual(
        isUserEligibleForDiscount("silver", ["silver", "gold"]),
        true
      );
      assert.strictEqual(
        isUserEligibleForDiscount("gold", ["silver", "gold"]),
        true
      );
      assert.strictEqual(
        isUserEligibleForDiscount("bronze", ["silver", "gold"]),
        false
      );
    });

    it("should handle edge cases", () => {
      assert.strictEqual(isUserEligibleForDiscount("bronze", []), false);
      assert.strictEqual(isUserEligibleForDiscount("gold", ["gold"]), true);
    });
  });
});

// Test Suite: Loyalty Tier Boundaries
describe("Loyalty Tier Boundaries", () => {
  it("should handle boundary values correctly", () => {
    // Bronze boundary
    assert.strictEqual(calculateTier(499999), "bronze");
    assert.strictEqual(calculateTier(500000), "silver");

    // Silver boundary
    assert.strictEqual(calculateTier(999999), "silver");
    assert.strictEqual(calculateTier(1000000), "gold");
  });

  it("should handle zero and large values", () => {
    assert.strictEqual(calculateTier(0), "bronze");
    assert.strictEqual(calculateTier(999999999), "gold");
  });
});

// Run tests if this file is executed directly
if (require.main === module) {
  console.log("Running Loyalty Service Tests...\n");

  // Run simple tests
  try {
    // Test calculateTier
    console.log("Testing calculateTier...");
    assert.strictEqual(calculateTier(0), "bronze");
    assert.strictEqual(calculateTier(500000), "silver");
    assert.strictEqual(calculateTier(1000000), "gold");
    console.log("✓ calculateTier tests passed\n");

    // Test getTierInfo
    console.log("Testing getTierInfo...");
    const bronzeInfo = getTierInfo("bronze");
    assert.strictEqual(bronzeInfo.name, "Bronze");
    console.log("✓ getTierInfo tests passed\n");

    // Test getNextTierInfo
    console.log("Testing getNextTierInfo...");
    const nextTier = getNextTierInfo("bronze", 0);
    assert.strictEqual(nextTier.nextTier, "silver");
    assert.strictEqual(nextTier.amountNeeded, 500000);
    console.log("✓ getNextTierInfo tests passed\n");

    // Test isUserEligibleForDiscount
    console.log("Testing isUserEligibleForDiscount...");
    assert.strictEqual(isUserEligibleForDiscount("bronze", "all"), true);
    assert.strictEqual(isUserEligibleForDiscount("silver", "silver"), true);
    assert.strictEqual(
      isUserEligibleForDiscount("bronze", ["silver", "gold"]),
      false
    );
    console.log("✓ isUserEligibleForDiscount tests passed\n");

    console.log("✅ All tests passed!");
  } catch (error) {
    console.error("❌ Test failed:", error.message);
    process.exit(1);
  }
}

module.exports = {
  calculateTier,
  getTierInfo,
  getNextTierInfo,
  isUserEligibleForDiscount,
};
