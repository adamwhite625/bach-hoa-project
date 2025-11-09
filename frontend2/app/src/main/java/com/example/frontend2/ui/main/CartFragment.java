package com.example.frontend2.ui.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.R;

import java.text.NumberFormat;
import java.util.List;

public class CartFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_cart, container, false);
    }
}
// File: com/example/frontend2/ui/main/CartFragment.java (Đã code lại)
//package com.example.frontend2.ui.main;
//
//import android.os.Bundle;
//import android.util.Log;import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Toast;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.Nullable;
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//
//import com.example.frontend2.data.model.CartItem; // Giả sử bạn có model này
//import com.example.frontend2.data.remote.ApiClient;
//import com.example.frontend2.data.remote.ApiService;
//import com.example.frontend2.databinding.FragmentCartBinding; // Import class ViewBinding
//
//import java.text.NumberFormat;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Locale;
//
//// Giả sử bạn sẽ tạo interface này trong CartAdapter
//public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {
//
//    private static final String TAG = "CartFragment";
//
//    // 1. Khai báo ViewBinding và các biến cần thiết
//    private FragmentCartBinding binding;
//    private CartAdapter cartAdapter;
//    private com.google.firebase.appdistribution.gradle.ApiService apiService;
//    private List<CartItem> cartItems = new ArrayList<>();
//
//    @Override
//    public void onCreate(@Nullable Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        // Khởi tạo các đối tượng không phải View ở đây
//        apiService = ApiClient.getRetrofitInstance().create(com.google.firebase.appdistribution.gradle.ApiService.class);
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
//        // 2. Sử dụng ViewBinding để inflate layout
//        binding = FragmentCartBinding.inflate(inflater, container, false);
//        return binding.getRoot();
//    }
//
//    @Override
//    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
//        super.onViewCreated(view, savedInstanceState);
//
//        // 3. Thiết lập RecyclerView
//        setupRecyclerView();
//
//        // 4. Thiết lập các sự kiện click
//        setupClickListeners();
//
//        // 5. Tải dữ liệu giỏ hàng (từ API hoặc database local)
//        fetchCartItems();
//    }
//
//    private void setupRecyclerView() {
//        // Khởi tạo adapter với một danh sách rỗng và listener là Fragment này
//        cartAdapter = new CartAdapter(new ArrayList<>(), this);
//        binding.recyclerCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
//        binding.recyclerCartItems.setAdapter(cartAdapter);
//    }
//
//    private void setupClickListeners() {
//        binding.buttonCheckout.setOnClickListener(v -> {
//            if (cartItems.isEmpty()) {
//                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
//            } else {
//                // Xử lý logic chuyển sang màn hình thanh toán
//                Toast.makeText(getContext(), "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
//            }
//        });
//    }
//
//    private void fetchCartItems() {
//        // === PHẦN GIẢ LẬP DỮ LIỆU ===
//        // Trong thực tế, bạn sẽ gọi apiService.getCartItems() ở đây
//        // Ví dụ: apiService.getCart().enqueue(new Callback<CartResponse>() { ... });
//
//        // Tạm thời tạo dữ liệu giả để test giao diện
//        cartItems.clear();
//        cartItems.add(new CartItem("1", "Cà rốt Đà Lạt", 32900, "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/ca_rot_vcjtqu.webp", 2));
//        cartItems.add(new CartItem("2", "Thanh Long ruột đỏ", 11900, "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/thanh_long_o5jo83.webp", 1));
//        cartItems.add(new CartItem("3", "Dưa hấu đỏ", 14900, "https://res.cloudinary.com/daeeumk1i/image/upload/v1761484204/dua_hau_i4cw8r.webp", 3));
//
//        // Sau khi có dữ liệu, cập nhật giao diện
//        updateUI();
//        // ============================
//    }
//
//    // Hàm cập nhật toàn bộ giao diện dựa trên danh sách sản phẩm
//    private void updateUI() {
//        if (cartItems == null || cartItems.isEmpty()) {
//            binding.layoutEmptyCart.setVisibility(View.VISIBLE);
//            binding.recyclerCartItems.setVisibility(View.GONE);
//            binding.bottomSummaryLayout.setVisibility(View.GONE);
//        } else {
//            binding.layoutEmptyCart.setVisibility(View.GONE);
//            binding.recyclerCartItems.setVisibility(View.VISIBLE);
//            binding.bottomSummaryLayout.setVisibility(View.VISIBLE);
//
//            // Cập nhật dữ liệu cho adapter
//            cartAdapter.updateItems(cartItems);
//
//            // Tính toán và hiển thị tổng tiền
//            calculateAndDisplayTotal();
//        }
//    }
//
//    private void calculateAndDisplayTotal() {
//        double total = 0;
//        for (CartItem item : cartItems) {
//            total += item.getPrice() * item.getQuantity();
//        }
//        // Định dạng tiền tệ VNĐ
//        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
//        binding.textTotalPrice.setText(currencyFormat.format(total));
//    }
//
//
//    // --- Các phương thức Override từ Interface của Adapter ---
//
//    @Override
//    public void onIncreaseQuantity(CartItem item) {
//        androidx.camera.camera2.pipe.core.Log.d(TAG, "Tăng số lượng cho: " + item.getProductName());
//        int currentQuantity = item.getQuantity();
//        item.setQuantity(currentQuantity + 1);
//        // Trong thực tế: gọi API để cập nhật số lượng trên server
//        cartAdapter.notifyItemChanged(cartItems.indexOf(item));
//        calculateAndDisplayTotal();
//    }
//
//    @Override
//    public void onDecreaseQuantity(CartItem item) {
//        androidx.camera.camera2.pipe.core.Log.d(TAG, "Giảm số lượng cho: " + item.getProductName());
//        int currentQuantity = item.getQuantity();
//        if (currentQuantity > 1) {
//            item.setQuantity(currentQuantity - 1);
//            // Trong thực tế: gọi API để cập nhật số lượng trên server
//            cartAdapter.notifyItemChanged(cartItems.indexOf(item));
//            calculateAndDisplayTotal();
//        } else {
//            // Nếu số lượng là 1, giảm nữa sẽ là xóa sản phẩm
//            onRemoveItem(item);
//        }
//    }
//
//
//
//    @Override
//    public void onRemoveItem(CartItem item) {
//        androidx.camera.camera2.pipe.core.Log.d(TAG, "Xóa sản phẩm: " + item.getProductName());
//        int position = cartItems.indexOf(item);
//        if (position != -1) {
//            cartItems.remove(position);
//            // Trong thực tế: gọi API để xóa sản phẩm khỏi giỏ hàng trên server
//            updateUI(); // Gọi lại để kiểm tra xem giỏ hàng có rỗng không
//        }
//    }
//
//
//    // Đảm bảo giải phóng binding khi Fragment bị hủy View
//    @Override
//    public void onDestroyView() {
//        super.onDestroyView();
//        binding = null;
//    }
//}
