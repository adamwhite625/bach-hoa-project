package com.example.frontend2.data.model;

public class UpdateCartRequest {
    private int quantity;

    public UpdateCartRequest(int quantity) {
        this.quantity = quantity;
    }

    // Getter
    public int getQuantity() {
        return quantity;
    }
}
