package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CartResponse {

    @SerializedName("items")
    private List<CartItem> items;

    @SerializedName("totalPrice")
    private double totalPrice;

    public List<CartItem> getItems() {
        return items;
    }

    public double getTotalPrice() {
        return totalPrice;
    }
}

