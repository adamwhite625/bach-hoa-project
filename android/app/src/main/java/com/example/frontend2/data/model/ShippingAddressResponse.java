package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ShippingAddressResponse {

    @SerializedName("hasShippingAddress")
    private boolean hasShippingAddress;

    @SerializedName("shippingAddress")
    private ShippingAddress shippingAddress;

    @SerializedName("message")
    private String message;

    public boolean hasShippingAddress() {
        return hasShippingAddress;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public String getMessage() {
        return message;
    }
}
