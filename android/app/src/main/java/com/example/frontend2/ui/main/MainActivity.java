// XÓA HẾT CODE CŨ VÀ DÁN TOÀN BỘ CODE NÀY VÀO MainActivity.java

package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.frontend2.R;
import com.example.frontend2.databinding.ActivityMainBinding;
import com.example.frontend2.ui.fragment.CartFragment;
import com.example.frontend2.ui.fragment.HomeFragment;
import com.example.frontend2.ui.fragment.OrdersFragment;
import com.example.frontend2.ui.fragment.ProfileFragment;
import com.example.frontend2.ui.fragment.SearchFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.nav_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.nav_orders) {
                selectedFragment = new OrdersFragment();
            } else if (itemId == R.id.nav_cart) {
                selectedFragment = new CartFragment();
            } else if (itemId == R.id.nav_profile) {
                selectedFragment = new ProfileFragment();
            }

            if (selectedFragment != null) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, selectedFragment)
                        .commit();
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            if (!handleIntent(getIntent())) {
                binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
            }
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private boolean handleIntent(Intent intent) {
        if (intent == null) {
            return false;
        }

        if ("CART_FRAGMENT".equals(intent.getStringExtra("NAVIGATE_TO"))) {
            Log.d("MainActivity", "Nhận tín hiệu NAVIGATE_TO: CART_FRAGMENT");
            binding.bottomNavigation.setSelectedItemId(R.id.nav_cart);
            intent.removeExtra("NAVIGATE_TO");
            return true;
        }

        if ("OPEN_SEARCH_FRAGMENT".equals(intent.getStringExtra("ACTION"))) {
            Log.d("MainActivity", "Nhận tín hiệu ACTION: OPEN_SEARCH_FRAGMENT");
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new SearchFragment())
                    .addToBackStack(null) // Cho phép quay lại
                    .commit();
            intent.removeExtra("ACTION");
            return true;
        }

        return false; // Không có tín hiệu nào khớp
    }
}
