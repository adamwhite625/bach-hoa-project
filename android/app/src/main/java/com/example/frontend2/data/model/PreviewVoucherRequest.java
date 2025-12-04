// File: data/model/PreviewVoucherRequest.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class PreviewVoucherRequest {

    @SerializedName("code") // <<-- SỬA Ở ĐÂY: Bảo GSON tạo key "code"
    private String voucherCode;

    @SerializedName("items") // <<-- SỬA Ở ĐÂY: Bảo GSON tạo key "items"
    private List<CartItem> cartItems;

    @SerializedName("subtotal") // <<-- SỬA Ở ĐÂY: Bảo GSON tạo key "subtotal"
    private double currentSubtotal;

    // Constructor để nhận các giá trị
    public PreviewVoucherRequest(String voucherCode, List<CartItem> cartItems, double currentSubtotal) {
        this.voucherCode = voucherCode;
        this.cartItems = cartItems;
        this.currentSubtotal = currentSubtotal;
    }
}
