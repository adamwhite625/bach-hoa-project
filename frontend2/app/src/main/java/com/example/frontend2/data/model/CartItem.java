package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

public class CartItem {

    @SerializedName("_id")
    private String id;

    @SerializedName("product")
    private ProductInfo product;

    @SerializedName("quantity")
    private int quantity;

    @SerializedName("itemTotal")
    private double itemTotal;

    // THÊM TRƯỜNG outOfStock VÀO
    @SerializedName("outOfStock")
    private boolean outOfStock;

    // SỬA LẠI CONSTRUCTOR: Thêm outOfStock và tự tính itemTotal
    public CartItem(String id, ProductInfo product, int quantity, boolean outOfStock) {
        this.id = id;
        this.product = product;
        this.quantity = quantity;
        this.outOfStock = outOfStock; // Gán giá trị outOfStock
        if (this.product != null) {
            this.itemTotal = this.product.getFinalPrice() * this.quantity;
        } else {
            this.itemTotal = 0;
        }
    }

    public String getId() {
        return id;
    }

    public String get_id() {
        return id;
    }

    public ProductInfo getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getItemTotal() {
        return itemTotal;
    }

    // THÊM HÀM GET NÀY
    public boolean isOutOfStock() {
        return outOfStock;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
        if (this.product != null) {
            this.itemTotal = this.product.getFinalPrice() * this.quantity;
        }
    }

    // SỬA LẠI WITHNEWQUANTITY: Gọi constructor mới, tạm gán outOfStock là false
    public CartItem withNewQuantity(int newQuantity) {
        return new CartItem(this.id, this.product, newQuantity, false);
    }

    public static class ProductInfo {

        @SerializedName("_id")
        private String id;

        @SerializedName("name")
        private String name;

        @SerializedName("image")
        private String image;

        @SerializedName("images")
        private List<String> images;

        @SerializedName("price")
        private double price;

        @SerializedName("effectivePrice")
        private double effectivePrice;

        // THÊM LẠI TRƯỜNG stock
        @SerializedName("stock")
        private int stock;

        @SerializedName("sale")
        private SaleInfo sale;

        public String getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public double getPrice() {
            return price;
        }

        public double getEffectivePrice() {
            return effectivePrice;
        }

        // THÊM LẠI HÀM GET NÀY
        public int getStock() {
            return stock;
        }

        public SaleInfo getSale() {
            return sale;
        }

        public List<String> getImages() {
            if (images == null || images.isEmpty()) {
                if (image != null && !image.isEmpty()) {
                    List<String> imageList = new ArrayList<>();
                    imageList.add(image);
                    return imageList;
                }
            }
            return images;
        }

        public double getFinalPrice() {
            if (sale != null && sale.isActive() && effectivePrice > 0 && effectivePrice < price) {
                return effectivePrice;
            }
            return price;
        }
    }

    public static class SaleInfo {
        @SerializedName("active")
        private boolean isActive;

        public boolean isActive() {
            return isActive;
        }
    }
}
