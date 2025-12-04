package com.example.frontend2.ui.main;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.model.ShippingAddressResponse;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityShippingAddressBinding;
import com.example.frontend2.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ShippingAddressActivity extends AppCompatActivity {

    private static final String TAG = "ShippingAddressActivity";
    private ActivityShippingAddressBinding binding;
    private ApiService apiService;
    private String authToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityShippingAddressBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        authToken = "Bearer " + SharedPrefManager.getInstance(this).getAuthToken();

        setupToolbar();
        loadShippingAddress();
        setupSaveButton();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void loadShippingAddress() {
        apiService.getShippingAddress(authToken).enqueue(new Callback<ShippingAddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().hasShippingAddress()) {
                    populateUI(response.body().getShippingAddress());
                } else if (response.code() == 404) {
                    Log.d(TAG, "No shipping address found for this user.");
                } else {
                    Log.e(TAG, "Error loading address: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                Log.e(TAG, "API call failed: " + t.getMessage());
                Toast.makeText(ShippingAddressActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void populateUI(ShippingAddress address) {
        binding.etFullName.setText(address.getFullName());
        binding.etPhone.setText(address.getPhone());
        binding.etAddress.setText(address.getAddress());
        binding.etCity.setText(address.getCity());
    }

    private void setupSaveButton() {
        binding.btnSaveChanges.setOnClickListener(v -> saveChanges());
    }

    private void saveChanges() {
        String fullName = binding.etFullName.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String address = binding.etAddress.getText().toString().trim();
        String city = binding.etCity.getText().toString().trim();

        if (fullName.isEmpty() || phone.isEmpty() || address.isEmpty() || city.isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        ShippingAddress shippingAddress = new ShippingAddress();
        shippingAddress.setFullName(fullName);
        shippingAddress.setPhone(phone);
        shippingAddress.setAddress(address);
        shippingAddress.setCity(city);

        apiService.updateShippingAddress(authToken, shippingAddress).enqueue(new Callback<ShippingAddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(ShippingAddressActivity.this, "Cập nhật địa chỉ thành công", Toast.LENGTH_SHORT).show();
                    finish(); 
                } else {
                    Toast.makeText(ShippingAddressActivity.this, "Cập nhật thất bại", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Update failed: " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                Toast.makeText(ShippingAddressActivity.this, "Lỗi kết nối", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Update API call failed: " + t.getMessage());
            }
        });
    }
}
