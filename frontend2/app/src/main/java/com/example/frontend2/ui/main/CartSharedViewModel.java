package com.example.frontend2.ui.main;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class CartSharedViewModel extends ViewModel {

    // LiveData để giữ tổng số lượng sản phẩm
    private final MutableLiveData<Integer> cartItemCount = new MutableLiveData<>(0);

    // ---- Các hàm cho cartItemCount ----
    public LiveData<Integer> getCartItemCount() {
        return cartItemCount;
    }

    // Hàm để cập nhật số lượng từ bất cứ đâu (CartFragment, ProductDetailFragment...)
    public void setCartItemCount(int count) {
        // Dùng postValue để đảm bảo an toàn khi gọi từ bất kỳ luồng nào
        cartItemCount.postValue(count);
    }
}
