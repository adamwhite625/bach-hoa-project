package com.example.frontend2.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.BottomSheetVoucherBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
// Bạn sẽ cần tạo các lớp Request và Response này
import com.example.frontend2.data.model.ApplyVoucherRequest;
import com.example.frontend2.data.model.ApplyVoucherResponse;


public class VoucherBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetVoucherBinding binding;
    private ApiService apiService;

    // Interface để giao tiếp ngược lại với CartFragment
    public interface VoucherApplyListener {
        void onVoucherAppliedSuccessfully();
    }
    private VoucherApplyListener listener;

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

        binding.btnApplyVoucher.setOnClickListener(v -> {
            String voucherCode = binding.etVoucherCode.getText().toString().trim();
            if (voucherCode.isEmpty()) {
                binding.inputLayoutVoucher.setError("Vui lòng nhập mã giảm giá");
            } else {
                binding.inputLayoutVoucher.setError(null);
                applyVoucher(voucherCode);
            }
        });
    }

    private void applyVoucher(String code) {
        showLoading(true);
        // String authToken = ... // Lấy token xác thực
        // Call<ApplyVoucherResponse> call = apiService.applyVoucher(authToken, new ApplyVoucherRequest(code));

        // GIẢ LẬP GỌI API
        // Thay thế đoạn này bằng lời gọi API thật của bạn
        new android.os.Handler().postDelayed(() -> {
            showLoading(false);
            if (code.equals("GIAM50K")) {
                Toast.makeText(getContext(), "Áp dụng phiếu mua hàng thành công!", Toast.LENGTH_SHORT).show();
                if (listener != null) {
                    listener.onVoucherAppliedSuccessfully();
                }
                dismiss(); // Đóng BottomSheet
            } else {
                binding.inputLayoutVoucher.setError("Mã giảm giá không hợp lệ hoặc đã hết hạn");
            }
        }, 1500);
    }

    private void showLoading(boolean isLoading) {
        if (isLoading) {
            binding.btnApplyVoucher.setText("");
            binding.progressBarVoucher.setVisibility(View.VISIBLE);
        } else {
            binding.btnApplyVoucher.setText("Áp Dụng");
            binding.progressBarVoucher.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
