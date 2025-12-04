/**
 * ========================================
 * COMPLETE API DOCUMENTATION
 * ========================================
 * 
 * Project: Bach Hoa E-commerce Platform
 * Base URL: http://localhost:5000
 * 
 * Response Format:
 * {
 *   EC: 0 (success) or -1 (error),
 *   DT: data,
 *   EM: message
 * }
 * 
 * ========================================
 */

// ========================================
// AUTHENTICATION ENDPOINTS
// ========================================

/**
 * 1. Register User
 * POST /api/auth/register
 * Status: Public
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/auth/register",
  "description": "Đăng ký tài khoản người dùng mới",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "firstName": "Nguyễn",
    "lastName": "Văn A",
    "email": "user@example.com",
    "password": "password123"
  },
  "response_success": {
    "status": 201,
    "data": {
      "_id": "507f1f77bcf86cd799439011",
      "email": "user@example.com",
      "role": "Customer",
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "firstName": "Nguyễn",
      "lastName": "Văn A"
    }
  },
  "response_error": {
    "status": 400,
    "data": {
      "message": "Email này đã được đăng ký"
    }
  },
  "response_validation_error": {
    "status": 500,
    "data": {
      "message": "firstName: firstName is required, lastName: lastName is required"
    }
  }
}

/**
 * 2. Login User
 * POST /api/auth/login
 * Status: Public
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/auth/login",
  "description": "Đăng nhập tài khoản người dùng",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "email": "user@example.com",
    "password": "password123"
  },
  "response_success": {
    "status": 200,
    "data": {
      "_id": "507f1f77bcf86cd799439011",
      "firstName": "Nguyễn",
      "lastName": "Văn A",
      "email": "user@example.com",
      "role": "Customer",
      "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
    }
  },
  "response_error": {
    "status": 401,
    "data": {
      "message": "Invalid email or password"
    }
  }
}

/**
 * 3. Admin Login
 * POST /api/auth/admin/login
 * Status: Public
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/auth/admin/login",
  "description": "Đăng nhập tài khoản admin",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "email": "admin@example.com",
    "password": "admin123"
  },
  "response_success": {
    "status": 200,
    "data": {
      "EC": 0,
      "access_token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
      "user": {
        "_id": "507f1f77bcf86cd799439001",
        "email": "admin@example.com",
        "role": "Admin",
        "firstName": "Admin",
        "lastName": "User"
      }
    }
  },
  "response_error": {
    "status": 401,
    "data": {
      "message": "Không tìm thấy tài khoản admin",
      "EC": 1
    }
  }
}

/**
 * 4. Forgot Password
 * POST /api/auth/forgot-password
 * Status: Public
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/auth/forgot-password",
  "description": "Yêu cầu reset mật khẩu",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "email": "user@example.com"
  },
  "response": {
    "EC": 0,
    "DT": null,
    "EM": "Email reset mật khẩu đã được gửi"
  }
}

/**
 * 5. Reset Password
 * POST /api/auth/reset-password
 * Status: Public
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/auth/reset-password",
  "description": "Reset mật khẩu mới",
  "headers": {
    "Content-Type": "application/json"
  },
  "body": {
    "token": "reset_token_from_email",
    "newPassword": "newpassword123"
  },
  "response": {
    "EC": 0,
    "DT": null,
    "EM": "Mật khẩu đã được reset thành công"
  }
}

// ========================================
// USER ENDPOINTS
// ========================================

/**
 * 6. Get User Profile
 * GET /api/users/profile
 * Status: Protected (Requires token)
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users/profile",
  "description": "Lấy thông tin hồ sơ người dùng hiện tại",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439011",
      "fullName": "Nguyễn Văn A",
      "email": "user@example.com",
      "phone": "0901234567",
      "avatar": "https://cloudinary.com/...",
      "loyaltyTier": "silver",
      "totalSpent": 578630,
      "role": "Customer"
    },
    "EM": "Lấy thông tin thành công"
  }
}

/**
 * 7. Update User Profile
 * PUT /api/users/profile
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/users/profile",
  "description": "Cập nhật thông tin hồ sơ người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "multipart/form-data"
  },
  "body": {
    "fullName": "Nguyễn Văn B",
    "phone": "0909876543",
    "avatar": "[file]"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439011",
      "fullName": "Nguyễn Văn B",
      "email": "user@example.com",
      "phone": "0909876543",
      "avatar": "https://cloudinary.com/...",
      "loyaltyTier": "silver"
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 8. Get All Users (Admin only)
 * GET /api/users
 * Status: Protected (Admin)
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users",
  "description": "Lấy danh sách tất cả người dùng (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439011",
        "fullName": "Nguyễn Văn A",
        "email": "user@example.com",
        "role": "Customer",
        "loyaltyTier": "silver",
        "totalSpent": 578630,
        "isActive": true
      }
    ],
    "EM": "Lấy danh sách thành công"
  }
}

/**
 * 9. Delete User (Admin only)
 * DELETE /api/users/:id
 * Status: Protected (Admin)
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/users/507f1f77bcf86cd799439011",
  "description": "Xóa người dùng (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

/**
 * 10. Get Shipping Address
 * GET /api/users/shipping-address
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users/shipping-address",
  "description": "Lấy địa chỉ giao hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "addressLine1": "123 Đường ABC",
      "addressLine2": "Quận 1",
      "city": "Hồ Chí Minh",
      "state": "HCM",
      "postalCode": "700000",
      "country": "Vietnam",
      "phoneNumber": "0901234567"
    },
    "EM": "Lấy thành công"
  }
}

/**
 * 11. Update Shipping Address
 * PUT /api/users/shipping-address
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/users/shipping-address",
  "description": "Cập nhật địa chỉ giao hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "addressLine1": "456 Đường XYZ",
    "addressLine2": "Quận 2",
    "city": "Hồ Chí Minh",
    "state": "HCM",
    "postalCode": "700000",
    "country": "Vietnam",
    "phoneNumber": "0909876543"
  },
  "response": {
    "EC": 0,
    "DT": {
      "addressLine1": "456 Đường XYZ",
      "city": "Hồ Chí Minh"
    },
    "EM": "Cập nhật thành công"
  }
}

// ========================================
// LOYALTY TIER ENDPOINTS
// ========================================

/**
 * 12. Get Loyalty Information
 * GET /api/users/loyalty/info
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users/loyalty/info",
  "description": "Lấy thông tin cấp độ thân thiết đầy đủ",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "userId": "507f1f77bcf86cd799439011",
      "email": "user@example.com",
      "firstName": "Nguyễn",
      "lastName": "Văn A",
      "currentTier": "silver",
      "totalSpent": 578630,
      "formattedTotal": "578.630₫",
      "lastTierUpdateAt": "2025-11-30T15:57:59.389Z",
      "tierInfo": {
        "id": "silver",
        "name": "Silver",
        "minSpending": 500000,
        "maxSpending": 999999,
        "benefits": "Special discounts and priority support",
        "discount": 5
      },
      "nextTier": "gold",
      "amountToNextTier": 421370,
      "formattedAmountToNextTier": "421.370₫"
    },
    "EM": "Lấy thông tin thành công"
  }
}

/**
 * 13. Check Loyalty Status
 * GET /api/users/loyalty/status
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users/loyalty/status",
  "description": "Kiểm tra trạng thái cấp độ thân thiết hiện tại",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "currentTier": "silver",
      "tierName": "Silver",
      "totalSpent": 578630,
      "formattedTotal": "578.630₫",
      "benefits": "Special discounts and priority support",
      "discount": 5,
      "lastTierUpdateAt": "2025-11-30T15:57:59.389Z"
    },
    "EM": "Kiểm tra thành công"
  }
}

/**
 * 14. Get Total Spending
 * GET /api/users/loyalty/total-spent
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/users/loyalty/total-spent",
  "description": "Lấy tổng tiền chi tiêu của người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "totalSpent": 578630,
      "currentTier": "silver",
      "tierName": "Silver",
      "formattedTotal": "578.630₫"
    },
    "EM": "Lấy tổng chi tiêu thành công"
  }
}

// ========================================
// PRODUCT ENDPOINTS
// ========================================

/**
 * 15. Get All Products
 * GET /api/products
 * Status: Public
 * Query: ?page=1&limit=10&sort=-createdAt&category=507f1f77bcf86cd799439011
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/products?page=1&limit=10",
  "description": "Lấy danh sách sản phẩm (hỗ trợ phân trang, sắp xếp, lọc)",
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439021",
        "name": "Sản phẩm A",
        "description": "Mô tả sản phẩm",
        "price": 100000,
        "category": "507f1f77bcf86cd799439031",
        "images": ["https://cloudinary.com/..."],
        "rating": 4.5,
        "reviews": 10,
        "stock": 50,
        "sale": {
          "active": true,
          "type": "percent",
          "value": 10,
          "startAt": "2025-01-01",
          "endAt": "2025-01-31"
        }
      }
    ],
    "EM": "Lấy danh sách thành công"
  }
}

/**
 * 16. Get Product By ID
 * GET /api/products/:id
 * Status: Public
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/products/507f1f77bcf86cd799439021",
  "description": "Lấy chi tiết sản phẩm theo ID",
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439021",
      "name": "Sản phẩm A",
      "description": "Mô tả chi tiết",
      "price": 100000,
      "category": "507f1f77bcf86cd799439031",
      "images": ["https://cloudinary.com/..."],
      "rating": 4.5,
      "reviews": 10,
      "stock": 50,
      "sale": {
        "active": true,
        "type": "percent",
        "value": 10
      }
    },
    "EM": "Lấy thành công"
  }
}

/**
 * 17. Search Products
 * GET /api/products/search?q=keyword
 * Status: Public
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/products/search?q=điện thoại",
  "description": "Tìm kiếm sản phẩm theo từ khóa",
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439021",
        "name": "Điện thoại Samsung",
        "price": 5000000
      }
    ],
    "EM": "Tìm kiếm thành công"
  }
}

/**
 * 18. Create Product (Admin only)
 * POST /api/products
 * Status: Protected (Admin)
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/products",
  "description": "Tạo sản phẩm mới (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "multipart/form-data"
  },
  "body": {
    "name": "Sản phẩm mới",
    "description": "Mô tả sản phẩm",
    "price": 150000,
    "category": "507f1f77bcf86cd799439031",
    "stock": 100,
    "images": "[file1, file2, ...]"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439021",
      "name": "Sản phẩm mới",
      "price": 150000,
      "stock": 100
    },
    "EM": "Tạo thành công"
  }
}

/**
 * 19. Update Product (Admin only)
 * PUT /api/products/:id
 * Status: Protected (Admin)
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/products/507f1f77bcf86cd799439021",
  "description": "Cập nhật sản phẩm (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "name": "Sản phẩm cập nhật",
    "price": 120000,
    "stock": 80
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439021",
      "name": "Sản phẩm cập nhật",
      "price": 120000
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 20. Delete Product (Admin only)
 * DELETE /api/products/:id
 * Status: Protected (Admin)
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/products/507f1f77bcf86cd799439021",
  "description": "Xóa sản phẩm (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

/**
 * 21. Create Product Review
 * POST /api/products/:id/reviews
 * Status: Protected
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/products/507f1f77bcf86cd799439021/reviews",
  "description": "Đánh giá sản phẩm",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "rating": 5,
    "comment": "Sản phẩm tuyệt vời!"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439021",
      "rating": 4.7,
      "reviews": 15
    },
    "EM": "Đánh giá thành công"
  }
}

// ========================================
// CATEGORY ENDPOINTS
// ========================================

/**
 * 22. Get All Categories
 * GET /api/categories
 * Status: Public
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/categories",
  "description": "Lấy danh sách tất cả danh mục",
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439031",
        "name": "Điện thoại",
        "description": "Các thiết bị điện thoại",
        "image": "https://cloudinary.com/..."
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 23. Get Category Products
 * GET /api/categories/:id/products
 * Status: Public
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/categories/507f1f77bcf86cd799439031/products",
  "description": "Lấy danh sách sản phẩm trong danh mục",
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439021",
        "name": "Điện thoại Samsung",
        "price": 5000000
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 24. Create Category (Admin only)
 * POST /api/categories
 * Status: Protected (Admin)
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/categories",
  "description": "Tạo danh mục mới (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "name": "Máy tính",
    "description": "Các thiết bị máy tính"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439041",
      "name": "Máy tính",
      "description": "Các thiết bị máy tính"
    },
    "EM": "Tạo thành công"
  }
}

/**
 * 25. Update Category (Admin only)
 * PUT /api/categories/:id
 * Status: Protected (Admin)
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/categories/507f1f77bcf86cd799439031",
  "description": "Cập nhật danh mục (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "name": "Điện thoại di động",
    "description": "Các thiết bị điện thoại di động"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439031",
      "name": "Điện thoại di động"
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 26. Delete Category (Admin only)
 * DELETE /api/categories/:id
 * Status: Protected (Admin)
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/categories/507f1f77bcf86cd799439031",
  "description": "Xóa danh mục (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

// ========================================
// CART ENDPOINTS
// ========================================

/**
 * 27. Get User Cart
 * GET /api/carts
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/carts",
  "description": "Lấy giỏ hàng của người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439051",
      "user": "507f1f77bcf86cd799439011",
      "items": [
        {
          "_id": "507f1f77bcf86cd799439061",
          "product": "507f1f77bcf86cd799439021",
          "quantity": 2,
          "price": 100000,
          "finalPrice": 90000
        }
      ],
      "totalPrice": 180000,
      "totalDiscountPercent": 10
    },
    "EM": "Lấy thành công"
  }
}

/**
 * 28. Add To Cart
 * POST /api/carts
 * Status: Protected
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/carts",
  "description": "Thêm sản phẩm vào giỏ hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "productId": "507f1f77bcf86cd799439021",
    "quantity": 2
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439051",
      "items": [
        {
          "product": "507f1f77bcf86cd799439021",
          "quantity": 2,
          "price": 100000
        }
      ],
      "totalPrice": 200000
    },
    "EM": "Thêm vào giỏ thành công"
  }
}

/**
 * 29. Update Cart Item
 * PUT /api/carts/:itemId
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/carts/507f1f77bcf86cd799439061",
  "description": "Cập nhật số lượng sản phẩm trong giỏ",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "quantity": 5
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439051",
      "totalPrice": 450000
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 30. Remove From Cart
 * DELETE /api/carts/:itemId
 * Status: Protected
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/carts/507f1f77bcf86cd799439061",
  "description": "Xóa sản phẩm khỏi giỏ hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439051",
      "totalPrice": 0
    },
    "EM": "Xóa thành công"
  }
}

/**
 * 31. Clear Cart
 * DELETE /api/carts
 * Status: Protected
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/carts",
  "description": "Xóa toàn bộ giỏ hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

// ========================================
// DISCOUNT ENDPOINTS
// ========================================

/**
 * 32. Get All Discounts (Admin only)
 * GET /api/discounts
 * Status: Protected (Admin)
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/discounts",
  "description": "Lấy danh sách tất cả mã giảm giá (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439071",
        "code": "SUMMER10",
        "description": "Giảm 10% cho mùa hè",
        "type": "percent",
        "value": 10,
        "minOrder": 100000,
        "maxDiscount": 50000,
        "tierRequired": "all",
        "isActive": true
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 33. Get Available Discounts (User)
 * GET /api/discounts/available
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/discounts/available",
  "description": "Lấy danh sách mã giảm giá có sẵn cho người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439071",
        "code": "SUMMER10",
        "description": "Giảm 10%",
        "type": "percent",
        "value": 10,
        "tierRequired": "all",
        "minOrder": 100000
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 34. Validate Discount Code
 * POST /api/discounts/validate
 * Status: Protected
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/discounts/validate",
  "description": "Kiểm tra mã giảm giá có hợp lệ không",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "code": "SUMMER10",
    "totalPrice": 500000
  },
  "response": {
    "EC": 0,
    "DT": {
      "isValid": true,
      "discount": {
        "code": "SUMMER10",
        "type": "percent",
        "value": 10,
        "description": "Giảm 10%"
      },
      "discountAmount": 50000,
      "finalPrice": 450000
    },
    "EM": "Mã hợp lệ"
  }
}

/**
 * 35. Preview Discount
 * POST /api/discounts/preview
 * Status: Protected
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/discounts/preview",
  "description": "Xem trước kết quả áp dụng mã giảm giá",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "code": "SUMMER10",
    "totalPrice": 500000,
    "items": [
      {
        "productId": "507f1f77bcf86cd799439021",
        "quantity": 2,
        "price": 100000
      }
    ]
  },
  "response": {
    "EC": 0,
    "DT": {
      "originalPrice": 500000,
      "discountAmount": 50000,
      "finalPrice": 450000,
      "discountPercent": 10
    },
    "EM": "Xem trước thành công"
  }
}

/**
 * 36. Create Discount (Admin only)
 * POST /api/discounts
 * Status: Protected (Admin)
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/discounts",
  "description": "Tạo mã giảm giá mới (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "code": "NEWYEAR2025",
    "description": "Giảm 20% cho năm mới",
    "type": "percent",
    "value": 20,
    "minOrder": 200000,
    "maxDiscount": 100000,
    "tierRequired": "silver",
    "startAt": "2025-01-01",
    "endAt": "2025-01-31"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439081",
      "code": "NEWYEAR2025",
      "value": 20,
      "tierRequired": "silver"
    },
    "EM": "Tạo thành công"
  }
}

/**
 * 37. Update Discount (Admin only)
 * PUT /api/discounts/:id
 * Status: Protected (Admin)
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/discounts/507f1f77bcf86cd799439071",
  "description": "Cập nhật mã giảm giá (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "value": 15,
    "maxDiscount": 75000
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439071",
      "code": "SUMMER10",
      "value": 15
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 38. Delete Discount (Admin only)
 * DELETE /api/discounts/:id
 * Status: Protected (Admin)
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/discounts/507f1f77bcf86cd799439071",
  "description": "Xóa mã giảm giá (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

// ========================================
// ORDER ENDPOINTS
// ========================================

/**
 * 39. Create Order
 * POST /api/orders
 * Status: Protected
 */
{
  "method": "POST",
  "url": "http://localhost:5000/api/orders",
  "description": "Tạo đơn hàng mới",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "cartId": "507f1f77bcf86cd799439051",
    "shippingAddress": {
      "addressLine1": "123 Đường ABC",
      "city": "Hồ Chí Minh",
      "postalCode": "700000"
    },
    "paymentMethod": "COD",
    "discountCode": "SUMMER10"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439091",
      "orderCode": "0C791361",
      "user": "507f1f77bcf86cd799439011",
      "items": [
        {
          "product": "507f1f77bcf86cd799439021",
          "quantity": 2,
          "price": 100000,
          "finalPrice": 90000
        }
      ],
      "totalPrice": 180000,
      "finalPrice": 162000,
      "status": "Pending",
      "createdAt": "2025-12-03T10:00:00Z"
    },
    "EM": "Tạo đơn hàng thành công"
  }
}

/**
 * 40. Get All Orders (Admin only)
 * GET /api/orders
 * Status: Protected (Admin)
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/orders",
  "description": "Lấy danh sách tất cả đơn hàng (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439091",
        "orderCode": "0C791361",
        "user": {
          "_id": "507f1f77bcf86cd799439011",
          "fullName": "Nguyễn Văn A"
        },
        "totalPrice": 180000,
        "status": "Processing",
        "createdAt": "2025-12-03"
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 41. Get User Orders
 * GET /api/orders/user
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/orders/user",
  "description": "Lấy danh sách đơn hàng của người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439091",
        "orderCode": "0C791361",
        "totalPrice": 180000,
        "status": "Processing",
        "items": [
          {
            "product": {
              "_id": "507f1f77bcf86cd799439021",
              "name": "Sản phẩm A"
            },
            "quantity": 2
          }
        ]
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 42. Get Order By ID
 * GET /api/orders/:id
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/orders/507f1f77bcf86cd799439091",
  "description": "Lấy chi tiết đơn hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439091",
      "orderCode": "0C791361",
      "user": "507f1f77bcf86cd799439011",
      "items": [
        {
          "product": "507f1f77bcf86cd799439021",
          "quantity": 2,
          "price": 100000,
          "finalPrice": 90000
        }
      ],
      "totalPrice": 180000,
      "finalPrice": 162000,
      "status": "Processing",
      "shippingAddress": {
        "addressLine1": "123 Đường ABC",
        "city": "Hồ Chí Minh"
      },
      "createdAt": "2025-12-03T10:00:00Z"
    },
    "EM": "Lấy thành công"
  }
}

/**
 * 43. Update Order Status (Admin only)
 * PUT /api/orders/:id/status
 * Status: Protected (Admin)
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/orders/507f1f77bcf86cd799439091/status",
  "description": "Cập nhật trạng thái đơn hàng (chỉ admin)",
  "headers": {
    "Authorization": "Bearer YOUR_ADMIN_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "status": "Delivered"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439091",
      "status": "Delivered",
      "updatedAt": "2025-12-03T15:00:00Z"
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 44. Cancel Order
 * PUT /api/orders/:id/cancel
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/orders/507f1f77bcf86cd799439091/cancel",
  "description": "Hủy đơn hàng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN",
    "Content-Type": "application/json"
  },
  "body": {
    "reason": "Đổi ý"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439091",
      "status": "Cancelled"
    },
    "EM": "Hủy đơn hàng thành công"
  }
}

// ========================================
// NOTIFICATION ENDPOINTS
// ========================================

/**
 * 45. Get User Notifications
 * GET /api/notifications
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/notifications",
  "description": "Lấy danh sách thông báo của người dùng",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": [
      {
        "_id": "507f1f77bcf86cd799439101",
        "type": "order_status",
        "title": "Cập nhật đơn hàng",
        "message": "Đơn hàng của bạn đang được xử lý",
        "isRead": false,
        "createdAt": "2025-12-03T10:00:00Z"
      }
    ],
    "EM": "Lấy thành công"
  }
}

/**
 * 46. Get Unread Count
 * GET /api/notifications/unread-count
 * Status: Protected
 */
{
  "method": "GET",
  "url": "http://localhost:5000/api/notifications/unread-count",
  "description": "Lấy số lượng thông báo chưa đọc",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "unreadCount": 3
    },
    "EM": "Lấy thành công"
  }
}

/**
 * 47. Mark Notification As Read
 * PUT /api/notifications/:id/read
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/notifications/507f1f77bcf86cd799439101/read",
  "description": "Đánh dấu thông báo đã đọc",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "_id": "507f1f77bcf86cd799439101",
      "isRead": true,
      "readAt": "2025-12-03T10:05:00Z"
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 48. Mark All Notifications As Read
 * PUT /api/notifications
 * Status: Protected
 */
{
  "method": "PUT",
  "url": "http://localhost:5000/api/notifications",
  "description": "Đánh dấu tất cả thông báo đã đọc",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": {
      "modifiedCount": 3
    },
    "EM": "Cập nhật thành công"
  }
}

/**
 * 49. Delete Notification
 * DELETE /api/notifications/:id
 * Status: Protected
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/notifications/507f1f77bcf86cd799439101",
  "description": "Xóa thông báo",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 1 },
    "EM": "Xóa thành công"
  }
}

/**
 * 50. Delete All Notifications
 * DELETE /api/notifications
 * Status: Protected
 */
{
  "method": "DELETE",
  "url": "http://localhost:5000/api/notifications",
  "description": "Xóa tất cả thông báo",
  "headers": {
    "Authorization": "Bearer YOUR_JWT_TOKEN"
  },
  "response": {
    "EC": 0,
    "DT": { "deletedCount": 5 },
    "EM": "Xóa thành công"
  }
}

// ========================================
// SUMMARY
// ========================================
/*
Total APIs: 50

Group Breakdown:
- Authentication: 5 APIs (Endpoints 1-5)
- Users: 7 APIs (Endpoints 6-14 including loyalty)
- Products: 7 APIs (Endpoints 15-21)
- Categories: 5 APIs (Endpoints 22-26)
- Cart: 5 APIs (Endpoints 27-31)
- Discounts: 7 APIs (Endpoints 32-38)
- Orders: 6 APIs (Endpoints 39-44)
- Notifications: 6 APIs (Endpoints 45-50)

Status Types:
- Public: Anyone can access (no token required)
- Protected: Requires valid JWT token
- Protected (Admin): Requires admin JWT token

Response Format: All responses follow { EC, DT, EM } format
EC: Error Code (0 = success, -1 = error)
DT: Data (returned object or array)
EM: Error Message (success or error message in Vietnamese)
*/
