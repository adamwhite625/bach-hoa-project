package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ValidateVoucherResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("discount")
    private DiscountInfo discount;

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public DiscountInfo getDiscount() {
        return discount;
    }

    public static class DiscountInfo {
        @SerializedName("code")
        private String code;

        @SerializedName("type")
        private String type;

        @SerializedName("value")
        private double value;

        public String getCode() {
            return code;
        }

        public String getType() {
            return type;
        }

        public double getValue() {
            return value;
        }
    }
}
