// File: data/model/PreviewVoucherResponse.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;public class PreviewVoucherResponse {

    // Backend không trả về "success", nó trả về dữ liệu hoặc lỗi 4xx
    // Chúng ta sẽ dựa vào HTTP status code để xác định thành công

    @SerializedName("code")
    private String code; // Thêm trường này để lấy lại code voucher

    @SerializedName("discount")
    private double discountAmount; // Số tiền được giảm

    @SerializedName("subtotal")
    private double originalSubtotal; // Tiền hàng ban đầu

    @SerializedName("total")
    private double finalTotal; // Tổng tiền cuối cùng sau khi giảm

    @SerializedName("message") // Dùng để hứng thông báo lỗi
    private String message;

    // Getters
    public String getCode() {
        return code;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public String getMessage() {
        return message;
    }
}
