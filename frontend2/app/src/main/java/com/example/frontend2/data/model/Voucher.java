package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

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

    @SerializedName("minOrder")
    private double minOrder;

    @SerializedName("maxDiscount")
    private double maxDiscount;

    @SerializedName("startAt")
    private Date startAt;

    @SerializedName("endAt")
    private Date endAt;

    @SerializedName("isActive")
    private boolean isActive;

    @SerializedName("tierRequired")
    private String tierRequired;

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

    public double getMinOrder() {
        return minOrder;
    }

    public double getMaxDiscount() {
        return maxDiscount;
    }

    public Date getStartAt() {
        return startAt;
    }

    public Date getEndAt() {
        return endAt;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getTierRequired() {
        return tierRequired;
    }
}
