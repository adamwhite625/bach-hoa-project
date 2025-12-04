package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ProductListResponse {

    @SerializedName("products")
    private List<ProductInList> products;

    public List<ProductInList> getProducts() {
        return products;
    }
}
