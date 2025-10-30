package com.example.frontend2.api;

import com.example.frontend2.data.model.User;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;

public interface UserService {
    @GET("user/profile")  // Đường dẫn API endpoint phù hợp với backend
    Call<User> getCurrentUser(@Header("Authorization") String token);
}
