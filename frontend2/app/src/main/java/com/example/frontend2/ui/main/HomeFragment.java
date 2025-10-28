package com.example.frontend2.ui.main;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat; // THÊM IMPORT
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.frontend2.R;
import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.Product;
import com.example.frontend2.databinding.FragmentHomeBinding;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.google.android.material.appbar.AppBarLayout; // THÊM IMPORT

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private FragmentHomeBinding binding;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private static final String TAG = "HomeFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>());
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Thiết lập RecyclerView
        setupRecyclerViews();

        // ======================= BẮT ĐẦU: THÊM CODE HIỆU ỨNG CUỘN =======================
        setupHeaderScrollEffect();
        // ======================= KẾT THÚC: THÊM CODE HIỆU ỨNG CUỘN =======================

        // Tải dữ liệu từ API
        fetchCategories();
        fetchProducts();
    }

    // Tách logic hiệu ứng cuộn ra một hàm riêng để code sạch hơn
    private void setupHeaderScrollEffect() {
        // Lấy các màu từ file colors.xml một cách an toàn
        int colorPrimary = ContextCompat.getColor(requireContext(), R.color.primary); // Màu xanh
        int colorBg = ContextCompat.getColor(requireContext(), R.color.bg);       // Màu nền của trang

        binding.appBarLayout.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {
            boolean isHeaderShown = true;

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                // verticalOffset là độ dịch chuyển. 0 là trạng thái mở rộng, giá trị âm khi cuộn lên.

                // Kiểm tra xem người dùng đã cuộn chưa.
                // Nếu verticalOffset là 0, tức là header đang hiển thị đầy đủ, chưa bị cuộn.
                if (verticalOffset == 0) {
                    // Nếu trạng thái trước đó là header đã bị cuộn, giờ mới đổi lại màu nền sáng
                    if (!isHeaderShown) {
                        binding.headerContainer.setBackgroundColor(colorBg);
                        isHeaderShown = true;
                    }
                } else {
                    // Ngược lại, nếu người dùng đã cuộn dù chỉ một chút
                    // và trạng thái trước đó là header chưa bị cuộn, giờ mới đổi sang màu xanh
                    if (isHeaderShown) {
                        binding.headerContainer.setBackgroundColor(colorPrimary);
                        isHeaderShown = false;
                    }
                }
            }
        });
    }

    private void setupRecyclerViews() {
        // Danh mục hiển thị ngang
        binding.recyclerCategory.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        binding.recyclerCategory.setAdapter(categoryAdapter);

        // Sản phẩm hiển thị dạng lưới 2 cột
        GridLayoutManager gridLayoutManager = new GridLayoutManager(getContext(), 2);
        binding.recyclerProduct.setLayoutManager(gridLayoutManager);
        binding.recyclerProduct.setAdapter(productAdapter);

        // Tối ưu hiệu năng
        binding.recyclerProduct.setHasFixedSize(true);

        // Tắt cuộn lồng vì đang trong NestedScrollView
        binding.recyclerProduct.setNestedScrollingEnabled(false);
    }
    private void fetchCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.updateData(response.body());
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu danh mục", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi khi lấy danh mục: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải danh mục", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi kết nối API danh mục: ", t);
            }
        });
    }

    private void fetchProducts() {
        apiService.getProducts().enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(@NonNull Call<List<Product>> call, @NonNull Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productAdapter.updateData(response.body());
                } else {
                    Toast.makeText(getContext(), "Không có dữ liệu sản phẩm", Toast.LENGTH_SHORT).show();
                    Log.e(TAG, "Lỗi khi lấy sản phẩm: " + response.code() + " - " + response.message());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Product>> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi khi tải sản phẩm", Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Lỗi kết nối API sản phẩm: ", t);
            }
        });
    }

    @Override
    public void onItemClick(Product product) {
        Toast.makeText(getContext(), "Đã chọn: " + product.getName(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
