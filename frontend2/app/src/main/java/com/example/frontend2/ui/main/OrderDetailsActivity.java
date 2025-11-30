package com.example.frontend2.ui.main;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.frontend2.R;
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityOrderDetailsBinding;
import com.example.frontend2.ui.adapter.OrderDetailItemAdapter;
import com.example.frontend2.utils.SharedPrefManager;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderDetailsActivity extends AppCompatActivity {

    private ActivityOrderDetailsBinding binding;
    private ApiService apiService;
    private String orderId;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityOrderDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        orderId = getIntent().getStringExtra("ORDER_ID");
        if (orderId == null || orderId.isEmpty()) {
            Toast.makeText(this, "Không tìm thấy mã đơn hàng", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        setupToolbar();
        fetchOrderDetails();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void fetchOrderDetails() {
        showLoading(true);
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            showLoading(false);
            Toast.makeText(this, "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        apiService.getOrderDetails("Bearer " + token, orderId).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    updateUI(response.body());
                } else {
                    Toast.makeText(OrderDetailsActivity.this, "Lỗi khi tải chi tiết đơn hàng", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                showLoading(false);
                Toast.makeText(OrderDetailsActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(Order order) {
        binding.tvOrderId.setText("Mã đơn hàng: #" + order.getId().substring(0, 8).toUpperCase());
        binding.tvOrderDate.setText("Ngày đặt: " + formatDate(order.getCreatedAt()));
        binding.tvOrderStatus.setText(order.getOrderStatus());

        ShippingAddress address = order.getShippingAddress();
        if (address != null) {
            binding.tvShippingName.setText(address.getFullName());
            binding.tvShippingPhone.setText(address.getPhone());
            String fullAddress = address.getAddress() + ", " + address.getCity();
            binding.tvShippingAddress.setText(fullAddress);
        }

        OrderDetailItemAdapter adapter = new OrderDetailItemAdapter(this, order.getOrderItems(), order.getOrderStatus());
        binding.recyclerOrderItems.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerOrderItems.setAdapter(adapter);

        binding.tvSubtotalPrice.setText(currencyFormatter.format(order.getItemsPrice()));
        binding.tvShippingPrice.setText(currencyFormatter.format(order.getShippingPrice()));
        binding.tvTotalPrice.setText(currencyFormatter.format(order.getTotalPrice()));

        double discount = order.getItemsPrice() + order.getShippingPrice() - order.getTotalPrice();
        if (discount > 0.01) {
            binding.layoutDiscount.setVisibility(View.VISIBLE);
            binding.tvDiscountPrice.setText(String.format("-%s", currencyFormatter.format(discount)));
        } else {
            binding.layoutDiscount.setVisibility(View.GONE);
        }
    }

    private String formatDate(Date date) {
        if (date == null) return "N/A";
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
        return sdf.format(date);
    }

    private void showLoading(boolean isLoading) {
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.contentScrollView.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }
}
