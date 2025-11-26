package com.example.frontend2.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.model.ShippingAddressResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentCartBinding;
import com.example.frontend2.ui.adapter.CartAdapter;
import com.example.frontend2.ui.main.CartSharedViewModel;
import com.example.frontend2.ui.main.ShippingAddressActivity;
import com.example.frontend2.utils.SharedPrefManager;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements CartAdapter.OnCartItemInteractionListener {

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private ApiService apiService;
    private CartSharedViewModel sharedViewModel;
    private final ExecutorService backgroundExecutor = Executors.newSingleThreadExecutor();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(CartSharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentCartBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupClickListeners();
    }

    @Override
    public void onResume() {
        super.onResume();
        fetchAllCartScreenData();
    }

    private void setupRecyclerView() {
        cartAdapter = new CartAdapter(this);
        binding.recyclerCartItems.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerCartItems.setAdapter(cartAdapter);
        binding.recyclerCartItems.setNestedScrollingEnabled(false);
    }

    private void setupClickListeners() {
        binding.toolbarCart.setNavigationOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        });

        binding.buttonCheckout.setOnClickListener(v -> {
            if (cartAdapter.getCurrentList().isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(getContext(), "Chuyển đến màn hình thanh toán...", Toast.LENGTH_SHORT).show();
            }
        });

        binding.layoutAddress.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), ShippingAddressActivity.class);
            startActivity(intent);
        });
    }

    private void fetchAllCartScreenData() {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            Toast.makeText(getContext(), "Bạn cần đăng nhập để xem giỏ hàng", Toast.LENGTH_SHORT).show();
            showLoading(false);
            showEmptyCartView();
            return;
        }

        String bearerToken = "Bearer " + token;
        AtomicInteger apiCallCounter = new AtomicInteger(2);

        apiService.getCart(bearerToken).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<CartItem> items = response.body().getItems();
                    if (items == null || items.isEmpty()) {
                        showEmptyCartView();
                    } else {
                        showCartContentView(items);
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
                    showEmptyCartView();
                }
                if (apiCallCounter.decrementAndGet() == 0) {
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                showEmptyCartView();
                if (apiCallCounter.decrementAndGet() == 0) {
                    showLoading(false);
                }
            }
        });

        apiService.getShippingAddress(bearerToken).enqueue(new Callback<ShippingAddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    ShippingAddressResponse body = response.body();
                    if (body.hasShippingAddress()) {
                        updateShippingAddressUI(body.getShippingAddress());
                    } else {
                        updateShippingAddressUI(null);
                        if(body.getMessage() != null && !body.getMessage().isEmpty()){
                            Toast.makeText(getContext(), body.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                } else {
                    Toast.makeText(getContext(), "Lỗi tải địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                    updateShippingAddressUI(null);
                }
                if (apiCallCounter.decrementAndGet() == 0) {
                    showLoading(false);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                updateShippingAddressUI(null);
                if (apiCallCounter.decrementAndGet() == 0) {
                    showLoading(false);
                }
            }
        });
    }

    private void showEmptyCartView() {
        if(binding == null) return;
        updateUI(new ArrayList<>());
    }

    private void showCartContentView(List<CartItem> items) {
        if(binding == null) return;
        updateUI(items);
    }

    private void updateShippingAddressUI(ShippingAddress shippingAddress) {
        if (binding == null) return;
        if (shippingAddress != null) {
            String userInfo = shippingAddress.getFullName() + " | " + shippingAddress.getPhone();
            binding.tvUserInfo.setText(userInfo);

            String fullAddress = shippingAddress.getAddress() + ", " +
                    shippingAddress.getAddress() + ", " +
                    shippingAddress.getCity();
            binding.tvAddressDetail.setText(fullAddress);
        } else {
            binding.tvUserInfo.setText("Chưa có thông tin người nhận");
            binding.tvAddressDetail.setText("Vui lòng thêm địa chỉ giao hàng");
        }
    }

    @Override
    public void onIncreaseQuantity(CartItem item) {
        updateCartQuantity(item, item.getQuantity() + 1);
    }

    @Override
    public void onDecreaseQuantity(CartItem item) {
        int newQuantity = item.getQuantity() - 1;
        if (newQuantity < 1) {
            onRemoveItem(item);
        } else {
            updateCartQuantity(item, newQuantity);
        }
    }

    @Override
    public void onRemoveItem(CartItem item) {
        List<CartItem> currentList = new ArrayList<>(cartAdapter.getCurrentList());
        currentList.removeIf(cartItem -> cartItem.get_id().equals(item.get_id()));
        updateUI(currentList);
        syncRemovalWithApi(item.get_id());
    }

    private void updateCartQuantity(CartItem item, int newQuantity) {
        List<CartItem> currentList = new ArrayList<>(cartAdapter.getCurrentList());
        for (int i = 0; i < currentList.size(); i++) {
            if (currentList.get(i).get_id().equals(item.get_id())) {
                CartItem updatedItem = currentList.get(i).withNewQuantity(newQuantity);
                currentList.set(i, updatedItem);
                break;
            }
        }
        updateUI(currentList);
        syncQuantityWithApi(item.get_id(), newQuantity);
    }

    private void syncQuantityWithApi(String itemId, int newQuantity) {
        backgroundExecutor.execute(() -> {
            String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
            if (token == null) return;
            try {
                apiService.updateCartItem("Bearer " + token, itemId, new UpdateCartRequest(newQuantity)).execute();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        });
    }

    private void syncRemovalWithApi(String itemId) {
        backgroundExecutor.execute(() -> {
            String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
            if (token == null) return;
            try {
                apiService.removeFromCart("Bearer " + token, itemId).execute();
            } catch (Exception e) {
                // e.printStackTrace();
            }
        });
    }

    private void updateUI(List<CartItem> items) {
        if(binding == null) return;
        cartAdapter.submitList(new ArrayList<>(items));

        int totalCount = 0;
        for (CartItem item : items) {
            totalCount += item.getQuantity();
        }
        if(sharedViewModel != null){
            sharedViewModel.setCartItemCount(totalCount);
        }

        boolean isCartEmpty = items.isEmpty();
        binding.layoutEmptyCart.setVisibility(isCartEmpty ? View.VISIBLE : View.GONE);
        binding.cartContentLayout.setVisibility(isCartEmpty ? View.GONE : View.VISIBLE);

        if (!isCartEmpty) {
            calculateAndDisplayTotal(items);
        }
    }

    private void calculateAndDisplayTotal(List<CartItem> cartItems) {
        double total = 0;
        for (CartItem item : cartItems) {
            if (item.getProduct() != null) {
                total += item.getProduct().getFinalPrice() * item.getQuantity();
            }
        }
        NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));
        binding.textTotalPrice.setText(currencyFormat.format(total));
//        binding.textTotalPriceSummary.setText(currencyFormat.format(total));
        binding.subtotalPrice.setText(currencyFormat.format(total));
    }

    private void showLoading(boolean isLoading) {
        if (binding == null) return;
        binding.progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        if (isLoading) {
            binding.cartContentLayout.setVisibility(View.GONE);
            binding.layoutEmptyCart.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
