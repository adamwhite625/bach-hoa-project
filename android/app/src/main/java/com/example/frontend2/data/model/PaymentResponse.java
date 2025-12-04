package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentResponse {

    @SerializedName("paymentUrl")
    private String paymentUrl;

    @SerializedName("zp_trans_token")
    private String zpTransToken;

    @SerializedName("app_trans_id")
    private String appTransId;

    @SerializedName("message")
    private String message;

    // Getters
    public String getPaymentUrl() {
        return paymentUrl;
    }

    public String getZpTransToken() {
        return zpTransToken;
    }

    public String getAppTransId() {
        return appTransId;
    }

    public String getMessage() {
        return message;
    }

    // Setters
    public void setPaymentUrl(String paymentUrl) {
        this.paymentUrl = paymentUrl;
    }

    public void setZpTransToken(String zpTransToken) {
        this.zpTransToken = zpTransToken;
    }

    public void setAppTransId(String appTransId) {
        this.appTransId = appTransId;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}