# Hướng Dẫn Chạy Loyalty Tier Scripts

## 📋 Mức Tier Mới

- **Bronze**: 0 - 499,999 VND
- **Silver**: 500,000 - 999,999 VND
- **Gold**: 1,000,000+ VND

## 🚀 Cách Chạy

### 1. Update Discount Tiers (Update trường tierRequired cho discount hiện có)

```bash
node scripts/updateDiscountTiers.js
```

**Kết quả:**

- Tìm các discount code trong database
- Update trường `tierRequired` phù hợp
- Không thêm discount mới

**Discount được update:**

- `LOYALTY15`, `SILVER20` → silver
- `VIP30`, `NEWYEAR100` → gold
- Các discount khác → all

### 2. Seed 2 Đơn Hàng cho User (thienmocay1235@gmail.com)

```bash
node scripts/seedOrdersForUser.js
```

**Kết quả:**

- Tạo 2 đơn hàng đã deliver cho user:
  - Order 1: 150,000 VND (7 ngày trước)
  - Order 2: 350,000 VND (1 ngày trước)
- **Total spent: 500,000 VND** → User nâng lên **Silver tier**
- Gửi email thông báo nâng cấp

## 📊 Chi Tiết Đơn Hàng Được Seed

### Order 1

- Status: Delivered
- Total: 150,000 VND
- Items: 2x Product[0]

### Order 2

- Status: Delivered
- Total: 350,000 VND
- Items: 1x Product[1] + 2x Product[2]

## ⚙️ Cấu Hình Yêu Cầu

Đảm bảo `.env` có:

```
MONGO_URI=mongodb://...
EMAIL_SERVICE=gmail
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
```

## ✅ Kiểm Tra Kết Quả

Sau khi chạy script:

```bash
# Kiểm tra user info
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:5000/api/users/loyalty/info

# Kiểm tra total spent
curl -H "Authorization: Bearer YOUR_TOKEN" \
  http://localhost:5000/api/users/loyalty/total-spent
```

## 🧪 Chạy Tests

```bash
node tests/loyaltyService.test.js
```

## 📝 Ghi Chú

- Script sẽ tự động gọi `addSpending()` để cập nhật loyalty tier
- User sẽ nhận email thông báo nâng cấp từ Bronze → Silver
- Nếu user không tồn tại, script sẽ báo lỗi
- Nếu không có sản phẩm trong database, script sẽ báo lỗi
