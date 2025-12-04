package com.example.frontend2.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.example.frontend2.R;
import com.example.frontend2.databinding.BottomSheetPaymentMethodBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class PaymentMethodBottomSheet extends BottomSheetDialogFragment {

    private BottomSheetPaymentMethodBinding binding;
    private String selectedPaymentMethod = "COD";
    private double totalAmount;
    private PaymentMethodListener listener;

    public interface PaymentMethodListener {
        void onPaymentMethodConfirmed(String paymentMethod);
    }

    public static PaymentMethodBottomSheet newInstance(double totalAmount) {
        PaymentMethodBottomSheet fragment = new PaymentMethodBottomSheet();
        Bundle args = new Bundle();
        args.putDouble("totalAmount", totalAmount);
        fragment.setArguments(args);
        return fragment;
    }

    public void setCurrentPaymentMethod(String paymentMethod) {
        if (paymentMethod != null && !paymentMethod.isEmpty()) {
            this.selectedPaymentMethod = paymentMethod;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            totalAmount = getArguments().getDouble("totalAmount", 0);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = BottomSheetPaymentMethodBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.btnConfirmInBottomSheet.setVisibility(View.VISIBLE);

        setupListeners();
        displayTotalAmount();
        updateSelection(selectedPaymentMethod);
    }

    private void setupListeners() {
        binding.layoutCOD.setOnClickListener(v -> updateSelection("COD"));
        binding.layoutZaloPay.setOnClickListener(v -> updateSelection("ZaloPay"));

        binding.btnConfirmInBottomSheet.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPaymentMethodConfirmed(selectedPaymentMethod);
                dismiss();
            } else {
                Toast.makeText(getContext(), "Lỗi xác nhận thanh toán", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnClose.setOnClickListener(v -> dismiss());
    }

    private void updateSelection(String paymentMethod) {
        selectedPaymentMethod = paymentMethod;

        if (getContext() == null) return;

        boolean isCOD = "COD".equals(paymentMethod);
        binding.radioCOD.setChecked(isCOD);
        binding.radioZaloPay.setChecked(!isCOD);

        binding.layoutCOD.setBackground(ContextCompat.getDrawable(requireContext(),
                isCOD ? R.drawable.payment_method_selected_bg : R.drawable.payment_method_item_bg));
        binding.layoutZaloPay.setBackground(ContextCompat.getDrawable(requireContext(),
                isCOD ? R.drawable.payment_method_item_bg : R.drawable.payment_method_selected_bg));
    }

    private void displayTotalAmount() {
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvTotalAmount.setText(currencyFormatter.format(totalAmount));
    }

    public void setPaymentMethodListener(PaymentMethodListener listener) {
        this.listener = listener;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
