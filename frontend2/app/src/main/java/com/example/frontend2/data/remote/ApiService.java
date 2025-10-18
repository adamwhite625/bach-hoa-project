package com.example.frontend2.data.remote;

import com.example.frontend2.data.model.LoginRequest;
import com.example.frontend2.data.model.MessageResponse;
import com.example.frontend2.data.model.ResetPasswordRequest;
import com.example.frontend2.data.model.User;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("/api/auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @POST("/api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);
}