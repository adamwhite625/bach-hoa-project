package com.example.frontend2.ui.main;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.bumptech.glide.Glide;
import com.example.frontend2.R;
import com.example.frontend2.data.model.Review;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.data.remote.RetrofitClient;
import com.example.frontend2.utils.SharedPrefManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReviewActivity extends AppCompatActivity {

    private ImageView ivProductImage;
    private TextView tvProductName;
    private RatingBar ratingBar;
    private EditText etComment;
    private Button btnSubmitReview;
    private Toolbar toolbar;

    private String productId;
    private ApiService apiServices;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_review);

        toolbar = findViewById(R.id.toolbar_review);
        ivProductImage = findViewById(R.id.iv_product_image);
        tvProductName = findViewById(R.id.tv_product_name);
        ratingBar = findViewById(R.id.rating_bar);
        etComment = findViewById(R.id.et_comment);
        btnSubmitReview = findViewById(R.id.btn_submit_review);

        apiServices = RetrofitClient.getClient().create(ApiService.class);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            productId = extras.getString("PRODUCT_ID");
            String productName = extras.getString("PRODUCT_NAME");
            String productImage = extras.getString("PRODUCT_IMAGE");

            tvProductName.setText(productName);
            Glide.with(this)
                    .load(productImage)
                    .placeholder(R.drawable.placeholder)
                    .into(ivProductImage);
        }

        btnSubmitReview.setOnClickListener(v -> submitReview());
    }

    private void submitReview() {
        String token = SharedPrefManager.getInstance(this).getAuthToken();

        if (token == null) {
            Toast.makeText(this, "Phiên đăng nhập đã hết hạn. Vui lòng đăng nhập lại.", Toast.LENGTH_LONG).show();
            return;
        }

        float ratingValue = ratingBar.getRating();
        String comment = etComment.getText().toString().trim();

        if (ratingValue == 0) {
            Toast.makeText(this, "Vui lòng chọn số sao đánh giá", Toast.LENGTH_SHORT).show();
            return;
        }

        if (comment.isEmpty()) {
            Toast.makeText(this, "Vui lòng chia sẻ cảm nhận của bạn", Toast.LENGTH_SHORT).show();
            return;
        }

        btnSubmitReview.setEnabled(false);

        Review reviewData = new Review((int) ratingValue, comment);

        Call<Review> call = apiServices.createReview("Bearer " + token, productId, reviewData);
        call.enqueue(new Callback<Review>() {
            @Override
            public void onResponse(Call<Review> call, Response<Review> response) {
                btnSubmitReview.setEnabled(true);

                if (response.isSuccessful()) {
                    Toast.makeText(ReviewActivity.this, "Gửi đánh giá thành công!", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    try {
                        String errorBody = response.errorBody().string();
                        Toast.makeText(ReviewActivity.this, "Lỗi: " + errorBody, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        Toast.makeText(ReviewActivity.this, "Gửi đánh giá thất bại. Mã lỗi: " + response.code(), Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<Review> call, Throwable t) {
                btnSubmitReview.setEnabled(true);
                Log.e("ReviewActivity", "API Call Failed: " + t.getMessage());
                Toast.makeText(ReviewActivity.this, "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
