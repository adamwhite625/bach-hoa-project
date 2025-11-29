package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Voucher implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("code")
    private String code;

    @SerializedName("description")
    private String description;

    @SerializedName("type")
    private String type;

    @SerializedName("value")
    private double value;

    @SerializedName("minOrderValue")
    private double minOrderValue;

    @SerializedName("maxDiscountAmount")
    private double maxDiscountAmount;

    @SerializedName("startDate")
    private String startDate;

    @SerializedName("endDate")
    private String endDate;

    @SerializedName("isActive")
    private boolean isActive;

    public String getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }

    public String getType() {
        return type;
    }

    public double getValue() {
        return value;
    }

    public double getMinOrderValue() {
        return minOrderValue;
    }

    public double getMaxDiscountAmount() {
        return maxDiscountAmount;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public boolean isActive() {
        return isActive;
    }
}
