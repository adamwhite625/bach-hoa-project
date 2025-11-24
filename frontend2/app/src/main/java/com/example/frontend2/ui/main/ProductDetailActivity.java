// File: com/example/frontend2/ui/main/ProductDetailActivity.java
package com.example.frontend2.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.Toast;
import android.graphics.Paint;

import androidx.annotation.NonNull;
// SỬA: XÓA import @Nullable vì không cần onActivityResult nữa
// import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.ViewModelProvider; // SỬA 1: Import ViewModelProvider

import com.example.frontend2.R;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.ImageInfo;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.model.SaleInfo;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductDetailBinding;
import com.example.frontend2.ui.adapter.ImageUrlSliderAdapter;
import com.example.frontend2.ui.fragment.AddToCartBottomSheetFragment;
import com.example.frontend2.ui.fragment.SearchFragment;
import com.example.frontend2.utils.SharedPrefManager;

import java.text.NumberFormat;
import java.time.ZonedDateTime;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

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
    // SỬA 3: Khai báo SharedViewModel
    private CartSharedViewModel sharedViewModel;

    private CountDownTimer saleCountDownTimer;

    // SỬA 4: XÓA BỎ MÃ REQUEST_CODE_CART
    // private static final int REQUEST_CODE_CART = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // SỬA 5: Khởi tạo ViewModel
        sharedViewModel = new ViewModelProvider(this).get(CartSharedViewModel.class);
        // Lắng nghe sự thay đổi của ViewModel ngay lập tức
        setupViewModelObserver();

        setupBackButton();
        setupCartButton(); // Hàm này giờ chỉ để điều hướng, không dùng forResult
        fetchProductDataFromIntent();

        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng chờ tải xong dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        // Lấy dữ liệu giỏ hàng lần đầu khi mở Activity
        loadInitialCartData();

        binding.searchBarLayout.setOnClickListener(v -> {
            Log.d(TAG, "Thanh tìm kiếm được nhấn! Yêu cầu MainActivity mở SearchFragment.");

            // 1. Tạo một Intent để quay về MainActivity
            Intent intent = new Intent(this, MainActivity.class);

            // 2. Đính kèm một "extra" để làm tín hiệu.
            // "OPEN_SEARCH_FRAGMENT" là một khóa (key) do chúng ta tự định nghĩa.
            intent.putExtra("ACTION", "OPEN_SEARCH_FRAGMENT");

            // 3. (QUAN TRỌNG) Thêm các cờ này để xóa các Activity trung gian (như ProductDetailActivity)
            // và đưa MainActivity đã tồn tại ra phía trước, thay vì tạo mới.
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

            // 4. Bắt đầu Intent
            startActivity(intent);
        });
    }

    // SỬA 6: XÓA BỎ TOÀN BỘ HÀM onActivityResult
    // @Override
    // protected void onActivityResult(...) { ... }

    // SỬA 7: Hàm lắng nghe sự thay đổi từ ViewModel
    private void setupViewModelObserver() {
        sharedViewModel.getCartItemCount().observe(this, count -> {
            Log.d(TAG, "ViewModel thông báo số lượng giỏ hàng mới: " + count);
            if (count != null) {
                // Cập nhật giao diện badge với con số mới
                updateCartBadgeWithCount(count);
            }
        });
    }

    // SỬA 8: Thêm hàm load dữ liệu ban đầu
    private void loadInitialCartData() {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            sharedViewModel.setCartItemCount(0);
            return;
        }

        CartManager.getInstance().fetchCartFromServer("Bearer " + token, new CartManager.FetchCartCallback() {
            @Override
            public void onSuccess() {
                int count = CartManager.getInstance().getCartItemCount();
                sharedViewModel.setCartItemCount(count);
            }

            @Override
            public void onFailure(String error) {
                sharedViewModel.setCartItemCount(0); // Nếu lỗi thì trả về 0
            }
        });
    }


    @Override
    public void onCartUpdated(int quantity) {
        // ... (code gọi API thêm vào giỏ hàng của bạn)
        // ...
        // Trong onResponse của API addProductToCart
        CartManager.getInstance().addProductToCart("Bearer " + SharedPrefManager.getInstance(this).getAuthToken(), currentProduct, quantity, new CartManager.CartUpdateCallback() {
            @Override
            public void onSuccess(CartResponse updatedCart) {
                Log.d(TAG, "Thêm vào giỏ hàng thành công từ callback.");
                binding.btnAddToCart.setEnabled(true);
                // SỬA 9: Cập nhật ViewModel thay vì gọi thẳng hàm update UI
                int totalCount = CartManager.getInstance().getCartItemCount();
                sharedViewModel.setCartItemCount(totalCount);

                runFlyToCartAnimation(binding.btnAddToCart);
                Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onFailure(String error) {
                Log.e(TAG, "Lỗi khi thêm vào giỏ: " + error);
                binding.btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Thêm vào giỏ hàng thất bại", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupBackButton() {
        binding.btnBackSmall.setOnClickListener(v -> finish());
    }

    // SỬA 10: Hàm này giờ chỉ điều hướng đến MainActivity, nơi chứa CartFragment
    private void setupCartButton() {
        binding.cartIcon.setOnClickListener(v -> {
            // Mở MainActivity và có thể truyền một tín hiệu để nó điều hướng đến CartFragment
            Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
            intent.putExtra("NAVIGATE_TO_CART", true); // Gửi tín hiệu
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
        });
    }

    // ----- CÁC HÀM CÒN LẠI GIỮ NGUYÊN HOẶC THAY ĐỔI NHẸ -----

    // Hàm cập nhật badge dựa trên số lượng từ ViewModel
    private void updateCartBadgeWithCount(int count) {
        Log.d(TAG, "Đang cập nhật badge với số lượng: " + count);
        if (count > 0) {
            binding.cartBadge.setVisibility(View.VISIBLE);
            binding.cartBadge.setText(String.valueOf(count));
        } else {
            binding.cartBadge.setVisibility(View.GONE);
        }
    }

    // SỬA 11: Xóa bỏ hàm onResume, vì ViewModel đã xử lý việc cập nhật tự động
    // @Override
    // protected void onResume() { ... }

    // (Các hàm fetchProductDetails, displayProductData, runFlyToCartAnimation, animateCartIcon giữ nguyên)
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


    private void fetchProductDetails(String productId) {
        binding.scroll.setVisibility(View.GONE);
        apiService.getProductById(productId).enqueue(new Callback<ProductDetail>() {
            @Override
            public void onResponse(@NonNull Call<ProductDetail> call, @NonNull Response<ProductDetail> response) {
                binding.scroll.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    // Lấy đối tượng sản phẩm từ response
                    ProductDetail product = response.body();

                    // 1. Hiển thị thông tin sản phẩm (tên, giá, ảnh,...)
                    displayProductData(product);

                    // 2. Cập nhật trạng thái nút "Thêm vào giỏ hàng" dựa trên số lượng tồn kho
                    //    (Sử dụng hàm getStock() từ model của bạn)
                    if (product.getStock() > 0) {
                        // Trường hợp 1: CÒN HÀNG
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Thêm vào giỏ hàng");
                    } else {
                        // Trường hợp 2: HẾT HÀNG
                        binding.btnAddToCart.setEnabled(false);
                        binding.btnAddToCart.setText("Đã hết hàng");
                    }

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

        // === BƯỚC 1: HIỂN THỊ DỮ LIỆU TĨNH CƠ BẢN (TÊN, MÔ TẢ) ===
        binding.tvProductName.setText(productDetail.getName());
        binding.tvProductTitle.setText(productDetail.getName());
        binding.tvDescription.setText(Html.fromHtml(productDetail.getDescription(), Html.FROM_HTML_MODE_LEGACY));

        // === BƯỚC 2: HIỂN THỊ SLIDER ẢNH (DI CHUYỂN LÊN TRÊN ĐỂ ƯU TIÊN) ===
        // Luôn xử lý ảnh ngay lập tức, không phụ thuộc vào logic sale.
        List<String> listUrlImageInfo = new ArrayList<>();
        if (productDetail.getDetailImages() != null && !productDetail.getDetailImages().isEmpty()) {
            for (ImageInfo imageInfo : productDetail.getDetailImages()) {
                // Kiểm tra an toàn 3 bước để đảm bảo URL hợp lệ
                if (imageInfo != null && imageInfo.getUrl() != null && !imageInfo.getUrl().trim().isEmpty()) {
                    listUrlImageInfo.add(imageInfo.getUrl());
                } else {
                    Log.w(TAG, "Phát hiện URL hình ảnh không hợp lệ cho sản phẩm: " + productDetail.getName());
                }
            }
        }

        if (!listUrlImageInfo.isEmpty()) {
            imageUrlSliderAdapter = new ImageUrlSliderAdapter(listUrlImageInfo);
            binding.sliderProduct.setAdapter(imageUrlSliderAdapter);
            binding.sliderProduct.setVisibility(View.VISIBLE);
        } else {
            // Nếu không có ảnh, ẩn slider đi
            Log.e(TAG, "Sản phẩm không có ảnh hợp lệ, ẩn slider.");
            binding.sliderProduct.setVisibility(View.GONE);
        }

        // === BƯỚC 3: XỬ LÝ LOGIC SALE VÀ GIÁ (PHẦN CÓ THỂ GÂY TRỄ) ===
        SaleInfo sale = productDetail.getSale();

        if (sale != null && sale.isActive()) {
            // --- TRƯỜNG HỢP CÓ KHUYẾN MÃI ---

            // 3.1. Hiển thị component Flash Sale
            binding.flashSaleComponent.getRoot().setVisibility(View.VISIBLE);

            // **SỬA LỖI CRASH Ở ĐÂY:** Chuyển số lượng tồn kho sang String trước khi setText
            binding.flashSaleComponent.tvQuantityRemaining.setText("Còn " + productDetail.getStock());

            // 3.2. Khởi tạo và chạy đồng hồ đếm ngược
            try {
                ZonedDateTime now = ZonedDateTime.now();
                ZonedDateTime endTime = ZonedDateTime.parse(sale.getEndAt());
                long durationMillis = Duration.between(now, endTime).toMillis();

                if (durationMillis > 0) {
                    if (saleCountDownTimer != null) {
                        saleCountDownTimer.cancel();
                    }

                    saleCountDownTimer = new CountDownTimer(durationMillis, 1000) {
                        @Override
                        public void onTick(long millisUntilFinished) {
                            // ======================= BẮT ĐẦU PHẦN SỬA ĐỔI =======================

                            // 1. Tính toán tổng số ngày, giờ, phút, giây
                            long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                            long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24; // Lấy phần giờ lẻ trong ngày
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;

                            String timeLeftFormatted;

                            // 2. Kiểm tra nếu còn ngày
                            if (days > 0) {
                                // CÒN NHIỀU HƠN 1 NGÀY: Hiển thị "X ngày HH:mm:ss"
                                timeLeftFormatted = String.format(
                                        Locale.getDefault(),
                                        "%d ngày %02d:%02d:%02d",
                                        days, hours, minutes, seconds
                                );
                            } else {
                                // CÒN ÍT HƠN 1 NGÀY: Chỉ hiển thị "HH:mm:ss"
                                timeLeftFormatted = String.format(
                                        Locale.getDefault(),
                                        "%02d:%02d:%02d",
                                        hours, minutes, seconds
                                );
                            }

                            // 3. Cập nhật TextView
                            binding.flashSaleComponent.tvSaleEndTime.setText("Kết thúc trong " + timeLeftFormatted);

                            // ======================= KẾT THÚC PHẦN SỬA ĐỔI =======================
                        }

                        @Override
                        public void onFinish() {
                            binding.flashSaleComponent.tvSaleEndTime.setText("Đã kết thúc");
                        }
                    }.start();
                } else {
                    binding.flashSaleComponent.tvSaleEndTime.setText("Đã kết thúc");
                }
            } catch (Exception e) {
                Log.e(TAG, "Lỗi khi xử lý thời gian sale", e);
                binding.flashSaleComponent.tvSaleEndTime.setText("Lỗi thời gian");
            }

            // 3.3. Tính toán và hiển thị giá
            double newPrice;
            if ("percent".equals(sale.getType())) {
                newPrice = productDetail.getPrice() * (100.0 - sale.getValue()) / 100.0;
            } else {
                newPrice = productDetail.getPrice() - sale.getValue();
            }
            binding.tvPrice.setText(currencyFormat.format(newPrice));
            binding.tvPrice.setTextColor(getResources().getColor(R.color.red)); // Đảm bảo bạn có màu R.color.red

            binding.tvOldPrice.setVisibility(View.VISIBLE);
            binding.tvOldPrice.setText(currencyFormat.format(productDetail.getPrice()));
            binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);

        } else {
            // --- TRƯỜNG HỢP KHÔNG CÓ KHUYẾN MÃI ---
            binding.flashSaleComponent.getRoot().setVisibility(View.GONE);
            binding.tvOldPrice.setVisibility(View.GONE);
            binding.tvPrice.setText(currencyFormat.format(productDetail.getPrice()));
            binding.tvPrice.setTextColor(getResources().getColor(R.color.black));
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
}
