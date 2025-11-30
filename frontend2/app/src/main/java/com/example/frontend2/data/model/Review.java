package com.example.frontend2.data.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.Date;

public class Review implements Serializable {

    @SerializedName("_id")
    private String id;

    @SerializedName("user")
    private String user; // Giữ dưới dạng String (ObjectId từ server)

    @SerializedName("name")
    private String name;

    @SerializedName("rating")
    private int rating; // Dùng int cho số sao (1, 2, 3, 4, 5)

    @SerializedName("comment")
    private String comment;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;


    public Review(int rating, String comment) {
        this.rating = rating;
        this.comment = comment;
    }

    public Review(String id, String user, String name, int rating, String comment, Date createdAt, Date updatedAt) {
        this.id = id;
        this.user = user;
        this.name = name;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }
}
