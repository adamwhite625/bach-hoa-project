// File: com/example/frontend2/ui/main/ProductDetailActivity.java

package com.example.frontend2.ui.main;

// === CÁC IMPORT ĐÃ ĐƯỢC KHÔI PHỤC VÀ SẮP XẾP LẠI ===
import android.animation.Animator;
import androidx.lifecycle.Observer;
import android.animation.AnimatorListenerAdapter; // <-- ĐÃ KHÔI PHỤC
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;

import com.example.frontend2.R;
import com.example.frontend2.data.model.ImageInfo;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.ui.main.CartManager;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductDetailBinding;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private ActivityProductDetailBinding binding;
    private ApiService apiService;
    private ImageUrlSliderAdapter imageUrlSliderAdapter;

    private ProductDetail currentProduct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        setupBackButton();

        Intent intent = getIntent();
        String productId = intent.getStringExtra("product_id");
        if (productId != null) {
            fetchProductDetails(productId);
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Không nhận được PRODUCT_ID từ Intent");
            finish();
        }

        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                // 1. Thêm sản phẩm vào CartManager
                CartManager.getInstance().addProductToCart(currentProduct);
                Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();

                // 2. Cập nhật giao diện badge NGAY LẬP TỨC
                updateCartBadge();

                // 3. Chạy animation
                runFlyToCartAnimation(v);
            }
        });

        // === THAY ĐỔI CÁCH CẬP NHẬT GIAO DIỆN ===
        // Bắt đầu "quan sát" LiveData từ CartManager
        updateCartBadge();
    }

    private void setupBackButton() {
        binding.btnBackSmall.setOnClickListener(v -> finish());
    }

    private void fetchProductDetails(String productId) {
        binding.scroll.setVisibility(View.GONE);
        apiService.getProductById(productId).enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(@NonNull Call<ProductDetail> call, @NonNull Response<ProductDetail> response) {
                binding.scroll.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetail productDetail = response.body();
                    displayProductData(productDetail);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi API: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDetail> call, @NonNull Throwable t) {
                binding.scroll.setVisibility(View.VISIBLE);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi Failure: ", t);
            }
        });
    }

    private void displayProductData(ProductDetail productDetail) {
        this.currentProduct = productDetail;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.tvProductName.setText(productDetail.getName());
        binding.tvProductTitle.setText(productDetail.getName());
        binding.tvPrice.setText(currencyFormat.format(productDetail.getPrice()));
        binding.tvDescription.setText(Html.fromHtml(productDetail.getDescription(), Html.FROM_HTML_MODE_LEGACY));

        List<ImageInfo> listImageInfo = productDetail.getDetailImages();
        List<String> listUrlImageInfo = new ArrayList<>();
        for (ImageInfo imageInfo : listImageInfo) {
            listUrlImageInfo.add(imageInfo.getUrl());
        }
        imageUrlSliderAdapter = new ImageUrlSliderAdapter(listUrlImageInfo);
        binding.sliderProduct.setAdapter(imageUrlSliderAdapter);
    }

    private void runFlyToCartAnimation(View viewToAnimate) {
        // === ĐỔI LẠI TÊN HÀM CHO ĐÚNG ===
        // ... code tạo animation của bạn không thay đổi ...
        CoordinatorLayout rootLayout = binding.getRoot();
        ImageView flyingView = new ImageView(this);
        flyingView.setImageResource(R.drawable.circle_red_shape);
        int size = (int) getResources().getDimension(R.dimen.fly_to_cart_size);
        flyingView.setLayoutParams(new CoordinatorLayout.LayoutParams(size, size));
        rootLayout.addView(flyingView);

        int[] startPos = new int[2];
        viewToAnimate.getLocationInWindow(startPos);
        flyingView.setX(startPos[0] + (viewToAnimate.getWidth() / 2f) - (size / 2f));
        flyingView.setY(startPos[1] + (viewToAnimate.getHeight() / 2f) - (size / 2f));

        int[] endPos = new int[2];
        binding.cartIcon.getLocationInWindow(endPos);
        float endX = endPos[0] + (binding.cartIcon.getWidth() / 2f) - (size / 2f);
        float endY = endPos[1] + (binding.cartIcon.getHeight() / 2f) - (size / 2f);

        ObjectAnimator animX = ObjectAnimator.ofFloat(flyingView, "x", flyingView.getX(), endX);
        ObjectAnimator animY = ObjectAnimator.ofFloat(flyingView, "y", flyingView.getY(), endY);
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(flyingView, "scaleX", 1.0f, 0.1f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(flyingView, "scaleY", 1.0f, 0.1f);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(animX, animY, scaleX, scaleY);
        animatorSet.setDuration(800);
        animatorSet.setInterpolator(new AccelerateInterpolator());

        // === DỌN DẸP LẠI LISTENER ===
        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                // Nhiệm vụ của listener bây giờ rất đơn giản:
                // 1. Dọn dẹp "chấm đỏ"
                rootLayout.removeView(flyingView);
                // 2. Tạo hiệu ứng cho icon giỏ hàng
                animateCartIcon();
                // 3. KHÔNG CẦN CẬP NHẬT BADGE Ở ĐÂY NỮA! LiveData đã lo việc đó.
            }
        });

        animatorSet.start();
    }

    private void updateCartBadge() {
        int itemCount = CartManager.getInstance().getCartItemCount();
        Log.d("CartDebug", "Đang cập nhật badge. Số lượng: " + itemCount);

        if (itemCount > 0) {
            binding.cartBadge.setVisibility(View.VISIBLE);
            binding.cartBadge.setText(String.valueOf(itemCount));
        } else {
            binding.cartBadge.setVisibility(View.GONE);
        }
    }

    private void animateCartIcon() {
        binding.cartIcon.animate()
                .scaleX(1.3f).scaleY(1.3f)
                .setDuration(150)
                .withEndAction(() -> {
                    binding.cartIcon.animate()
                            .scaleX(1.0f).scaleY(1.0f)
                            .setDuration(150)
                            .start();
                }).start();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCartBadge();
    }
}

