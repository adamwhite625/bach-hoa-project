package com.example.frontend2.ui.fragment;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;

import com.example.frontend2.R;
import com.example.frontend2.adapter.ChatAdapter;
import com.example.frontend2.data.model.Category;
import com.example.frontend2.data.model.ChatMessage;
import com.example.frontend2.data.model.ChatMessageRequest;
import com.example.frontend2.data.model.ChatMessageResponse;
import com.example.frontend2.data.model.ProductInList;
import com.example.frontend2.data.model.UnreadCountResponse;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentHomeBinding;
import com.example.frontend2.ui.adapter.CategoryAdapter;
import com.example.frontend2.ui.adapter.FlashSaleAdapter;
import com.example.frontend2.ui.adapter.ProductAdapter;
import com.example.frontend2.ui.adapter.SliderAdapter;
import com.example.frontend2.ui.main.ProductDetailActivity;
import com.example.frontend2.ui.main.ProductListActivity;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.stream.Collectors;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment implements ProductAdapter.OnItemClickListener, CategoryAdapter.OnCategoryClickListener {

    private static final String TAG = "HomeFragment";
    private static final String CHATBOT_RESPONSE_TAG = "CHATBOT_RESPONSE";
    public static final String KEY_PRODUCT_TYPE = "PRODUCT_TYPE";
    public static final String TYPE_SALE = "SALE";
    public static final String TYPE_FEATURED = "FEATURED";
    private static final int MAX_PRODUCTS_HOME = 10;

    private FragmentHomeBinding binding;
    private ApiService apiService; // Dành cho backend chính
    private ApiService chatbotApiService; // Dành cho backend chatbot

    private CategoryAdapter categoryAdapter;
    private ProductAdapter productAdapter;
    private FlashSaleAdapter flashSaleAdapter;
    private SliderAdapter sliderAdapter;
    private final Handler sliderHandler = new Handler(Looper.getMainLooper());
    private Timer sliderTimer;

    private String authToken;

    // --- Biến cho Chatbot ---
    private ChatAdapter chatAdapter;
    private List<ChatMessage> messageList;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        chatbotApiService = ApiClient.getChatbotRetrofitInstance().create(ApiService.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        if (getContext() != null) {
            authToken = "Bearer " + SharedPrefManager.getInstance(getContext()).getAuthToken();
        }

        setupBannerSlider();
        setupRecyclerViews();
        setupClickListeners();
        fetchCategories();
        fetchProducts();

        binding.fabChatbot.setOnClickListener(v -> showChatbotDialog());

        binding.searchBarLayout.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new SearchFragment())
                        .addToBackStack(null)
                        .commit();
            }
        });
    }


    private void checkUnreadNotifications() {
        if (binding == null || getContext() == null) {
            return;
        }

        String token = SharedPrefManager.getInstance(getContext()).getAuthToken();

        if (token == null || token.isEmpty()) {
            binding.notificationBadge.setVisibility(View.GONE);
            return;
        }

        String authHeader = "Bearer " + token;

        apiService.getUnreadNotificationCount(authHeader).enqueue(new Callback<UnreadCountResponse>() {
            @Override
            public void onResponse(@NonNull Call<UnreadCountResponse> call, @NonNull Response<UnreadCountResponse> response) {
                if (!isAdded() || binding == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    boolean hasUnread = response.body().hasUnread();
                    binding.notificationBadge.setVisibility(hasUnread ? View.VISIBLE : View.GONE);
                } else {
                    binding.notificationBadge.setVisibility(View.GONE);
                    Log.e("CheckNotifications", "API call successful but returned an error: " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<UnreadCountResponse> call, @NonNull Throwable t) {
                if (!isAdded() || binding == null) {
                    return;
                }
                binding.notificationBadge.setVisibility(View.GONE);
                Log.e("CheckNotifications", "API call failed due to network error.", t);
            }
        });
    }

    private void showChatbotDialog() {
        final Dialog dialog = new Dialog(requireContext(), android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_chatbot);

        ImageButton closeButton = dialog.findViewById(R.id.button_close);
        RecyclerView chatRecyclerView = dialog.findViewById(R.id.recycler_view_chat);
        EditText messageEditText = dialog.findViewById(R.id.edit_text_chatbox);
        ImageButton sendButton = dialog.findViewById(R.id.button_chatbox_send);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        messageList = new ArrayList<>();
        chatAdapter = new ChatAdapter(messageList);

        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        chatRecyclerView.setLayoutManager(layoutManager);
        chatRecyclerView.setAdapter(chatAdapter);

        messageList.add(new ChatMessage("Xin chào! Tôi có thể giúp gì cho bạn?", false));
        chatAdapter.notifyItemInserted(messageList.size() - 1);

        sendButton.setOnClickListener(v -> {
            String userMessage = messageEditText.getText().toString().trim();
            if (!userMessage.isEmpty()) {
                messageList.add(new ChatMessage(userMessage, true));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
                messageEditText.setText("");
                sendMessageToBot(userMessage, chatRecyclerView);
            }
        });

        dialog.show();
    }

    private void sendMessageToBot(String message, RecyclerView chatRecyclerView) {
        final int typingMessagePosition = messageList.size();
        messageList.add(new ChatMessage("Bot đang trả lời...", false));
        chatAdapter.notifyItemInserted(typingMessagePosition);
        chatRecyclerView.scrollToPosition(typingMessagePosition);

        chatbotApiService.sendMessageToChatbot(new ChatMessageRequest(message)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                messageList.remove(typingMessagePosition);
                chatAdapter.notifyItemRemoved(typingMessagePosition);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String rawJsonResponse = response.body().string();
                        Log.d(CHATBOT_RESPONSE_TAG, "Raw JSON: " + rawJsonResponse);

                        Gson gson = new Gson();
                        ChatMessageResponse chatResponse = gson.fromJson(rawJsonResponse, ChatMessageResponse.class);

                        if (chatResponse != null && chatResponse.getResponse() != null) {
                            messageList.add(new ChatMessage(chatResponse.getResponse(), false));
                        } else {
                            Log.e(CHATBOT_RESPONSE_TAG, "Parsed response or its content is null.");
                            messageList.add(new ChatMessage("Lỗi xử lý phản hồi từ bot.", false));
                        }
                    } catch (IOException | JsonSyntaxException e) {
                        Log.e(CHATBOT_RESPONSE_TAG, "Error parsing JSON", e);
                        messageList.add(new ChatMessage("Lỗi đọc phản hồi từ bot.", false));
                    }
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        Log.e(CHATBOT_RESPONSE_TAG, "API Error: " + response.code() + " - " + errorBody);
                        messageList.add(new ChatMessage("Bot đang gặp sự cố. Vui lòng thử lại sau.", false));
                    } catch (IOException e) {
                        Log.e(CHATBOT_RESPONSE_TAG, "Error reading error body", e);
                    }
                }
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e(CHATBOT_RESPONSE_TAG, "API Call Failed", t);
                messageList.remove(typingMessagePosition);
                chatAdapter.notifyItemRemoved(typingMessagePosition);
                messageList.add(new ChatMessage("Lỗi kết nối tới chatbot, vui lòng thử lại sau.", false));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                chatRecyclerView.scrollToPosition(messageList.size() - 1);
            }
        });
    }

    private void setupRecyclerViews() {
        if (binding == null) return;
        // === Category RecyclerView ===
        binding.recyclerCategory.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerCategory.setAdapter(categoryAdapter);

        // === Product RecyclerView (Sản phẩm thường) ===
        binding.recyclerProduct.setLayoutManager(new GridLayoutManager(getContext(), 2));
        productAdapter = new ProductAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerProduct.setAdapter(productAdapter);
        binding.recyclerProduct.setNestedScrollingEnabled(false);

        // === Flash Sale RecyclerView (Sản phẩm khuyến mãi) ===
        binding.recyclerFlashSale.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
        flashSaleAdapter = new FlashSaleAdapter(getContext(), new ArrayList<>(), this);
        binding.recyclerFlashSale.setAdapter(flashSaleAdapter);
    }

    private void setupBannerSlider() {
        if (binding == null) return;
        List<Integer> imageList = Arrays.asList(R.drawable.slide1, R.drawable.slide2, R.drawable.slide3, R.drawable.slide4);
        sliderAdapter = new SliderAdapter(imageList);
        if (binding == null) return;
        binding.viewPagerBanner.setAdapter(sliderAdapter);
        binding.viewPagerBanner.setClipToPadding(false);
        binding.viewPagerBanner.setClipChildren(false);
        binding.viewPagerBanner.setOffscreenPageLimit(3);
        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        binding.viewPagerBanner.setPageTransformer(compositePageTransformer);
    }

    private void fetchCategories() {
        apiService.getCategories().enqueue(new Callback<List<Category>>() {
            @Override
            public void onResponse(@NonNull Call<List<Category>> call, @NonNull Response<List<Category>> response) {
                if (isAdded() && response.isSuccessful() && response.body() != null && categoryAdapter != null) {
                    categoryAdapter.updateData(response.body());
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<Category>> call, @NonNull Throwable t) {
                if(isAdded()) Log.e(TAG, "API Failure: fetchCategories", t);
            }
        });
    }

    private void fetchProducts() {
        if (apiService == null) {
            Log.e(TAG, "Lỗi nghiêm trọng: ApiService chưa được khởi tạo.");
            return;
        }

        apiService.getProducts().enqueue(new Callback<List<ProductInList>>() {
            @Override
            public void onResponse(@NonNull Call<List<ProductInList>> call, @NonNull Response<List<ProductInList>> response) {
                if (!isAdded() || binding == null) {
                    Log.w(TAG, "onResponse được gọi nhưng Fragment/View không còn tồn tại. Bỏ qua cập nhật UI.");
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    List<ProductInList> allProducts = response.body();
                    Log.d(TAG, "Tải thành công " + allProducts.size() + " sản phẩm từ API.");

                    List<ProductInList> saleProducts = allProducts.stream()
                            .filter(p -> p.getSale() != null && p.getSale().isActive())
                            .collect(Collectors.toList());

                    List<ProductInList> normalProducts = allProducts.stream()
                            .filter(p -> p.getSale() == null || !p.getSale().isActive())
                            .collect(Collectors.toList());

                    Log.d(TAG, "Đã lọc ra: " + saleProducts.size() + " sản phẩm khuyến mãi và " + normalProducts.size() + " sản phẩm thường.");

                    if (saleProducts.isEmpty()) {
                        binding.flashSaleSectionContainer.setVisibility(View.GONE);
                    } else {
                        binding.flashSaleSectionContainer.setVisibility(View.VISIBLE);
                    }

                    if (flashSaleAdapter != null) {
                        List<ProductInList> limitedSaleProducts = saleProducts.stream().limit(MAX_PRODUCTS_HOME).collect(Collectors.toList());
                        flashSaleAdapter.updateData(limitedSaleProducts);
                    }

                    if (productAdapter != null) {
                        List<ProductInList> limitedProducts = normalProducts.stream().limit(MAX_PRODUCTS_HOME).collect(Collectors.toList());
                        productAdapter.updateData(limitedProducts);
                    }

                } else {
                    Log.e(TAG, "Lỗi khi lấy sản phẩm, mã lỗi HTTP: " + response.code());
                    Toast.makeText(getContext(), "Lỗi tải dữ liệu: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<ProductInList>> call, @NonNull Throwable t) {
                if (!isAdded()) {
                    Log.w(TAG, "onFailure được gọi nhưng Fragment không còn tồn tại.");
                    return;
                }
                Log.e(TAG, "Lỗi kết nối mạng khi gọi API sản phẩm: ", t);
                Toast.makeText(getContext(), "Không thể kết nối tới máy chủ, vui lòng thử lại.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        if (binding == null) {
            return;
        }

        binding.notificationsLayout.setOnClickListener(v -> {

            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, new NotificationsFragment()) // Sửa lại ID nếu cần
                        .addToBackStack(null)
                        .commit();
            }
        });

        binding.btnViewAllSale.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Xem tất cả Khuyến mãi' được nhấn.");
            navigateToProductList(TYPE_SALE);
        });

        binding.btnViewAllFeatured.setOnClickListener(v -> {
            Log.d(TAG, "Nút 'Xem tất cả Sản phẩm nổi bật' được nhấn.");
            navigateToProductList(TYPE_FEATURED);
        });
    }


    private void navigateToProductList(String productType) {
        if (getActivity() == null) {
            Log.e(TAG, "Không thể mở ProductListActivity vì getActivity() là null.");
            return;
        }
        Intent intent = new Intent(getActivity(), ProductListActivity.class);
        intent.putExtra(KEY_PRODUCT_TYPE, productType);
        startActivity(intent);
    }

    @Override
    public void onItemClick(ProductInList productInList) {
        if (getContext() == null) return;
        Intent intent = new Intent(getContext(), ProductDetailActivity.class);
        intent.putExtra("product_id", productInList.getId());
        startActivity(intent);
    }

    @Override
    public void onCategoryClick(Category category) {
        if (getActivity() == null) return;
        Intent intent = new Intent(getActivity(), ProductListActivity.class);
        intent.putExtra("CATEGORY_ID", category.getId());
        intent.putExtra("CATEGORY_NAME", category.getName());
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        startAutoSlider();
        checkUnreadNotifications();
    }

    @Override
    public void onPause() {
        super.onPause();
        stopAutoSlider();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        stopAutoSlider();
        binding = null;
    }

    private void startAutoSlider() {
        if (sliderTimer != null) return;
        sliderTimer = new Timer();
        sliderTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                sliderHandler.post(() -> {
                    if (binding == null || binding.viewPagerBanner == null || binding.viewPagerBanner.getAdapter() == null) return;
                    int count = binding.viewPagerBanner.getAdapter().getItemCount();
                    if (count == 0) return;
                    int currentItem = binding.viewPagerBanner.getCurrentItem();
                    int nextItem = (currentItem + 1) % count;
                    binding.viewPagerBanner.setCurrentItem(nextItem, true);
                });
            }
        }, 3000, 3000);
    }

    private void stopAutoSlider() {
        if (sliderTimer != null) {
            sliderTimer.cancel();
            sliderTimer = null;
        }
    }
}
