// File: com/example/frontend2/manager/CartManager.java (hoặc package đúng của bạn)
// SỬA LẠI ĐÚNG PACKAGE
package com.example.frontend2.ui.main;

import com.example.frontend2.data.model.ProductDetail;
import java.util.ArrayList;
import java.util.List;

// LỚP CARTMANAGER ĐƠN GIẢN, KHÔNG CẦN LIVADATA
public class CartManager {

    private static CartManager instance;
    private final List<ProductDetail> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    public void addProductToCart(ProductDetail product) {
        cartItems.add(product);
    }

    public int getCartItemCount() {
        return cartItems.size();
    }

    public List<ProductDetail> getCartItems() {
        return new ArrayList<>(cartItems);
    }
}
