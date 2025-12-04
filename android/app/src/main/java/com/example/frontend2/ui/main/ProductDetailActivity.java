package com.example.frontend2.ui.main;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.coordinatorlayout.widget.CoordinatorLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.frontend2.R;
import com.example.frontend2.data.model.AddToCartRequest;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.ImageInfo;
import com.example.frontend2.data.model.ProductDetail;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.SaleInfo;
import com.example.frontend2.data.model.Review;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductDetailBinding;
import com.example.frontend2.ui.adapter.ImageUrlSliderAdapter;
import com.example.frontend2.ui.adapter.ProductAdapter;
import com.example.frontend2.ui.adapter.ReviewAdapter;
import com.example.frontend2.ui.fragment.AddToCartBottomSheetFragment;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;


import java.text.NumberFormat;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
public class ProductDetailActivity extends AppCompatActivity implements AddToCartBottomSheetFragment.CartUpdateListener, ProductAdapter.OnItemClickListener {

    private static final String TAG = "ProductDetailActivity";
    private ActivityProductDetailBinding binding;
    private ApiService apiService;
    private ImageUrlSliderAdapter imageUrlSliderAdapter;
    private ProductDetail currentProduct;
    private CountDownTimer saleCountDownTimer;

    private RecyclerView rvReviews;
    private ReviewAdapter reviewAdapter;

    private TextView tvNoReviews;
    private List<Review> reviewList = new ArrayList<>();

    private LinearLayout layoutAverageRating;
    private TextView tvAverageRatingScore;
    private TextView tvTotalReviewsCount;


    private RecyclerView rvRelatedProducts;
    private TextView tvRelatedProductsTitle;
    private ProductAdapter relatedProductsAdapter;
    private List<ProductInList> relatedProductList = new ArrayList<>();

    private static final int REQUEST_CODE_CART = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        tvRelatedProductsTitle = findViewById(R.id.tv_related_products_title);
        rvRelatedProducts = findViewById(R.id.rv_related_products);

        layoutAverageRating = findViewById(R.id.layout_average_rating);
        tvAverageRatingScore = findViewById(R.id.tv_average_rating_score);
        tvTotalReviewsCount = findViewById(R.id.tv_total_reviews_count);

        setupBackButton();
        setupCartButton();
        setupRecyclerViews();
        fetchProductDataFromIntent();
        fetchInitialCartCount();

        rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        tvNoReviews = findViewById(R.id.tv_no_reviews);

        binding.btnAddToCart.setOnClickListener(v -> {
            if (currentProduct != null) {
                showAddToCartBottomSheet();
            } else {
                Toast.makeText(this, "Vui lòng chờ tải xong dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
            }
        });

        binding.searchBarLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("ACTION", "OPEN_SEARCH_FRAGMENT");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CART && resultCode == RESULT_OK) {
            if (data != null) {
                int cartItemCount = data.getIntExtra("CART_ITEM_COUNT", 0);
                updateCartBadgeWithCount(cartItemCount);
            }
        }
    }

    @Override
    public void onCartUpdated(int quantity) {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            Toast.makeText(this, "Vui lòng đăng nhập để thêm sản phẩm", Toast.LENGTH_SHORT).show();
            return;
        }

        binding.btnAddToCart.setEnabled(false);
        AddToCartRequest request = new AddToCartRequest(currentProduct.getId(), quantity);

        apiService.addToCart("Bearer " + token, request).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                binding.btnAddToCart.setEnabled(true);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(ProductDetailActivity.this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
                    runFlyToCartAnimation(binding.btnAddToCart);
                    int totalCount = 0;
                    if (response.body().getItems() != null) {
                        for (CartItem item : response.body().getItems()) {
                            totalCount += item.getQuantity();
                        }
                    }
                    updateCartBadgeWithCount(totalCount);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Thêm thất bại: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                binding.btnAddToCart.setEnabled(true);
                Toast.makeText(ProductDetailActivity.this, "Lỗi mạng: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchInitialCartCount() {
        String token = SharedPrefManager.getInstance(this).getAuthToken();
        if (token == null) {
            updateCartBadgeWithCount(0);
            return;
        }

        apiService.getCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                int totalCount = 0;
                if (response.isSuccessful() && response.body() != null && response.body().getItems() != null) {
                    for (CartItem item : response.body().getItems()) {
                        totalCount += item.getQuantity();
                    }
                }
                updateCartBadgeWithCount(totalCount);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                updateCartBadgeWithCount(0);
                Log.e(TAG, "Lỗi khi lấy số lượng giỏ hàng ban đầu: " + t.getMessage());
            }
        });
    }

    private void setupBackButton() {
        binding.btnBackSmall.setOnClickListener(v -> finish());
    }

    private void setupCartButton() {
        binding.cartIcon.setOnClickListener(v -> {
            Intent intent = new Intent(ProductDetailActivity.this, MainActivity.class);
            intent.putExtra("NAVIGATE_TO", "CART_FRAGMENT");intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void updateCartBadgeWithCount(int count) {
        if (count > 0) {
            binding.cartBadge.setVisibility(View.VISIBLE);
            binding.cartBadge.setText(String.valueOf(count));
        } else {
            binding.cartBadge.setVisibility(View.GONE);
        }
    }

    private void fetchProductDataFromIntent() {
        Intent intent = getIntent();
        String productId = intent.getStringExtra("product_id");
        if (productId != null) {
            fetchProductDetails(productId);
        } else {
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
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
                if (binding == null) {
                    return;
                }
                binding.scroll.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null) {
                    ProductDetail product = response.body();
                    displayProductData(product);

                    if (currentProduct.getCategory() != null && !currentProduct.getCategory().isEmpty()) {
                        fetchRelatedProducts(currentProduct.getCategory());
                    }

                    if (product.getStock() > 0) {
                        binding.btnAddToCart.setEnabled(true);
                        binding.btnAddToCart.setText("Thêm vào giỏ hàng");
                    } else {
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

    private void fetchRelatedProducts(String categoryId) {
        apiService.getProductsByCategoryId(categoryId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isJsonObject()) {

                    JsonElement productsArrayElement = response.body().getAsJsonObject().get("products");

                    if (productsArrayElement != null && productsArrayElement.isJsonArray()) {
                        Gson gson = new Gson();
                        Type productListType = new TypeToken<List<ProductInList>>(){}.getType();
                        List<ProductInList> allProductsInCategory = gson.fromJson(productsArrayElement, productListType);

                        if (allProductsInCategory != null) {
                            List<ProductInList> filteredProducts = allProductsInCategory.stream()
                                    .filter(p -> !p.getId().equals(currentProduct.getId()))
                                    .collect(Collectors.toList());

                            if (!filteredProducts.isEmpty()) {
                                tvRelatedProductsTitle.setVisibility(View.VISIBLE);
                                rvRelatedProducts.setVisibility(View.VISIBLE);
                                relatedProductsAdapter.updateData(filteredProducts);
                            } else {
                                tvRelatedProductsTitle.setVisibility(View.GONE);
                                rvRelatedProducts.setVisibility(View.GONE);
                            }
                        }
                    }
                } else {
                    Log.e(TAG, "Lỗi khi tải sản phẩm liên quan hoặc response không hợp lệ: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi kết nối khi tải sản phẩm liên quan: " + t.getMessage());
            }
        });
    }

    private void displayProductData(ProductDetail productDetail) {
        this.currentProduct = productDetail;
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        binding.tvProductName.setText(productDetail.getName());
        binding.tvProductTitle.setText(productDetail.getName());
        binding.tvDescription.setText(Html.fromHtml(productDetail.getDescription(), Html.FROM_HTML_MODE_LEGACY));

        List<String> listUrlImageInfo = new ArrayList<>();
        if (productDetail.getDetailImages() != null && !productDetail.getDetailImages().isEmpty()) {
            for (ImageInfo imageInfo : productDetail.getDetailImages()) {
                if (imageInfo != null && imageInfo.getUrl() != null && !imageInfo.getUrl().trim().isEmpty()) {
                    listUrlImageInfo.add(imageInfo.getUrl());
                }
            }
        }

        if (!listUrlImageInfo.isEmpty()) {
            imageUrlSliderAdapter = new ImageUrlSliderAdapter(listUrlImageInfo);
            binding.sliderProduct.setAdapter(imageUrlSliderAdapter);
            binding.sliderProduct.setVisibility(View.VISIBLE);
        } else {
            binding.sliderProduct.setVisibility(View.GONE);
        }

        int numberOfReviews = productDetail.getNumReviews();
        if (numberOfReviews > 0) {
            rvReviews.setVisibility(View.VISIBLE);
            tvNoReviews.setVisibility(View.GONE);
            layoutAverageRating.setVisibility(View.VISIBLE);
            tvTotalReviewsCount.setVisibility(View.VISIBLE);

            double averageRating = productDetail.getRating();
            tvAverageRatingScore.setText(String.format("%.1f", averageRating));
            tvTotalReviewsCount.setText(String.format("(%d đánh giá)", numberOfReviews));

            if (reviewAdapter != null && productDetail.getReviews() != null) {
                reviewAdapter.updateData(productDetail.getReviews());
            }

        } else {
            rvReviews.setVisibility(View.GONE);
            tvNoReviews.setVisibility(View.VISIBLE);
            layoutAverageRating.setVisibility(View.GONE);
            tvTotalReviewsCount.setVisibility(View.GONE);
        }

        SaleInfo sale = productDetail.getSale();
        if (sale != null && sale.isActive()) {
            binding.flashSaleComponent.getRoot().setVisibility(View.VISIBLE);
            binding.flashSaleComponent.tvQuantityRemaining.setText("Còn " + productDetail.getStock());

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
                            long days = TimeUnit.MILLISECONDS.toDays(millisUntilFinished);
                            long hours = TimeUnit.MILLISECONDS.toHours(millisUntilFinished) % 24;
                            long minutes = TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished) % 60;
                            long seconds = TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) % 60;
                            String timeLeftFormatted;
                            if (days > 0) {
                                timeLeftFormatted = String.format(Locale.getDefault(), "%d ngày %02d:%02d:%02d", days, hours, minutes, seconds);
                            } else {
                                timeLeftFormatted = String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds);
                            }
                            binding.flashSaleComponent.tvSaleEndTime.setText("Kết thúc trong " + timeLeftFormatted);
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
                binding.flashSaleComponent.tvSaleEndTime.setText("Lỗi thời gian");
            }

            double newPrice = productDetail.getFinalPrice();
            binding.tvPrice.setText(currencyFormat.format(newPrice));
            binding.tvPrice.setTextColor(getResources().getColor(R.color.red));
            binding.tvOldPrice.setVisibility(View.VISIBLE);
            binding.tvOldPrice.setText(currencyFormat.format(productDetail.getPrice()));
            binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
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

    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private void setupRecyclerViews() {
        RecyclerView rvReviews = findViewById(R.id.rv_reviews);
        rvReviews.setLayoutManager(new LinearLayoutManager(this));
        reviewAdapter = new ReviewAdapter(this, reviewList);
        rvReviews.setAdapter(reviewAdapter);

        rvRelatedProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        relatedProductsAdapter = new ProductAdapter(this, relatedProductList, this);
        rvRelatedProducts.setAdapter(relatedProductsAdapter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (saleCountDownTimer != null) {
            saleCountDownTimer.cancel();
        }

        binding = null;
    }

}
