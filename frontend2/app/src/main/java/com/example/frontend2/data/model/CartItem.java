// File: com/example/frontend2/data/model/CartItem.java
package com.example.frontend2.data.model;

import java.util.List;

public class CartItem {
    // ID của item trong giỏ hàng (ví dụ: 60c72b...)
    private String _id;

    // Đối tượng Product được lồng vào
    private ProductInfo product;

    // Số lượng
    private int quantity;


    // Getters and Setters
    public String get_id() { return _id; }
    public void set_id(String _id) { this._id = _id; }
    public ProductInfo getProduct() { return product; }
    public void setProduct(ProductInfo product) { this.product = product; }
    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }


    // Lớp con để chứa thông tin Product lồng vào
    public static class ProductInfo {
        private String _id;
        private String name;
        private double price;
        private List<String> images;

        // Getters and Setters
        public String get_id() { return _id; }
        public void set_id(String _id) { this._id = _id; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public double getPrice() { return price; }
        public void setPrice(double price) { this.price = price; }
        public List<String> getImages() { return images; }
        public void setImages(List<String> images) { this.images = images; }
    }
}
