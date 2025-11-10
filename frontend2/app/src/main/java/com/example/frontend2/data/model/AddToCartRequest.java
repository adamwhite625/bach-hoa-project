package com.example.frontend2.data.model;

public class AddToCartRequest {
    private String productId;
    private int quantity;

    public AddToCartRequest(String productId, int quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    // Getters (không cần setters)
    public String getProductId() {
        return productId;
    }

    public int getQuantity() {
        return quantity;
    }
}
