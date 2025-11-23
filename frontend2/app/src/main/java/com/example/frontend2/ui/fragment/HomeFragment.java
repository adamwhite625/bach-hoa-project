package com.example.frontend2.ui.fragment;

// CÁC IMPORT ĐÃ ĐƯỢC DỌN DẸP
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import java.util.stream.Collectors;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.frontend2.R;
import com.example.frontend2.ui.adapter.CategoryAdapter;
import com.example.frontend2.ui.adapter.ProductAdapter;
import com.example.frontend2.ui.adapter.SliderAdapter;
import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentHomeBinding;
import com.example.frontend2.ui.main.ProductDetailActivity;
import com.example.frontend2.ui.main.ProductListActivity;
import com.example.frontend2.ui.adapter.FlashSaleAdapter;

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
    public static final String KEY_PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_FEATURED = "FEATURED";

    private FragmentHomeBinding binding;
    private ApiService apiService;

    // --- Adapters cho RecyclerViews ---
    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;

    private FlashSaleAdapter flashSaleAdapter;

    // --- Biến dành cho Banner Slider ---
    private SliderAdapter sliderAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Timer sliderTimer;

    private static final int MAX_PRODUCTS_HOME = 10;

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

        setupClickListeners();
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
        // === Category RecyclerView ===
        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerCategory.setAdapter(categoryAdapter);

        // === Product RecyclerView (Sản phẩm thường) ===
        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerProduct.setAdapter(productAdapter);
        binding.recyclerProduct.setNestedScrollingEnabled(false);

        // =========================================================================
        // *** BỔ SUNG PHẦN CÒN THIẾU TẠI ĐÂY ***

        // === Flash Sale RecyclerView (Sản phẩm khuyến mãi) ===
        // 1. Thiết lập LayoutManager cuộn ngang
        LinearLayoutManager flashSaleLayoutManager = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false);
        binding.recyclerFlashSale.setLayoutManager(flashSaleLayoutManager);

        // 2. Khởi tạo FlashSaleAdapter
        flashSaleAdapter = new FlashSaleAdapter(getContext(), new ArrayList<>(), this);

        // 3. Gán Adapter cho RecyclerView
        binding.recyclerFlashSale.setAdapter(flashSaleAdapter);

        // =========================================================================
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

    private void fetchProducts() {        // 1. Kiểm tra để đảm bảo apiService đã được khởi tạo, tránh NullPointerException
        if (apiService == null) {
            Log.e(TAG, "Lỗi nghiêm trọng: ApiService chưa được khởi tạo.");
            return;
        }

        // 2. Thực hiện cuộc gọi API bất đồng bộ
        apiService.getProducts().enqueue(new Callback<List<ProductInList>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductInList>> call, @NonNull Response<List<ProductInList>> response) {
                // 3. KIỂM TRA AN TOÀN VÒNG ĐỜI: Đảm bảo Fragment còn "sống" trước khi làm bất cứ điều gì
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "onResponse được gọi nhưng Fragment không còn tồn tại. Bỏ qua cập nhật UI.");
                    return; // Thoát ngay để tránh crash
                }

                // 4. KIỂM TRA AN TOÀN VIEW: Đảm bảo binding (đại diện cho View) chưa bị hủy
                if (binding == null) {
                    Log.w(TAG, "onResponse được gọi nhưng View đã bị hủy. Bỏ qua cập nhật UI.");
                    return; // Thoát ngay
                }

                // 5. Xử lý kết quả trả về từ API
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductInList> allProducts = response.body();
                    Log.d(TAG, "Tải thành công " + allProducts.size() + " sản phẩm từ API.");

                    // --- PHẦN LOGIC LỌC SẢN PHẨM ---
                    List<ProductInList> saleProducts = allProducts.stream()
                            .filter(p -> p.getSale() != null && p.getSale().isActive())
                            .collect(Collectors.toList());

                    List<ProductInList> normalProducts = allProducts.stream()
                            .filter(p -> p.getSale() == null || !p.getSale().isActive())
                            .collect(Collectors.toList());

                    Log.d(TAG, "Đã lọc ra: " + saleProducts.size() + " sản phẩm khuyến mãi và " + normalProducts.size() + " sản phẩm thường.");

                    if (saleProducts.isEmpty()) {
                        // Nếu không có sản phẩm sale nào, ẩn toàn bộ khu vực đi
                        binding.flashSaleSectionContainer.setVisibility(View.GONE);
                    } else {
                        // Nếu có sản phẩm sale, đảm bảo khu vực được hiển thị
                        binding.flashSaleSectionContainer.setVisibility(View.VISIBLE);
                    }

                    // --- CÁCH CẬP NHẬT DỮ LIỆU AN TOÀN VÀ HIỆU QUẢ NHẤT ---
                    if (flashSaleAdapter != null) {
                        List<ProductInList> limitedSaleProducts = saleProducts.stream()
                                .limit(MAX_PRODUCTS_HOME)
                                .collect(Collectors.toList());
                        flashSaleAdapter.updateData(limitedSaleProducts);
                    } else {
                        Log.w(TAG, "flashSaleAdapter là null, không thể cập nhật.");
                    }

                    if (productAdapter != null) {
                        List<ProductInList> limitedProducts = normalProducts.stream()
                                .limit(MAX_PRODUCTS_HOME)
                                .collect(Collectors.toList());
                        productAdapter.updateData(limitedProducts);
                    } else {
                        Log.w(TAG, "productAdapter là null, không thể cập nhật.");
                    }

                } else {
                    // Xử lý trường hợp API trả về lỗi (ví dụ: 404, 500)
                    Log.e(TAG, "Lỗi khi lấy sản phẩm, mã lỗi HTTP: " + response.code());
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductInList>> call, @NonNull Throwable t) {
                // 6. KIỂM TRA AN TOÀN VÒNG ĐỜI (cho trường hợp lỗi mạng)
                if (!isAdded() || getContext() == null) {
                    Log.w(TAG, "onFailure được gọi nhưng Fragment không còn tồn tại.");
                    return;
                }

                // Xử lý trường hợp không thể kết nối tới server
                Log.e(TAG, "Lỗi kết nối mạng khi gọi API sản phẩm: ", t);
                Toast.makeText(getContext(), "Không thể kết nối tới máy chủ, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
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

    private void setupClickListeners() {
        // 1. Sự kiện click cho nút "Xem tất cả" của mục KHUYẾN MÃI
        binding.btnViewAllSale.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Xem tất cả Khuyến mãi' được nhấn.");
            // Gọi hàm điều hướng và truyền vào loại "SALE"
            navigateToProductList(TYPE_SALE);
        });

        // 2. Sự kiện click cho nút "Xem tất cả" của mục SẢN PHẨM NỔI BẬT
        binding.btnViewAllFeatured.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Xem tất cả Sản phẩm nổi bật' được nhấn.");
            // Gọi hàm điều hướng và truyền vào loại "FEATURED"
            navigateToProductList(TYPE_FEATURED);
        });

        // Bạn có thể thêm các sự kiện click khác ở đây (ví dụ: thanh tìm kiếm, chuông thông báo)
        // binding.searchBarLayout.setOnClickListener(...);
    }

    private void navigateToProductList(String productType) {
        // Kiểm tra để đảm bảo context (Activity) không bị null
        if (getActivity() == null) {
            Log.e(TAG, "Không thể mở ProductListActivity vì getActivity() là null.");
            return;
        }

        // Tạo một Intent để mở ProductListActivity
        Intent intent = new Intent(getActivity(), ProductListActivity.class);

        // Đính kèm dữ liệu vào Intent để Activity mới biết phải làm gì
        intent.putExtra(KEY_PRODUCT_TYPE, productType);

        // Bắt đầu Activity mới
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
