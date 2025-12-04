package com.example.frontend2.ui.main;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.util.Pair;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.R;
import com.example.frontend2.ui.adapter.ProductAdapter;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.databinding.ActivityProductListBinding;
import com.example.frontend2.ui.fragment.SearchFragment;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnItemClickListener {

    private static final String TAG = "ProductListActivity";
    private ActivityProductListBinding binding;
    private ProductListViewModel viewModel;

    private ProductAdapter allProductsAdapter;
    private ProductAdapter saleProductsAdapter;
    private boolean isCategoryView = false;

    public static final String KEY_CATEGORY_ID = "CATEGORY_ID";
    public static final String KEY_CATEGORY_NAME = "CATEGORY_NAME";
    public static final String KEY_PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_FEATURED = "FEATURED";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityProductListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(ProductListViewModel.class);

        handleIntent();
        setupRecyclerViews();
        setupClickListeners();
        setupSortListeners();
        setupObservers();
        binding.btnBackSmall.setOnClickListener(v -> onBackPressed());

        if (viewModel.getSortedAllProducts().getValue() == null) {
            if (isCategoryView) {
                viewModel.fetchDataByCategoryId(getIntent().getStringExtra(KEY_CATEGORY_ID));
            } else if (getIntent().hasExtra(KEY_PRODUCT_TYPE)) {
                viewModel.fetchDataByType(getIntent().getStringExtra(KEY_PRODUCT_TYPE));
            }
        }
    }

    private void handleIntent() {
        Intent intent = getIntent();
        if (intent.hasExtra(KEY_CATEGORY_ID)) {
            isCategoryView = true;
            String categoryName = intent.getStringExtra(KEY_CATEGORY_NAME);
            String title = (categoryName != null && !categoryName.isEmpty()) ? categoryName : "Sản phẩm";
            binding.tvProductTitle.setText(title);
        } else if (intent.hasExtra(KEY_PRODUCT_TYPE)) {
            isCategoryView = false;
            String productType = intent.getStringExtra(KEY_PRODUCT_TYPE);
            String title = TYPE_SALE.equals(productType) ? "Khuyến mãi sốc" : "Sản phẩm nổi bật";
            binding.tvProductTitle.setText(title);
        } else {
            Toast.makeText(this, "Dữ liệu không hợp lệ", Toast.LENGTH_SHORT).show();
            Log.e(TAG, "Không có CATEGORY_ID hoặc PRODUCT_TYPE hợp lệ.");
            finish();
        }
    }

    private void setupRecyclerViews() {
        binding.recyclerProducts.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerProducts.setNestedScrollingEnabled(false);
        allProductsAdapter = new ProductAdapter(this, new ArrayList<>(), this);
        binding.recyclerProducts.setAdapter(allProductsAdapter);

        if (isCategoryView) {
            binding.recyclerSaleProducts.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
            saleProductsAdapter = new ProductAdapter(this, new ArrayList<>(), this);
            binding.recyclerSaleProducts.setAdapter(saleProductsAdapter);
        } else {
            binding.layoutSaleSection.setVisibility(View.GONE);
            binding.dividerView.setVisibility(View.GONE);
            binding.tvAllProductsHeader.setVisibility(View.GONE);
        }
    }

    private void setupClickListeners() {
        binding.searchBarLayout.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("ACTION", "OPEN_SEARCH_FRAGMENT");
            intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
            startActivity(intent);
        });
    }

    private void setupSortListeners() {
        binding.chipGroupSort.setOnCheckedChangeListener((group, checkedId) -> {
            if (viewModel.getSortedAllProducts().getValue() == null) return;

            if (checkedId == R.id.chip_sort_price_asc) {
                viewModel.setSortType(SortType.PRICE_ASCENDING);
            } else if (checkedId == R.id.chip_sort_price_desc) {
                viewModel.setSortType(SortType.PRICE_DESCENDING);
            } else {
                viewModel.setSortType(SortType.DEFAULT);
            }
        });
    }

    private void setupObservers() {
        if (isCategoryView) {
            MediatorLiveData<Pair<List<ProductInList>, List<ProductInList>>> mediator = new MediatorLiveData<>();

            LiveData<List<ProductInList>> allProductsLiveData = viewModel.getSortedAllProducts();
            LiveData<List<ProductInList>> saleProductsLiveData = viewModel.getSortedSaleProducts();

            mediator.addSource(allProductsLiveData, allProducts -> {
                List<ProductInList> saleProducts = saleProductsLiveData.getValue();
                mediator.setValue(new Pair<>(allProducts, saleProducts));
            });

            mediator.addSource(saleProductsLiveData, saleProducts -> {
                List<ProductInList> allProducts = allProductsLiveData.getValue();
                mediator.setValue(new Pair<>(allProducts, saleProducts));
            });

            mediator.observe(this, pair -> {
                List<ProductInList> allProducts = pair.first;
                List<ProductInList> saleProducts = pair.second;

                if (allProducts == null || saleProducts == null) {
                    return;
                }

                List<ProductInList> nonSaleProducts = allProducts.stream()
                        .filter(p -> (p.getSale() == null || !p.getSale().isActive()))
                        .collect(Collectors.toList());

                if (!saleProducts.isEmpty()) {
                    binding.layoutSaleSection.setVisibility(View.VISIBLE);
                    binding.dividerView.setVisibility(View.VISIBLE);
                    binding.tvAllProductsHeader.setVisibility(View.VISIBLE);
                    saleProductsAdapter.updateData(saleProducts);
                } else {
                    binding.layoutSaleSection.setVisibility(View.GONE);
                    binding.dividerView.setVisibility(View.GONE);
                }

                allProductsAdapter.updateData(nonSaleProducts);

                if (allProducts.isEmpty()) {
                    Toast.makeText(ProductListActivity.this, "Không có sản phẩm nào", Toast.LENGTH_LONG).show();
                }
            });

        } else {
            viewModel.getSortedAllProducts().observe(this, allProducts -> {
                if (allProducts != null) {
                    allProductsAdapter.updateData(allProducts);
                    if (allProducts.isEmpty()) {
                        Toast.makeText(ProductListActivity.this, "Không có sản phẩm nào", Toast.LENGTH_LONG).show();
                    }
                }
            });
        }

        viewModel.getIsLoading().observe(this, isLoading -> {
            binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null && !error.isEmpty()) {
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onItemClick(ProductInList productInList) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        startActivity(intent);
    }
}
