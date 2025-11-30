package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class OrderRequest {

    @SerializedName("orderItems")
    private List<OrderItem> orderItems;

    @SerializedName("shippingAddress")
    private ShippingAddress shippingAddress;

    @SerializedName("paymentMethod")
    private String paymentMethod;

    @SerializedName("itemsPrice")
    private double itemsPrice;

    @SerializedName("taxPrice")
    private double taxPrice;

    @SerializedName("shippingPrice")
    private double shippingPrice;

    @SerializedName("totalPrice")
    private double totalPrice;

    public OrderRequest(List<OrderItem> orderItems, ShippingAddress shippingAddress, String paymentMethod, double itemsPrice, double taxPrice, double shippingPrice, double totalPrice) {
        this.orderItems = orderItems;
        this.shippingAddress = shippingAddress;
        this.paymentMethod = paymentMethod;
        this.itemsPrice = itemsPrice;
        this.taxPrice = taxPrice;
        this.shippingPrice = shippingPrice;
        this.totalPrice = totalPrice;
    }

    public List<OrderItem> getOrderItems() {
        return orderItems;
    }

    public void setOrderItems(List<OrderItem> orderItems) {
        this.orderItems = orderItems;
    }

    public ShippingAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(ShippingAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public double getItemsPrice() {
        return itemsPrice;
    }

    public void setItemsPrice(double itemsPrice) {
        this.itemsPrice = itemsPrice;
    }

    public double getTaxPrice() {
        return taxPrice;
    }

    public void setTaxPrice(double taxPrice) {
        this.taxPrice = taxPrice;
    }

    public double getShippingPrice() {
        return shippingPrice;
    }

    public void setShippingPrice(double shippingPrice) {
        this.shippingPrice = shippingPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(double totalPrice) {
        this.totalPrice = totalPrice;
    }
}
