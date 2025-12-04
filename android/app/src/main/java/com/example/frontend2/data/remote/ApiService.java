package com.example.frontend2.data.remote;


import com.example.frontend2.data.model.ApiResponse;
import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.LoginRequest;
import com.example.frontend2.data.model.LoyaltyInfo;
import com.example.frontend2.data.model.LoyaltyStatus;
import com.example.frontend2.data.model.MessageResponse;
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.model.OrderRequest;
import com.example.frontend2.data.model.OrderSummary;
import com.example.frontend2.data.model.PreviewVoucherRequest;
import com.example.frontend2.data.model.PreviewVoucherResponse;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.ProductListResponse;
import com.example.frontend2.data.model.RegisterRequest;
import com.example.frontend2.data.model.ResetPasswordFinalRequest;
import com.example.frontend2.data.model.ResetPasswordRequest;
import com.example.frontend2.data.model.Review;
import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.model.ShippingAddressResponse;
import com.example.frontend2.data.model.TotalSpent;
import com.example.frontend2.data.model.UnreadCountResponse;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.model.AddToCartRequest;
import com.example.frontend2.data.model.ChatMessageRequest;
import com.example.frontend2.data.model.Notification;


import com.example.frontend2.data.model.ValidateVoucherResponse;
import com.example.frontend2.data.model.Voucher;
import com.google.gson.JsonElement;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
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
import retrofit2.http.Query;
import retrofit2.http.DELETE;

public interface ApiService {

    // --- Auth ---
    @POST("api/auth/register")
    Call<User> registerUser(@Body RegisterRequest registerRequest);

    @POST("api/auth/login")
    Call<User> loginUser(@Body LoginRequest loginRequest);

    @POST("api/auth/forgot-password")
    Call<MessageResponse> forgotPassword(@Body ResetPasswordRequest request);

    @POST("api/auth/reset-password")
    Call<MessageResponse> resetPassword(@Body ResetPasswordFinalRequest request);

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

    // --- Loyalty Tier ---
    @GET("api/users/loyalty/info")
    Call<ApiResponse<LoyaltyInfo>> getLoyaltyInfo(@Header("Authorization") String token);

    @GET("api/users/loyalty/status")
    Call<ApiResponse<LoyaltyStatus>> getLoyaltyStatus(@Header("Authorization") String token);

    @GET("api/users/loyalty/total-spent")
    Call<ApiResponse<TotalSpent>> getTotalSpent(@Header("Authorization") String token);

    // --- Shipping Address ---
    @GET("api/users/shipping-address")
    Call<ShippingAddressResponse> getShippingAddress(@Header("Authorization") String token);

    @PUT("api/users/shipping-address")
    Call<ShippingAddressResponse> updateShippingAddress(
            @Header("Authorization") String token,
            @Body ShippingAddress shippingAddress
    );

    // --- Products ---
    @GET("api/products")
    Call<List<ProductInList>> getProducts();

    @GET("api/products")
    Call<ProductListResponse> getProductsByCategory(@Query("category") String categoryId);

    @GET("api/products/{id}")
    Call<ProductDetail> getProductById(@Path("id") String productId);

    @POST("api/products/{id}/reviews")
    Call<Review> createReview(@Header("Authorization") String token,
                              @Path("id") String productId,
                              @Body Review reviewData);

    @GET("api/products/search")
    Call<JsonElement> searchProducts(@Query("keyword") String keyword);

    // --- Categories ---
    @GET("api/categories")
    Call<List<Category>> getCategories();

    @GET("api/categories/{id}/products")
    Call<JsonElement> getProductsByCategoryId(@Path("id") String categoryId);

    // --- Orders ---
    @GET("api/orders/myorders")
    Call<List<OrderSummary>> getMyOrders(@Header("Authorization") String token);

    @POST("api/orders")
    Call<Order> createOrder(@Header("Authorization") String token, @Body OrderRequest orderRequest);

    @GET("api/orders/{id}")
    Call<Order> getOrderDetails(@Header("Authorization") String token, @Path("id") String orderId);

    // --- Cart ---

    @GET("api/carts")
    Call<CartResponse> getCart(@Header("Authorization") String token);

    @POST("api/carts")
    Call<CartResponse> addToCart(@Header("Authorization") String token, @Body AddToCartRequest request);

    @PUT("api/carts/{itemId}")
    Call<CartResponse> updateCartItem(
            @Header("Authorization") String token,
            @Path("itemId") String itemId,
            @Body UpdateCartRequest request
    );

    @DELETE("api/carts/{itemId}")
    Call<CartResponse> removeFromCart(
            @Header("Authorization") String token,
            @Path("itemId") String itemId
    );

    @DELETE("api/carts")
    Call<CartResponse> clearCart(@Header("Authorization") String token);

    // --- Discount ---

    @GET("api/discounts/available")
    Call<List<Voucher>> getAvailableDiscounts(
            @Header("Authorization") String token
    );

    @POST("api/discounts/validate")
    Call<ValidateVoucherResponse> validateDiscount(@Header("Authorization") String token, @Body PreviewVoucherRequest request);

    @POST("api/discounts/preview")
    Call<PreviewVoucherResponse> previewDiscount(@Header("Authorization") String token, @Body PreviewVoucherRequest request);


    // --- Notification ---


    @GET("api/notifications")
    Call<ResponseBody> getNotifications(@Header("Authorization") String authHeader);

    @GET("api/notifications/unread-count")
    Call<UnreadCountResponse> getUnreadNotificationCount(@Header("Authorization") String authHeader);

    @PUT("api/notifications/{id}/read")
    Call<Void> markAsRead(@Header("Authorization") String authHeader, @Path("id") String notificationId);

    @PUT("api/notifications")
    Call<Void> markAllAsRead(@Header("Authorization") String authHeader);

    @DELETE("api/notifications/{id}")
    Call<Void> deleteNotification(@Header("Authorization") String authHeader, @Path("id") String notificationId);

    // --- Chatbot (TẠM THỜI DÙNG ResponseBody ĐỂ DEBUG) ---
    @POST("api/chat")
    Call<ResponseBody> sendMessageToChatbot(@Body ChatMessageRequest request);
}
