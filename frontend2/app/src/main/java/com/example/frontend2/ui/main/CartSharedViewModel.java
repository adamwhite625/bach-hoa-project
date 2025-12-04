package com.example.frontend2.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CartSharedViewModel extends ViewModel {

    private final MutableLiveData<Integer> cartItemCount = new MutableLiveData<>(0);

    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    public void setCartItemCount(int count) {
        if (cartItemCount.getValue() == null || cartItemCount.getValue() != count) {
            cartItemCount.postValue(count);
        }
    }
}
