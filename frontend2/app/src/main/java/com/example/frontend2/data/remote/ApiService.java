package com.example.frontend2.data.remote;

import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.LoginRequest;
import com.example.frontend2.data.model.MessageResponse;
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.ResetPasswordRequest;
import com.example.frontend2.data.model.User;

import java.util.List;
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
import retrofit2.http.Path;

public interface ApiService {

    // --- Auth ---
    @POST("api/auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @POST("api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordRequest resetPasswordRequest);

    // --- User ---
    @GET("api/users/profile")
    Call<User> getUserProfile(@Header("Authorization") String token);

    @Multipart
    @PUT("api/users/profile")
    Call<User> updateUserProfile(
            @Header("Authorization") String token,
            @PartMap Map<String, RequestBody> partMap,
            @Part MultipartBody.Part file
    );

    // --- Products ---
    @GET("api/products")
    Call<List<ProductInList>> getProducts();

    @GET("api/products/{id}")
    Call<ProductDetail> getProductById(@Path("id") String productId);

    // --- Categories ---
    @GET("api/categories")
    Call<List<Category>> getCategories();

    // --- Orders ---
    @GET("api/orders/myorders")
    Call<List<Order>> getMyOrders(@Header("Authorization") String token);
}
s