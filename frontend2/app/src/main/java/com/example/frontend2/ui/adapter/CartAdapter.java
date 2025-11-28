package com.example.frontend2.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.databinding.ItemCartBinding;
import com.example.frontend2.ui.main.OnCartItemInteractionListener; // Đảm bảo import đúng interface

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private final OnCartItemInteractionListener mListener;

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            if (oldItem == null || newItem == null || oldItem.get_id() == null || newItem.get_id() == null) {
                return false;
            }
            return oldItem.get_id().equals(newItem.get_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            if (oldItem == null || newItem == null) return false;
            if (oldItem.getProduct() == null || newItem.getProduct() == null) return false;

            return oldItem.getQuantity() == newItem.getQuantity()
                    && oldItem.getProduct().getStock() == newItem.getProduct().getStock();
        }
    };

    public CartAdapter(OnCartItemInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.mListener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        CartItem item = getItem(position);
        if (item == null) return;

        holder.bind(item);

        // Sự kiện nút Tăng (+)
        holder.binding.btnPlus.setOnClickListener(v -> {
            if (mListener != null) {
                if (item.getProduct() != null && item.getQuantity() < item.getProduct().getStock()) {
                    mListener.onUpdateQuantity(item.get_id(), item.getQuantity() + 1);
                } else {
                    Toast.makeText(holder.itemView.getContext(), "Đã đạt số lượng tồn kho tối đa", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Sự kiện nút Giảm (-)
        holder.binding.btnMinus.setOnClickListener(v -> {
            if (mListener != null) {
                int newQuantity = item.getQuantity() - 1;
                if (newQuantity < 1) {
                    mListener.onRemoveItem(item.get_id());
                } else {
                    mListener.onUpdateQuantity(item.get_id(), newQuantity);
                }
            }
        });
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final CartItem cartItem) {
            if (cartItem == null || cartItem.getProduct() == null) {
                return;
            }

            CartItem.ProductInfo product = cartItem.getProduct();
            binding.tvProductName.setText(product.getName());
            binding.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            // Cập nhật trạng thái của nút tăng/giảm dựa trên tồn kho
            boolean canIncrease = cartItem.getQuantity() < product.getStock();
            binding.btnPlus.setEnabled(canIncrease);
            binding.btnPlus.setAlpha(canIncrease ? 1.0f : 0.5f);

            // Cập nhật giá sản phẩm
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            boolean hasSale = product.getSale() != null && product.getSale().isActive() && product.getFinalPrice() < product.getPrice();

            if (hasSale) {
                binding.tvFinalPrice.setText(currencyFormat.format(product.getFinalPrice()));
                binding.tvOriginalPrice.setText(currencyFormat.format(product.getPrice()));
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                binding.tvOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                binding.tvFinalPrice.setText(currencyFormat.format(product.getPrice()));
                binding.tvOriginalPrice.setVisibility(View.GONE);
                binding.tvOriginalPrice.setPaintFlags(binding.tvOriginalPrice.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            }

            // Cập nhật hình ảnh sản phẩm
            List<String> images = product.getImages();
            if (images != null && !images.isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(images.get(0))
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.placeholder);
            }
        }
    }
}
