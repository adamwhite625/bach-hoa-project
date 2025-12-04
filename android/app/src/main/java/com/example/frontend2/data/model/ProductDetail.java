package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class ProductDetail implements Serializable {

    private static final long serialVersionUID = 1L;

    @SerializedName("_id")
    private String id;

    private String name;
    private String sku;
    private String description;
    private String image;
    private double price;

    @SerializedName("quantity")
    private int stock;

    private double rating;
    private int numReviews;
    private boolean isActive;
    private List<ImageInfo> detailImages;
    private String category;

    @SerializedName("sale")
    private SaleInfo sale;

    @SerializedName("reviews")
    private List<Review> reviews;

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
    public SaleInfo getSale() { return sale; }
    public List<Review> getReviews() { return reviews; }

    public double getFinalPrice() {
        if (sale != null && sale.isActive() && sale.getValue() > 0) {
            double finalPrice = this.price - (this.price * sale.getValue() / 100.0);
            if (finalPrice >= 0) {
                return finalPrice;
            }
        }
        return this.price;
    }
}
