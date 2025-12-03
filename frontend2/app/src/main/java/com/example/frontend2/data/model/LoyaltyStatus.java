package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class LoyaltyStatus {

    @SerializedName("currentTier")
    private String currentTier;

    @SerializedName("tierName")
    private String tierName;

    @SerializedName("totalSpent")
    private long totalSpent;

    @SerializedName("benefits")
    private String benefits;

    @SerializedName("discount")
    private int discount;

    @SerializedName("lastTierUpdateAt")
    private Date lastTierUpdateAt;

    // Getters
    public String getCurrentTier() {
        return currentTier;
    }

    public String getTierName() {
        return tierName;
    }

    public long getTotalSpent() {
        return totalSpent;
    }

    public String getBenefits() {
        return benefits;
    }

    public int getDiscount() {
        return discount;
    }

    public Date getLastTierUpdateAt() {
        return lastTierUpdateAt;
    }
}
