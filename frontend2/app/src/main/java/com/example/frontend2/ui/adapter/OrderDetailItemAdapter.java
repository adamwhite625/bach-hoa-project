package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.OrderItem;
import com.example.frontend2.ui.main.ReviewActivity;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class OrderDetailItemAdapter extends RecyclerView.Adapter<OrderDetailItemAdapter.ViewHolder> {

    private final Context context;
    private final List<OrderItem> orderItems;
    private final String orderStatus;
    private final NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

    public OrderDetailItemAdapter(Context context, List<OrderItem> orderItems, String orderStatus) {
        this.context = context;
        this.orderItems = orderItems;
        this.orderStatus = orderStatus;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_detail_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        OrderItem item = orderItems.get(position);
        holder.bind(item);

        if ("Delivered".equalsIgnoreCase(orderStatus)) {
            holder.btnReviewProduct.setVisibility(View.VISIBLE);
        } else {
            holder.btnReviewProduct.setVisibility(View.GONE);
        }

        holder.btnReviewProduct.setOnClickListener(v -> {
            Toast.makeText(context, "Đánh giá sản phẩm: " + item.getName(), Toast.LENGTH_SHORT).show();
        });

        holder.btnReviewProduct.setOnClickListener(v -> {
            Intent intent = new Intent(context, ReviewActivity.class);
            intent.putExtra("PRODUCT_ID", item.getProductId()); // ID của sản phẩm để gửi lên API
            intent.putExtra("PRODUCT_NAME", item.getName()); // Tên để hiển thị
            intent.putExtra("PRODUCT_IMAGE", item.getImage()); // Ảnh để hiển thị
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return orderItems != null ? orderItems.size() : 0;
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivProductImage;
        TextView tvProductName, tvProductQuantity, tvProductPrice;
        Button btnReviewProduct;

        ViewHolder(View itemView) {
            super(itemView);
            ivProductImage = itemView.findViewById(R.id.ivProductImage);
            tvProductName = itemView.findViewById(R.id.tvProductName);
            tvProductQuantity = itemView.findViewById(R.id.tvProductQuantity);
            tvProductPrice = itemView.findViewById(R.id.tvProductPrice);
            btnReviewProduct = itemView.findViewById(R.id.btnReviewProduct);
        }

        void bind(OrderItem item) {
            tvProductName.setText(item.getName());
            tvProductQuantity.setText("x " + item.getQuantity());
            tvProductPrice.setText(currencyFormatter.format(item.getPrice()));

            Glide.with(itemView.getContext())
                    .load(item.getImage())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .into(ivProductImage);
        }
    }
}
