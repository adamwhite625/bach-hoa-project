package com.example.frontend2.data.model;

import java.util.List;

public class CartResponse {
    private List<CartItem> items;
    // Thêm các trường khác nếu API trả về (ví dụ: totalPrice)

    // Getters and Setters
    public List<CartItem> getItems() {
        return items;
    }

    public void setItems(List<CartItem> items) {
        this.items = items;
    }
}
