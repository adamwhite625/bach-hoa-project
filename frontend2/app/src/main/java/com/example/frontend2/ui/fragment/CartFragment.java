package com.example.frontend2.ui.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.R;
import com.example.frontend2.data.model.CartItem;
import com.example.frontend2.data.model.CartResponse;
import com.example.frontend2.data.model.PreviewVoucherRequest;
import com.example.frontend2.data.model.PreviewVoucherResponse;
import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.model.ShippingAddressResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.model.ValidateVoucherResponse;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentCartBinding;
import com.example.frontend2.ui.adapter.CartAdapter;
import com.example.frontend2.ui.main.CartSharedViewModel;
import com.example.frontend2.ui.main.OnCartItemInteractionListener;
import com.example.frontend2.ui.main.ShippingAddressActivity;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.text.Annotation;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Converter;
import retrofit2.Response;

public class CartFragment extends Fragment implements OnCartItemInteractionListener, VoucherBottomSheetFragment.VoucherApplyListener {

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private ApiService apiService;
    private CartSharedViewModel sharedViewModel;

    private CartResponse mCartData;
    private PreviewVoucherResponse mDiscountData;

    private ValidateVoucherResponse mValidateData;
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
        fetchAllCartScreenData(true);
    }

    private void validateAndPreviewVoucher(String voucherCode, boolean isAutoUpdate) {
        if (getContext() == null) {
            showLoading(false);
            return;
        }

        if (voucherCode == null || voucherCode.trim().isEmpty()) {
            mDiscountData = null;
            if(mCartData != null) updateCartUI(mCartData.getItems());
            showLoading(false);
            return;
        }

        if (mCartData == null || mCartData.getItems() == null || mCartData.getItems().isEmpty()) {
            if (!isAutoUpdate) {
                Toast.makeText(getContext(), "Giỏ hàng rỗng, không thể áp dụng voucher.", Toast.LENGTH_SHORT).show();
            }
            showLoading(false);
            return;
        }

        if (!isAutoUpdate) {
            Toast.makeText(getContext(), "Đang kiểm tra voucher: " + voucherCode, Toast.LENGTH_SHORT).show();
        }

        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            if (!isAutoUpdate) {
                Toast.makeText(getContext(), "Vui lòng đăng nhập.", Toast.LENGTH_SHORT).show();
            }
            showLoading(false);
            return;
        }

        PreviewVoucherRequest requestBody = new PreviewVoucherRequest(voucherCode, mCartData.getItems(), mCartData.getTotalPrice());

        apiService.previewDiscount("Bearer " + token, requestBody).enqueue(new Callback<PreviewVoucherResponse>() {
            @Override
            public void onResponse(@NonNull Call<PreviewVoucherResponse> call, @NonNull Response<PreviewVoucherResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mDiscountData = response.body();
                    if (!isAutoUpdate) {
                        Toast.makeText(getContext(), "Áp dụng thành công!", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    mDiscountData = null;
                    if (!isAutoUpdate) {
                        String errorMessage = "Voucher không hợp lệ hoặc không đủ điều kiện.";
                        if (response.errorBody() != null) {
                            try {
                                String errorBodyString = response.errorBody().string();
                                Gson gson = new Gson();
                                PreviewVoucherResponse errorResponse = gson.fromJson(errorBodyString, PreviewVoucherResponse.class);
                                if (errorResponse != null && errorResponse.getMessage() != null && !errorResponse.getMessage().isEmpty()) {
                                    errorMessage = errorResponse.getMessage();
                                }
                            } catch (Exception e) {
                                // Ignore
                            }
                        }
                        Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }
                }
                if(mCartData != null) updateCartUI(mCartData.getItems());
                showLoading(false);
            }

            @Override
            public void onFailure(@NonNull Call<PreviewVoucherResponse> call, @NonNull Throwable t) {
                mDiscountData = null;
                if(mCartData != null) updateCartUI(mCartData.getItems());
                if (!isAutoUpdate) {
                    Toast.makeText(getContext(), "Lỗi kết nối khi kiểm tra voucher.", Toast.LENGTH_SHORT).show();
                }
                showLoading(false);
            }
        });
    }


    @Override
    public void onVoucherSelectedForValidation(String voucherCode) {
        validateAndPreviewVoucher(voucherCode, false);
    }


    private void fetchAllCartScreenData(boolean showLoader) {
        if (showLoader) {
            showLoading(true);
        }
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showLoading(false);
            updateUIForEmptyCart();
            return;
        }

        String bearerToken = "Bearer " + token;

        apiService.getCart(bearerToken).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    mCartData = response.body();

                    apiService.getShippingAddress(bearerToken).enqueue(new Callback<ShippingAddressResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                            if (response.isSuccessful() && response.body() != null) {
                                updateShippingAddressUI(response.body().getShippingAddress());
                            } else {
                                updateShippingAddressUI(null);
                            }

                            String currentVoucherCode = (mDiscountData != null) ? mDiscountData.getCode() : null;
                            if (currentVoucherCode != null && !currentVoucherCode.isEmpty()) {
                                validateAndPreviewVoucher(currentVoucherCode, true);
                            } else {
                                mDiscountData = null;
                                if (mCartData != null) {
                                    updateCartUI(mCartData.getItems());
                                }
                                showLoading(false);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                            updateShippingAddressUI(null);
                            if (mCartData != null) {
                                updateCartUI(mCartData.getItems());
                            }
                            showLoading(false);
                        }
                    });
                } else {
                    showLoading(false);
                    updateUIForEmptyCart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                updateUIForEmptyCart();
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        apiService.getShippingAddress(bearerToken).enqueue(new Callback<ShippingAddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    updateShippingAddressUI(response.body().getShippingAddress());
                } else {
                    updateShippingAddressUI(null);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                updateShippingAddressUI(null);
            }
        });
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

        binding.discountSelectionContainer.setOnClickListener(v -> {
            VoucherBottomSheetFragment bottomSheet = new VoucherBottomSheetFragment();
            bottomSheet.setVoucherApplyListener(CartFragment.this);
            bottomSheet.show(getChildFragmentManager(), "VoucherBottomSheetTag");
        });

        binding.paymentMethodContainer.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Mở chức năng đổi phương thức thanh toán", Toast.LENGTH_SHORT).show();
        });

        binding.btnClearAll.setOnClickListener(v -> {
            if (cartAdapter.getCurrentList().isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng đã trống!", Toast.LENGTH_SHORT).show();
            } else {
                showClearCartConfirmationDialog();
            }
        });
    }

    private void showClearCartConfirmationDialog() {
        if(getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("Xóa Tất Cả Sản Phẩm")
                .setMessage("Bạn có chắc chắn muốn xóa tất cả sản phẩm khỏi giỏ hàng không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> clearAllCartItems())
                .setNegativeButton("Hủy", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void updateCartUI(List<CartItem> items) {
        if (getContext() == null || binding == null) return;

        if (items == null || items.isEmpty()) {
            updateUIForEmptyCart();
            return;
        }

        // Bước 1: Cập nhật danh sách cho Adapter
        binding.layoutEmptyCart.setVisibility(View.GONE);
        binding.cartContentLayout.setVisibility(View.VISIBLE);
        cartAdapter.submitList(new ArrayList<>(items));

        // Bước 2: Tính toán và cập nhật lại toàn bộ UI (Logic của updateCombinedUI cũ)
        NumberFormat currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        double subtotal = 0;
        int totalCount = 0;
        for (CartItem item : items) {
            subtotal += item.getProduct().getFinalPrice() * item.getQuantity();
            totalCount += item.getQuantity();
        }

        binding.subtotalPrice.setText(currencyFormatter.format(subtotal));
        if (sharedViewModel != null) {
            sharedViewModel.setCartItemCount(totalCount);
        }

        double finalPrice = subtotal;

        if (mDiscountData != null && mDiscountData.getDiscountAmount() > 0) {
            binding.discountContainer.setVisibility(View.VISIBLE);
            binding.tvDiscountAmount.setText(String.format("-%s", currencyFormatter.format(mDiscountData.getDiscountAmount())));
            binding.tvDiscountSelection.setText(mDiscountData.getCode());
            binding.tvDiscountSelection.setTextColor(ContextCompat.getColor(getContext(), R.color.red));
            finalPrice = mDiscountData.getFinalTotal();
        } else {
            binding.discountContainer.setVisibility(View.GONE);
            binding.tvDiscountSelection.setText("Chọn hoặc nhập mã");
            binding.tvDiscountSelection.setTextColor(ContextCompat.getColor(getContext(), R.color.default_text_color));
        }

        double shippingFee = 0; // Thay đổi nếu có
        binding.extraFee.setText(currencyFormatter.format(shippingFee));
        binding.textTotalPrice.setText(currencyFormatter.format(finalPrice + shippingFee));
    }
    private void clearAllCartItems() {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showLoading(false);
            return;
        }

        apiService.clearCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                showLoading(false);
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "Đã xóa toàn bộ giỏ hàng", Toast.LENGTH_SHORT).show();
                    fetchAllCartScreenData(false);
                } else {
                    String errorMessage = "Xóa giỏ hàng thất bại.";
                    int errorCode = response.code();
                    if (response.errorBody() != null) {
                        try {
                            errorMessage = response.errorBody().string();
                            Log.e("ClearCartAPI", "Error " + errorCode + ": " + errorMessage);
                        } catch (IOException e) {
                            Log.e("ClearCartAPI", "Error parsing error body", e);
                        }
                    } else {
                        Log.e("ClearCartAPI", "Error " + errorCode);
                    }
                    Toast.makeText(getContext(), "Lỗi " + errorCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                showLoading(false);
                Log.e("ClearCartAPI", "Failure: Network or parsing error.", t);
                Toast.makeText(getContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
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
    public void onUpdateQuantity(String productId, int newQuantity) {
        showLoading(true); // Hiển thị loading ngay
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showLoading(false);
            return;
        }

        // API update giỏ hàng
        apiService.updateCartItem("Bearer " + token, productId, new UpdateCartRequest(newQuantity)).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                // Luôn gọi lại để làm mới toàn bộ dữ liệu từ server
                fetchAllCartScreenData(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                Toast.makeText(getContext(), "Lỗi cập nhật giỏ hàng", Toast.LENGTH_SHORT).show();
                fetchAllCartScreenData(false);
            }
        });
    }

    @Override
    public void onRemoveItem(String productId) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa Sản Phẩm")
                .setMessage("Bạn có muốn xóa sản phẩm này khỏi giỏ hàng?")
                .setPositiveButton("Xóa", (dialog, which) -> {
                    showLoading(true);
                    String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
                    if (token == null) {
                        showLoading(false);
                        return;
                    }

                    apiService.removeFromCart("Bearer " + token, productId).enqueue(new Callback<CartResponse>() {
                        @Override
                        public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                            if (response.isSuccessful()) {
                                Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();

                                if (mCartData != null && mCartData.getItems() != null && mCartData.getItems().size() == 1) {
                                    showLoading(false);
                                    updateUIForEmptyCart();
                                } else {
                                    fetchAllCartScreenData(false);
                                }
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                showLoading(false);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                            Toast.makeText(getContext(), "Lỗi kết nối khi xóa", Toast.LENGTH_SHORT).show();
                            showLoading(false);
                        }
                    });
                })
                .setNegativeButton("Hủy", null)
                .show();
    }


    private void updateUIForEmptyCart() {
        if(binding == null) return;
        binding.layoutEmptyCart.setVisibility(View.VISIBLE);
        binding.cartContentLayout.setVisibility(View.GONE);
        if(sharedViewModel != null){
            sharedViewModel.setCartItemCount(0);
        }
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
