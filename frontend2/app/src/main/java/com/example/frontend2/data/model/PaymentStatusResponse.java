package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class PaymentStatusResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    @SerializedName("orderId")
    private String orderId;

    @SerializedName("data")
    private ZaloPayData data;

    // Getters
    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getOrderId() {
        return orderId;
    }

    public ZaloPayData getData() {
        return data;
    }

    // Setters
    public void setSuccess(boolean success) {
        this.success = success;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public void setData(ZaloPayData data) {
        this.data = data;
    }

    // Inner class for ZaloPay response data
    public static class ZaloPayData {

        @SerializedName("return_code")
        private int returnCode;

        @SerializedName("return_message")
        private String returnMessage;

        @SerializedName("amount")
        private long amount;

        @SerializedName("zp_trans_id")
        private String zpTransId;

        @SerializedName("server_time")
        private long serverTime;

        @SerializedName("discount_amount")
        private long discountAmount;

        // Getters
        public int getReturnCode() {
            return returnCode;
        }

        public String getReturnMessage() {
            return returnMessage;
        }

        public long getAmount() {
            return amount;
        }

        public String getZpTransId() {
            return zpTransId;
        }

        public long getServerTime() {
            return serverTime;
        }

        public long getDiscountAmount() {
            return discountAmount;
        }

        // Setters
        public void setReturnCode(int returnCode) {
            this.returnCode = returnCode;
        }

        public void setReturnMessage(String returnMessage) {
            this.returnMessage = returnMessage;
        }

        public void setAmount(long amount) {
            this.amount = amount;
        }

        public void setZpTransId(String zpTransId) {
            this.zpTransId = zpTransId;
        }

        public void setServerTime(long serverTime) {
            this.serverTime = serverTime;
        }

        public void setDiscountAmount(long discountAmount) {
            this.discountAmount = discountAmount;
        }
    }
}