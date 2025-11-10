package com.example.frontend2.ui.main;

import com.example.frontend2.data.model.CartItem;

/**
 * Interface này định nghĩa các hành động mà người dùng có thể thực hiện
 * trên một item trong giỏ hàng.
 *
 * CartFragment sẽ implement interface này để nhận và xử lý các sự kiện đó,
 * giúp cho Adapter không cần phải chứa logic nghiệp vụ phức tạp.
 */
public interface OnCartItemInteractionListener {

    /**
     * Được gọi khi người dùng nhấn nút xóa một sản phẩm khỏi giỏ hàng.
     *
     * @param item     Đối tượng CartItem cần xóa.
     * @param position Vị trí của item trong danh sách của adapter.
     */
    void onRemoveItem(CartItem item, int position);

    /**
     * Được gọi khi người dùng nhấn nút tăng hoặc giảm số lượng của một sản phẩm.
     *
     * @param item        Đối tượng CartItem được cập nhật.
     * @param newQuantity Số lượng mới sau khi đã tăng/giảm.
     * @param position    Vị trí của item trong danh sách của adapter.
     */
    void onUpdateQuantity(CartItem item, int newQuantity, int position);
}
