package com.example.frontend2.ui.main;

// === CÁC IMPORT ĐÃ ĐƯỢC CẬP NHẬT ===
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.frontend2.adapter.ProductAdapter;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentSearchBinding;
import com.google.gson.Gson;
import com.google.gson.JsonElement; // <-- Import cho việc phân tích JSON thủ công
import com.google.gson.reflect.TypeToken; // <-- Import cho việc phân tích JSON thủ công
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment implements ProductAdapter.OnItemClickListener {

    private static final String TAG = "SearchFragment";
    private FragmentSearchBinding binding;
    private ProductAdapter productAdapter;
    private ApiService apiService;

    private Handler searchHandler = new Handler(Looper.getMainLooper());
    private Runnable searchRunnable;
    private Call<JsonElement> currentSearchCall; // <-- Sửa kiểu dữ liệu của Call

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setupRecyclerView();
        setupSearchInput();
        setupBackButton();

        binding.textNoResults.setText("Nhập từ khóa để tìm kiếm sản phẩm");
        binding.textNoResults.setVisibility(View.VISIBLE);

        binding.searchInput.requestFocus();
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.searchInput, InputMethodManager.SHOW_IMPLICIT);
    }

    private void setupBackButton() {
        binding.buttonBack.setOnClickListener(v -> {
            if (isAdded()) {
                // Ẩn bàn phím trước khi thoát
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                // Quay lại fragment trước đó
                getParentFragmentManager().popBackStack();
            }
        });
    }

    private void setupRecyclerView() {
        binding.recyclerSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerSearchResults.setAdapter(productAdapter);
    }

    private void setupSearchInput() {
        binding.searchInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (searchRunnable != null) {
                    searchHandler.removeCallbacks(searchRunnable);
                }
                if (currentSearchCall != null && currentSearchCall.isExecuted() && !currentSearchCall.isCanceled()) {
                    currentSearchCall.cancel();
                }

                searchRunnable = () -> {
                    String keyword = s.toString().trim();
                    if (!keyword.isEmpty()) {
                        performSearch(keyword);
                    } else {
                        if (binding != null) {
                            productAdapter.updateData(new ArrayList<>());
                            binding.textNoResults.setText("Nhập từ khóa để tìm kiếm sản phẩm");
                            binding.textNoResults.setVisibility(View.VISIBLE);
                            binding.recyclerSearchResults.setVisibility(View.GONE);
                        }
                    }
                };
                searchHandler.postDelayed(searchRunnable, 500);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.searchInput.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
                return true;
            }
            return false;
        });
    }

    private void performSearch(String keyword) {
        Log.d(TAG, "Bắt đầu tìm kiếm với từ khóa: " + keyword);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.textNoResults.setVisibility(View.GONE);
        binding.recyclerSearchResults.setVisibility(View.GONE);

        currentSearchCall = apiService.searchProducts(keyword);
        currentSearchCall.enqueue(new Callback<JsonElement>() { // <-- Sửa kiểu dữ liệu của Callback
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (getContext() == null || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                if (response.isSuccessful() && response.body() != null) {
                    JsonElement responseBody = response.body();
                    List<ProductInList> results = new ArrayList<>();

                    // === LOGIC TỰ PHÂN TÍCH JSON BẮT ĐẦU TỪ ĐÂY ===
                    try {
                        // 1. Kiểm tra xem response có phải là một đối tượng JSON không (có dấu { })
                        if (responseBody.isJsonObject()) {
                            // 2. Lấy ra trường "products" từ đối tượng đó
                            JsonElement productsElement = responseBody.getAsJsonObject().get("products");

                            // 3. Dùng Gson để chuyển đổi trường "products" (là một mảng JSON)
                            //    thành một List<ProductInList> của Java.
                            if (productsElement != null && productsElement.isJsonArray()) {
                                Gson gson = new Gson();
                                Type productListType = new TypeToken<ArrayList<ProductInList>>(){}.getType();
                                results = gson.fromJson(productsElement, productListType);
                            }
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Lỗi tự phân tích JSON: ", e);
                        results.clear(); // Đảm bảo danh sách trống nếu có lỗi
                    }
                    // === KẾT THÚC LOGIC TỰ PHÂN TÍCH JSON ===

                    // Phần logic hiển thị kết quả không thay đổi
                    if (results != null && !results.isEmpty()) {
                        binding.recyclerSearchResults.setVisibility(View.VISIBLE);
                        productAdapter.updateData(results);
                    } else {
                        binding.textNoResults.setText("Không tìm thấy kết quả nào");
                        binding.textNoResults.setVisibility(View.VISIBLE);
                    }

                } else {
                    binding.textNoResults.setText("Có lỗi xảy ra, vui lòng thử lại");
                    binding.textNoResults.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Lỗi API tìm kiếm: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                if (getContext() == null || binding == null) return;
                binding.progressBar.setVisibility(View.GONE);

                if (call.isCanceled()) {
                    Log.d(TAG, "Yêu cầu tìm kiếm đã bị hủy.");
                } else {
                    binding.textNoResults.setText("Lỗi kết nối mạng");
                    binding.textNoResults.setVisibility(View.VISIBLE);
                    Log.e(TAG, "Lỗi mạng khi tìm kiếm: ", t);
                }
            }
        });
    }

    @Override
    public void onItemClick(ProductInList productInList) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        startActivity(intent);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (searchRunnable != null) {
            searchHandler.removeCallbacks(searchRunnable);
        }
        if (currentSearchCall != null) {
            currentSearchCall.cancel();
        }
        binding = null;
    }
}
