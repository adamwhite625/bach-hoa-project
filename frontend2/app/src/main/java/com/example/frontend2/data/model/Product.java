package com.example.frontend2.data.model;

public class Product {
    private String _id;
    private String name;
    private String sku;
    private String description;
    private String image; // 👈 thay vì List<String> images
    private String category;
    private double price;
    private int quantity;
    private double rating;
    private int numReviews;
    private boolean isActive;

    public String getId() { return _id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public String getImage() { return image; } // 👈 cập nhật getter
    public String getCategory() { return category; }
    public double getPrice() { return price; }
    public int getQuantity() { return quantity; }
    public double getRating() { return rating; }
    public int getNumReviews() { return numReviews; }
    public boolean isActive() { return isActive; }
}
