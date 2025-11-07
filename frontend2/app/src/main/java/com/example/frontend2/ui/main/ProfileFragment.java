package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.frontend2.R;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.databinding.FragmentProfileBinding;
import com.example.frontend2.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private static final String TAG = "ProfileFragment";
    private FragmentProfileBinding binding;
    private ApiService apiService;
    private User currentUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        setupClickListeners();
        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void setupClickListeners() {
        binding.editProfileOption.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("USER_DATA", currentUser);
                startActivity(intent);
            } else {
                showError("Đang tải dữ liệu, vui lòng thử lại sau");
            }
        });
    }

    private void loadUserData() {
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null || token.isEmpty()) {
            Log.e(TAG, "loadUserData: Auth Token is null or empty. User is not logged in.");
            updateUI(null);
            return;
        }

        String authHeader = "Bearer " + token;
        Log.d(TAG, "loadUserData: Attempting to load user profile with token.");

        apiService.getUserProfile(authHeader).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Log.d(TAG, "onResponse: API call successful. User data received.");
                    currentUser = response.body();
                    updateUI(currentUser);
                } else {
                    Log.e(TAG, "onResponse: API call not successful. Code: " + response.code());
                    updateUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Log.e(TAG, "onFailure: API call failed.", t);
                updateUI(null);
            }
        });
    }

    private void updateUI(User user) {
        if (!isAdded() || binding == null) return;

        if (user != null) {
            binding.userName.setText(user.getFullName());
            if (user.getAvatar() != null && !user.getAvatar().isEmpty()) {
                Glide.with(this)
                        .load(user.getAvatar())
                        .circleCrop()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .placeholder(R.drawable.placeholder)
                        .into(binding.avatarImage);
            } else {
                binding.avatarImage.setImageResource(R.drawable.placeholder);
            }
        } else {
            binding.userName.setText("Người dùng");
            binding.avatarImage.setImageResource(R.drawable.placeholder);
            currentUser = null;
        }
    }

    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
