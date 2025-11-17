package com.example.frontend2.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.ProductInList;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<ProductInList> productInListList;
    private final OnItemClickListener listener;

    // Interface callback khi click
    public interface OnItemClickListener {
        void onItemClick(ProductInList productInList);
    }

    public ProductAdapter(Context context, List<ProductInList> productInListList, OnItemClickListener listener) {
        this.context = context;
        this.productInListList = productInListList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ProductViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product, parent, false);
        return new ProductViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductViewHolder holder, int position) {
        ProductInList productInList = productInListList.get(position);
        holder.bind(productInList, listener);
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

    static class ProductViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        TextView nameText, priceText;

        public ProductViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.product_image);
            nameText = itemView.findViewById(R.id.product_name);
            priceText = itemView.findViewById(R.id.product_price);
        }

        public void bind(ProductInList productInList, OnItemClickListener listener) {
            // Log sản phẩm ra để kiểm tra
            nameText.setText(productInList.getName());
            priceText.setText(String.format("%,.0f ₫", productInList.getPrice()));

            // ✅ Load ảnh (vì giờ là String, không phải List)
            if (productInList.getImage() != null && !productInList.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(productInList.getImage())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }

            // Xử lý click item
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(productInList);
                else Toast.makeText(itemView.getContext(),
                        "Sản phẩm: " + productInList.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
