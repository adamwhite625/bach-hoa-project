package com.example.frontend2.ui.main;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.frontend2.R;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.PaymentStatusResponse;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.utils.SharedPrefManager;

import java.io.IOException;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PaymentResultActivity extends AppCompatActivity {

    private static final String TAG = "PaymentResult";

    private ImageView ivStatusIcon;
    private TextView tvStatus;
    private TextView tvMessage;
    private TextView tvOrderId;
    private Button btnViewOrder;
    private Button btnBackHome;
    private ProgressBar progressBar;

    private ApiService apiService;
    private String orderId;
    private boolean isSuccess;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_result);

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        initViews();
        setupBackPressHandler();
        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        handleIntent(intent);
    }

    private void initViews() {
        ivStatusIcon = findViewById(R.id.ivStatusIcon);
        tvStatus = findViewById(R.id.tvStatus);
        tvMessage = findViewById(R.id.tvMessage);
        tvOrderId = findViewById(R.id.tvOrderId);
        btnViewOrder = findViewById(R.id.btnViewOrder);
        btnBackHome = findViewById(R.id.btnBackHome);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupBackPressHandler() {
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (progressBar.getVisibility() != View.VISIBLE) {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void handleIntent(Intent intent) {
        Uri data = intent.getData();
        if (data != null) {
            handleDeepLink(data);
        } else if (intent.hasExtra("ORDER_ID")) {
            String finalOrderId = intent.getStringExtra("ORDER_ID");
            navigateToOrderSuccess(finalOrderId);
        } else {
            showErrorUI("Không nhận được thông tin đơn hàng.");
        }
    }

    private void handleDeepLink(Uri data) {
        progressBar.setVisibility(View.VISIBLE);
        tvStatus.setText("Đang xử lý thanh toán...");

        SharedPreferences prefs = getSharedPreferences("payment", MODE_PRIVATE);
        String savedOrderId = prefs.getString("current_order_id", null);

        if (savedOrderId != null) {
            verifyPaymentByOrderId(savedOrderId);
        } else {
            String appTransId = data.getQueryParameter("apptransid");
            if (appTransId != null) {
                verifyPaymentByAppTransId(appTransId);
            } else {
                showErrorUI("Không nhận được thông tin thanh toán");
            }
        }
    }

    private void verifyPaymentByOrderId(String orderIdToVerify) {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            showErrorUI("Vui lòng đăng nhập lại");
            return;
        }

        apiService.queryZaloPayStatusByOrderId("Bearer " + token, orderIdToVerify)
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PaymentStatusResponse> call, @NonNull Response<PaymentStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentStatusResponse result = response.body();
                            isSuccess = result.isSuccess();
                            orderId = result.getOrderId();
                            clearPendingPayment();
                            if (isSuccess) {
                                clearCartAfterSuccessfulPayment();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                showFailureUI();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showErrorUI("Không thể xác thực thanh toán");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaymentStatusResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showErrorUI("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void verifyPaymentByAppTransId(String appTransId) {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            showErrorUI("Vui lòng đăng nhập lại");
            return;
        }

        apiService.queryZaloPayStatus("Bearer " + token, appTransId)
                .enqueue(new Callback<PaymentStatusResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PaymentStatusResponse> call, @NonNull Response<PaymentStatusResponse> response) {
                        if (response.isSuccessful() && response.body() != null) {
                            PaymentStatusResponse result = response.body();
                            isSuccess = result.isSuccess();
                            orderId = result.getOrderId();
                            clearPendingPayment();
                            if (isSuccess) {
                                clearCartAfterSuccessfulPayment();
                            } else {
                                progressBar.setVisibility(View.GONE);
                                showFailureUI();
                            }
                        } else {
                            progressBar.setVisibility(View.GONE);
                            showErrorUI("Không thể xác thực thanh toán");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaymentStatusResponse> call, @NonNull Throwable t) {
                        progressBar.setVisibility(View.GONE);
                        showErrorUI("Lỗi kết nối: " + t.getMessage());
                    }
                });
    }

    private void clearCartAfterSuccessfulPayment() {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            progressBar.setVisibility(View.GONE);
            navigateToOrderSuccess(orderId);
            return;
        }

        apiService.clearCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                progressBar.setVisibility(View.GONE);
                navigateToOrderSuccess(orderId);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                navigateToOrderSuccess(orderId);
            }
        });
    }

    private void navigateToOrderSuccess(String finalOrderId) {
        Intent intent = new Intent(this, OrderSuccessActivity.class);
        intent.putExtra("ORDER_ID", finalOrderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showFailureUI() {
        ivStatusIcon.setImageResource(R.drawable.ic_failed);
        tvStatus.setText("Thanh toán thất bại");
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
        tvMessage.setText("Vui lòng thử lại hoặc chọn phương thức thanh toán khác");
        btnViewOrder.setVisibility(View.GONE);
        btnBackHome.setVisibility(View.VISIBLE);
        btnBackHome.setOnClickListener(v -> finish());
    }

    private void showErrorUI(String errorMessage) {
        ivStatusIcon.setImageResource(R.drawable.ic_error);
        tvStatus.setText("Lỗi");
        tvStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_dark));
        tvMessage.setText(errorMessage);
        btnBackHome.setVisibility(View.VISIBLE);
        btnBackHome.setOnClickListener(v -> finish());
    }

    private void clearPendingPayment() {
        SharedPreferences prefs = getSharedPreferences("payment", MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
