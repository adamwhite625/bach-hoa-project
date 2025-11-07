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

import com.example.frontend2.adapter.OrderAdapter;
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentOrdersBinding;
import com.example.frontend2.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersFragment extends Fragment {

    private static final String TAG = "OrdersFragment";
    private FragmentOrdersBinding binding;
    private ApiService apiService;
    private OrderAdapter orderAdapter;

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

        setupRecyclerView();
        fetchOrders();
    }

    private void setupRecyclerView() {
        binding.ordersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        orderAdapter = new OrderAdapter(getContext(), new ArrayList<>());
        binding.ordersRecyclerView.setAdapter(orderAdapter);
    }

    private void fetchOrders() {
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null || token.isEmpty()) {
            showError("Vui lòng đăng nhập để xem đơn hàng");
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getMyOrders(authHeader).enqueue(new Callback<List<Order>>() {
            @Override
            public void onResponse(Call<List<Order>> call, Response<List<Order>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderAdapter.updateOrders(response.body());
                } else {
                    showError("Không thể tải danh sách đơn hàng");
                    Log.e(TAG, "Error fetching orders: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Order>> call, Throwable t) {
                showError("Lỗi kết nối mạng");
                Log.e(TAG, "Network error: ", t);
            }
        });
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
