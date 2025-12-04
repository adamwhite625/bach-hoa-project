package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.Date;

public class LoyaltyInfo {

    @SerializedName("userId")
    private String userId;

    @SerializedName("email")
    private String email;

    @SerializedName("firstName")
    private String firstName;

    @SerializedName("lastName")
    private String lastName;

    @SerializedName("currentTier")
    private String currentTier;

    @SerializedName("totalSpent")
    private long totalSpent;

    @SerializedName("lastTierUpdateAt")
    private Date lastTierUpdateAt;

    @SerializedName("tierInfo")
    private TierInfo tierInfo;

    @SerializedName("nextTier")
    private String nextTier;

    @SerializedName("amountToNextTier")
    private long amountToNextTier;

    // Getters
    public String getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getCurrentTier() {
        return currentTier;
    }

    public long getTotalSpent() {
        return totalSpent;
    }

    public Date getLastTierUpdateAt() {
        return lastTierUpdateAt;
    }

    public TierInfo getTierInfo() {
        return tierInfo;
    }

    public String getNextTier() {
        return nextTier;
    }

    public long getAmountToNextTier() {
        return amountToNextTier;
    }
}
