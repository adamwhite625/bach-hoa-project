package com.example.frontend2.ui.main;

import android.content.Intent;import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.frontend2.adapter.ProductAdapter;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.ActivityProductListBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement; // <-- Import cần thiết cho giải pháp 2
import com.google.gson.reflect.TypeToken; // <-- Import cần thiết cho giải pháp 2

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private static final String TAG = "ProductListActivity";
    private ActivityProductListBinding binding;
    private ApiService apiService;
    private ProductAdapter productAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

        String categoryId = getIntent().getStringExtra("CATEGORY_ID");
        String categoryName = getIntent().getStringExtra("CATEGORY_NAME");

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(categoryName != null ? categoryName : "Sản phẩm");
        }

        setupRecyclerView();

        if (categoryId != null) {
            fetchProductsByCategoryId(categoryId);
        } else {
            Toast.makeText(this, "ID danh mục không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "CATEGORY_ID không hợp lệ.");
            finish();
        }
    }

    private void setupRecyclerView() {
        binding.recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        productAdapter = new ProductAdapter(this, new ArrayList<>(), this);
        binding.recyclerProducts.setAdapter(productAdapter);
    }

    private void fetchProductsByCategoryId(String categoryId) {
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.recyclerProducts.setVisibility(View.GONE);
//        if (binding.tvEmptyList != null) binding.tvEmptyList.setVisibility(View.GONE);


        apiService.getProductsByCategoryId(categoryId).enqueue(new Callback<JsonElement>() { // <-- Sửa Callback để nhận JsonElement
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonElement responseBody = response.body();
                    List<ProductInList> productList = new ArrayList<>();

                    // === LOGIC TỰ PHÂN TÍCH JSON BẮT ĐẦU TỪ ĐÂY ===
                    try {
                        if (responseBody.isJsonObject()) {
                            JsonElement productsElement = responseBody.getAsJsonObject().get("products");

                            if (productsElement != null && productsElement.isJsonArray()) {
                                Gson gson = new Gson();
                                Type type = new TypeToken<ArrayList<ProductInList>>(){}.getType();
                                productList = gson.fromJson(productsElement, type);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi tự phân tích JSON: ", e);
                        productList.clear(); // Đảm bảo danh sách trống nếu có lỗi
                    }
                    // === KẾT THÚC LOGIC TỰ PHÂN TÍCH JSON ===


                    if (productList != null && !productList.isEmpty()) {
                        binding.recyclerProducts.setVisibility(View.VISIBLE);
                        productAdapter.updateData(productList);
                    } else {
                        Toast.makeText(ProductListActivity.this, "Không có sản phẩm nào trong danh mục này", Toast.LENGTH_LONG).show();
//                        if (binding.tvEmptyList != null) {
//                            binding.tvEmptyList.setText("Không có sản phẩm nào");
//                            binding.tvEmptyList.setVisibility(View.VISIBLE);
//                        }
                    }
                } else {
                    Toast.makeText(ProductListActivity.this, "Lỗi khi tải sản phẩm: " + response.code(), Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "API Error: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                Toast.makeText(ProductListActivity.this, "Lỗi kết nối mạng", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "API Failure: ", t);
//                if (binding.tvEmptyList != null) {
//                    binding.tvEmptyList.setText("Lỗi kết nối. Vui lòng thử lại.");
//                    binding.tvEmptyList.setVisibility(View.VISIBLE);
//                }
            }
        });
    }

    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        startActivity(intent);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
