package com.example.frontend2.ui.main;

import android.content.Context;
import android.util.Log;
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
import com.example.frontend2.data.model.Product;
import com.google.gson.Gson;

import java.util.List;

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ProductViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final OnItemClickListener listener;

    // Interface callback khi click
    public interface OnItemClickListener {
        void onItemClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnItemClickListener listener) {
        this.context = context;
        this.productList = productList;
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
        Product product = productList.get(position);
        holder.bind(product, listener);
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    public void updateData(List<Product> newData) {
        this.productList.clear();
        this.productList.addAll(newData);
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

        public void bind(Product product, OnItemClickListener listener) {
            // Log sản phẩm ra để kiểm tra
            nameText.setText(product.getName());
            priceText.setText(String.format("%,.0f ₫", product.getPrice()));

            // ✅ Load ảnh (vì giờ là String, không phải List)
            if (product.getImage() != null && !product.getImage().isEmpty()) {
                Glide.with(itemView.getContext())
                        .load(product.getImage())
                        .placeholder(R.drawable.placeholder)
                        .error(R.drawable.error_image)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.placeholder);
            }

            // Xử lý click item
            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(product);
                else Toast.makeText(itemView.getContext(),
                        "Sản phẩm: " + product.getName(), Toast.LENGTH_SHORT).show();
            });
        }
    }
}
