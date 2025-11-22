// File: data/model/ProductDetail.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

import java.io.Serializable; // <-- SỬA 1: THÊM IMPORT NÀY
import java.util.List;

// SỬA 2: THÊM "implements Serializable"
public class ProductDetail implements Serializable {

    // Thêm một serialVersionUID để quản lý phiên bản của class khi được tuần tự hóa.
    // Đây là một good practice, giúp tránh lỗi khi bạn thay đổi cấu trúc class sau này.
    private static final long serialVersionUID = 1L;

    @SerializedName("_id")
    private String id;

    private String name;
    private String sku;
    private String description;
    private String image;
    private double price;

    @SerializedName("quantity") // Đảm bảo tên JSON và tên biến khớp nhau
    private int stock; // <-- SỬA 3: Đổi tên 'quantity' thành 'stock' cho rõ ràng hơn (số lượng tồn kho)

    private double rating;
    private int numReviews;
    private boolean isActive;
    private List<ImageInfo> detailImages;

    private String category;

    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getSku() { return sku; }
    public String getDescription() { return description; }
    public String getImage() { return image; }
    public double getPrice() { return price; }

    // Getter cho số lượng tồn kho
    public int getStock() { return stock; } // <-- SỬA 3: Cập nhật getter

    public double getRating() { return rating; }
    public int getNumReviews() { return numReviews; }
    public boolean isActive() { return isActive; }
    public List<ImageInfo> getDetailImages() { return detailImages; }

    public String getCategory() {
        return category;
    }
}
