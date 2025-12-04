package com.example.frontend2.data.model;

public class ChatMessage {
    private String message;
    private boolean isFromUser;

    public ChatMessage(String message, boolean isFromUser) {
        this.message = message;
        this.isFromUser = isFromUser;
    }

    public String getMessage() {
        return message;
    }

    public boolean isFromUser() {
        return isFromUser;
    }
}
