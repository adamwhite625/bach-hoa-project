package com.example.frontend2.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.R;
import com.example.frontend2.data.model.MessageResponse;
import com.example.frontend2.data.model.ResetPasswordRequest;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForgotPasswordActivity extends AppCompatActivity {

    private EditText etEmail;
    private ApiService apiService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_password);

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        etEmail = findViewById(R.id.et_email);
        Button btnConfirm = findViewById(R.id.btn_confirm);
        TextView tvBackToLogin = findViewById(R.id.tv_back_to_login);

        btnConfirm.setOnClickListener(v -> sendResetRequest());

        tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void sendResetRequest() {
        String email = etEmail.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập email của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        ResetPasswordRequest request = new ResetPasswordRequest(email);
        // Đổi tên phương thức gọi API cho đúng
        apiService.forgotPassword(request).enqueue(new Callback<MessageResponse>() {
            @Override
            public void onResponse(@NonNull Call<MessageResponse> call, @NonNull Response<MessageResponse> response) {
                // Backend luôn trả về 200 OK để bảo mật, nên ta chỉ cần hiển thị thông báo và chuyển màn hình
                Toast.makeText(ForgotPasswordActivity.this, "Yêu cầu đã được gửi đi.", Toast.LENGTH_LONG).show();

                // Chuyển sang màn hình ResetPasswordActivity
                Intent intent = new Intent(ForgotPasswordActivity.this, ResetPasswordActivity.class);
                intent.putExtra("USER_EMAIL", email); // Truyền email qua màn hình tiếp theo
                startActivity(intent);
            }

            @Override
            public void onFailure(@NonNull Call<MessageResponse> call, @NonNull Throwable t) {
                Toast.makeText(ForgotPasswordActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
