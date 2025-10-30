package com.example.frontend2.ui.main;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.frontend2.R;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.data.remote.RetrofitClient;
import com.example.frontend2.data.model.User;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private View topSection;
    private CardView profileCard;
    private ImageView avatarImage;
    private TextView userNameText;
    private EditText searchBar;

    private static final String PREFS_NAME = "MyPrefs";
    private static final String KEY_TOKEN = "token";
    private ApiService apiService;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = RetrofitClient.getClient().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = null;
        try {
            root = inflater.inflate(R.layout.fragment_profile, container, false);
            if (root == null) return null;

            initializeViews(root);
            setupSearchBar();
            root.post(() -> applyDynamicHeights());

            // Load user data from API
            loadUserData();

        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    private void initializeViews(View root) {
        try {
            topSection = root.findViewById(R.id.topSection);
            profileCard = root.findViewById(R.id.profileCard);
            avatarImage = root.findViewById(R.id.avatarImage);
            userNameText = root.findViewById(R.id.userName);
            searchBar = root.findViewById(R.id.searchBar);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupSearchBar() {
        if (searchBar != null) {
            try {
                searchBar.setFocusable(false);
                searchBar.setClickable(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void loadUserData() {
        String token = getStoredToken();
        if (token == null || token.isEmpty()) {
            showError("Vui lòng đăng nhập để xem thông tin");
            Log.d(TAG, "Token not found in SharedPreferences");
            return;
        }

        Log.d(TAG, "Calling API with token: " + token.substring(0, Math.min(token.length(), 10)) + "...");
        String authHeader = "Bearer " + token;

        apiService.getCurrentUser(authHeader).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    Log.d(TAG, "API call successful. User: " + user.getEmail() +
                            ", Name: " + user.getFullName() +
                            ", Avatar: " + (user.getAvatar() != null ? user.getAvatar() : "null"));
                    updateUI(user);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ?
                                response.errorBody().string() : "Unknown error";
                        Log.e(TAG, "API error: " + response.code() + " - " + errorBody);
                        showError("Không thể tải thông tin người dùng (" + response.code() + ")");
                    } catch (Exception e) {
                        Log.e(TAG, "Error reading error body", e);
                    }
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                Log.e(TAG, "API call failed", t);
                showError("Lỗi kết nối: " + t.getMessage());
            }
        });
    }

    private void updateUI(User user) {
        if (!isAdded()) return;

        if (userNameText != null) {
            userNameText.setText(user.getFullName());
        }

        if (avatarImage != null && user.getAvatar() != null) {
            Log.d(TAG, "Loading avatar from URL: " + user.getAvatar());
            Glide.with(this)
                    .load(user.getAvatar())
                    .circleCrop()
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.ic_search)
                    .into(avatarImage);
        } else {
            Log.d(TAG, "No avatar URL available");
        }
    }

    private String getStoredToken() {
        if (getContext() == null) return null;
        SharedPreferences prefs = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String token = prefs.getString(KEY_TOKEN, null);
        Log.d(TAG, "Retrieved token from SharedPreferences: " + (token != null ? "exists" : "null"));
        return token;
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void applyDynamicHeights() {
        if (!isAdded() || getActivity() == null) return;

        try {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            int screenH = dm.heightPixels;

            if (topSection != null) {
                int topH = Math.max((int) (screenH * 0.15f), dpToPx(80));
                ViewGroup.LayoutParams topLp = topSection.getLayoutParams();
                if (topLp != null) {
                    topLp.height = topH;
                    topSection.setLayoutParams(topLp);
                }
            }

            if (profileCard != null) {
                int cardH = Math.max((int) (screenH * 0.20f), dpToPx(120));
                ViewGroup.LayoutParams cardLp = profileCard.getLayoutParams();
                if (cardLp != null) {
                    cardLp.height = cardH;
                    profileCard.setLayoutParams(cardLp);
                }
            }

            if (avatarImage != null) {
                avatarImage.setTranslationY(-dpToPx(36));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private int dpToPx(int dp) {
        if (!isAdded()) return dp;
        try {
            float density = getResources().getDisplayMetrics().density;
            return Math.round(dp * density);
        } catch (Exception e) {
            return dp;
        }
    }

}