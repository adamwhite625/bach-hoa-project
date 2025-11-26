package com.example.frontend2.ui.adapter;

import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.databinding.ItemCartBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends ListAdapter<CartItem, CartAdapter.CartViewHolder> {

    private final OnCartItemInteractionListener listener;

    private static final DiffUtil.ItemCallback<CartItem> DIFF_CALLBACK = new DiffUtil.ItemCallback<CartItem>() {
        @Override
        public boolean areItemsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            return oldItem.get_id().equals(newItem.get_id());
        }

        @Override
        public boolean areContentsTheSame(@NonNull CartItem oldItem, @NonNull CartItem newItem) {
            if (oldItem.getProduct() == null || newItem.getProduct() == null) return false;
            return oldItem.getQuantity() == newItem.getQuantity()
                    && oldItem.isOutOfStock() == newItem.isOutOfStock()
                    && oldItem.getProduct().getStock() == newItem.getProduct().getStock();
        }
    };

    public CartAdapter(@NonNull OnCartItemInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCartBinding binding = ItemCartBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(getItem(position), listener);
    }

    public static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;

        public CartViewHolder(@NonNull ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        public void bind(final CartItem cartItem, final OnCartItemInteractionListener listener) {
            if (cartItem == null || cartItem.getProduct() == null) {
                return;
            }

            showQuantityControls(true);

            CartItem.ProductInfo product = cartItem.getProduct();
            binding.tvProductName.setText(product.getName());
            binding.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));

            if (cartItem.isOutOfStock()) {
//                binding.tvStockWarning.setVisibility(View.VISIBLE);
//                binding.tvStockWarning.setText("Vượt tồn kho (còn " + product.getStock() + ")");
                binding.btnPlus.setEnabled(false);
                binding.btnPlus.setAlpha(0.5f);
            } else if (cartItem.getQuantity() >= product.getStock()) {
//                binding.tvStockWarning.setVisibility(View.GONE);
                binding.btnPlus.setEnabled(false);
                binding.btnPlus.setAlpha(0.5f);
            } else {
//                binding.tvStockWarning.setVisibility(View.GONE);
                binding.btnPlus.setEnabled(true);
                binding.btnPlus.setAlpha(1.0f);
            }

            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            boolean hasSale = product.getSale() != null && product.getSale().isActive() && product.getEffectivePrice() < product.getPrice();

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

            List<String> images = product.getImages();
            if (images != null && !images.isEmpty()) {
                Glide.with(itemView.getContext()).load(images.get(0)).placeholder(R.drawable.placeholder).error(R.drawable.error_image).into(binding.imgProduct);
            } else {
                binding.imgProduct.setImageResource(R.drawable.placeholder);
            }

            binding.btnPlus.setOnClickListener(v -> {
                if (listener != null) {
                    if (cartItem.getQuantity() < product.getStock()) {
                        showQuantityControls(false);
                        listener.onIncreaseQuantity(cartItem);
                    } else {
                        Toast.makeText(itemView.getContext(), "Đã đạt số lượng tồn kho tối đa", Toast.LENGTH_SHORT).show();
                    }
                }
            });

            binding.btnMinus.setOnClickListener(v -> {
                if (listener != null) {
                    showQuantityControls(false);
                    listener.onDecreaseQuantity(cartItem);
                }
            });
        }

        private void showQuantityControls(boolean show) {
            binding.quantitySelector.setVisibility(show ? View.VISIBLE : View.INVISIBLE);
            binding.quantityProgressBar.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    public interface OnCartItemInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }
}
