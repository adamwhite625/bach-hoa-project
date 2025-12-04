package com.example.frontend2.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.frontend2.data.model.AddToCartRequest;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartManager {

    private static final String TAG = "CartManager";
    private static volatile CartManager instance;
    private final ApiService apiService;

    private final MutableLiveData<List<CartItem>> _cartItems = new MutableLiveData<>(Collections.emptyList());
    public final LiveData<List<CartItem>> cartItems = _cartItems;

    private final MutableLiveData<Integer> _cartItemCount = new MutableLiveData<>(0);
    public final LiveData<Integer> cartItemCount = _cartItemCount;

    private final MutableLiveData<String> _error = new MutableLiveData<>();
    public final LiveData<String> error = _error;

    private CartManager() {
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    public static CartManager getInstance() {
        if (instance == null) {
            synchronized (CartManager.class) {
                if (instance == null) {
                    instance = new CartManager();
                }
            }
        }
        return instance;
    }

    public void fetchCart(String authToken) {
        if (authToken == null || authToken.isEmpty()) {
            _cartItems.postValue(Collections.emptyList());
            updateCartCount(0);
            return;
        }

        apiService.getCart("Bearer " + authToken).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateLocalCart(response.body());
                } else {
                    handleApiError(response);
                    _cartItems.postValue(Collections.emptyList());
                    updateCartCount(0);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Fetch Cart Failure: " + t.getMessage());
                _error.postValue("Lỗi mạng: " + t.getMessage());
                _cartItems.postValue(Collections.emptyList());
                updateCartCount(0);
            }
        });
    }

    public void addToCart(String authToken, ProductDetail product, int quantity) {
        if (authToken == null || authToken.isEmpty()) {
            _error.postValue("Bạn cần đăng nhập để thêm sản phẩm");
            return;
        }

        AddToCartRequest request = new AddToCartRequest(product.getId(), quantity);
        apiService.addToCart("Bearer " + authToken, request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateLocalCart(response.body());
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "Add To Cart Failure: " + t.getMessage());
                _error.postValue("Lỗi mạng: " + t.getMessage());
            }
        });
    }

    private void updateLocalCart(CartResponse cartResponse) {
        List<CartItem> items = (cartResponse != null && cartResponse.getItems() != null)
                ? cartResponse.getItems()
                : Collections.emptyList();

        _cartItems.postValue(items);

        int totalCount = 0;
        for (CartItem item : items) {
            totalCount += item.getQuantity();
        }
        updateCartCount(totalCount);
    }

    private void updateCartCount(int count) {
        if (_cartItemCount.getValue() == null || _cartItemCount.getValue() != count) {
            _cartItemCount.postValue(count);
        }
    }

    private void handleApiError(Response<?> response) {
        String errorMessage = "Lỗi không xác định";
        if (response.errorBody() != null) {
            try {
                errorMessage = response.errorBody().string();
            } catch (IOException e) {
                errorMessage = "Lỗi khi đọc phản hồi từ server";
            }
        }
        Log.e(TAG, "API Error: " + response.code() + " - " + errorMessage);
        _error.postValue("Lỗi " + response.code() + ": " + errorMessage);
    }

    public void clearCart() {
        _cartItems.postValue(Collections.emptyList());
        _cartItemCount.postValue(0);
    }
}
