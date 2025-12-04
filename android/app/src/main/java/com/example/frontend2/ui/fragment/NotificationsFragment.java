package com.example.frontend2.ui.fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.frontend2.data.model.Notification;
import com.example.frontend2.data.remote.ApiClient;
import com.example.frontend2.data.remote.ApiService;
import com.example.frontend2.databinding.FragmentNotificationsBinding;
import com.example.frontend2.ui.adapter.NotificationAdapter;
import com.example.frontend2.utils.SharedPrefManager;
import com.google.gson.Gson;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsFragment extends Fragment implements NotificationAdapter.OnNotificationListener {

    private FragmentNotificationsBinding binding;
    private NotificationAdapter adapter;
    private ApiService apiService;
    private String authToken;
    private static final String TAG = "NotificationsFragment";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apiService = ApiClient.getRetrofitInstance().create(ApiService.class);
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_prefs", requireContext().MODE_PRIVATE);
        authToken = SharedPrefManager.getInstance(getContext()).getAuthToken();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentNotificationsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupRecyclerView();
        setupClickListeners();
        fetchNotifications();
    }

    private void setupRecyclerView() {
        adapter = new NotificationAdapter(this);
        binding.notificationsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.notificationsRecyclerView.setAdapter(adapter);
    }

    private void setupClickListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener(this::fetchNotifications);
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isAdded()) {
                getParentFragmentManager().popBackStack();
            }
        });
        binding.btnReadAll.setOnClickListener(v -> showMarkAllReadDialog());
    }

    private void fetchNotifications() {
        if (authToken == null) {
            if (binding.swipeRefreshLayout.isRefreshing()) {
                binding.swipeRefreshLayout.setRefreshing(false);
            }
            binding.progressBar.setVisibility(View.GONE);
            showErrorState("Lỗi xác thực người dùng.");
            return;
        }

        if (!binding.swipeRefreshLayout.isRefreshing()) {
            binding.progressBar.setVisibility(View.VISIBLE);
        }
        binding.tvNoNotifications.setVisibility(View.GONE);
        binding.notificationsRecyclerView.setVisibility(View.VISIBLE);

        String authHeader = "Bearer " + authToken;
        apiService.getNotifications(authHeader).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                if (!isAdded()) return;

                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);

                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("API_RESPONSE", "Notifications JSON: " + jsonResponse);

                        Gson gson = new Gson();
                        Notification fullResponseObject = gson.fromJson(jsonResponse, Notification.class);

                        if (fullResponseObject != null && fullResponseObject.getNotifications() != null) {
                            List<Notification.NotificationItem> items = fullResponseObject.getNotifications();
                            adapter.updateData(items);
                            if (items.isEmpty()) {
                                showEmptyState();
                            }
                        } else {
                            showEmptyState();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        showErrorState("Lỗi xử lý dữ liệu.");
                    }
                } else {
                    handleApiError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                if (!isAdded()) return;
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefreshLayout.setRefreshing(false);
                showErrorState("Lỗi mạng, không thể kết nối.");
                Log.e("API_FAILURE", "Lỗi gọi API: " + t.getMessage(), t);
            }
        });
    }

    private void showEmptyState() {
        binding.notificationsRecyclerView.setVisibility(View.GONE);
        binding.tvNoNotifications.setVisibility(View.VISIBLE);
        binding.tvNoNotifications.setText("Bạn chưa có thông báo nào");
    }

    private void showErrorState(String message) {
        if (isAdded() && binding != null) {
            binding.notificationsRecyclerView.setVisibility(View.GONE);
            binding.tvNoNotifications.setVisibility(View.VISIBLE);
            binding.tvNoNotifications.setText(message);
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        }
    }

    private void handleApiError(Response<?> response) {
        try {
            String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown server error";
            Log.e("API_ERROR", "Code: " + response.code() + ", Body: " + errorBody);
        } catch (IOException e) {
            Log.e("API_ERROR", "Lỗi đọc error body", e);
        }
        showErrorState("Lỗi tải thông báo từ máy chủ.");
    }

    @Override
    public void onNotificationClick(Notification.NotificationItem notification, int position) {
        if (!notification.isRead()) {
            markNotificationAsReadOnServer(notification.getId(), position);
        }
    }

    @Override
    public void onNotificationLongClick(Notification.NotificationItem notification, int position) {
        showDeleteConfirmationDialog(notification, position);
    }

    private void markNotificationAsReadOnServer(String notificationId, int position) {
        if (authToken == null) return;
        String authHeader = "Bearer " + authToken;
        apiService.markAsRead(authHeader, notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() && isAdded()) {
                    // Bước 1: Lấy danh sách hiện tại từ adapter
                    List<Notification.NotificationItem> currentList = new ArrayList<>(adapter.getCurrentList());
                    if (position >= 0 && position < currentList.size()) {
                        // Bước 2: Lấy ra item cần sửa
                        Notification.NotificationItem clickedItem = currentList.get(position);
                        // Bước 3: Thay đổi trạng thái của item đó
                        clickedItem.setRead(true);
                        // Bước 4: Cập nhật lại toàn bộ danh sách cho adapter
                        adapter.updateData(currentList);
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                Log.e(TAG, "Lỗi mạng khi đánh dấu đã đọc: " + t.getMessage());
            }
        });
    }

    private void showDeleteConfirmationDialog(final Notification.NotificationItem notification, final int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Xóa thông báo")
                .setMessage("Bạn có chắc chắn muốn xóa thông báo này không?")
                .setPositiveButton("Xóa", (dialog, which) -> deleteNotificationOnServer(notification.getId(), position))
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void deleteNotificationOnServer(String notificationId, int position) {
        if (authToken == null) return;
        String authHeader = "Bearer " + authToken;
        apiService.deleteNotification(authHeader, notificationId).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() && isAdded()) {
                    // Bước 1: Lấy danh sách hiện tại
                    List<Notification.NotificationItem> currentList = new ArrayList<>(adapter.getCurrentList());
                    if (position >= 0 && position < currentList.size()) {
                        // Bước 2: Xóa item khỏi danh sách
                        currentList.remove(position);
                        // Bước 3: Cập nhật lại danh sách cho adapter
                        adapter.updateData(currentList);
                        if (currentList.isEmpty()) {
                            showEmptyState();
                        }
                    }
                } else {
                    if (isAdded()) Toast.makeText(requireContext(), "Lỗi khi xóa thông báo", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if (isAdded()) Toast.makeText(requireContext(), "Lỗi mạng, không thể xóa", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showMarkAllReadDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Đánh dấu tất cả đã đọc")
                .setMessage("Bạn có muốn đánh dấu tất cả thông báo là đã đọc không?")
                .setPositiveButton("Đồng ý", (dialog, which) -> markAllAsReadOnServer())
                .setNegativeButton("Hủy", null)
                .show();
    }

    private void markAllAsReadOnServer() {
        if (authToken == null) return;
        String authHeader = "Bearer " + authToken;
        apiService.markAllAsRead(authHeader).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(@NonNull Call<Void> call, @NonNull Response<Void> response) {
                if (response.isSuccessful() && isAdded()) {
                    List<Notification.NotificationItem> currentList = new ArrayList<>(adapter.getCurrentList());
                    for (Notification.NotificationItem item : currentList) {
                        item.setRead(true);
                    }
                    adapter.updateData(currentList);
                } else {
                    if(isAdded()) Toast.makeText(requireContext(), "Lỗi khi thực hiện", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<Void> call, @NonNull Throwable t) {
                if(isAdded()) Toast.makeText(requireContext(), "Lỗi mạng", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (binding != null) {
            binding.notificationsRecyclerView.setAdapter(null);
        }
        binding = null;
    }
}
