# API Documentation - Loyalty Tier System

## Overview

Hệ thống Loyalty Tier cho phép theo dõi và quản lý cấp độ thân thiết của người dùng dựa trên tổng chi tiêu. Hệ thống tự động cập nhật cấp độ và gửi thông báo email cho người dùng.

## Loyalty Tiers

### Các Cấp Độ

1. **Bronze** (Đồng)

   - Tổng chi tiêu: 0 - 499,999 VND
   - Chiết khấu: 0%
   - Lợi ích: Basic member benefits

2. **Silver** (Bạc)

   - Tổng chi tiêu: 500,000 - 999,999 VND
   - Chiết khấu: 5%
   - Lợi ích: Special discounts and priority support

3. **Gold** (Vàng)
   - Tổng chi tiêu: 1,000,000+ VND
   - Chiết khấu: 10%
   - Lợi ích: Exclusive offers, priority support, and special events

## API Endpoints

### 1. Get Loyalty Information

**Endpoint:** `GET /api/users/loyalty/info`

**Authentication:** Required (Bearer Token)

**Description:** Lấy đầy đủ thông tin cấp độ thân thiết của người dùng hiện tại, bao gồm cấp độ hiện tại, tổng chi tiêu, và thông tin cấp độ tiếp theo.

**Response Example:**

```json
{
  "EC": 0,
  "DT": {
    "userId": "507f1f77bcf86cd799439011",
    "email": "user@example.com",
    "firstName": "John",
    "lastName": "Doe",
    "currentTier": "silver",
    "totalSpent": 525123,
    "formattedTotal": "525.123₫",
    "lastTierUpdateAt": "2025-11-20T10:30:00Z",
    "tierInfo": {
      "id": "silver",
      "name": "Silver",
      "minSpending": 500000,
      "maxSpending": 999999,
      "benefits": "Special discounts and priority support",
      "discount": 5
    },
    "nextTier": "gold",
    "amountToNextTier": 474877,
    "formattedAmountToNextTier": "474.877₫"
  },
  "EM": "Lấy thông tin cấp độ thân thiết thành công"
}
```

### 2. Check Loyalty Status

**Endpoint:** `GET /api/users/loyalty/status`

**Authentication:** Required (Bearer Token)

**Description:** Kiểm tra trạng thái cấp độ thân thiết hiện tại của người dùng, bao gồm cấp độ, tên cấp độ, chiết khấu, và lợi ích.

**Response Example:**

```json
{
  "EC": 0,
  "DT": {
    "currentTier": "gold",
    "tierName": "Gold",
    "totalSpent": 1500123,
    "formattedTotal": "1.500.123₫",
    "benefits": "Exclusive offers, priority support, and special events",
    "discount": 10,
    "lastTierUpdateAt": "2025-11-15T14:20:00Z"
  },
  "EM": "Kiểm tra trạng thái thành công"
}
```

### 3. Get Total Spending

**Endpoint:** `GET /api/users/loyalty/total-spent`

**Authentication:** Required (Bearer Token)

**Description:** Lấy tổng tiền mà người dùng đã chi tiêu, bao gồm cả giá trị được định dạng theo tiền tệ Việt Nam.

**Response Example:**

```json
{
  "EC": 0,
  "DT": {
    "totalSpent": 525123,
    "currentTier": "silver",
    "tierName": "Silver",
    "formattedTotal": "525.123₫"
  },
  "EM": "Lấy tổng chi tiêu thành công"
}
```

## Discount Tier Requirements

### New Field: `tierRequired`

Trường `tierRequired` trong Discount Model xác định cấp độ thân thiết yêu cầu để sử dụng mã giảm giá.

**Possible Values:**

- `"all"` - Tất cả người dùng có thể sử dụng (mặc định)
- `"bronze"` - Chỉ Bronze members
- `"silver"` - Chỉ Silver members
- `"gold"` - Chỉ Gold members

### Discount Model Example

```json
{
  "_id": "507f1f77bcf86cd799439012",
  "code": "LOYALTY15",
  "description": "Giảm 15% cho khách hàng Silver",
  "type": "percent",
  "value": 15,
  "minOrder": 150000,
  "maxDiscount": 80000,
  "tierRequired": "silver",
  "startAt": "2025-10-01T00:00:00Z",
  "endAt": "2025-12-31T23:59:59Z",
  "isActive": true
}
```

## User Model Updates

### New Fields in User Schema

1. **loyaltyTier** (String)

   - Enum: `["bronze", "silver", "gold"]`
   - Default: `"bronze"`
   - Description: Cấp độ thân thiết hiện tại của người dùng

2. **totalSpent** (Number)

   - Default: 0
   - Minimum: 0
   - Description: Tổng tiền đã chi tiêu tất cả thời gian

3. **lastTierUpdateAt** (Date)
   - Default: null
   - Description: Thời gian cập nhật cấp độ thân thiết lần cuối

### User Model Example

```json
{
  "_id": "507f1f77bcf86cd799439011",
  "firstName": "John",
  "lastName": "Doe",
  "email": "user@example.com",
  "loyaltyTier": "silver",
  "totalSpent": 7500000,
  "lastTierUpdateAt": "2025-11-20T10:30:00Z",
  "role": "Customer",
  "isActive": true
}
```

## How Loyalty System Works

### Automatic Tier Update

1. Khi đơn hàng được giao thành công (`status: "Delivered"`), hệ thống tự động:
   - Cộng giá trị đơn hàng vào `totalSpent` của người dùng
   - Tính toán lại cấp độ thân thiết dựa trên `totalSpent`
   - Nếu cấp độ thay đổi, gửi email thông báo cho người dùng

### Email Notifications

Khi cấp độ thay đổi, người dùng sẽ nhận được email thông báo với:

- Cấp độ cũ và cấp độ mới
- Lợi ích của cấp độ mới
- Hướng dẫn nâng cấp tiếp theo (nếu có)

## Service Methods

### loyaltyService.js

#### `updateUserLoyaltyTier(userId)`

Cập nhật cấp độ thân thiết của người dùng dựa trên `totalSpent`.

- **Parameter:** userId (string) - ID của người dùng
- **Returns:** `{ oldTier, newTier, tierChanged, user }`
- **Throws:** Lỗi nếu người dùng không tồn tại

#### `getUserLoyaltyInfo(userId)`

Lấy đầy đủ thông tin cấp độ thân thiết của người dùng.

- **Parameter:** userId (string) - ID của người dùng
- **Returns:** Loyalty information object
- **Throws:** Lỗi nếu người dùng không tồn tại

#### `addSpending(userId, amount)`

Thêm chi tiêu cho người dùng và cập nhật cấp độ.

- **Parameters:**
  - userId (string) - ID của người dùng
  - amount (number) - Số tiền chi tiêu (VND)
- **Returns:** `{ oldTier, newTier, tierChanged, loyaltyInfo }`
- **Throws:** Lỗi nếu amount <= 0

#### `isUserEligibleForDiscount(userTier, tierRequired)`

Kiểm tra xem người dùng có đủ điều kiện sử dụng mã giảm giá hay không.

- **Parameters:**
  - userTier (string) - Cấp độ hiện tại của người dùng
  - tierRequired (string|array) - Yêu cầu cấp độ cho mã giảm giá
- **Returns:** boolean - true nếu người dùng đủ điều kiện

## Configuration

### loyaltyTiers.js Constants

```javascript
const LOYALTY_TIERS = {
  BRONZE: { minSpending: 0, maxSpending: 4999999, discount: 0 },
  SILVER: { minSpending: 5000000, maxSpending: 9999999, discount: 5 },
  GOLD: { minSpending: 10000000, maxSpending: Infinity, discount: 10 },
};
```

### Environment Variables Required

```
EMAIL_SERVICE=gmail
EMAIL_USER=your-email@gmail.com
EMAIL_PASS=your-app-password
```

## Error Handling

### Standard Error Responses

1. **User Not Found**

```json
{
  "EC": -1,
  "DT": null,
  "EM": "Không tìm thấy người dùng"
}
```

2. **Invalid Spending Amount**

```json
{
  "EC": -1,
  "DT": null,
  "EM": "Amount must be greater than 0"
}
```

## Testing

Run loyalty tests:

```bash
node tests/loyaltyService.test.js
```

## Examples

### Example 1: Bronze to Silver Upgrade

```
User spends: 5,000,000 VND
Old tier: bronze → New tier: silver
Email sent: Tier upgrade notification
```

### Example 2: Using Tier-Specific Discount

```
User tier: silver
Discount code: SILVER20 (tierRequired: "silver")
Eligible: YES ✓
```

```
User tier: bronze
Discount code: SILVER20 (tierRequired: "silver")
Eligible: NO ✗
```

## Notes for Frontend Integration

### Currency Formatting

Tất cả các giá trị tiền tệ được trả về với hai định dạng:

1. **Raw number**: `totalSpent` (number) - giá trị nguyên bản
2. **Formatted string**: `formattedTotal` (string) - định dạng với dấu chấm phân cách hàng ngàn, không làm tròn

Ví dụ:

- `totalSpent: 525123` → `formattedTotal: "525.123₫"`
- `totalSpent: 1500000` → `formattedTotal: "1.500.000₫"`
- `totalSpent: 999999` → `formattedTotal: "999.999₫"`

  ```javascript
  GET / api / users / loyalty / info;
  ```

2. **Display in Order Checkout:** Hiển thị danh sách mã giảm giá phù hợp với cấp độ người dùng

   ```javascript
   // Check tierRequired field before showing discount
   if (isEligible(userTier, discount.tierRequired)) {
     showDiscount(discount);
   }
   ```

3. **Show Tier Progress:** Hiển thị tiến độ nâng cấp tới cấp độ tiếp theo

   ```javascript
   const progress = (currentSpending / nextTierSpending) * 100;
   ```

4. **Refresh After Order:** Cập nhật thông tin cấp độ sau khi đặt hàng thành công
   ```javascript
   GET / api / users / loyalty / status;
   ```

## Changes Summary

### Files Modified:

- `models/userModel.js` - Added loyaltyTier, totalSpent, lastTierUpdateAt fields
- `models/discountModel.js` - Added tierRequired field
- `controllers/orderController.js` - Updated updateOrderStatus to add spending
- `routes/userRoutes.js` - Added loyalty endpoints

### Files Created:

- `config/loyaltyTiers.js` - Tier configuration and constants
- `services/loyaltyService.js` - Loyalty business logic
- `services/emailService.js` - Email notification service
- `controllers/loyaltyController.js` - Loyalty API controllers
- `scripts/seedDiscountsWithTiers.js` - Seed data with tier support
- `tests/loyaltyService.test.js` - Unit tests
