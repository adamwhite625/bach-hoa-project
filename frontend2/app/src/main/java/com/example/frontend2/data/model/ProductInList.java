package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class ProductInList {

    @SerializedName("_id")
    private String id;

    private String name;
    private String sku;
    private String description;
    private String image; // Ảnh đại diện chính
    private double price;
    private int quantity;
    private double rating;
    private int numReviews;
    private boolean isActive;
    private Category category;
    private List<ImageInfo> detailImages;

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getSku() {
        return sku;
    }

    public String getDescription() {
        return description;
    }

    public String getImage() {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getRating() {
        return rating;
    }

    public int getNumReviews() {
        return numReviews;
    }

    public boolean isActive() {
        return isActive;
    }

    public Category getCategory() {
        return category;
    }

    public List<ImageInfo> getDetailImages() {
        return detailImages;
    }

    // Hàm tiện ích
    public String getCategoryName() {
        if (category != null) {
            return category.getName();
        }
        return "Chưa phân loại";
    }
}
