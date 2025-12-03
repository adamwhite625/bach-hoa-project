# Loyalty Tier System - Implementation Summary

## 📋 Overview

Hệ thống cấp độ thân thiết (Loyalty Tier) hoàn chỉnh cho backend với:

- 3 cấp độ: Bronze, Silver, Gold
- Tự động cập nhật dựa trên lịch sử mua hàng
- Email thông báo khi thay đổi cấp độ
- API endpoints để người dùng kiểm tra cấp độ
- Mã giảm giá cấp độ (tier-specific discounts)
- Unit tests đầy đủ
- Tài liệu API chi tiết

## 🎯 Chức Năng Đã Thêm

### 1. ✅ Chia Cấp Độ Thân Thiết (3 Cấp)

**File:** `config/loyaltyTiers.js`

| Cấp Độ | Tổng Chi Tiêu         | Chiết Khấu | Lợi Ích          |
| ------ | --------------------- | ---------- | ---------------- |
| Bronze | 0 - 2,999,999         | 0%         | Basic benefits   |
| Silver | 3,000,000 - 5,999,999 | 5%         | Priority support |
| Gold   | 6,000,000+            | 10%        | Exclusive offers |

**Hàm tính toán:** `calculateTier(totalSpent)` → Trả về cấp độ phù hợp

### 2. ✅ Xem Tổng Chi Tiêu

**Endpoint:** `GET /api/users/loyalty/total-spent`

Response:

```json
{
  "totalSpent": 7500000,
  "currentTier": "silver",
  "formattedTotal": "7.500.000 ₫"
}
```

### 3. ✅ Phân Loại Discount Theo Tier

**Model Field:** `Discount.tierRequired`

Giá trị có thể:

- `"all"` - Tất cả người dùng
- `"bronze"` - Chỉ Bronze members
- `"silver"` - Chỉ Silver members
- `"gold"` - Chỉ Gold members

### 4. ✅ Kiểm Tra Cấp Độ Thân Thiết

**Endpoint:** `GET /api/users/loyalty/status`

Response:

```json
{
  "currentTier": "gold",
  "tierName": "Gold",
  "discount": 10,
  "benefits": "Exclusive offers, priority support, and special events"
}
```

### 5. ✅ Thông Báo Email

**File:** `services/emailService.js`

- Tự động gửi khi tier thay đổi
- HTML email với định dạng chuyên nghiệp
- Thông báo nâng cấp hoặc hạ cấp
- Hiển thị lợi ích mới

### 6. ✅ Cập Nhật User Model

**File:** `models/userModel.js`

Thêm 3 trường:

```javascript
loyaltyTier: String (default: "bronze")
totalSpent: Number (default: 0)
lastTierUpdateAt: Date (default: null)
```

### 7. ✅ Cập Nhật Seed Data

**File:** `scripts/seedDiscountsWithTiers.js`

- 12 mã giảm giá với `tierRequired` field
- Ví dụ:
  - `LOYALTY15` → Dành cho Silver
  - `VIP30` → Dành cho Gold
  - `WELCOME10` → Cho tất cả

### 8. ✅ Unit Tests

**File:** `tests/loyaltyService.test.js`

Tests bao gồm:

- ✓ calculateTier function
- ✓ getTierInfo function
- ✓ getNextTierInfo function
- ✓ isUserEligibleForDiscount function
- ✓ Boundary testing
- ✓ Edge cases

Chạy tests:

```bash
node tests/loyaltyService.test.js
```

### 9. ✅ API Documentation

**File:** `LOYALTY_API_DOCUMENTATION.md`

Tài liệu chi tiết bao gồm:

- Cấu trúc API endpoints
- Request/Response examples
- Environment variables
- Integration guide cho frontend
- Troubleshooting

## 📁 Files Created/Modified

### Created Files:

```
backend/
├── config/
│   └── loyaltyTiers.js                    [NEW] Constants & calculations
├── services/
│   ├── loyaltyService.js                  [NEW] Business logic
│   └── emailService.js                    [NEW] Email notifications
├── controllers/
│   └── loyaltyController.js               [NEW] API endpoints
├── scripts/
│   └── seedDiscountsWithTiers.js          [NEW] Seed data with tiers
├── tests/
│   └── loyaltyService.test.js             [NEW] Unit tests
└── LOYALTY_API_DOCUMENTATION.md           [NEW] Complete API docs
```

### Modified Files:

```
backend/
├── models/
│   ├── userModel.js                       [MODIFIED] Added loyalty fields
│   └── discountModel.js                   [MODIFIED] Added tierRequired
├── controllers/
│   └── orderController.js                 [MODIFIED] Auto-update loyalty
└── routes/
    └── userRoutes.js                      [MODIFIED] Added loyalty routes
```

## 🔄 Workflow

```
1. Người dùng đặt hàng → Đơn hàng created
2. Đơn hàng được giao (Delivered) → updateOrderStatus gọi addSpending
3. addSpending → Cộng totalSpent + tính lại tier
4. Tier thay đổi → Gửi email thông báo
5. User kiểm tra: GET /api/users/loyalty/info → Xem cấp độ mới
```

## 🚀 How to Use

### 1. Set Up Email Service

Thêm vào `.env`:

```
EMAIL_SERVICE=gmail
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
```

### 2. Run Seed Data

```bash
node scripts/seedDiscountsWithTiers.js
```

### 3. Test Loyalty Service

```bash
node tests/loyaltyService.test.js
```

### 4. API Endpoints để Frontend sử dụng

**Get Full Loyalty Info:**

```bash
curl -X GET http://localhost:5000/api/users/loyalty/info \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Check Current Status:**

```bash
curl -X GET http://localhost:5000/api/users/loyalty/status \
  -H "Authorization: Bearer YOUR_TOKEN"
```

**Get Total Spending:**

```bash
curl -X GET http://localhost:5000/api/users/loyalty/total-spent \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 📊 Database Queries

### Check User Loyalty:

```javascript
db.users.find({ _id: ObjectId("...") }).pretty();
// Xem loyaltyTier, totalSpent, lastTierUpdateAt
```

### Check Discounts by Tier:

```javascript
db.discounts.find({ tierRequired: "gold" });
```

## 🔐 Security Notes

- Tất cả endpoints được bảo vệ bằng `protect` middleware (xác thực)
- Email chỉ gửi khi tier thực sự thay đổi
- totalSpent chỉ tăng khi order delivered, không thể thay đổi trực tiếp
- tierRequired validation trong discount model

## 📈 Performance Considerations

- Tier calculation: O(1) - Simple thresholds
- Email sending: Async - Không block order update
- Database: 3 thêm fields, indexes trên `loyaltyTier` khuyến khích

## 🐛 Troubleshooting

### Email không gửi?

- Kiểm tra `EMAIL_USER` và `EMAIL_PASS` trong .env
- Cho phép "Less secure apps" nếu dùng Gmail
- Kiểm tra logs để tìm lỗi

### Tier không cập nhật?

- Chắc chắn order status là "Delivered"
- Kiểm tra `totalSpent` trong database
- Gọi `updateOrderStatus` để trigger update

### Test failed?

- Chắc chắn mongoose không cần connect
- Kiểm tra file path trong require statements

## ✨ Next Steps (Optional Enhancements)

1. **Referral Program** - Thêm trường referralBonus
2. **Birthday Discount** - Extra discount sinh nhật
3. **Seasonal Promotions** - Seasonal tier boosts
4. **Leaderboard** - Top spenders
5. **Tier Expiry** - Tier reset nếu không mua trong X tháng
6. **Points System** - Thêm loyalty points

## 📚 References

- **Loyalty Service:** `services/loyaltyService.js`
- **Configuration:** `config/loyaltyTiers.js`
- **Email Templates:** `services/emailService.js`
- **Tests:** `tests/loyaltyService.test.js`
- **Documentation:** `LOYALTY_API_DOCUMENTATION.md`

## ✅ Checklist Hoàn Thành

- ✅ Chia 3 cấp độ thân thiết
- ✅ Tính toán dựa trên lịch sử mua hàng
- ✅ API endpoint xem tổng chi tiêu
- ✅ API endpoint kiểm tra cấp độ
- ✅ Phân loại discount theo tier
- ✅ Email thông báo thay đổi tier
- ✅ Cập nhật User model
- ✅ Cập nhật Discount model
- ✅ Seed data với tier support
- ✅ Unit tests hoàn chỉnh
- ✅ API documentation

---

**Last Updated:** November 2025
**Status:** ✅ COMPLETE
