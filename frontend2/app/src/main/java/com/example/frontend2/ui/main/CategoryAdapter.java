package com.example.frontend2.ui.main;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.Category;
import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categoryList;

    public CategoryAdapter(Context context, List<Category> categoryList) {
        this.context = context;
        this.categoryList = categoryList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.txtName.setText(category.getName());
        Glide.with(context).load(category.getImage()).into(holder.imgCategory);
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }
    public void updateData(List<Category> newData) {
        // 1. Xóa toàn bộ dữ liệu cũ trong danh sách
        this.categoryList.clear();
        // 2. Thêm tất cả dữ liệu mới được truyền vào
        this.categoryList.addAll(newData);
        // 3. Thông báo cho Adapter rằng toàn bộ dữ liệu đã thay đổi
        //    RecyclerView sẽ vẽ lại tất cả các item
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgCategory;
        TextView txtName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgCategory = itemView.findViewById(R.id.imageCategory);
            txtName = itemView.findViewById(R.id.textCategoryName);
        }
    }
}
