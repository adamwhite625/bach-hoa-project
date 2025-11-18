// File: com/example/frontend2/ui/main/CartAdapter.java
package com.example.frontend2.ui.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
// === GIỮ NGUYÊN CÁC IMPORT BẠN CẦN ===
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.ImageInfo;      // << THÊM IMPORT NÀY
import com.example.frontend2.data.model.ProductDetail;    // Dùng cho kết quả API
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ItemCartBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.CartViewHolder> {

    private final List<CartItem> cartItems;
    private final OnCartItemInteractionListener listener;

    public interface OnCartItemInteractionListener {
        void onIncreaseQuantity(CartItem item);
        void onDecreaseQuantity(CartItem item);
        void onRemoveItem(CartItem item);
    }

    public CartAdapter(List<CartItem> cartItems, OnCartItemInteractionListener listener) {
        this.cartItems = cartItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        ItemCartBinding binding = ItemCartBinding.inflate(inflater, parent, false);
        return new CartViewHolder(binding, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull CartViewHolder holder, int position) {
        holder.bind(cartItems.get(position));
    }

    @Override
    public int getItemCount() {
        return cartItems != null ? cartItems.size() : 0;
    }

    public void updateItems(List<CartItem> newItems) {
        if (newItems == null) return;
        this.cartItems.clear();
        this.cartItems.addAll(newItems);
        notifyDataSetChanged();
    }

    // =================================================================
    // ===                 LỚP VIEW HOLDER ĐÃ SỬA LẠI              ===
    // =================================================================
    public static class CartViewHolder extends RecyclerView.ViewHolder {
        private final ItemCartBinding binding;
        private final ApiService apiService;
        private final OnCartItemInteractionListener listener;

        public CartViewHolder(@NonNull ItemCartBinding binding, OnCartItemInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener;
            this.apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        }

        public void bind(final CartItem cartItem) {
            binding.imgProduct.setImageResource(R.drawable.placeholder);

            CartItem.ProductInfo productInfo = cartItem.getProduct();
            if (productInfo == null) {
                Log.e("BIND_ERROR", "ProductInfo trong CartItem bị null!");
                return;
            }

            binding.tvProductName.setText(productInfo.getName());
            binding.tvQuantity.setText(String.valueOf(cartItem.getQuantity()));
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
            binding.tvPrice.setText(currencyFormat.format(productInfo.getPrice()));

            // GIỮ NGUYÊN LOGIC BẤM NÚT CỦA BẠN
            binding.btnPlus.setOnClickListener(v -> listener.onIncreaseQuantity(cartItem));
            binding.btnMinus.setOnClickListener(v -> listener.onDecreaseQuantity(cartItem));
            binding.tvRemove.setOnClickListener(v -> listener.onRemoveItem(cartItem));

            String productId = productInfo.get_id();
            if (productId == null || productId.isEmpty()) {
                Log.w("API_CALL", "ProductId bị null hoặc rỗng, không thể gọi API.");
                return;
            }

            Log.d("API_CALL", "Chuẩn bị gọi API chi tiết sản phẩm với ID: " + productId);

            apiService.getProductById(productId).enqueue(new Callback<ProductDetail>() {
                @Override
                public void onResponse(@NonNull Call<ProductDetail> call, @NonNull Response<ProductDetail> response) {
                    if (getBindingAdapterPosition() == RecyclerView.NO_POSITION) return;

                    if (response.isSuccessful() && response.body() != null) {
                        ProductDetail fullProductDetails = response.body();

                        // === SỬA LỖI DUY NHẤT TẠI ĐÂY ===
                        // 1. Thay List<String> bằng List<ImageInfo> để khớp với model ProductDetail
                        List<ImageInfo> images = fullProductDetails.getDetailImages();
                        Log.d("API_CALL", "Gọi API thành công cho sản phẩm: " + fullProductDetails.getName());

                        if (images != null && !images.isEmpty()) {
                            // 2. Lấy đối tượng ImageInfo đầu tiên
                            ImageInfo firstImage = images.get(0);
                            // 3. Từ đối tượng ImageInfo, lấy ra chuỗi URL
                            String imageUrl = firstImage.getUrl();

                            Log.i("API_CALL", "URL ảnh lấy được: " + imageUrl);

                            if (itemView.getContext() != null) {
                                Glide.with(itemView.getContext())
                                        .load(imageUrl) // Tải URL vừa lấy được
                                        .placeholder(R.drawable.placeholder)
                                        .error(R.drawable.error_image)
                                        .into(binding.imgProduct);
                            }
                        } else {
                            Log.w("API_CALL", "Sản phẩm '" + fullProductDetails.getName() + "' có trả về nhưng không có ảnh.");
                        }
                    } else {
                        Log.e("API_CALL", "Lỗi khi gọi API chi tiết sản phẩm. Code: " + response.code());
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ProductDetail> call, @NonNull Throwable t) {
                    Log.e("API_CALL", "Thất bại khi gọi API chi tiết sản phẩm. ID: " + productId + ", Lỗi: " + t.getMessage());
                }
            });
        }
    }
}
