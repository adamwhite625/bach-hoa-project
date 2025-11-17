package com.example.frontend2.ui.main;

// CÁC IMPORT ĐÃ ĐƯỢC DỌN DẸP
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.frontend2.R;
import com.example.frontend2.adapter.CategoryAdapter;
import com.example.frontend2.adapter.ProductAdapter;
import com.example.frontend2.adapter.SliderAdapter;
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

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private ApiService apiService;

    // --- Adapters cho RecyclerViews ---
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    // --- Biến dành cho Banner Slider ---
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
        super.onViewCreated(view, savedInstanceState);

        // 1. Thiết lập các RecyclerViews và Slider
        setupBannerSlider();
        setupRecyclerViews();

        // 2. Tải dữ liệu từ API
        fetchCategories();
        fetchProducts();

        if (binding != null) {
            binding.searchBarLayout.setOnClickListener(v -> {
                Log.d(TAG, "Thanh tìm kiếm được nhấn! Thực hiện FragmentTransaction.");

                // 1. Tạo một instance của SearchFragment
                SearchFragment searchFragment = new SearchFragment();

                // 2. Lấy FragmentManager từ Activity cha (MainActivity)
                if (getActivity() != null) {
                    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();

                    // 3. Bắt đầu một giao dịch Fragment
                    fragmentManager.beginTransaction()
                            // Thay thế Fragment hiện tại trong container bằng SearchFragment
                            // R.id.fragment_container là ID của FrameLayout trong activity_main.xml
                            .replace(R.id.fragment_container, searchFragment)

                            // (QUAN TRỌNG) Thêm giao dịch này vào Back Stack
                            // Điều này cho phép người dùng nhấn nút Back để quay lại HomeFragment
                            .addToBackStack(null)

                            // Thực thi giao dịch
                            .commit();
                }
            });
        }
    }

    // =================================================================
    // VÒNG ĐỜI FRAGMENT & QUẢN LÝ SLIDER
    // =================================================================

    @Override
    public void onResume() {
        super.onResume();
        // Bắt đầu lại slider khi người dùng quay lại màn hình
        startAutoSlider();
    }

    @Override
    public void onPause() {
        super.onPause();
        // Tạm dừng slider khi người dùng rời khỏi màn hình để tiết kiệm pin
        stopAutoSlider();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Dừng hẳn và dọn dẹp slider để tránh rò rỉ bộ nhớ
        stopAutoSlider();
        // Quan trọng: giải phóng tham chiếu đến binding
        binding = null;
    }

    // =================================================================
    // SETUP VIEWS
    // =================================================================

    private void setupBannerSlider() {
        List<Integer> imageList = Arrays.asList(
                R.drawable.slide1, R.drawable.slide2, R.drawable.slide3, R.drawable.slide4
        );
        sliderAdapter = new SliderAdapter(imageList);
        binding.viewPagerBanner.setAdapter(sliderAdapter);
        binding.viewPagerBanner.setClipToPadding(false);
        binding.viewPagerBanner.setClipChildren(false);
        binding.viewPagerBanner.setOffscreenPageLimit(3);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerBanner.setPageTransformer(compositePageTransformer);
    }

    private void setupRecyclerViews() {
        // Category RecyclerView
        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerCategory.setAdapter(categoryAdapter);

        // Product RecyclerView
        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerProduct.setAdapter(productAdapter);
        binding.recyclerProduct.setNestedScrollingEnabled(false); // Giúp cuộn mượt hơn trong NestedScrollView
    }

    // =================================================================
    // LẤY DỮ LIỆU TỪ API
    // =================================================================

    private void fetchCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (response.isSuccessful() && response.body() != null && categoryAdapter != null) {
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
                if (response.isSuccessful() && response.body() != null && productAdapter != null) {
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

    // =================================================================
    // XỬ LÝ SỰ KIỆN CLICK
    // =================================================================

    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(Category category) {
        Intent intent = new Intent(getActivity(), ProductListActivity.class);
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("CATEGORY_NAME", category.getName());
        startActivity(intent);
    }

    // =================================================================
    // LOGIC TỰ ĐỘNG CUỘN SLIDER
    // =================================================================

    private void startAutoSlider() {
        // Kiểm tra để đảm bảo slider không chạy 2 lần
        if (sliderTimer != null) {
            return;
        }
        sliderTimer = new Timer();
        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sliderHandler.post(() -> {
                    if (binding == null || binding.viewPagerBanner.getAdapter() == null) return;
                    int count = binding.viewPagerBanner.getAdapter().getItemCount();
                    int currentItem = binding.viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % count;
                    binding.viewPagerBanner.setCurrentItem(nextItem, true);
                });
            }
        }, 3000, 3000); // Bắt đầu sau 3s, lặp lại mỗi 3s
    }

    private void stopAutoSlider() {
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }
    }

    // === TOÀN BỘ LOGIC VỀ "observeCartChanges" ĐÃ ĐƯỢC XÓA VÌ NÓ KHÔNG THUỘC VỀ HOMEFRAGMENT ===

}
