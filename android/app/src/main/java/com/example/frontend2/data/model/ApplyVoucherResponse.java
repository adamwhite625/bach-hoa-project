package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho phản hồi từ server khi áp dụng một mã voucher.
 */
public class ApplyVoucherResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;

    // Sẽ chứa thông tin voucher nếu áp dụng thành công (success = true)
    // Sẽ là null nếu thất bại (success = false)
    @SerializedName("voucher")
    private Voucher voucher;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Voucher getVoucher() {
        return voucher;
    }
}
