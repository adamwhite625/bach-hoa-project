// File: com/example/frontend2/ui/main/ProductListActivity.java
package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.ProductListResponse;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductListBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// FIX 1: Implement đúng tên interface lồng bên trong ProductAdapter
public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private static final String TAG = "ProductListActivity";
    private ActivityProductListBinding binding; // Sử dụng ViewBinding
    private ApiService apiService;
    private ProductAdapter productAdapter; // Dùng lại ProductAdapter của bạn

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 1. Khởi tạo ApiService
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        // 2. Lấy dữ liệu từ Intent gửi đến
        Intent intent = getIntent();
        String categoryId = intent.getStringExtra("CATEGORY_ID");
        String categoryName = intent.getStringExtra("CATEGORY_NAME");

        // 3. Thiết lập Toolbar
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true); // Hiển thị nút Back
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Sản phẩm"); // Đặt tên danh mục làm tiêu đề
        }

        // 4. Thiết lập RecyclerView
        setupRecyclerView();

        // 5. Gọi API để lấy sản phẩm theo danh mục
        if (categoryId != null) {
            fetchProductsByCategory(categoryId);
        } else {
            Toast.makeText(this, "Không tìm thấy danh mục", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "CATEGORY_ID is null.");
            finish(); // Đóng Activity nếu không có ID
        }
    }

    private void setupRecyclerView() {
        binding.recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));

        // FIX 2: Khởi tạo adapter. Bây giờ "this" đã được hiểu đúng là một ProductAdapter.OnItemClickListener
        productAdapter = new ProductAdapter(this, new ArrayList<>(), this);
        binding.recyclerProducts.setAdapter(productAdapter);
    }

    private void fetchProductsByCategory(String categoryId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerProducts.setVisibility(View.GONE);

        // FIX 1: Kiểu dữ liệu trong Callback đã được đổi thành "ProductListResponse"
        // để khớp với định nghĩa mới trong ApiService.
        apiService.getProductsByCategory(categoryId).enqueue(new Callback<ProductListResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductListResponse> call, @NonNull Response<ProductListResponse> response) {
                // Dù thành công hay thất bại, cũng nên ẩn ProgressBar đi
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerProducts.setVisibility(View.VISIBLE);

                // FIX 2: response.body() bây giờ là một đối tượng ProductListResponse, không phải List.
                if (response.isSuccessful() && response.body() != null) {

                    // FIX 3: Lấy danh sách sản phẩm TỪ BÊN TRONG đối tượng response.
                    // Đây là bước quan trọng nhất.
                    List<ProductInList> productList = response.body().getProducts();
                    Log.d(TAG, productList.toString());
                    // Kiểm tra xem danh sách lấy ra có rỗng hay không
                    if (productList != null && !productList.isEmpty()) {
                        // Nếu có sản phẩm, cập nhật dữ liệu cho adapter
                        productAdapter.updateData(productList);
                    } else {
                        // Nếu không có sản phẩm, hiển thị thông báo
                        Toast.makeText(ProductListActivity.this, "Không có sản phẩm nào trong danh mục này", Toast.LENGTH_LONG).show();
                        // Bạn cũng có thể hiện một text view "Danh sách trống" ở đây
                    }
                } else {
                    // Xử lý khi API trả về lỗi (ví dụ: 404 Not Found, 500 Server Error)
                    Toast.makeText(ProductListActivity.this, "Lỗi khi tải sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductListResponse> call, @NonNull Throwable t) {
                // Xử lý khi không có kết nối mạng hoặc lỗi phân tích JSON
                binding.progressBar.setVisibility(View.GONE);
                binding.recyclerProducts.setVisibility(View.VISIBLE); // Vẫn hiện recycler để người dùng thấy list trống
                Toast.makeText(ProductListActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Failure: ", t);
            }
        });
    }

    // FIX 3: Override đúng phương thức từ interface của bạn (onItemClick với tham số ProductInList)
    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId()); // Lấy ID từ đối tượng productInList
        startActivity(intent);
    }

    // Xử lý sự kiện khi nhấn nút Back trên Toolbar
    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed(); // Hoặc finish();
        return true;
    }
}
