package com.example.frontend2.ui.fragment;

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
import com.example.frontend2.data.model.LoyaltyStatus;
import com.example.frontend2.data.model.Voucher;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentVouchersBinding;
import com.example.frontend2.ui.adapter.VoucherAdapter;
import com.example.frontend2.utils.SharedPrefManager;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VouchersFragment extends Fragment {

    private static final String TAG = "VouchersFragment";
    private FragmentVouchersBinding binding;
    private ApiService apiService;
    private VoucherAdapter voucherAdapter;
    private String userTier = "bronze"; // Default tier

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentVouchersBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        setupToolbar();
        setupRecyclerView();
        fetchData();
    }

    private void setupToolbar() {
        // SỬA LỖI DỨT ĐIỂM: Dùng FragmentManager để quay lại màn hình trước đó
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        voucherAdapter = new VoucherAdapter(getContext(), new ArrayList<>());
        binding.vouchersRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.vouchersRecyclerView.setAdapter(voucherAdapter);
    }

    private void fetchData() {
        if (getContext() == null) return;
        String token = "Bearer " + SharedPrefManager.getInstance(getContext()).getAuthToken();

        // First, get the user's loyalty status
        apiService.getLoyaltyStatus(token).enqueue(new Callback<ApiResponse<LoyaltyStatus>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoyaltyStatus>> call, @NonNull Response<ApiResponse<LoyaltyStatus>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    userTier = response.body().getData().getCurrentTier();
                }
                fetchVouchers(token);
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoyaltyStatus>> call, @NonNull Throwable t) {
                Log.e(TAG, "Failed to get loyalty status", t);
                fetchVouchers(token);
            }
        });
    }

    private void fetchVouchers(String token) {
        apiService.getAvailableDiscounts(token).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(@NonNull Call<List<Voucher>> call, @NonNull Response<List<Voucher>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    filterAndDisplayVouchers(response.body());
                } else {
                    showError("Không thể tải phiếu mua hàng.");
                    Log.e(TAG, "Error fetching vouchers: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Voucher>> call, @NonNull Throwable t) {
                showError("Lỗi kết nối mạng khi tải phiếu mua hàng.");
                Log.e(TAG, "Network error fetching vouchers: ", t);
            }
        });
    }

    private void filterAndDisplayVouchers(List<Voucher> allVouchers) {
        if (userTier == null) userTier = "bronze";
        String finalUserTier = userTier.toLowerCase();

        List<Voucher> eligibleVouchers = allVouchers.stream()
                .filter(v -> {
                    String tierRequired = v.getTierRequired().toLowerCase();
                    return "all".equals(tierRequired) || finalUserTier.equals(tierRequired);
                })
                .collect(Collectors.toList());

        if (eligibleVouchers.isEmpty()) {
            binding.tvEmptyVouchers.setVisibility(View.VISIBLE);
            binding.vouchersRecyclerView.setVisibility(View.GONE);
        } else {
            binding.tvEmptyVouchers.setVisibility(View.GONE);
            binding.vouchersRecyclerView.setVisibility(View.VISIBLE);
            voucherAdapter.updateVouchers(eligibleVouchers);
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
            binding.tvEmptyVouchers.setText(message);
            binding.tvEmptyVouchers.setVisibility(View.VISIBLE);
            binding.vouchersRecyclerView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
