package com.example.frontend2.ui.fragment;

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
import androidx.fragment.app.FragmentManager;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.ApiResponse;
import com.example.frontend2.data.model.LoyaltyInfo;
import com.example.frontend2.data.model.TierInfo;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentProfileBinding;
import com.example.frontend2.ui.auth.LoginActivity;
import com.example.frontend2.ui.main.EditProfileActivity;
import com.example.frontend2.ui.main.ShippingAddressActivity;
import com.example.frontend2.utils.SharedPrefManager;

import java.io.Serializable;
import java.text.NumberFormat;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private ApiService apiService;
    private User currentUser;
    private static final String TAG = "ProfileFragment";

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
        // Tải lại cả thông tin người dùng và thông tin loyalty
        loadUserProfile();
    }

    private void setupClickListeners() {
        // Listener cho Thông báo
        binding.notificationsOption.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NotificationsFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });

        // Listener cho Sửa thông tin cá nhân
        binding.editProfileOption.setOnClickListener(v -> {
            if (currentUser != null) {
                Intent intent = new Intent(getActivity(), EditProfileActivity.class);
                intent.putExtra("USER_DATA", (Serializable) currentUser);
                startActivity(intent);
            } else {
                Toast.makeText(getContext(), "Không có dữ liệu người dùng", Toast.LENGTH_SHORT).show();
            }
        });

        // Listener cho Địa chỉ nhận hàng
        binding.shippingAddressOption.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShippingAddressActivity.class);
            startActivity(intent);
        });

        // Listener cho Phiếu mua hàng
        binding.vouchersOption.setOnClickListener(v -> {
            if (getActivity() != null) {
                FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.fragment_container, new VouchersFragment()) // Thay thế bằng VouchersFragment
                        .addToBackStack(null) // Thêm vào back stack để có thể quay lại
                        .commit();
            }
        });

        // Listener cho Đăng xuất
        binding.logoutOption.setOnClickListener(v -> {
            if (getContext() != null) {
                SharedPrefManager.getInstance(getContext()).clear();

                Intent intent = new Intent(getActivity(), LoginActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                if (getActivity() != null) {
                    getActivity().finish();
                }
            }
        });
    }

    private void loadUserProfile() {
        if (getContext() == null) return;
        String token = "Bearer " + SharedPrefManager.getInstance(getContext()).getAuthToken();
        apiService.getUserProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    updateUI(currentUser);
                    // Sau khi có thông tin user, tải thông tin loyalty
                    loadLoyaltyInfo();
                } else {
                     // Có thể token hết hạn, xóa và quay về login
                    if(getContext() != null) {
                        SharedPrefManager.getInstance(getContext()).clear();
                        Intent intent = new Intent(getActivity(), LoginActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        if(getActivity() != null) getActivity().finish();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                if(getContext() != null) {
                    Toast.makeText(getContext(), "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void loadLoyaltyInfo() {
        if (getContext() == null) return;
        String token = "Bearer " + SharedPrefManager.getInstance(getContext()).getAuthToken();
        apiService.getLoyaltyInfo(token).enqueue(new Callback<ApiResponse<LoyaltyInfo>>() {
            @Override
            public void onResponse(@NonNull Call<ApiResponse<LoyaltyInfo>> call, @NonNull Response<ApiResponse<LoyaltyInfo>> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    updateLoyaltyUI(response.body().getData());
                } else {
                    Log.e(TAG, "Failed to load loyalty info: " + response.message());
                    if(binding != null) {
                       binding.loyaltyCard.setVisibility(View.GONE);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ApiResponse<LoyaltyInfo>> call, @NonNull Throwable t) {
                Log.e(TAG, "Error loading loyalty info", t);
                 if(binding != null) {
                    binding.loyaltyCard.setVisibility(View.GONE);
                }
            }
        });
    }

    private void updateUI(User user) {
        if (binding == null) return;

        binding.userName.setText(user.getFullName());
        binding.userEmail.setText(user.getEmail());
        if (getActivity() != null) {
            Glide.with(this)
                    .load(user.getAvatar())
                    .placeholder(R.drawable.placeholder)
                    .error(R.drawable.placeholder)
                    .circleCrop()
                    .into(binding.avatarImage);
        }
    }

    private void updateLoyaltyUI(LoyaltyInfo loyaltyInfo) {
        if (binding == null || loyaltyInfo == null) return;

        binding.loyaltyCard.setVisibility(View.VISIBLE);

        TierInfo currentTier = loyaltyInfo.getTierInfo();
        if (currentTier != null) {
            binding.loyaltyTierName.setText(currentTier.getName());
        }

        // Định dạng tiền tệ
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        long totalSpent = loyaltyInfo.getTotalSpent();
        String nextTierName = loyaltyInfo.getNextTier();

        if (nextTierName != null && !nextTierName.isEmpty() && !nextTierName.equalsIgnoreCase(loyaltyInfo.getCurrentTier())) {
            long amountToNextTier = loyaltyInfo.getAmountToNextTier();
            long nextTierMinSpending = totalSpent + amountToNextTier;
            
            int progress = (int) (((double) totalSpent / nextTierMinSpending) * 100);
            binding.loyaltyProgressBar.setProgress(progress);

            binding.loyaltyProgressText.setText("Cần " + currencyFormatter.format(amountToNextTier) + " để đạt hạng " + nextTierName);
        } else {
            // Đã đạt hạng cao nhất
            binding.loyaltyProgressBar.setProgress(100);
            binding.loyaltyProgressText.setText("Bạn đã đạt hạng cao nhất!");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
