package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.frontend2.R;
import com.example.frontend2.ui.fragment.CartFragment;
import com.example.frontend2.ui.fragment.HomeFragment;
import com.example.frontend2.ui.fragment.OrdersFragment;
import com.example.frontend2.ui.fragment.ProfileFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnItemSelectedListener(navListener);

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();

        if (savedInstanceState == null) { // Chỉ chạy khi Activity được tạo lần đầu, không phải khi xoay màn hình
            processIntentNavigation(getIntent(), bottomNav);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        // Khi MainActivity đã chạy và nhận được một Intent mới (từ ProductDetailActivity)
        // hàm này sẽ được gọi.
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        processIntentNavigation(intent, bottomNav);
    }


    private final BottomNavigationView.OnItemSelectedListener navListener =
            item -> {
                Fragment selectedFragment = null;
                int id = item.getItemId();

                if (id == R.id.nav_home) {
                    selectedFragment = new HomeFragment();
                } else if (id == R.id.nav_orders) {
                    selectedFragment = new OrdersFragment();
                } else if (id == R.id.nav_cart) {
                    selectedFragment = new CartFragment();
                } else if (id == R.id.nav_profile) {
                    selectedFragment = new ProfileFragment();
                }

                if (selectedFragment != null) {
                    getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, selectedFragment)
                            .commit();
                    return true;
                }

                return false;
            };
    private void processIntentNavigation(Intent intent, BottomNavigationView bottomNav) {
        // Kiểm tra xem có tín hiệu điều hướng đến giỏ hàng không
        if (intent != null && intent.getBooleanExtra("NAVIGATE_TO_CART", false)) {
            Log.d("MainActivity", "Nhận được yêu cầu điều hướng đến CartFragment.");

            // A. Chuyển sang CartFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new CartFragment())
                    .commit();

            // B. Cập nhật trạng thái "selected" trên BottomNavigationView
            //    Hãy chắc chắn ID của item giỏ hàng trong menu của bạn là 'nav_cart'
            bottomNav.setSelectedItemId(R.id.nav_cart);

            // C. Xóa tín hiệu đi để tránh các hành vi không mong muốn
            intent.removeExtra("NAVIGATE_TO_CART");
        } else {
            // Nếu không có tín hiệu đặc biệt, mặc định mở HomeFragment
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new HomeFragment())
                    .commit();
        }
    }
}