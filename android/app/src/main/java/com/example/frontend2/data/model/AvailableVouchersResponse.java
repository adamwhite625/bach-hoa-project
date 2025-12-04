package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Lớp này đại diện cho phản hồi từ server khi yêu cầu danh sách các
 * phiếu giảm giá (vouchers) có sẵn.
 * Tương ứng với API endpoint: GET /api/discounts/available
 */
public class AvailableVouchersResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("message")
    private String message;
    @SerializedName("data")
    private List<Voucher> data;

    // --- Getters ---

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    /**
     * Lấy danh sách các đối tượng Voucher có sẵn.
     * @return Một List<Voucher> hoặc có thể là null nếu có lỗi.
     */
    public List<Voucher> getData() {
        return data;
    }
}
