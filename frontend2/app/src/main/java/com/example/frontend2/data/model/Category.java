package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;

public class Category {

    // Giữ tên biến theo chuẩn Java (camelCase)
    @SerializedName("_id") // Ánh xạ biến "id" này với trường "_id" trong JSON
    private String id;

    private String name;
    private String description;
    private String image;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
