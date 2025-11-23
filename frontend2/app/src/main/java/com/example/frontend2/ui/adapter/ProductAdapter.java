package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.SaleInfo;

import java.util.List;
import java.util.Locale;

public class ProductAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final Context context;
    private List<ProductInList> productInListList;
    private final OnItemClickListener listener;

    private static final int VIEW_TYPE_NORMAL = 1;
    private static final int VIEW_TYPE_SALE = 2;

    public interface OnItemClickListener {
        void onItemClick(ProductInList productInList);
    }

    public ProductAdapter(Context context, List<ProductInList> productInListList, OnItemClickListener listener) {
        this.context = context;
        this.productInListList = productInListList;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        ProductInList product = productInListList.get(position);
        SaleInfo saleInfo = product.getSale();
        return (saleInfo != null && saleInfo.isActive()) ? VIEW_TYPE_SALE : VIEW_TYPE_NORMAL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == VIEW_TYPE_SALE) {
            View view = inflater.inflate(R.layout.item_product_flash_sale, parent, false);
            return new SaleProductViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_product, parent, false);
            return new NormalProductViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ProductInList product = productInListList.get(position);
        if (holder.getItemViewType() == VIEW_TYPE_SALE) {
            ((SaleProductViewHolder) holder).bind(product, listener);
        } else {
            ((NormalProductViewHolder) holder).bind(product, listener);
        }
    }

    @Override
    public int getItemCount() {
        return productInListList != null ? productInListList.size() : 0;
    }

    public void updateData(List<ProductInList> newData) {
        this.productInListList.clear();
        this.productInListList.addAll(newData);
        notifyDataSetChanged();
    }

    static class NormalProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, priceText;

        public NormalProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.product_image);
            nameText = itemView.findViewById(R.id.product_name);
            priceText = itemView.findViewById(R.id.product_price);
        }

        public void bind(final ProductInList product, final OnItemClickListener listener) {
            nameText.setText(product.getName());
            priceText.setText(String.format(Locale.GERMANY, "%,d ₫", product.getPrice()));

            // ✅ SỬA LỖI TẠI ĐÂY
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext()).load(product.getImageUrl()).into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }

            itemView.setOnClickListener(v -> listener.onItemClick(product));
        }
    }

    static class SaleProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, salePriceText, originalPriceText, discountPercentText;

        public SaleProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.product_image);
            nameText = itemView.findViewById(R.id.product_name);
            salePriceText = itemView.findViewById(R.id.product_price);
            originalPriceText = itemView.findViewById(R.id.product_old_price);
            discountPercentText = itemView.findViewById(R.id.sale_badge);

            if (nameText == null || salePriceText == null || originalPriceText == null || discountPercentText == null) {
                throw new IllegalStateException("CRASH PREVENTION: One or more TextViews in SaleProductViewHolder are null. " +
                        "Check the IDs in 'item_product_flash_sale.xml'!");
            }
        }

        public void bind(final ProductInList product, final OnItemClickListener listener) {
            // 1. Hiển thị các thông tin cơ bản không cần tính toán
            nameText.setText(product.getName());

            // 2. Load hình ảnh bằng Glide
            if (product.getImageUrl() != null && !product.getImageUrl().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImageUrl())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }

            // 3. Lấy thông tin sale
            SaleInfo saleInfo = product.getSale();

            long originalPrice = product.getPrice();

            int discountPercent = saleInfo.getValue();

            long finalSalePrice = originalPrice - (originalPrice * discountPercent / 100);

            salePriceText.setText(String.format(Locale.GERMANY, "%,d ₫", finalSalePrice));
            originalPriceText.setText(String.format(Locale.GERMANY, "%,d ₫", originalPrice));

            // Dùng định dạng "-%d%%" cho biến kiểu int (discountPercent). KHÔNG LỖI.
            discountPercentText.setText(String.format("-%d%%", discountPercent));

            // Thêm hiệu ứng gạch ngang cho giá gốc
            originalPriceText.setPaintFlags(originalPriceText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

            // 7. THIẾT LẬP SỰ KIỆN CLICK
            itemView.setOnClickListener(v -> listener.onItemClick(product));
        }
    }
}
