package com.example.frontend2.data.remote;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {
    // URL mới của backend
//    private static final String BASE_URL = "http://10.0.2.2:5000/";
    static final String BASE_URL = "http://192.168.1.9:5000/";
    //private static final String BASE_URL = "https://philips-solving-dsc-billing.trycloudflare.com";


    // Đối tượng Retrofit duy nhất (Singleton)
    private static Retrofit retrofit = null;

    /**
     * Phương thức này tạo và trả về một instance duy nhất của Retrofit.
     * Nếu instance đã được tạo, nó sẽ trả về instance cũ.
     *
     * @return một instance của Retrofit.
     */
    public static Retrofit getRetrofitInstance() {
        if (retrofit == null) {
            // Nếu chưa có instance nào, tạo một instance mới
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL) // Thiết lập URL gốc cho mọi request
                    .addConverterFactory(GsonConverterFactory.create()) // Thêm bộ chuyển đổi JSON (Gson)
                    .build();
        }
        return retrofit;
    }
}
