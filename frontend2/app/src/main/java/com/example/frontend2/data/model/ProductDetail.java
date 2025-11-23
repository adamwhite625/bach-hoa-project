// File: data/model/ProductDetail.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

// Giữ nguyên "implements Serializable"
public class ProductDetail implements Serializable {

    // Giữ nguyên serialVersionUID
    private static final long serialVersionUID = 1L;

    @SerializedName("_id")
    private String id;

    private String name;
    private String sku;
    private String description;
    private String image;
    private double price;

    @SerializedName("quantity")
    private int stock; // Giữ tên 'stock' cho rõ ràng

    private double rating;
    private int numReviews;
    private boolean isActive;
    private List<ImageInfo> detailImages;

    private String category;

    // --- PHẦN SỬA DUY NHẤT: THÊM TRƯỜNG "sale" ---
    @SerializedName("sale")
    private SaleInfo sale;
    // ---------------------------------------------


    // --- Getters ---
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public int getStock() { return stock; }
    public double getRating() { return rating; }
    public int getNumReviews() { return numReviews; }
    public boolean isActive() { return isActive; }
    public List<ImageInfo> getDetailImages() { return detailImages; }
    public String getCategory() { return category; }

    // --- PHẦN SỬA DUY NHẤT: THÊM GETTER CHO "sale" ---
    public SaleInfo getSale() {
        return sale;
    }
    // --------------------------------------------------
}

