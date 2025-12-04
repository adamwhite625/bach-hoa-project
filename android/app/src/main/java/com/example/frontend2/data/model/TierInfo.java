package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class TierInfo {

    @SerializedName("id")
    private String id;

    @SerializedName("name")
    private String name;

    @SerializedName("minSpending")
    private long minSpending;

    @SerializedName("maxSpending")
    private long maxSpending;

    @SerializedName("benefits")
    private String benefits;

    @SerializedName("discount")
    private int discount;

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getMinSpending() {
        return minSpending;
    }

    public long getMaxSpending() {
        return maxSpending;
    }

    public String getBenefits() {
        return benefits;
    }

    public int getDiscount() {
        return discount;
    }
}
