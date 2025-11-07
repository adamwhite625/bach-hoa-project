// File: data/model/ProductDetail.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductDetail {

    @SerializedName("_id")
    private String id;

    private String name;
    private String sku;
    private String description;
    private String image;
    private double price;
    private int quantity;
    private double rating;
    private int numReviews;
    private boolean isActive;
    private List<ImageInfo> detailImages;

    private String category; // <-- Kiểu dữ liệu là CHUỖI (STRING)

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getRating() { return rating; }
    public int getNumReviews() { return numReviews; }
    public boolean isActive() { return isActive; }
    public List<ImageInfo> getDetailImages() { return detailImages; }

    // Getter cho category bây giờ trả về String
    public String getCategory() { // <-- Trả về String ID
        return category;
    }
}
