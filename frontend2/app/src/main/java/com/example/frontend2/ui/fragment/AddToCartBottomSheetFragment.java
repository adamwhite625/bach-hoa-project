package com.example.frontend2.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.databinding.BottomSheetAddToCartBinding;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.text.NumberFormat;
import java.util.Locale;

public class AddToCartBottomSheetFragment extends BottomSheetDialogFragment {

    private BottomSheetAddToCartBinding binding;
    private ProductDetail mProduct;
    private int quantity = 1;

    public interface CartUpdateListener {
        void onCartUpdated(int quantity);
    }

    private CartUpdateListener mListener;

    public static AddToCartBottomSheetFragment newInstance(ProductDetail product) {
        AddToCartBottomSheetFragment fragment = new AddToCartBottomSheetFragment();
        Bundle args = new Bundle();
        args.putSerializable("product_data", product);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Fragment parentFragment = getParentFragment();
        if (parentFragment instanceof CartUpdateListener) {
            mListener = (CartUpdateListener) parentFragment;
        } else if (context instanceof CartUpdateListener) {
            mListener = (CartUpdateListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement CartUpdateListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mProduct = (ProductDetail) getArguments().getSerializable("product_data");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = BottomSheetAddToCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (mProduct == null) {
            Toast.makeText(getContext(), "Lỗi, không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            dismiss();
            return;
        }

        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvPriceBottomSheet.setText(currencyFormat.format(mProduct.getFinalPrice()));
        binding.tvStockBottomSheet.setText("Kho: " + mProduct.getStock());

        if (mProduct.getDetailImages() != null && !mProduct.getDetailImages().isEmpty()) {
            Glide.with(this)
                    .load(mProduct.getDetailImages().get(0).getUrl())
                    .into(binding.imgProductBottomSheet);
        }

        setupEventListeners();
    }

    private void setupEventListeners() {
        binding.btnDecrease.setOnClickListener(v -> {
            if (quantity > 1) {
                quantity--;
                binding.tvQuantity.setText(String.valueOf(quantity));
            }
        });

        binding.btnIncrease.setOnClickListener(v -> {
            if (quantity < mProduct.getStock()) {
                quantity++;
                binding.tvQuantity.setText(String.valueOf(quantity));
            } else {
                Toast.makeText(getContext(), "Số lượng đã đạt giới hạn tồn kho", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnConfirmAdd.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onCartUpdated(quantity);
            }
            dismiss();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
