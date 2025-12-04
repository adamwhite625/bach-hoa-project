package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class ImageInfo {

    @SerializedName("_id")
    private String id;
    private String url;

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
