// File: src/main/java/com/example/frontend2/data/model/SaleInfo.java

package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

/**
 * Lớp này đại diện cho đối tượng "sale" được trả về từ API.
 * Nó chứa thông tin về một chương trình khuyến mãi cho sản phẩm.
 */
public class SaleInfo {

    /**
     * Tên trường trong JSON: "active"
     * Cho biết khuyến mãi có đang được áp dụng hay không.
     */
    @SerializedName("active")
    private boolean active;

    /**
     * Tên trường trong JSON: "type"
     * Loại khuyến mãi, ví dụ: "percent" (giảm theo phần trăm) hoặc "fixed" (giảm một số tiền cố định).
     */
    @SerializedName("type")
    private String type;

    /**
     * Tên trường trong JSON: "value"
     * Giá trị của khuyến mãi.
     * Nếu type là "percent", đây là số phần trăm giảm (ví dụ: 30).
     * Nếu type là "fixed", đây là số tiền giảm.
     */
    @SerializedName("value")
    private int value;
    /**
     * Tên trường trong JSON: "startAt"
     * Thời gian bắt đầu khuyến mãi, định dạng chuỗi ISO 8601 (ví dụ: "2025-11-19T14:45:25.250Z").
     */
    @SerializedName("startAt")
    private String startAt;

    /**
     * Tên trường trong JSON: "endAt"
     * Thời gian kết thúc khuyến mãi, định dạng chuỗi ISO 8601 (ví dụ: "2025-11-19T14:58:00.000Z").
     */
    @SerializedName("endAt")
    private String endAt;
    // =============================================================


    // --- Getters ---
    // Các phương thức này dùng để các lớp khác (như Adapter, Activity) có thể truy cập vào dữ liệu.

    public boolean isActive() {
        return active;
    }

    public String getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    // --- Getters MỚI ---
    public String getStartAt() {
        return startAt;
    }

    public String getEndAt() {
        return endAt;
    }


    // --- Setters (Tùy chọn) ---
    // Thường không cần thiết khi chỉ đọc dữ liệu từ API.

    public void setActive(boolean active) {
        this.active = active;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setValue(int value) { // SỬA: Kiểu tham số là double
        this.value = value;
    }

    // --- Setters MỚI ---
    public void setStartAt(String startAt) {
        this.startAt = startAt;
    }

    public void setEndAt(String endAt) {
        this.endAt = endAt;
    }
}
