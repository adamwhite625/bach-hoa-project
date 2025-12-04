package com.example.frontend2.ui.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide; // <-- IMPORT THƯ VIỆN GLIDE
import com.example.frontend2.R;

import java.util.List;

// Adapter này CHUYÊN DÙNG để hiển thị ảnh từ danh sách các URL (String)
public class ImageUrlSliderAdapter extends RecyclerView.Adapter<ImageUrlSliderAdapter.SliderViewHolder> {

    private final List<String> imageUrls;

    // Hàm khởi tạo nhận vào một List<String>
    public ImageUrlSliderAdapter(List<String> imageUrls) {
        this.imageUrls = imageUrls;
    }

    @NonNull
    @Override
    public SliderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Bạn có thể dùng lại layout item_slider.xml của SliderAdapter cũ
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_slider, parent, false);
        return new SliderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SliderViewHolder holder, int position) {
        String imageUrl = imageUrls.get(position);

        // Dùng Glide để tải ảnh từ URL vào ImageView
        Glide.with(holder.itemView.getContext())
                .load(imageUrl) // Nguồn ảnh là một URL (String)
                .placeholder(R.drawable.placeholder) // Ảnh hiển thị trong lúc chờ tải
                .error(R.drawable.error_image)         // Ảnh hiển thị nếu tải lỗi
                .into(holder.imageView);               // ImageView đích để hiển thị ảnh
    }

    @Override
    public int getItemCount() {
        return imageUrls != null ? imageUrls.size() : 0;
    }

    // ViewHolder class, có thể giống hệt SliderAdapter cũ
    public static class SliderViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public SliderViewHolder(@NonNull View itemView) {
            super(itemView);
            // Đảm bảo ID này khớp với ID của ImageView trong file item_slider.xml
            imageView = itemView.findViewById(R.id.image_slide);
        }
    }
}
