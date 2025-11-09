// File: com/example/frontend2/data/model/ProductListResponse.java (Đã hoàn thiện)
package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Class này đại diện cho cấu trúc JSON Object mà API trả về
 * khi lấy danh sách sản phẩm. Đã được cập nhật để khớp 100% với JSON thực tế.
 */
public class ProductListResponse {

    // 1. Key chứa danh sách sản phẩm
    @SerializedName("products")
    private List<ProductInList> products;

    // 2. Key chứa thông tin trang hiện tại
    @SerializedName("page")
    private int page;

    // 3. Key chứa tổng số trang
    @SerializedName("pages")
    private int pages;

    // 4. Key chứa tổng số sản phẩm
    @SerializedName("total")
    private int total;


    // --- Getters ---
    // Các hàm này giúp bạn truy cập vào dữ liệu

    public List<ProductInList> getProducts() {
        return products;
    }

    public int getPage() {
        return page;
    }

    public int getPages() {
        return pages;
    }

    public int getTotal() {
        return total;
    }
}
