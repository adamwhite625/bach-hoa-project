package com.example.frontend2.ui.main;

import com.example.frontend2.data.model.CartItem;

public interface OnCartItemInteractionListener {

    void onIncreaseQuantity(CartItem item);

    void onDecreaseQuantity(CartItem item);

    void onRemoveItem(CartItem item);
}
