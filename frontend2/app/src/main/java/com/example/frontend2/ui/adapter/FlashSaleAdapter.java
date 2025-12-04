// File: src/main/java/com/example/frontend2/ui/adapter/FlashSaleAdapter.java

package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.SaleInfo;
// QUAN TRỌNG: Đảm bảo tên file binding này chính xác.
// Nó được tạo tự động từ tên file layout: item_product_flash_sale.xml -> ItemProductFlashSaleBinding
import com.example.frontend2.databinding.ItemProductFlashSaleBinding;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class FlashSaleAdapter extends RecyclerView.Adapter<FlashSaleAdapter.FlashSaleViewHolder> {

    private final Context context;
    private final List<ProductInList> productList;
    // Tái sử dụng interface OnItemClickListener từ ProductAdapter để xử lý click
    private final ProductAdapter.OnItemClickListener listener;

    public FlashSaleAdapter(Context context, List<ProductInList> productList, ProductAdapter.OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FlashSaleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Sử dụng ViewBinding để tạo ViewHolder, cách làm hiện đại và an toàn
        ItemProductFlashSaleBinding binding = ItemProductFlashSaleBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new FlashSaleViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull FlashSaleViewHolder holder, int position) {
        ProductInList product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    public List<ProductInList> getProductList() {
        return productList;
    }

    // Lớp ViewHolder chứa các View của một item
    class FlashSaleViewHolder extends RecyclerView.ViewHolder {
        private final ItemProductFlashSaleBinding binding;

        public FlashSaleViewHolder(ItemProductFlashSaleBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Gắn dữ liệu từ đối tượng Product vào các View trong item layout.
         * @param product Đối tượng sản phẩm chứa dữ liệu.
         * @param listener Interface để xử lý sự kiện click.
         */
        public void bind(final ProductInList product, final ProductAdapter.OnItemClickListener listener) {
            // Khởi tạo đối tượng để định dạng tiền tệ cho Việt Nam
            NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

            // Hiển thị thông tin cơ bản
            binding.productName.setText(product.getName());
            Glide.with(context)
                    .load(product.getImageUrl())
                    .into(binding.productImage);

            // Xử lý logic hiển thị giá và khuyến mãi
            SaleInfo sale = product.getSale();
            if (sale != null && sale.isActive()) {
                // Nếu có khuyến mãi và đang hoạt động
                binding.productOldPrice.setVisibility(View.VISIBLE);
                binding.saleBadge.setVisibility(View.VISIBLE);

                // 1. Hiển thị giá gốc (bị gạch ngang)
                binding.productOldPrice.setText(currencyFormat.format(product.getPrice()));
                binding.productOldPrice.setPaintFlags(binding.productOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

                // 2. Tính và hiển thị giá mới (sau khi giảm)
                double newPrice = product.getPrice();
                if ("percent".equals(sale.getType())) {
                    newPrice = product.getPrice() * (100.0 - sale.getValue()) / 100.0;
                    binding.saleBadge.setText("-" + sale.getValue() + "%");
                }
                // (Bạn có thể thêm logic cho type "fixed" ở đây nếu cần)
                binding.productPrice.setText(currencyFormat.format(newPrice));

            } else {
                // Nếu không có sale, ẩn các view liên quan và chỉ hiển thị giá gốc
                binding.productPrice.setText(currencyFormat.format(product.getPrice()));
                binding.productOldPrice.setVisibility(View.GONE);
                binding.saleBadge.setVisibility(View.GONE);
            }

            // Bắt sự kiện click cho toàn bộ item
            itemView.setOnClickListener(v -> listener.onItemClick(product));
        }
    }

    public void updateData(List<ProductInList> newProductList) {
        // 1. Xóa sạch danh sách hiện tại mà Adapter đang giữ
        this.productList.clear();
        // 2. Thêm tất cả các sản phẩm từ danh sách mới vào
        this.productList.addAll(newProductList);
        // 3. Quan trọng nhất: Báo cho RecyclerView biết dữ liệu đã thay đổi để nó vẽ lại.
        notifyDataSetChanged();

        // Thêm log để kiểm tra
//        androidx.camera.camera2.pipe.core.Log.d("FlashSaleAdapter", "Data updated. New size: " + this.productList.size());
    }
}
