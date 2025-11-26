package com.example.frontend2.data.remote;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // URL cho backend chính (sản phẩm, user, etc.)
    private static final String MAIN_BASE_URL = "http://192.168.1.7:5000/";
    // URL cho backend chatbot
    private static final String CHATBOT_BASE_URL = "http://10.0.2.2:8001/";

    private static Retrofit mainRetrofit = null;
    private static Retrofit chatbotRetrofit = null;

    // Tạo một OkHttpClient với thời gian chờ tùy chỉnh
    private static final OkHttpClient okHttpClient = new OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS) // Thời gian chờ kết nối
            .readTimeout(60, TimeUnit.SECONDS)    // Thời gian chờ đọc dữ liệu
            .writeTimeout(60, TimeUnit.SECONDS)   // Thời gian chờ ghi dữ liệu
            .build();

    /**
     * Lấy instance Retrofit cho backend CHÍNH.
     */
    public static Retrofit getRetrofitInstance() {
        if (mainRetrofit == null) {
            mainRetrofit = new Retrofit.Builder()
                    .baseUrl(MAIN_BASE_URL)
                    .client(okHttpClient) // Sử dụng OkHttpClient tùy chỉnh
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return mainRetrofit;
    }

    /**
     * Lấy instance Retrofit cho backend CHATBOT.
     */
    public static Retrofit getChatbotRetrofitInstance() {
        if (chatbotRetrofit == null) {
            chatbotRetrofit = new Retrofit.Builder()
                    .baseUrl(CHATBOT_BASE_URL)
                    .client(okHttpClient) // Sử dụng OkHttpClient tùy chỉnh
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return chatbotRetrofit;
    }
}
