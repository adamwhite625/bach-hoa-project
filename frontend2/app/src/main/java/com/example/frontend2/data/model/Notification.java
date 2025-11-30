package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

public class Notification {

    @SerializedName("notifications")
    private List<NotificationItem> notifications;

    @SerializedName("unreadCount")
    private int unreadCount;

    public List<NotificationItem> getNotifications() {
        return notifications;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public static class NotificationItem {
        @SerializedName("_id")
        private String id;
        @SerializedName("user")
        private String userId;
        @SerializedName("type")
        private String type;
        @SerializedName("title")
        private String title;
        @SerializedName("message")
        private String message;
        @SerializedName("data")
        private NotificationData data;
        @SerializedName("isRead")
        private boolean isRead;
        @SerializedName("readAt")
        private String readAt;
        @SerializedName("createdAt")
        private String createdAt;
        @SerializedName("updatedAt")
        private String updatedAt;

        public String getId() {
            return id;
        }

        public String getUserId() {
            return userId;
        }

        public String getType() {
            return type;
        }

        public String getTitle() {
            return title;
        }

        public String getMessage() {
            return message;
        }

        public NotificationData getData() {
            return data;
        }

        public boolean isRead() {
            return isRead;
        }

        public String getReadAt() {
            return readAt;
        }

        public String getCreatedAt() {
            return createdAt;
        }

        public String getUpdatedAt() {
            return updatedAt;
        }

        public void setRead(boolean read) {
            isRead = read;
        }

        public String getFormattedCreatedAt() {
            if (createdAt == null || createdAt.isEmpty()) {
                return "";
            }
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            try {
                Date notificationDate = sdf.parse(createdAt);
                if (notificationDate == null) return "";
                long timeInMillis = notificationDate.getTime();
                long now = System.currentTimeMillis();
                long diff = now - timeInMillis;
                long seconds = TimeUnit.MILLISECONDS.toSeconds(diff);
                long minutes = TimeUnit.MILLISECONDS.toMinutes(diff);
                long hours = TimeUnit.MILLISECONDS.toHours(diff);
                long days = TimeUnit.MILLISECONDS.toDays(diff);
                if (seconds < 60) {
                    return "vài giây trước";
                } else if (minutes < 60) {
                    return minutes + " phút trước";
                } else if (hours < 24) {
                    return hours + " giờ trước";
                } else if (days == 1) {
                    return "Hôm qua";
                } else if (days < 7) {
                    return days + " ngày trước";
                } else {
                    SimpleDateFormat fullDateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
                    fullDateFormat.setTimeZone(TimeZone.getDefault());
                    return fullDateFormat.format(notificationDate);
                }
            } catch (ParseException e) {
                e.printStackTrace();
                if (createdAt.length() >= 10) {
                    return createdAt.substring(0, 10);
                }
                return "";
            }
        }
    }

    public static class NotificationData {
        @SerializedName("orderId")
        private String orderId;
        @SerializedName("orderCode")
        private String orderCode;
        @SerializedName("oldStatus")
        private String oldStatus;
        @SerializedName("newStatus")
        private String newStatus;

        public String getOrderId() {
            return orderId;
        }

        public String getOrderCode() {
            return orderCode;
        }

        public String getOldStatus() {
            return oldStatus;
        }

        public String getNewStatus() {
            return newStatus;
        }
    }
}
