package com.example.frontend2.ui.main;

import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.User;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityEditProfileBinding;
import com.example.frontend2.utils.SharedPrefManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import okhttp3.MediaType;
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

    // Trình khởi chạy để chọn ảnh từ thư viện
    private ActivityResultLauncher<PickVisualMediaRequest> pickMediaLauncher;
    // Uri của ảnh đã chọn
    private Uri selectedImageUri = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Khởi tạo trình chọn ảnh
        setupImagePicker();

        setupBackButton();
        setupSaveButton();
        setupAvatarClickListener();

        currentUser = (User) getIntent().getSerializableExtra("USER_DATA");

        if (currentUser != null) {
            populateUI(currentUser);
        } else {
            loadUserDataFromApi();
        }
    }

    private void setupImagePicker() {
        // Đăng ký callback để xử lý kết quả sau khi người dùng chọn ảnh
        pickMediaLauncher = registerForActivityResult(new ActivityResultContracts.PickVisualMedia(), uri -> {
            if (uri != null) {
                Log.d("PhotoPicker", "Selected URI: " + uri);
                selectedImageUri = uri; // Lưu lại Uri của ảnh đã chọn
                // Hiển thị ảnh đã chọn lên avatar để xem trước
                Glide.with(this)
                        .load(uri)
                        .placeholder(R.drawable.placeholder)
                        .circleCrop()
                        .into(binding.avatarImage);
            } else {
                Log.d("PhotoPicker", "No media selected");
            }
        });
    }

    private void setupAvatarClickListener() {
        binding.avatarImage.setOnClickListener(v -> {
            // Khởi chạy trình chọn ảnh, chỉ cho phép chọn ảnh
            pickMediaLauncher.launch(new PickVisualMediaRequest.Builder()
                    .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                    .build());
        });
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

        // Tạo các part cho dữ liệu văn bản
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

        // Tạo part cho file ảnh nếu người dùng đã chọn ảnh mới
        MultipartBody.Part avatarPart = null;
        if (selectedImageUri != null) {
            try {
                InputStream inputStream = getContentResolver().openInputStream(selectedImageUri);
                // Đọc dữ liệu từ InputStream vào một mảng byte
                ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    byteStream.write(buffer, 0, bytesRead);
                }
                byte[] fileBytes = byteStream.toByteArray();
                inputStream.close();

                // Tạo RequestBody từ mảng byte với ĐÚNG THỨ TỰ THAM SỐ
                RequestBody requestFile = RequestBody.create(MediaType.parse(getContentResolver().getType(selectedImageUri)), fileBytes);
                // Tạo MultipartBody.Part từ RequestBody
                avatarPart = MultipartBody.Part.createFormData("avatar", "avatar.jpg", requestFile);
            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(this, "Không thể xử lý ảnh đã chọn", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Thực hiện gọi API, đính kèm file ảnh (nếu có)
        apiService.updateUserProfile(authToken, map, avatarPart).enqueue(new Callback<User>() {
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
