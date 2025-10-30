package com.example.frontend2.ui.main; // Hoặc package của bạn

import android.content.Intent;
import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide; // Cần thêm thư viện Glide để load ảnh
import com.example.frontend2.data.model.Product; // Model sản phẩm của bạn
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductDetailBinding; // <-- Quan trọng: tên file binding

import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";
    private ActivityProductDetailBinding binding; // Sử dụng ViewBinding
    private ApiService apiService;
    private SliderAdapter sliderAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Khởi tạo ViewBinding
        binding = ActivityProductDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Khởi tạo ApiService
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // Thiết lập nút quay lại
        setupBackButton();

        // Lấy ID sản phẩm từ Intent
        Intent intent = getIntent();
        String productId = intent.getStringExtra("PRODUCT_ID");
        if (productId != null) {
            // Nếu có ID hợp lệ, gọi API để lấy chi tiết sản phẩm
            fetchProductDetails(productId);
        } else {
            // Xử lý lỗi nếu không nhận được ID
            Toast.makeText(this, "Không tìm thấy sản phẩm", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Không nhận được PRODUCT_ID từ Intent");
            finish(); // Đóng Activity nếu có lỗi
        }

        // Cài đặt listener cho các nút khác (nếu cần)
        // Ví dụ: nút thêm vào giỏ hàng
        // binding.btnAddToCart.setOnClickListener(v -> {
        //     Toast.makeText(this, "Đã thêm vào giỏ hàng", Toast.LENGTH_SHORT).show();
        // });
    }

    private void setupBackButton() {
        // Gán sự kiện click cho ImageButton quay lại
        binding.btnBackSmall.setOnClickListener(v -> {
            // Đóng Activity hiện tại và quay về màn hình trước
            finish();
        });
    }

    private void fetchProductDetails(String productId) {
        binding.scroll.setVisibility(View.GONE); // Ẩn nội dung đi trong lúc tải
        // Hiển thị ProgressBar nếu có

        apiService.getProductById(productId).enqueue(new Callback<Product>() {
            @Override
            public void onResponse(@NonNull Call<Product> call, @NonNull Response<Product> response) {
                binding.scroll.setVisibility(View.VISIBLE); // Hiện lại nội dung sau khi tải xong

                if (response.isSuccessful() && response.body() != null) {
                    Product product = response.body();
                    // Hiển thị dữ liệu lên giao diện
                    displayProductData(product);
                } else {
                    Toast.makeText(ProductDetailActivity.this, "Lỗi khi tải dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi API: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<Product> call, @NonNull Throwable t) {
                binding.scroll.setVisibility(View.VISIBLE); // Hiện lại nội dung dù có lỗi
                Toast.makeText(ProductDetailActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi Failure: ", t);
            }
        });
    }

    private void displayProductData(Product product) {
        // Dùng NumberFormat để định dạng giá tiền cho đẹp (ví dụ: 10,000đ)
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        // Cập nhật các TextView
        binding.tvCategory.setText(product.getCategory().getName()); // Giả sử Product có getCategoryName()
        binding.tvProductTitle.setText(product.getName());
        binding.tvPrice.setText(currencyFormat.format(product.getPrice()));
        binding.tvDescription.setText(product.getDescription());

        // Xử lý giá cũ và gạch ngang
//        if (product.getOldPrice() > 0 && product.getOldPrice() > product.getPrice()) {
//            binding.tvOldPrice.setVisibility(View.VISIBLE);
//            binding.tvOldPrice.setText(currencyFormat.format(product.getOldPrice()));
//            // Thêm gạch ngang cho giá cũ
//            binding.tvOldPrice.setPaintFlags(binding.tvOldPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
//        } else {
//            binding.tvOldPrice.setVisibility(View.GONE);
//        }

        // Cập nhật slider ảnh
        // Giả sử product.getImageUrls() trả về một List<String>
        // sliderAdapter = new SliderAdapter(product.getImageUrls());

        // VÍ DỤ TẠM VỚI ẢNH CỨNG
//        sliderAdapter = new SliderAdapter(Arrays.asList(
//                R.drawable.slide1,
//                R.drawable.slide2,
//                R.drawable.slide3
//        ));
//        binding.sliderProduct.setAdapter(sliderAdapter);

        // Bạn có thể thêm CircleIndicator cho slider ở đây nếu muốn
    }
}
