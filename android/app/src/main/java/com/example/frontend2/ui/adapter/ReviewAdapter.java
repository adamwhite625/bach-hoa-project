package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.R;
import com.example.frontend2.data.model.Review;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

public class ReviewAdapter extends RecyclerView.Adapter<ReviewAdapter.ReviewViewHolder> {

    private Context context;
    private List<Review> reviewList;

    public ReviewAdapter(Context context, List<Review> reviewList) {
        this.context = context;
        this.reviewList = reviewList;
    }

    @NonNull
    @Override
    public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.review_item, parent, false);
        return new ReviewViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ReviewViewHolder holder, int position) {
        Review review = reviewList.get(position);

        holder.tvReviewerName.setText(review.getName());
        holder.rbReviewRating.setRating(review.getRating());
        holder.tvReviewComment.setText(review.getComment());

        // Định dạng ngày tháng cho đẹp
        if (review.getCreatedAt() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            holder.tvReviewDate.setText(sdf.format(review.getCreatedAt()));
        }
    }

    @Override
    public int getItemCount() {
        return reviewList != null ? reviewList.size() : 0;
    }

    public static class ReviewViewHolder extends RecyclerView.ViewHolder {
        TextView tvReviewerName, tvReviewComment, tvReviewDate;
        RatingBar rbReviewRating;

        public ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReviewerName = itemView.findViewById(R.id.tv_reviewer_name);
            rbReviewRating = itemView.findViewById(R.id.rb_review_rating);
            tvReviewComment = itemView.findViewById(R.id.tv_review_comment);
            tvReviewDate = itemView.findViewById(R.id.tv_review_date);
        }
    }

    public void updateData(List<Review> newList) {
        this.reviewList.clear();
        this.reviewList.addAll(newList);
        notifyDataSetChanged();
    }

}
