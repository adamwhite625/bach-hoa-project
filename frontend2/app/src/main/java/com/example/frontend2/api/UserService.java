package com.example.frontend2.api;

import com.example.frontend2.data.model.LoginRequest;
import com.example.frontend2.data.model.User;

import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.PartMap;

public interface UserService {

    // Auth
    @POST("api/auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    // User
    @GET("api/users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    @Multipart
    @PUT("api/users/profile")
    Call<User> updateUserProfile(
            @Header("Authorization") String token,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );
}
