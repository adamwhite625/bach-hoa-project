package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentProfileBinding;
import com.example.frontend2.ui.auth.LoginActivity;
import com.example.frontend2.utils.SharedPrefManager;

import java.io.Serializable;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ApiService apiService;
    private User currentUser;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        setupClickListeners();
        loadUserProfile();

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserProfile();
    }

    private void setupClickListeners() {
        // Sử dụng đúng ID từ file XML: editProfileOption
        binding.editProfileOption.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("USER_DATA", (Serializable) currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        // Sử dụng đúng ID từ file XML: logoutOption
        binding.logoutOption.setOnClickListener(v -> {
            SharedPrefManager.getInstance(getContext()).clear();

            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) {
                getActivity().finish();
            }
        });
    }

    private void loadUserProfile() {
        String token = "Bearer " + SharedPrefManager.getInstance(getContext()).getAuthToken();
        apiService.getUserProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    updateUI(currentUser);
                } else {
                     // Có thể token hết hạn, xóa và quay về login
                    SharedPrefManager.getInstance(getContext()).clear();
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    if(getActivity() != null) getActivity().finish();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateUI(User user) {
        binding.userName.setText(user.getFullName());
        Glide.with(this)
                .load(user.getAvatar())
                .placeholder(R.drawable.placeholder)
                .error(R.drawable.placeholder)
                .circleCrop()
                .into(binding.avatarImage);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
