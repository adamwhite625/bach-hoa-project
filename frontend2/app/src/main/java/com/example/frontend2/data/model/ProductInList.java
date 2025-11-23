// File: data/model/ProductInList.java
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable; // <-- SỬA 1: THÊM IMPORT
import java.util.List;

// SỬA 2: THÊM "implements Serializable"
public class ProductInList implements Serializable {

    // Thêm serialVersionUID, một good practice cho Serializable
    private static final long serialVersionUID = 2L;

    @SerializedName("_id")
    private String id;

    private String name;
    private long price;

    @SerializedName("image")
    private String imageUrl;
    @SerializedName("sale")
    private SaleInfo sale;
    // -------------------------------------------

    // Các trường khác có thể không cần thiết cho danh sách, nhưng giữ lại nếu API trả về
    private String sku;
    private String description;
    private int quantity;
    private double rating;
    private int numReviews;
    private boolean isActive;

    // --- Getters ---
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public long getPrice() {
        return price;
    }

    // SỬA 3: Cập nhật getter cho ảnh
    public String getImageUrl() {
        return imageUrl;
    }

    // --- PHẦN SỬA CHÍNH: THÊM GETTER CHO "sale" ---
    public SaleInfo getSale() {
        return sale;
    }
    // ------------------------------------------------

    // Getters cho các trường ít dùng hơn
    public String getSku() {
        return sku;
    }
    public String getDescription() {
        return description;
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
