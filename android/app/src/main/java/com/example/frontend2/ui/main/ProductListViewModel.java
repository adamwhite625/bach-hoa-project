package com.example.frontend2.ui.main;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListViewModel extends ViewModel {

    private static final String TAG = "ProductListViewModel";
    private final ApiService apiService = ApiClient.getRetrofitInstance().create(ApiService.class);

    private List<ProductInList> originalProducts = new ArrayList<>();

    // --- PHẦN 1: SỬA LẠI KHAI BÁO LIVEDATA ---
    // Thay thế một LiveData duy nhất bằng hai LiveData
    private final MutableLiveData<List<ProductInList>> sortedAllProducts = new MutableLiveData<>();
    private final MutableLiveData<List<ProductInList>> sortedSaleProducts = new MutableLiveData<>();
    private final MutableLiveData<SortType> currentSortType = new MutableLiveData<>(SortType.DEFAULT);
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    // Cung cấp getter cho cả hai LiveData mới
    public LiveData<List<ProductInList>> getSortedAllProducts() { return sortedAllProducts; }
    public LiveData<List<ProductInList>> getSortedSaleProducts() { return sortedSaleProducts; }
    public LiveData<Boolean> getIsLoading() { return isLoading; }
    public LiveData<String> getErrorMessage() { return errorMessage; }


    public void fetchDataByType(String type) {
        if (isLoading.getValue() != null && isLoading.getValue()) return;
        isLoading.setValue(true);

        apiService.getProducts().enqueue(new Callback<List<ProductInList>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductInList>> call, @NonNull Response<List<ProductInList>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductInList> allProducts = response.body();
                    List<ProductInList> filteredProducts = new ArrayList<>();

                    if (type.equals(ProductListActivity.TYPE_SALE)) {
                        filteredProducts = allProducts.stream()
                                .filter(p -> p.getSale() != null && p.getSale().isActive())
                                .collect(Collectors.toList());
                    } else if (type.equals(ProductListActivity.TYPE_FEATURED)) {
                        filteredProducts = allProducts.stream()
                                .filter(p -> p.getSale() == null || !p.getSale().isActive())
                                .collect(Collectors.toList());
                    }
                    originalProducts = filteredProducts;
                    // --- PHẦN 3: SỬA LẠI LỜI GỌI HÀM ---
                    applySortingAndFiltering(); // Gọi hàm mới
                } else {
                    errorMessage.postValue("Lỗi " + response.code());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductInList>> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    public void fetchDataByCategoryId(String categoryId) {
        if (isLoading.getValue() != null && isLoading.getValue()) return;
        isLoading.setValue(true);

        apiService.getProductsByCategoryId(categoryId).enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(@NonNull Call<JsonElement> call, @NonNull Response<JsonElement> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ProductInList> productList = parseProductsFromJson(response.body());
                    originalProducts = productList;
                    // --- PHẦN 3: SỬA LẠI LỜI GỌI HÀM ---
                    applySortingAndFiltering(); // Gọi hàm mới
                } else {
                    errorMessage.postValue("Lỗi " + response.code());
                }
                isLoading.setValue(false);
            }

            @Override
            public void onFailure(@NonNull Call<JsonElement> call, @NonNull Throwable t) {
                errorMessage.postValue("Lỗi mạng: " + t.getMessage());
                isLoading.setValue(false);
            }
        });
    }

    private List<ProductInList> parseProductsFromJson(JsonElement responseBody) {
        List<ProductInList> productList = new ArrayList<>();
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
            errorMessage.postValue("Lỗi định dạng dữ liệu trả về.");
        }
        return productList != null ? productList : new ArrayList<>();
    }

    public void setSortType(SortType sortType) {
        if (sortType == currentSortType.getValue()) {
            return;
        }
        currentSortType.setValue(sortType);
        // --- PHẦN 3: SỬA LẠI LỜI GỌI HÀM ---
        applySortingAndFiltering(); // Gọi hàm mới
    }

    // --- PHẦN 2: THAY THẾ HÀM applySorting() BẰNG HÀM MỚI ---
    /**
     * Sắp xếp danh sách tổng, sau đó lọc ra danh sách sale và cập nhật cho cả 2 LiveData.
     */
    private void applySortingAndFiltering() {
        SortType sortType = currentSortType.getValue();
        if (sortType == null) {
            sortType = SortType.DEFAULT;
        }

        List<ProductInList> sortedList = new ArrayList<>(originalProducts);

        // 1. Sắp xếp danh sách tổng trước
        switch (sortType) {
            case PRICE_ASCENDING:
                Collections.sort(sortedList, Comparator.comparingDouble(ProductInList::getFinalPrice));
                break;
            case PRICE_DESCENDING:
                Collections.sort(sortedList, Comparator.comparingDouble(ProductInList::getFinalPrice).reversed());
                break;
            case DEFAULT:
                // Không làm gì, giữ nguyên thứ tự gốc
                break;
        }

        // 2. Từ danh sách tổng đã sắp xếp, lọc ra danh sách sale
        List<ProductInList> saleList = sortedList.stream()
                .filter(p -> p.getSale() != null && p.getSale().isActive())
                .collect(Collectors.toList());

        // 3. Cập nhật cho cả hai LiveData
        sortedAllProducts.postValue(sortedList);
        sortedSaleProducts.postValue(saleList);
    }
}
