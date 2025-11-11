// File: com/example/frontend2/ui/main/CartManager.java
package com.example.frontend2.ui.main;

import android.util.Log;
import androidx.annotation.NonNull;
import com.example.frontend2.data.model.AddToCartRequest;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Lớp quản lý trạng thái giỏ hàng (Singleton).
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;
    private final ApiService apiService;

    private CartManager() {
        cartItems = new ArrayList<>();
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // === Định nghĩa các interface Callback ===

    /**
     * Interface để lắng nghe kết quả THÊM sản phẩm vào giỏ hàng.
     */
    public interface CartUpdateCallback {
        void onSuccess(CartResponse updatedCart);
        void onFailure(String error);
    }

    /**
     * SỬA: THÊM MỚI INTERFACE NÀY
     * Interface để lắng nghe kết quả LẤY giỏ hàng từ server.
     */
    public interface FetchCartCallback {
        void onSuccess();
        void onFailure(String error);
    }


    // === Các hàm tương tác với API ===

    /**
     * Thêm một sản phẩm vào giỏ hàng và đồng bộ với backend.
     */
    public void addProductToCart(String authToken, ProductDetail product, int quantityToAdd, @NonNull CartUpdateCallback callback) {
        if (authToken == null || authToken.isEmpty()) {
            Log.e("CartManager", "Token không hợp lệ!");
            callback.onFailure("Token không hợp lệ!");
            return;
        }

        Log.d("CartManager", "Yêu cầu thêm " + quantityToAdd + " sản phẩm '" + product.getName() + "'");
        AddToCartRequest request = new AddToCartRequest(product.getId(), quantityToAdd);
        Log.d("CartManager_API", "Đang gọi API: addToCart...");

        apiService.addToCart(authToken, request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d("CartManager_API", "Thêm vào giỏ hàng THÀNH CÔNG!");
                    CartResponse newCart = response.body();
                    updateLocalCartData(newCart); // Cập nhật dữ liệu nội bộ
                    callback.onSuccess(newCart); // Trả kết quả về
                } else {
                    String errorMsg = "Lỗi " + response.code() + ": " + response.message();
                    Log.e("CartManager_API", "Thêm vào giỏ hàng thất bại. " + errorMsg);
                    callback.onFailure(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                String errorMsg = "Lỗi mạng hoặc kết nối: " + t.getMessage();
                Log.e("CartManager_API", errorMsg);
                callback.onFailure(errorMsg);
            }
        });
    }

    /**
     * SỬA: THÊM MỚI TOÀN BỘ HÀM NÀY
     * Gọi API để lấy thông tin giỏ hàng mới nhất từ server và đồng bộ.
     * @param authToken Token xác thực của người dùng.
     * @param callback Callback để nhận kết quả.
     */
    public void fetchCartFromServer(String authToken, @NonNull FetchCartCallback callback) {
        if (authToken == null || authToken.isEmpty()) {
            Log.w("CartManager", "Không có token, không thể lấy giỏ hàng từ server. Coi như giỏ hàng rỗng.");
            this.cartItems.clear(); // Xóa sạch dữ liệu local cũ
            callback.onSuccess(); // Báo thành công vì đã xử lý xong (giỏ hàng rỗng)
            return;
        }

        Log.d("CartManager_API", "Đang gọi API: getCart...");
        apiService.getCart(authToken).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful()) {
                    Log.d("CartManager_API", "Lấy giỏ hàng từ server thành công!");
                    updateLocalCartData(response.body());
                    callback.onSuccess();
                } else {
                    Log.e("CartManager_API", "Lỗi khi lấy giỏ hàng từ server: " + response.code());
                    // Khi có lỗi (ví dụ token hết hạn), ta cũng xóa sạch giỏ hàng local
                    cartItems.clear();
                    callback.onFailure("Lỗi " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Log.e("CartManager_API", "Lỗi mạng khi lấy giỏ hàng: " + t.getMessage());
                // Lỗi mạng không thể kết nối, không nên xóa giỏ hàng local
                callback.onFailure("Lỗi mạng");
            }
        });
    }

    // === Các hàm tiện ích ===

    /**
     * Cập nhật dữ liệu giỏ hàng nội bộ từ server.
     */
    public void updateLocalCartData(CartResponse cartResponse) {
        this.cartItems.clear();
        if (cartResponse != null && cartResponse.getItems() != null) {
            this.cartItems.addAll(cartResponse.getItems());
        }
        Log.d("CartManager", "Dữ liệu giỏ hàng nội bộ đã được cập nhật. Tổng số lượng: " + getCartItemCount());
    }

    /**
     * Lấy tổng số lượng của TẤT CẢ các mặt hàng trong giỏ.
     */
    public int getCartItemCount() {
        int totalCount = 0;
        for (CartItem item : cartItems) {
            totalCount += item.getQuantity();
        }
        return totalCount;
    }

    /**
     * Lấy danh sách các CartItem hiện có.
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
}
