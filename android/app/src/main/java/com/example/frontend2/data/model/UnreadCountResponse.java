package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class UnreadCountResponse {

    @SerializedName("unreadCount")
    private int unreadCount;

    public int getUnreadCount() {
        return unreadCount;
    }

    public boolean hasUnread() {
        return unreadCount > 0;
    }
}
