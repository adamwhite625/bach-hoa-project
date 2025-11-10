// File: com/example/frontend2/ui/main/CartAdapter.java
package com.example.frontend2.ui.main;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // Thư viện phổ biến để tải ảnh
import com.example.frontend2.R;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.databinding.ItemCartBinding; // Sử dụng ViewBinding cho item

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private List<CartItem> cartItems;
    private final OnCartItemInteractionListener listener;

    // --- 1. Interface để giao tiếp với Fragment ---
    // Fragment sẽ implement interface này để nhận sự kiện từ Adapter
    public interface OnCartItemInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    // --- 2. Constructor ---
    public CartAdapter(List<CartItem> cartItems, OnCartItemInteractionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    // --- 3. ViewHolder: Nắm giữ các View của một item ---
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding; // Sử dụng ViewBinding

        public CartViewHolder(ItemCartBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        // Hàm bind dữ liệu từ CartItem vào các View
        public void bind(final CartItem item, final OnCartItemInteractionListener listener) {
            // Định dạng tiền tệ VNĐ
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            binding.tvProductName.setText(item.getProduct().getName());
            binding.tvPrice.setText(currencyFormat.format(item.getProduct().getPrice()));
            binding.tvQuantity.setText(String.valueOf(item.getQuantity()));

            // Dùng Glide để tải ảnh từ URL vào ImageView
            Glide.with(itemView.getContext())
                    .load(item.getProduct().getImages())
                    .placeholder(R.drawable.placeholder) // Ảnh hiển thị trong lúc tải
                    .error(R.drawable.error_image)         // Ảnh hiển thị nếu tải lỗi
                    .into(binding.imgProduct);

            // --- Gán sự kiện click cho các nút ---
            binding.btnPlus.setOnClickListener(v -> listener.onIncreaseQuantity(item));
            binding.btnMinus.setOnClickListener(v -> listener.onDecreaseQuantity(item));
            binding.tvRemove.setOnClickListener(v -> listener.onRemoveItem(item));
        }
    }


    // --- 4. Các phương thức bắt buộc của Adapter ---
    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Tạo ViewHolder mới bằng cách inflate layout item_cart.xml với ViewBinding
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCartBinding binding = ItemCartBinding.inflate(inflater, parent, false);
        return new CartViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        // Lấy item tại vị trí `position` và gọi hàm bind của ViewHolder
        CartItem currentItem = cartItems.get(position);
        holder.bind(currentItem, listener);
    }

    @Override
    public int getItemCount() {
        // Trả về số lượng item trong danh sách
        return cartItems != null ? cartItems.size() : 0;
    }


    // --- 5. Hàm tiện ích để cập nhật dữ liệu cho Adapter ---
    public void updateItems(List<CartItem> newItems) {
        this.cartItems.clear();
        this.cartItems.addAll(newItems);
        notifyDataSetChanged(); // Thông báo cho RecyclerView vẽ lại toàn bộ danh sách
    }
}
