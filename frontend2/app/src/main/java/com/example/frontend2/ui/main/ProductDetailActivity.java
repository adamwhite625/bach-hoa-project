// File: com/example/frontend2/ui/main/ProductDetailActivity.java
package com.example.frontend2.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductDetailBinding;
import com.example.frontend2.utils.SharedPrefManager; // SỬA: Import lớp quản lý SharedPreferences

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Implement interface để lắng nghe sự kiện từ BottomSheet
public class ProductDetailActivity extends AppCompatActivity implements AddToCartBottomSheetFragment.CartUpdateListener {

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
        fetchProductDataFromIntent();

        // Khi nhấn nút "Thêm vào giỏ hàng", sẽ hiển thị BottomSheet
        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng chờ tải xong dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Cập nhật số lượng trên icon giỏ hàng khi activity được tạo
        // Tạm thời có thể gọi ở đây, nhưng tốt nhất là lấy từ server khi vào app
        updateCartBadge();
    }

    private void fetchProductDataFromIntent() {
        Intent intent = getIntent();
        String productId = intent.getStringExtra("product_id");
        if (productId != null) {
            fetchProductDetails(productId);
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Không nhận được PRODUCT_ID từ Intent");
            finish();
        }
    }

    private void showAddToCartBottomSheet() {
        AddToCartBottomSheetFragment bottomSheet = AddToCartBottomSheetFragment.newInstance(currentProduct);
        bottomSheet.show(getSupportFragmentManager(), "AddToCartBottomSheetFragmentTag");
    }

    // ====================================================================
    // ===          SỬA LẠI HÀM NÀY ĐỂ LẤY VÀ TRUYỀN TOKEN            ===
    // ====================================================================
    /**
     * Hàm này được gọi từ BottomSheet sau khi người dùng xác nhận số lượng.
     * @param quantity Số lượng sản phẩm người dùng đã chọn.
     */
    @Override
    public void onCartUpdated(int quantity) {
        Log.d(TAG, "Nhận được sự kiện onCartUpdated với số lượng: " + quantity);

        // 1. LẤY TOKEN TỪ SHARED PREFERENCES
        String token = SharedPrefManager.getInstance(this).getAuthToken();

        // Kiểm tra token có hợp lệ không
        if (token == null || token.isEmpty()) {
            Toast.makeText(this, "Bạn cần đăng nhập để thực hiện chức năng này", Toast.LENGTH_LONG).show();
            // TODO: Chuyển người dùng đến màn hình Đăng nhập
            // startActivity(new Intent(this, LoginActivity.class));
            return;
        }

        // 2. GỌI CARTMANAGER VÀ TRUYỀN TOKEN VÀO
        // Thêm "Bearer " vào trước token theo chuẩn OAuth2
        CartManager.getInstance().addProductToCart("Bearer " + token, currentProduct, quantity);
        Toast.makeText(this, "Đang thêm " + quantity + " sản phẩm vào giỏ...", Toast.LENGTH_SHORT).show();

        // 3. CẬP NHẬT GIAO DIỆN SAU KHI GỌI API
        // Vì API cần thời gian để phản hồi, chúng ta không thể cập nhật số lượng ngay lập tức.
        // Cách tốt nhất là dùng Callback hoặc LiveData từ CartManager.
        // Cách đơn giản là cập nhật sau một khoảng thời gian ngắn để chờ API.
        new Handler(Looper.getMainLooper()).postDelayed(this::updateCartBadge, 1500); // Cập nhật sau 1.5 giây

        // 4. CHẠY ANIMATION CHO THÊM PHẦN SINH ĐỘNG
        runFlyToCartAnimation(binding.btnAddToCart);
    }

    // ----- CÁC HÀM KHÁC KHÔNG THAY ĐỔI -----

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
                    displayProductData(response.body());
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải dữ liệu", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductDetail> call, @NonNull Throwable t) {
                binding.scroll.setVisibility(View.VISIBLE);
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
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

        List<String> listUrlImageInfo = new ArrayList<>();
        if (productDetail.getDetailImages() != null) {
            for (ImageInfo imageInfo : productDetail.getDetailImages()) {
                listUrlImageInfo.add(imageInfo.getUrl());
            }
        }
        imageUrlSliderAdapter = new ImageUrlSliderAdapter(listUrlImageInfo);
        binding.sliderProduct.setAdapter(imageUrlSliderAdapter);
    }

    private void updateCartBadge() {
        int itemCount = CartManager.getInstance().getCartItemCount();
        Log.d(TAG, "Đang cập nhật badge. Số lượng từ CartManager: " + itemCount);

        if (itemCount > 0) {
            binding.cartBadge.setVisibility(View.VISIBLE);
            binding.cartBadge.setText(String.valueOf(itemCount));
        } else {
            binding.cartBadge.setVisibility(View.GONE);
        }
    }

    private void runFlyToCartAnimation(View viewToAnimate) {
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

        animatorSet.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.removeView(flyingView);
                animateCartIcon();
            }
        });
        animatorSet.start();
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
        // Luôn cập nhật lại icon giỏ hàng khi quay lại màn hình
        updateCartBadge();
    }
}
