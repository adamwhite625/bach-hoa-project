// File: com/example/frontend2/ui/main/CartManager.java
package com.example.frontend2.ui.main;

import android.util.Log;
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
 * Chịu trách nhiệm đồng bộ giỏ hàng với backend.
 * Lớp này không biết về Context hay SharedPreferences, nó chỉ nhận token và làm việc.
 */
public class CartManager {

    private static CartManager instance;
    private final List<CartItem> cartItems;

    private CartManager() {
        cartItems = new ArrayList<>();
        // Trong một ứng dụng thực tế, bạn có thể muốn gọi một API
        // để tải giỏ hàng hiện có của người dùng ngay khi khởi tạo Manager.
    }

    public static synchronized CartManager getInstance() {
        if (instance == null) {
            instance = new CartManager();
        }
        return instance;
    }

    // ====================================================================
    // ===       HÀM CHÍNH ĐỂ THÊM SẢN PHẨM VÀ ĐỒNG BỘ VỚI SERVER      ===
    // ====================================================================
    /**
     * Thêm một sản phẩm vào giỏ hàng và đồng bộ với backend.
     * @param authToken Token xác thực của người dùng (có "Bearer ").
     * @param product   Đối tượng sản phẩm chi tiết cần thêm.
     * @param quantityToAdd Số lượng sản phẩm muốn thêm.
     */
    public void addProductToCart(String authToken, ProductDetail product, int quantityToAdd) {
        // 1. Kiểm tra điều kiện đầu vào
        if (authToken == null || authToken.isEmpty()) {
            Log.e("CartManager", "Không thể thêm vào giỏ hàng: Auth Token không hợp lệ hoặc bị thiếu!");
            // Trong một ứng dụng thực tế, bạn có thể ném một Exception hoặc sử dụng một Callback để
            // báo lỗi về cho tầng UI (Activity/Fragment) để xử lý (ví dụ: chuyển đến màn hình đăng nhập).
            return;
        }

        Log.d("CartManager", "Yêu cầu thêm " + quantityToAdd + " sản phẩm '" + product.getName() + "'");

        // 2. Chuẩn bị Request Body cho API
        AddToCartRequest request = new AddToCartRequest(product.getId(), quantityToAdd);

        // 3. Khởi tạo và gọi API bằng Retrofit
        ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        Log.d("CartManager_API", "Đang gọi API: addToCart...");

        apiService.addToCart(authToken, request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(Call<CartResponse> call, Response<CartResponse> response) {
                // 4. Xử lý kết quả trả về từ API
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    Log.d("CartManager_API", "Thêm vào giỏ hàng THÀNH CÔNG! Cập nhật lại giỏ hàng từ server.");

                    // Cập nhật lại toàn bộ danh sách giỏ hàng phía client
                    // bằng dữ liệu mới nhất, đáng tin cậy nhất từ server.
                    cartItems.clear();
                    cartItems.addAll(response.body().getItems());

                    Log.d("CartManager", "Cập nhật thành công. Tổng số loại sản phẩm trong giỏ: " + cartItems.size());
                    Log.d("CartManager", "Tổng số lượng hàng trong giỏ: " + getCartItemCount());

                    // TODO (Nâng cao): Gửi một sự kiện (Broadcast, LiveData, EventBus) để
                    // các màn hình khác (như MainActivity) tự động cập nhật số lượng trên icon giỏ hàng.
                } else {
                    // Xử lý khi API trả về lỗi (ví dụ: 401 Unauthorized, 400 Bad Request, 500 Server Error)
                    Log.e("CartManager_API", "Thêm vào giỏ hàng thất bại. Code: " + response.code() + ", Message: " + response.message());
                }
            }

            @Override
            public void onFailure(Call<CartResponse> call, Throwable t) {
                // Xử lý khi không có kết nối mạng hoặc các lỗi khác của Retrofit
                Log.e("CartManager_API", "Lỗi mạng hoặc lỗi kết nối khi thêm vào giỏ hàng: " + t.getMessage());
            }
        });
    }

    /**
     * Lấy tổng số lượng của TẤT CẢ các mặt hàng trong giỏ (ví dụ: 2 táo + 3 cam = 5).
     * @return Tổng số lượng các sản phẩm.
     */
    public int getCartItemCount() {
        int totalCount = 0;
        for (CartItem item : cartItems) {
            totalCount += item.getQuantity();
        }
        return totalCount;
    }

    /**
     * Lấy danh sách các CartItem hiện có trong giỏ.
     * Trả về một bản sao của danh sách để đảm bảo dữ liệu gốc không bị thay đổi từ bên ngoài.
     * @return Một danh sách mới chứa các CartItem.
     */
    public List<CartItem> getCartItems() {
        return new ArrayList<>(cartItems);
    }
}
