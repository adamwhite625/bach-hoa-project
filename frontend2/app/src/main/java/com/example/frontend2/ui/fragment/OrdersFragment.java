package com.example.frontend2.ui.fragment;

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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.data.model.ApiResponse;
import com.example.frontend2.data.model.OrderSummary;
import com.example.frontend2.data.model.TotalSpent;
import com.example.frontend2.ui.adapter.OrderAdapter;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentOrdersBinding;
import com.example.frontend2.ui.main.MainActivity;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.android.material.chip.Chip;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private static final String TAG = "OrdersFragment";
    private FragmentOrdersBinding binding;
    private ApiService apiService;
    private OrderAdapter orderAdapter;

    private List<OrderSummary> allOrders = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentOrdersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.searchBarLayout.setOnClickListener(v -> {
            if (getActivity() != null) {
                Intent intent = new Intent(getActivity(), MainActivity.class);
                intent.putExtra("ACTION", "OPEN_SEARCH_FRAGMENT");
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
            }
        });

        setupRecyclerView();
        setupFilterChips();
        fetchOrders();
        fetchTotalSpent(); // Gọi hàm để lấy tổng chi tiêu
    }

    private void setupRecyclerView() {
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(getContext(), new ArrayList<>());
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void setupFilterChips() {
        binding.chipGroupStatus.setOnCheckedChangeListener((group, checkedId) -> {
            Chip selectedChip = group.findViewById(checkedId);
            if (selectedChip != null) {
                filterOrdersByStatus(selectedChip.getText().toString());
            }
        });
    }

    private void fetchOrders() {
        if (getContext() == null) return;
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null || token.isEmpty()) {
            showError("Vui lòng đăng nhập để xem đơn hàng");
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getMyOrders(authHeader).enqueue(new Callback<List<OrderSummary>>() {
            @Override
            public void onResponse(@NonNull Call<List<OrderSummary>> call, @NonNull Response<List<OrderSummary>> response) {
                if (getContext() == null) return;

                if (response.isSuccessful() && response.body() != null) {
                    allOrders.clear();
                    allOrders.addAll(response.body());

                    // Set chip "Tất cả" làm chip mặc định được chọn
                    binding.chipAll.setChecked(true);
                    filterOrdersByStatus("Tất cả");

                } else {
                    showError("Không thể tải danh sách đơn hàng");
                    Log.e(TAG, "Error fetching orders: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<OrderSummary>> call, @NonNull Throwable t) {
                if (getContext() == null) return;
                showError("Lỗi kết nối mạng");
                Log.e(TAG, "Network error: ", t);
            }
        });
    }

    private void fetchTotalSpent() {
        if (getContext() == null) return;
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null || token.isEmpty()) {
            binding.totalSpentCard.setVisibility(View.GONE);
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getTotalSpent(authHeader).enqueue(new Callback<ApiResponse<TotalSpent>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<TotalSpent>> call, @NonNull Response<ApiResponse<TotalSpent>> response) {
                if (isAdded() && binding != null && response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    binding.totalSpentValue.setText(response.body().getData().getFormattedTotal());
                } else {
                    if (binding != null) {
                        binding.totalSpentValue.setText("Lỗi");
                    }
                    String errorMsg = response.body() != null ? response.body().getErrorMessage() : "Unknown Error";
                    Log.e(TAG, "Error fetching total spent: " + errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<TotalSpent>> call, @NonNull Throwable t) {
                if (isAdded() && binding != null) {
                    binding.totalSpentValue.setText("Lỗi mạng");
                }
                Log.e(TAG, "Network error fetching total spent: ", t);
            }
        });
    }


    private void filterOrdersByStatus(String chipText) {
        List<OrderSummary> filteredList;

        if ("Tất cả".equalsIgnoreCase(chipText)) {
            filteredList = new ArrayList<>(allOrders);
        } else {
            String backendStatus = mapChipTextToBackendStatus(chipText);

            filteredList = allOrders.stream()
                    .filter(order -> backendStatus.equalsIgnoreCase(order.getOrderStatus()))
                    .collect(Collectors.toList());
        }

        orderAdapter.updateData(filteredList);

        if (filteredList.isEmpty()) {
            binding.ordersRecyclerView.setVisibility(View.GONE);
            // Optionally show an empty state message
        } else {
            binding.ordersRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private String mapChipTextToBackendStatus(String chipText) {
        switch (chipText) {
            case "Chờ xử lý":
                return "Pending";
            case "Đang xử lý":
                return "Processing";
            case "Đang giao":
                return "Shipped";
            case "Hoàn thành":
                return "Delivered";
            case "Đã hủy":
                return "Cancelled";
            default:
                return "a_non_matching_string";
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
