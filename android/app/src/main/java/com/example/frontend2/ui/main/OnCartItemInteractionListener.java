package com.example.frontend2.ui.main;

import com.example.frontend2.data.model.CartItem;

public interface OnCartItemInteractionListener {

    void onUpdateQuantity(String productId, int newQuantity);
    void onRemoveItem(String productId);
}
