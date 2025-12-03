package com.example.frontend2.ui.fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.example.frontend2.data.model.Order;
import com.example.frontend2.data.model.OrderItem;
import com.example.frontend2.data.model.OrderRequest;
import com.example.frontend2.data.model.PaymentResponse;
import com.example.frontend2.data.model.PreviewVoucherRequest;
import com.example.frontend2.data.model.PreviewVoucherResponse;
import com.example.frontend2.data.model.ShippingAddress;
import com.example.frontend2.data.model.ShippingAddressResponse;
import com.example.frontend2.data.model.UpdateCartRequest;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentCartBinding;
import com.example.frontend2.ui.adapter.CartAdapter;
import com.example.frontend2.ui.main.CartSharedViewModel;
import com.example.frontend2.ui.main.OnCartItemInteractionListener;
import com.example.frontend2.ui.main.OrderSuccessActivity;
import com.example.frontend2.ui.main.ShippingAddressActivity;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartFragment extends Fragment implements OnCartItemInteractionListener, VoucherBottomSheetFragment.VoucherApplyListener, PaymentMethodBottomSheet.PaymentMethodListener {

    private static final String TAG = "CartFragment";

    private FragmentCartBinding binding;
    private CartAdapter cartAdapter;
    private ApiService apiService;
    private CartSharedViewModel sharedViewModel;

    private CartResponse mCartData;
    private PreviewVoucherResponse mDiscountData;
    private ShippingAddress mShippingAddress;

    private String selectedPaymentMethod = "COD";

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
        updatePaymentMethodUI(selectedPaymentMethod);
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
                if (!isAdded()) return;
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
                                Log.e(TAG, "Error parsing voucher error", e);
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
                if (!isAdded()) return;
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
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    mCartData = response.body();
                    if (mCartData.getItems() == null || mCartData.getItems().isEmpty()) {
                        showLoading(false);
                        updateUIForEmptyCart();
                        return;
                    }
                    fetchShippingAddress(bearerToken);
                } else {
                    showLoading(false);
                    updateUIForEmptyCart();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                updateUIForEmptyCart();
                Toast.makeText(getContext(), "Lỗi kết nối khi tải giỏ hàng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchShippingAddress(String bearerToken) {
        apiService.getShippingAddress(bearerToken).enqueue(new Callback<ShippingAddressResponse>() {
            @Override
            public void onResponse(@NonNull Call<ShippingAddressResponse> call, @NonNull Response<ShippingAddressResponse> response) {
                if (!isAdded()) return;
                if (response.isSuccessful() && response.body() != null) {
                    mShippingAddress = response.body().getShippingAddress();
                } else {
                    mShippingAddress = null;
                }
                updateShippingAddressUI(mShippingAddress);
                checkVoucherAndFinalizeUI(true);
            }

            @Override
            public void onFailure(@NonNull Call<ShippingAddressResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                mShippingAddress = null;
                updateShippingAddressUI(null);
                checkVoucherAndFinalizeUI(true);
            }
        });
    }

    private void checkVoucherAndFinalizeUI(boolean isAutoUpdate) {
        String currentVoucherCode = (mDiscountData != null) ? mDiscountData.getCode() : null;
        if (currentVoucherCode != null && !currentVoucherCode.isEmpty()) {
            validateAndPreviewVoucher(currentVoucherCode, isAutoUpdate);
        } else {
            mDiscountData = null;
            updateCartUI(mCartData.getItems());
            showLoading(false);
        }
        updatePaymentMethodUI(selectedPaymentMethod);
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
            if (mCartData == null || mCartData.getItems() == null || mCartData.getItems().isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng của bạn đang trống!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (mShippingAddress == null) {
                Toast.makeText(getContext(), "Vui lòng chọn địa chỉ giao hàng", Toast.LENGTH_SHORT).show();
                return;
            }

            createOrder(selectedPaymentMethod);
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
            showPaymentMethodBottomSheet();
        });

        binding.btnClearAll.setOnClickListener(v -> {
            if (cartAdapter.getCurrentList().isEmpty()) {
                Toast.makeText(getContext(), "Giỏ hàng đã trống!", Toast.LENGTH_SHORT).show();
            } else {
                showClearCartConfirmationDialog();
            }
        });
    }

    private void showPaymentMethodBottomSheet() {
        double finalTotal = (mCartData != null && mDiscountData != null && mDiscountData.getDiscountAmount() > 0)
                ? mDiscountData.getFinalTotal()
                : (mCartData != null ? mCartData.getTotalPrice() : 0);

        PaymentMethodBottomSheet bottomSheet = PaymentMethodBottomSheet.newInstance(finalTotal);
        bottomSheet.setCurrentPaymentMethod(selectedPaymentMethod);
        bottomSheet.setPaymentMethodListener(this);
        bottomSheet.show(getChildFragmentManager(), "PaymentMethodBottomSheet");
    }

    @Override
    public void onPaymentMethodConfirmed(String paymentMethod) {
        selectedPaymentMethod = paymentMethod;
        updatePaymentMethodUI(paymentMethod);
    }

    private void updatePaymentMethodUI(String paymentMethod) {
        if (binding == null || getContext() == null) return;

        if ("ZaloPay".equals(paymentMethod)) {
            binding.tvPaymentMethod.setText("Thanh toán qua ZaloPay");
            binding.ivPaymentIcon.setImageResource(R.drawable.ic_zalopay);
        } else {
            binding.tvPaymentMethod.setText("Thanh toán khi nhận hàng (COD)");
            binding.ivPaymentIcon.setImageResource(R.drawable.ic_cash);
        }
    }

    private void createOrder(String paymentMethod) {
        List<OrderItem> orderItems = new ArrayList<>();
        for (CartItem cartItem : mCartData.getItems()) {
            orderItems.add(new OrderItem(
                    cartItem.getProduct().getId(),
                    cartItem.getProduct().getName(),
                    cartItem.getQuantity(),
                    cartItem.getProduct().getFinalPrice(),
                    cartItem.getProduct().getImages().get(0)
            ));
        }

        double itemsPrice = mCartData.getTotalPrice();
        double taxPrice = 0;
        double shippingPrice = 0;
        double finalTotalPrice = itemsPrice + taxPrice + shippingPrice;

        if (mDiscountData != null && mDiscountData.getDiscountAmount() > 0) {
            finalTotalPrice = mDiscountData.getFinalTotal();
        }

        OrderRequest orderRequest = new OrderRequest(
                orderItems,
                mShippingAddress,
                paymentMethod,
                itemsPrice,
                taxPrice,
                shippingPrice,
                finalTotalPrice
        );

        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showLoading(false);
            Toast.makeText(getContext(), "Vui lòng đăng nhập lại", Toast.LENGTH_SHORT).show();
            return;
        }

        final double calculatedFinalPrice = (mDiscountData != null && mDiscountData.getDiscountAmount() > 0)
                ? mDiscountData.getFinalTotal()
                : itemsPrice + taxPrice + shippingPrice;

        Log.d(TAG, "🔄 Creating order with payment method: " + paymentMethod);

        apiService.createOrder("Bearer " + token, orderRequest).enqueue(new Callback<Order>() {
            @Override
            public void onResponse(@NonNull Call<Order> call, @NonNull Response<Order> response) {
                if (!isAdded()) return;

                if (response.isSuccessful() && response.body() != null) {
                    Order order = response.body();
                    Log.d(TAG, "✅ Order created successfully: " + order.getId());

                    if ("ZaloPay".equals(paymentMethod)) {
                        createZaloPayPayment(order.getId(), (int) calculatedFinalPrice, token);
                    } else {
                        clearCartAndNavigate(token, order.getId());
                    }
                } else {
                    showLoading(false);
                    String errorMessage = "Đặt hàng thất bại. Vui lòng thử lại.";
                    try {
                        if (response.errorBody() != null) {
                            errorMessage = response.errorBody().string();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing order error", e);
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Order> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                Log.e(TAG, "❌ Order creation failed", t);
                Toast.makeText(getContext(), "Lỗi kết nối khi đặt hàng: " + t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void createZaloPayPayment(String orderId, int amount, String token) {
        JsonObject requestBody = new JsonObject();
        requestBody.addProperty("amount", amount);
        requestBody.addProperty("orderInfo", "Thanh toan don hang");
        requestBody.addProperty("orderId", orderId);

        Log.d(TAG, "🔄 Creating ZaloPay payment - OrderID: " + orderId + ", Amount: " + amount);

        apiService.createZaloPayPayment("Bearer " + token, requestBody)
                .enqueue(new Callback<PaymentResponse>() {
                    @Override
                    public void onResponse(@NonNull Call<PaymentResponse> call,
                                           @NonNull Response<PaymentResponse> response) {
                        if (!isAdded()) return;
                        showLoading(false);

                        if (response.isSuccessful() && response.body() != null) {
                            PaymentResponse paymentResponse = response.body();
                            String paymentUrl = paymentResponse.getPaymentUrl();
                            String appTransId = paymentResponse.getAppTransId();

                            Log.d(TAG, "✅ Payment created - app_trans_id: " + appTransId);

                            if (paymentUrl != null && !paymentUrl.isEmpty()) {
                                if (getContext() != null) {
                                    SharedPreferences prefs = getContext().getSharedPreferences("payment",
                                            getContext().MODE_PRIVATE);
                                    prefs.edit()
                                            .putString("current_app_trans_id", appTransId)
                                            .putString("current_order_id", orderId)
                                            .apply();
                                }

                                try {
                                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(paymentUrl));
                                    startActivity(intent);
                                    Toast.makeText(getContext(), "Vui lòng hoàn tất thanh toán trên ZaloPay", Toast.LENGTH_LONG).show();
                                } catch (Exception e) {
                                    Log.e(TAG, "❌ Error opening ZaloPay URL", e);
                                    showPaymentFailedDialog("Không thể mở ứng dụng ZaloPay.\n\nĐơn hàng đã bị hủy do lỗi thanh toán.");
                                }
                            } else {
                                showPaymentFailedDialog("Không nhận được URL thanh toán.\n\nĐơn hàng đã bị hủy do lỗi thanh toán.");
                            }
                        } else {
                            String errorMessage = "Lỗi tạo thanh toán ZaloPay";

                            try {
                                if (response.errorBody() != null) {
                                    String errorBody = response.errorBody().string();
                                    Log.e(TAG, "❌ Payment error: " + errorBody);

                                    Gson gson = new Gson();
                                    JsonObject errorJson = gson.fromJson(errorBody, JsonObject.class);
                                    if (errorJson.has("message")) {
                                        errorMessage = errorJson.get("message").getAsString();
                                    }
                                }
                            } catch (Exception e) {
                                Log.e(TAG, "Error parsing error response", e);
                            }

                            showPaymentFailedDialog(errorMessage + "\n\nĐơn hàng đã bị hủy do lỗi thanh toán.");
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<PaymentResponse> call, @NonNull Throwable t) {
                        if (!isAdded()) return;
                        showLoading(false);

                        Log.e(TAG, "❌ Network error during payment creation", t);

                        showPaymentFailedDialog(
                                "Lỗi kết nối đến ZaloPay.\n\n" +
                                        "Đơn hàng đã bị hủy. Vui lòng kiểm tra kết nối mạng và thử lại."
                        );
                    }
                });
    }

    private void showPaymentFailedDialog(String message) {
        if (getContext() == null) return;

        new AlertDialog.Builder(getContext())
                .setTitle("❌ Thanh toán thất bại")
                .setMessage(message)
                .setPositiveButton("Thử lại", (dialog, which) -> {
                    fetchAllCartScreenData(true);
                })
                .setNegativeButton("Đóng", (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .show();
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

        binding.layoutEmptyCart.setVisibility(View.GONE);
        binding.cartContentLayout.setVisibility(View.VISIBLE);
        cartAdapter.submitList(new ArrayList<>(items));

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

        double shippingFee = 0;
        binding.extraFee.setText(currencyFormatter.format(shippingFee));
        binding.textTotalPrice.setText(currencyFormatter.format(finalPrice + shippingFee));
    }

    private void clearCartAndNavigate(String token, String orderId) {
        if (getContext() == null || apiService == null) {
            showLoading(false);
            return;
        }

        apiService.clearCart("Bearer " + token).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (!isAdded()) return;
                showLoading(false);
                Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                navigateToOrderSuccess(orderId);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                showLoading(false);
                Toast.makeText(getContext(), "Đặt hàng thành công!", Toast.LENGTH_LONG).show();
                navigateToOrderSuccess(orderId);
            }
        });
    }

    private void navigateToOrderSuccess(String orderId) {
        if (!isAdded()) return;
        Intent intent = new Intent(getActivity(), OrderSuccessActivity.class);
        intent.putExtra("ORDER_ID", orderId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
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
                if (!isAdded()) return;
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
                    }
                    Toast.makeText(getContext(), "Lỗi " + errorCode + ": " + errorMessage, Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
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

            String fullAddress = shippingAddress.getAddress() + ", " + shippingAddress.getCity();
            binding.tvAddressDetail.setText(fullAddress);
        } else {
            binding.tvUserInfo.setText("Chưa có thông tin người nhận");
            binding.tvAddressDetail.setText("Vui lòng thêm địa chỉ giao hàng");
        }
    }

    @Override
    public void onUpdateQuantity(String productId, int newQuantity) {
        showLoading(true);
        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();
        if (token == null) {
            showLoading(false);
            return;
        }

        apiService.updateCartItem("Bearer " + token, productId, new UpdateCartRequest(newQuantity)).enqueue(new Callback<CartResponse>() {
            @Override
            public void onResponse(@NonNull Call<CartResponse> call, @NonNull Response<CartResponse> response) {
                if (!isAdded()) return;
                fetchAllCartScreenData(false);
            }

            @Override
            public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                if (!isAdded()) return;
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
                            if (!isAdded()) return;

                            if (response.isSuccessful() && response.body() != null) {
                                Toast.makeText(getContext(), "Đã xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                mCartData = response.body();
                                checkVoucherAndFinalizeUI(true);
                            } else {
                                Toast.makeText(getContext(), "Lỗi khi xóa sản phẩm", Toast.LENGTH_SHORT).show();
                                showLoading(false);
                            }
                        }

                        @Override
                        public void onFailure(@NonNull Call<CartResponse> call, @NonNull Throwable t) {
                            if (!isAdded()) return;
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