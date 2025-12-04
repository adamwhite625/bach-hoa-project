package com.example.frontend2.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.LayoutInflater;import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.AsyncListDiffer;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.R;
import com.example.frontend2.data.model.Notification;
import com.example.frontend2.databinding.NotificationItemBinding;

import java.util.List;
import java.util.Objects;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {

    private final AsyncListDiffer<Notification.NotificationItem> differ;
    private final OnNotificationListener listener;

    public interface OnNotificationListener {
        void onNotificationClick(Notification.NotificationItem notification, int position);
        void onNotificationLongClick(Notification.NotificationItem notification, int position);
    }

    public NotificationAdapter(OnNotificationListener listener) {
        this.listener = listener;
        this.differ = new AsyncListDiffer<>(this, new DiffUtil.ItemCallback<Notification.NotificationItem>() {
            @Override
            public boolean areItemsTheSame(@NonNull Notification.NotificationItem oldItem, @NonNull Notification.NotificationItem newItem) {
                return Objects.equals(oldItem.getId(), newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Notification.NotificationItem oldItem, @NonNull Notification.NotificationItem newItem) {
                return Objects.equals(oldItem, newItem);
            }
        });
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        NotificationItemBinding binding = NotificationItemBinding.inflate(
                LayoutInflater.from(parent.getContext()),
                parent,
                false
        );
        return new NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notification.NotificationItem notification = differ.getCurrentList().get(position);
        holder.bind(notification, listener);
    }

    @Override
    public int getItemCount() {
        return differ.getCurrentList().size();
    }

    public void updateData(List<Notification.NotificationItem> newList) {
        differ.submitList(newList);
    }

    public List<Notification.NotificationItem> getCurrentList() {
        return differ.getCurrentList();
    }

    static class NotificationViewHolder extends RecyclerView.ViewHolder {
        private final NotificationItemBinding binding;
        private final Context context;

        public NotificationViewHolder(NotificationItemBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
            this.context = itemView.getContext();
        }

        public void bind(final Notification.NotificationItem notification, final OnNotificationListener listener) {
            binding.tvNotificationTitle.setText(notification.getTitle());
            binding.tvNotificationContent.setText(notification.getMessage());
            binding.tvNotificationTime.setText(notification.getFormattedCreatedAt());

            if (notification.isRead()) {
                binding.unreadIndicator.setVisibility(View.GONE);
                binding.tvNotificationTitle.setTypeface(null, Typeface.NORMAL);
                binding.getRoot().setBackgroundColor(Color.WHITE);
            } else {
                binding.unreadIndicator.setVisibility(View.VISIBLE);
                binding.tvNotificationTitle.setTypeface(null, Typeface.BOLD);
                binding.getRoot().setBackgroundColor(ContextCompat.getColor(context, R.color.notification_unread_bg));
            }

            itemView.setOnClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNotificationClick(notification, position);
                }
            });

            itemView.setOnLongClickListener(v -> {
                int position = getBindingAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    listener.onNotificationLongClick(notification, position);
                    return true;
                }
                return false;
            });
        }
    }
}
