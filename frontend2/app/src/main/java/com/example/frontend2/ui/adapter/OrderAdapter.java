package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.model.OrderItem;

import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private final Context context;
    private List<Order> orderList;

    public OrderAdapter(Context context, List<Order> orderList) {
        this.context = context;
        this.orderList = orderList;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);
        holder.bind(order);
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public void updateOrders(List<Order> newOrders) {
        this.orderList = newOrders;
        notifyDataSetChanged();
    }

    class OrderViewHolder extends RecyclerView.ViewHolder {
        private final TextView orderIdText;
        private final TextView orderDateText;
        private final TextView orderTotalText;
        private final LinearLayout productImagesContainer;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            orderIdText = itemView.findViewById(R.id.orderIdText);
            orderDateText = itemView.findViewById(R.id.orderDateText);
            orderTotalText = itemView.findViewById(R.id.orderTotalText);
            productImagesContainer = itemView.findViewById(R.id.productImagesContainer);
        }

        public void bind(Order order) {
            orderIdText.setText("Đơn hàng #" + order.getId().substring(0, 8));
            orderDateText.setText("Mua lúc: " + formatDateTime(order.getCreatedAt()));

            DecimalFormat formatter = new DecimalFormat("###,###,###");
            orderTotalText.setText(formatter.format(order.getTotalPrice()) + "đ");

            // Xử lý hiển thị ảnh sản phẩm
            productImagesContainer.removeAllViews(); // Xóa ảnh cũ
            List<OrderItem> items = order.getOrderItems();
            int imageCount = Math.min(items.size(), 3);

            for (int i = 0; i < imageCount; i++) {
                ImageView imageView = createImageView(items.get(i).getImage());
                productImagesContainer.addView(imageView);
            }

            if (items.size() > 3) {
                View moreView = createMoreView(items.size() - 3);
                productImagesContainer.addView(moreView);
            }
        }

        private ImageView createImageView(String imageUrl) {
            ImageView imageView = new ImageView(context);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(dpToPx(60), dpToPx(60));
            if (productImagesContainer.getChildCount() > 0) {
                params.setMarginStart(dpToPx(8));
            }
            imageView.setLayoutParams(params);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            Glide.with(context).load(imageUrl).placeholder(R.drawable.placeholder).into(imageView);
            return imageView;
        }

        private View createMoreView(int remainingCount) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View moreView = inflater.inflate(R.layout.item_more_products, productImagesContainer, false);

            TextView countText = moreView.findViewById(R.id.more_count_text);
            countText.setText("+" + remainingCount);

            return moreView;
        }

        private String formatDateTime(String isoDate) {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault());
            try {
                Date date = inputFormat.parse(isoDate);
                return outputFormat.format(date);
            } catch (ParseException e) {
                e.printStackTrace();
                return isoDate; // Trả về ngày gốc nếu không parse được
            }
        }

        private int dpToPx(int dp) {
            return (int) (dp * context.getResources().getDisplayMetrics().density);
        }
    }
}
