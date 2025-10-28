package com.example.frontend2.data.remote;

import com.example.frontend2.data.model.LoginRequest;
import com.example.frontend2.data.model.MessageResponse;
import com.example.frontend2.data.model.Product;
import com.example.frontend2.data.model.ResetPasswordRequest;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.model.Category;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import  retrofit2.http.GET;

public interface ApiService {
    @POST("/api/auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

    @GET("/api/products")
    Call<List<Product>> getProducts();

    @GET("/api/categories")
    Call<List<Category>> getCategories();
}