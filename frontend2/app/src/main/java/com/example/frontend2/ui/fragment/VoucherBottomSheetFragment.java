package com.example.frontend2.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.data.model.Voucher;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.BottomSheetVoucherBinding;
import com.example.frontend2.ui.adapter.SelectableVoucherAdapter;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VoucherBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetVoucherBinding binding;
    private ApiService apiService;
    private SelectableVoucherAdapter voucherAdapter;
    private VoucherApplyListener listener;

    public interface VoucherApplyListener {
        void onVoucherSelectedForValidation(String voucherCode);
    }

    public void setVoucherApplyListener(VoucherApplyListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetVoucherBinding.inflate(inflater, container, false);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupClickListeners();
        fetchAvailableVouchers();
    }

    private void setupRecyclerView() {
        voucherAdapter = new SelectableVoucherAdapter(new ArrayList<>(), voucherCode -> {
            if (listener != null) {
                listener.onVoucherSelectedForValidation(voucherCode);
            }
            dismiss();
        });
        binding.recyclerViewVouchers.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewVouchers.setAdapter(voucherAdapter);
    }

    private void setupClickListeners() {
        binding.btnApplyVoucher.setOnClickListener(v -> {
            String voucherCode = binding.etVoucherCode.getText().toString().trim().toUpperCase();
            if (voucherCode.isEmpty()) {
                binding.inputLayoutVoucher.setError("Vui lòng nhập mã");
            } else {
                binding.inputLayoutVoucher.setError(null);
                if (listener != null) {
                    listener.onVoucherSelectedForValidation(voucherCode);
                }
                dismiss();
            }
        });
    }

    private void fetchAvailableVouchers() {
        showListLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showListLoading(false);
            return;
        }

        apiService.getAvailableDiscounts("Bearer " + token).enqueue(new Callback<List<Voucher>>() {
            @Override
            public void onResponse(@NonNull Call<List<Voucher>> call, @NonNull Response<List<Voucher>> response) {
                showListLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    List<Voucher> vouchers = response.body();
                    if (vouchers.isEmpty()) {
                        binding.tvAvailableVouchersTitle.setText("Không có voucher nào khả dụng");
                    } else {
                        voucherAdapter.updateVouchers(vouchers);
                    }
                } else {
                    binding.tvAvailableVouchersTitle.setText("Không thể tải danh sách voucher");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Voucher>> call, @NonNull Throwable t) {
                showListLoading(false);
                binding.tvAvailableVouchersTitle.setText("Lỗi kết nối");
            }
        });
    }

    private void showListLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBarList.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        binding.recyclerViewVouchers.setVisibility(isLoading ? View.GONE : View.VISIBLE);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
