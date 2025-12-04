package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessageRequest {

    @SerializedName("message")
    private String message;

    public ChatMessageRequest(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
