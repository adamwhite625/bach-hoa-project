package com.example.frontend2.ui.auth;

import android.content.Intent;
import android.os.Build; // << Import Build
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen; // << Import SplashScreen

import com.example.frontend2.R; // << Import R
import com.example.frontend2.ui.main.MainActivity;
import com.example.frontend2.utils.SharedPrefManager;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // === SỬA ĐỔI QUAN TRỌNG NHẤT ===
        // Luôn gọi installSplashScreen() TRƯỚC super.onCreate()
        SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        // =================================================================
        // === VAI TRÒ CỦA activity_splash.xml LÀ ĐỂ HỖ TRỢ ANDROID < 12 ===
        // Chúng ta không cần gọi setContentView cho Android 12+ nữa
        // vì theme đã xử lý giao diện.
        // =================================================================
        setContentView(R.layout.activity_splash);


        // Logic chuyển màn hình vẫn giữ nguyên
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            String token = SharedPrefManager.getInstance(getApplicationContext()).getAuthToken();
            Intent intent;
            if (token != null && !token.isEmpty()) {
                intent = new Intent(SplashActivity.this, MainActivity.class);
            } else {
                intent = new Intent(SplashActivity.this, LoginActivity.class);
            }
            startActivity(intent);
            finish();
        }, 1500);
    }
}
