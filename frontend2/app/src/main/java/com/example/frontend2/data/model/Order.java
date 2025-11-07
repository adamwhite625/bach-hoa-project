package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("orderItems")
    private List<OrderItem> orderItems;

    @SerializedName("totalPrice")
    private double totalPrice;

    @SerializedName("createdAt")
    private String createdAt;

    // Getters
    public String getId() {
        return id;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
