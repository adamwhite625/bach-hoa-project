package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class TotalSpent {

    @SerializedName("totalSpent")
    private long totalSpent;

    @SerializedName("currentTier")
    private String currentTier;

    @SerializedName("tierName")
    private String tierName;

    @SerializedName("formattedTotal")
    private String formattedTotal;

    // Getters
    public long getTotalSpent() {
        return totalSpent;
    }

    public String getCurrentTier() {
        return currentTier;
    }

    public String getTierName() {
        return tierName;
    }

    public String getFormattedTotal() {
        return formattedTotal;
    }
}
