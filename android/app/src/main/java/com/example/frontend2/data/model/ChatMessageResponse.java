package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ChatMessageResponse {

    @SerializedName("response")
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
