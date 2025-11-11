// File: com/example/frontend2/ui/main/CartFragment.java
package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider; // SỬA 1: Import ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentCartBinding;
import com.example.frontend2.utils.SharedPrefManager;
import com.example.frontend2.ui.main.CartSharedViewModel; // SỬA 2: Import SharedViewModel

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

    // SỬA 3: Khai báo SharedViewModel
    private CartSharedViewModel sharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // SỬA 4: Khởi tạo SharedViewModel với scope của Activity
        // Điều này đảm bảo nó dùng chung ViewModel với các Fragment/Activity khác
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CartSharedViewModel.class);
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
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchCartItems();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(cartItems, this);
        binding.recyclerCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCartItems.setAdapter(cartAdapter);
    }

    private void setupClickListeners() {
        // Logic nút back của bạn không sai, nhưng trong kiến trúc Fragment,
        // người dùng thường dùng nút back của hệ thống.
        // Tôi giữ nguyên logic này của bạn.
        binding.toolbarCart.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed(); // Cách xử lý back chuẩn hơn
            }
        });

        binding.buttonCheckout.setOnClickListener(v -> {
            if (cartItems.isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchCartItems() {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            showLoading(false);
            this.cartItems.clear();
            updateUI(null); // Truyền null để báo là giỏ hàng trống
            return;
        }

        apiService.getCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body()); // SỬA 5: Gọi hàm updateUI với dữ liệu mới
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải giỏ hàng: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    /**
     * SỬA LẠI TOÀN BỘ CÁC HÀM GỌI API ĐỂ CẬP NHẬT VIEWMODEL
     */

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
                handleApiResponse(response); // Gọi hàm xử lý chung
            }
            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                handleApiFailure(t); // Gọi hàm xử lý chung
            }
        });
    }

    private void updateItemQuantity(CartItem item, int newQuantity) {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) return;

        UpdateCartRequest request = new UpdateCartRequest(newQuantity);
        apiService.updateCartItem("Bearer " + token, item.get_id(), request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                handleApiResponse(response); // Gọi hàm xử lý chung
            }
            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                handleApiFailure(t); // Gọi hàm xử lý chung
            }
        });
    }

    /**
     * SỬA 6: Tạo các hàm xử lý API chung để tránh lặp code và tích hợp ViewModel
     */
    private void handleApiResponse(Response<CartResponse> response) {
        showLoading(false);
        if (response.isSuccessful() && response.body() != null) {
            Toast.makeText(getContext(), "Cập nhật giỏ hàng thành công", Toast.LENGTH_SHORT).show();
            updateUI(response.body()); // Cập nhật giao diện và ViewModel
        } else {
            Toast.makeText(getContext(), "Thao tác thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
        }
    }

    private void handleApiFailure(Throwable t) {
        showLoading(false);
        Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
    }

    /**
     * SỬA 7: Hàm updateUI giờ sẽ nhận dữ liệu và cập nhật cả ViewModel
     */
    private void updateUI(CartResponse cartResponse) {
        int totalCount = 0;
        // Cập nhật lại danh sách local
        if (cartResponse != null && cartResponse.getItems() != null) {
            this.cartItems = cartResponse.getItems();
            // Tính tổng số lượng
            for (CartItem item : this.cartItems) {
                totalCount += item.getQuantity();
            }
        } else {
            this.cartItems.clear(); // Nếu cartResponse null thì giỏ hàng rỗng
        }

        // THÔNG BÁO CHO CÁC FRAGMENT/ACTIVITY KHÁC BIẾT SỐ LƯỢNG MỚI
        sharedViewModel.setCartItemCount(totalCount);

        // Cập nhật giao diện của chính CartFragment
        boolean isCartEmpty = cartItems.isEmpty();
        binding.layoutEmptyCart.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
        binding.cartContentLayout.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);
        if (!isCartEmpty) {
            cartAdapter.updateItems(cartItems);
            calculateAndDisplayTotal();
        }
    }

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

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.buttonCheckout.setEnabled(!isLoading);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
