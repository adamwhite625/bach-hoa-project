package com.example.frontend2.ui.main;

// CÁC IMPORT CẦN THIẾT CHO CẢ RECYCLERVIEW VÀ SLIDER
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Intent;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.frontend2.R;
import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {

    private FragmentHomeBinding binding;
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private ApiService apiService;
    private static final String TAG = "HomeFragment";

    // --- BIẾN DÀNH CHO SLIDER BANNER ---
    private SliderAdapter sliderAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Timer sliderTimer;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
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
        setupRecyclerViews();

        // 2. Thiết lập Banner Slider
        setupBannerSlider();

        // 3. Tải dữ liệu từ API
        fetchCategories();
        fetchProducts();
    }

    private void setupBannerSlider() {
        // 1. Chuẩn bị danh sách ảnh từ drawable
        List<Integer> imageList = Arrays.asList(
                R.drawable.slide1,
                R.drawable.slide2,
                R.drawable.slide3,
                R.drawable.slide4
        );

        // 2. Khởi tạo adapter và gán cho ViewPager2
        sliderAdapter = new SliderAdapter(imageList);
        binding.viewPagerBanner.setAdapter(sliderAdapter);

        // 3. (Tùy chọn) Thêm hiệu ứng và bo góc
        binding.viewPagerBanner.setClipToPadding(false);
        binding.viewPagerBanner.setClipChildren(false);
        binding.viewPagerBanner.setOffscreenPageLimit(3);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(30)); // Khoảng cách giữa các slide
//        compositePageTransformer.addTransformer((page, position) -> {
//            float r = 1 - Math.abs(position);
//            page.setScaleY(0.85f + r * 0.15f); // Hiệu ứng thu nhỏ slide ở 2 bên
//        });
        binding.viewPagerBanner.setPageTransformer(compositePageTransformer);

        // 4. Bắt đầu tự động cuộn
        startAutoSlider(imageList.size());
    }

    private void startAutoSlider(final int count) {
        sliderTimer = new Timer();
        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sliderHandler.post(() -> {
                    if (binding == null) return; // Tránh crash nếu fragment đã bị hủy
                    int currentItem = binding.viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % count;
                    binding.viewPagerBanner.setCurrentItem(nextItem, true); // true để cuộn mượt
                });
            }
        }, 1500, 1500); // Bắt đầu sau 1s, lặp lại mỗi 1s
    }

    private void stopAutoSlider() {
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }
    }

    private void setupRecyclerViews() {
        // Thiết lập Category RecyclerView
        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), this); // không cần ép kiểu nếu implement đúng
        binding.recyclerCategory.setAdapter(categoryAdapter);

        // Thiết lập Product RecyclerView
        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this); // không cần ép kiểu nếu implement đúng
        binding.recyclerProduct.setAdapter(productAdapter);
    }

    private void fetchCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categoryAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "Lỗi khi lấy danh mục: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi kết nối API danh mục: ", t);
            }
        });
    }

    private void fetchProducts() {
        apiService.getProducts().enqueue(new Callback<List<ProductInList>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductInList>> call, @NonNull Response<List<ProductInList>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    productAdapter.updateData(response.body());
                } else {
                    Log.e(TAG, "Lỗi khi lấy sản phẩm: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductInList>> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi kết nối API sản phẩm: ", t);
            }
        });
    }
    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);

        // Gửi product_id (đặt key thống nhất là "product_id")
        Log.d(TAG, productInList.getId());
        intent.putExtra("product_id", productInList.getId());

        startActivity(intent);
    }

    @Override
    public void onCategoryClick(Category category) {
        // 1. Log để kiểm tra (lấy thông tin từ đối tượng category)
        Log.d(TAG, "Clicked on Category: " + category.getName() + " with ID: " + category.getId());

        // 2. Tạo Intent để mở màn hình ProductListActivity
        Intent intent = new Intent(getActivity(), ProductListActivity.class);

        // 3. Đính kèm dữ liệu (ID và Tên) lấy trực tiếp từ đối tượng category
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("CATEGORY_NAME", category.getName());

        // 4. Bắt đầu Activity mới
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlider(); // QUAN TRỌNG: Dừng slider khi thoát màn hình để tránh rò rỉ bộ nhớ
        binding = null;
    }
}
