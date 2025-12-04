package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class UpdateCartRequest {

    @SerializedName("quantity")
    private int quantity;

    public UpdateCartRequest(int quantity) {
        this.quantity = quantity;
    }

    public int getQuantity() {
        return quantity;
    }
}
