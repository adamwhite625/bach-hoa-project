package com.example.frontend2.ui.main;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityEditProfileBinding;
import com.example.frontend2.utils.SharedPrefManager;

import java.util.HashMap;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private ApiService apiService;
    private String authToken;
    private User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        setupBackButton();
        setupSaveButton();

        currentUser = (User) getIntent().getSerializableExtra("USER_DATA");

        if (currentUser != null) {
            populateUI(currentUser);
        } else {
            loadUserDataFromApi();
        }
    }

    private void setupBackButton() {
        binding.backButton.setOnClickListener(v -> finish());
    }

    private void loadUserDataFromApi() {
        authToken = "Bearer " + SharedPrefManager.getInstance(this).getAuthToken();

        if (authToken == null) {
            Toast.makeText(this, "Lỗi xác thực, vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        apiService.getUserProfile(authToken).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    currentUser = response.body();
                    populateUI(currentUser);
                } else {
                    Toast.makeText(EditProfileActivity.this, "Không thể tải thông tin", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(User user) {
        binding.userName.setText(user.getFullName());
        binding.etFirstName.setText(user.getFirstName());
        binding.etLastName.setText(user.getLastName());
        binding.etEmail.setText(user.getEmail());
        binding.etPhone.setText(user.getPhone());

        if (user.getGender() != null) {
            switch (user.getGender()) {
                case "Male":
                    binding.rbMale.setChecked(true);
                    break;
                case "Female":
                    binding.rbFemale.setChecked(true);
                    break;
                case "Other":
                    binding.rbOther.setChecked(true);
                    break;
            }
        }

        Glide.with(this)
                .load(user.getAvatar())
                .placeholder(R.drawable.placeholder)
                .circleCrop()
                .into(binding.avatarImage);
    }

    private void setupSaveButton() {
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private RequestBody createPartFromString(String descriptionString) {
        if (descriptionString == null) {
            return RequestBody.create(MultipartBody.FORM, "");
        }
        return RequestBody.create(MultipartBody.FORM, descriptionString);
    }

    private void saveChanges() {
        authToken = "Bearer " + SharedPrefManager.getInstance(this).getAuthToken();
        if (authToken == null) return;

        Map<String, RequestBody> map = new HashMap<>();
        map.put("firstName", createPartFromString(binding.etFirstName.getText().toString()));
        map.put("lastName", createPartFromString(binding.etLastName.getText().toString()));
        map.put("email", createPartFromString(binding.etEmail.getText().toString()));
        map.put("phone", createPartFromString(binding.etPhone.getText().toString()));

        int selectedGenderId = binding.rgGender.getCheckedRadioButtonId();
        String gender = "Other";
        if (selectedGenderId == R.id.rbMale) gender = "Male";
        else if (selectedGenderId == R.id.rbFemale) gender = "Female";
        map.put("gender", createPartFromString(gender));

        apiService.updateUserProfile(authToken, map, null).enqueue(new Callback<User>() {
            @Override
            public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(EditProfileActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
