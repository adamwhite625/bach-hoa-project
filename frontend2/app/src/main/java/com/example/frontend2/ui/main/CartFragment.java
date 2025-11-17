package com.example.frontend2.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.adapter.CartAdapter;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentCartBinding;
// SỬA 1: Import đúng lớp SharedPrefManager của bạn
import com.example.frontend2.utils.SharedPrefManager;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {

    private static final String TAG = "CartFragment";

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private List<CartItem> cartItems = new ArrayList<>();
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupClickListeners();
        // Không cần gọi fetchCartItems() ở đây nữa vì onResume() sẽ xử lý
    }

    // Luôn làm mới giỏ hàng khi người dùng quay lại Fragment này
    @Override
    public void onResume() {
        super.onResume();
        // Đây là nơi tốt nhất để tải lại dữ liệu vì nó được gọi mỗi khi fragment hiển thị cho người dùng
        fetchCartItems();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, this);
        binding.recyclerCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        binding.buttonCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
                // TODO: Triển khai logic chuyển sang Activity/Fragment thanh toán
            }
        });
    }

    // Hàm gọi API để lấy giỏ hàng từ server
    private void fetchCartItems() {
        showLoading(true); // Hiển thị loading

        // SỬA 2: Sử dụng SharedPrefManager để lấy token
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            showLoading(false);
            // Khi giỏ hàng trống, ta vẫn cần cập nhật UI để hiển thị màn hình "giỏ hàng trống"
            this.cartItems.clear();
            updateUI();
            return;
        }

        apiService.getCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    cartItems = response.body().getItems();
                    updateUI();
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải giỏ hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Failure: ", t);
            }
        });
    }

    // Hàm cập nhật giao diện dựa trên dữ liệu mới
    private void updateUI() {
        boolean isCartEmpty = cartItems == null || cartItems.isEmpty();

        // Ẩn/hiện layout giỏ hàng trống và layout nội dung
        binding.layoutEmptyCart.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
        binding.cartContentLayout.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);

        if (!isCartEmpty) {
            cartAdapter.updateItems(cartItems);
            calculateAndDisplayTotal();
        }
    }

    // Hàm tính và hiển thị tổng tiền
    private void calculateAndDisplayTotal() {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct() != null) {
                total += item.getProduct().getPrice() * item.getQuantity();
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.textTotalPrice.setText(currencyFormat.format(total));
    }

    // --- Các hàm tương tác giỏ hàng gọi API ---

    @Override
    public void onIncreaseQuantity(CartItem item) {
        updateItemQuantity(item, item.getQuantity() + 1);
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        if (item.getQuantity() > 1) {
            updateItemQuantity(item, item.getQuantity() - 1);
        } else {
            onRemoveItem(item);
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) return;

        apiService.removeFromCart("Bearer " + token, item.get_id()).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                    cartItems = response.body().getItems();
                    updateUI();
                } else {
                    Toast.makeText(getContext(), "Xóa thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm chung để gọi API cập nhật số lượng
    private void updateItemQuantity(CartItem item, int newQuantity) {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) return;

        UpdateCartRequest request = new UpdateCartRequest(newQuantity);

        apiService.updateCartItem("Bearer " + token, item.get_id(), request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    cartItems = response.body().getItems();
                    updateUI();
                } else {
                    Toast.makeText(getContext(), "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }
            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Hàm tiện ích để hiển thị/ẩn ProgressBar
    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            // Vô hiệu hóa các nút để người dùng không nhấn nhiều lần
            binding.buttonCheckout.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.buttonCheckout.setEnabled(true);
        }
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Quan trọng để tránh rò rỉ bộ nhớ
    }
}
