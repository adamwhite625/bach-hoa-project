// const Discount = require('../models/discountModel');

// const normalizeBody = (body = {}) => {
//   const data = { ...body };
//   if (typeof data.code === 'string') data.code = data.code.trim().toUpperCase();
//   if (data.description === '') data.description = undefined;
//   return data;
// };

// const getDiscounts = async (req, res) => {
//   try {
//     const discounts = await Discount.find({}).sort({ createdAt: -1 });
//     res.json(discounts);
//   } catch (error) {
//     res.status(500).json({ message: error.message });
//   }
// };
 
// const createDiscount = async (req, res) => {
//   try {
//     const payload = normalizeBody(req.body);
//     const discount = new Discount(payload);
//     const created = await discount.save();
//     res.status(201).json(created);
//   } catch (error) {
//     res.status(400).json({ message: error.message });
//   }
// };

// const updateDiscount = async (req, res) => {
//   try {
//     const payload = normalizeBody(req.body);
//     const discount = await Discount.findById(req.params.id);
//     if (!discount) {
//       return res.status(404).json({ message: 'Discount not found' });
//     }

//     Object.assign(discount, payload);
//     const updated = await discount.save();
//     res.json(updated);
//   } catch (error) {
//     res.status(400).json({ message: error.message });
//   }
// };

// const deleteDiscount = async (req, res) => {
//   try {
//     const discount = await Discount.findById(req.params.id);
//     if (!discount) {
//       return res.status(404).json({ message: 'Discount not found' });
//     }
//     await discount.deleteOne();
//     res.json({ message: 'Discount deleted' });
//   } catch (error) {
//     res.status(500).json({ message: error.message });
//   }
// };

// module.exports = {
//   getDiscounts,
//   createDiscount,
//   updateDiscount,
//   deleteDiscount
// };


const Discount = require('../models/discountModel');
const DiscountUsage = require('../models/discountUsageModel');
const Product = require('../models/productModel');
const User = require('../models/userModel');

// Helper: check if user is eligible for discount
const isUserEligible = async (discount, userId) => {
  if (!userId) return false;
  
  if (discount.userType === 'all') return true;
  
  if (discount.userType === 'specific') {
    return discount.allowedUsers.some(id => String(id) === String(userId));
  }
  
  if (discount.userType === 'new' || discount.userType === 'vip') {
    const user = await User.findById(userId);
    if (!user) return false;
    // Check customerTier field for new/vip/regular
    return user.customerTier === discount.userType;
  }
  
  return false;
};

// Helper: calculate discount amount
const calculateDiscountAmount = (discount, orderValue) => {
  let amount = 0;
  
  if (discount.type === 'percent') {
    amount = Math.round(orderValue * (discount.value / 100));
  } else {
    amount = discount.value;
  }
  
  // Apply max discount cap if set
  if (discount.maxDiscount && amount > discount.maxDiscount) {
    amount = discount.maxDiscount;
  }
  
  return amount;
};

const normalizeBody = (body = {}) => {
  const data = { ...body };
  if (typeof data.code === 'string') data.code = data.code.trim().toUpperCase();
  if (data.description === '') data.description = undefined;
  // numeric coercion
  ['value','minOrder','maxDiscount','usageLimit','perUserLimit'].forEach(k => {
    if (data[k] !== undefined && data[k] !== null && data[k] !== '') {
      data[k] = Number(data[k]);
    }
  });
  return data;
};

// Admin: Get all discounts
const getDiscounts = async (req, res) => {
  try {
    const { code, active } = req.query;
    const filter = {};
    if (code) filter.code = code.trim().toUpperCase();
    if (active !== undefined) filter.isActive = active === '1' || active === 'true';
    const discounts = await Discount.find(filter).sort({ createdAt: -1 });
    res.json(discounts);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// Admin: Create discount
const createDiscount = async (req, res) => {
  try {
    const payload = normalizeBody(req.body);

    // basic validation
    if (!payload.code) return res.status(400).json({ message: 'Mã không được trống' });
    if (!['percent','fixed'].includes(payload.type)) return res.status(400).json({ message: 'Loại mã không hợp lệ' });
    if (payload.type === 'percent' && (payload.value < 0 || payload.value > 100))
      return res.status(400).json({ message: 'Phần trăm phải từ 0-100' });

    const created = await Discount.create(payload);
    res.status(201).json(created);
  } catch (error) {
    if (error.code === 11000) {
      return res.status(400).json({ message: 'Mã đã tồn tại' });
    }
    res.status(400).json({ message: error.message });
  }
};

// Admin: Update discount
const updateDiscount = async (req, res) => {
  try {
    const payload = normalizeBody(req.body);
    const discount = await Discount.findById(req.params.id);
    if (!discount) {
      return res.status(404).json({ message: 'Không tìm thấy mã' });
    }

    Object.assign(discount, payload);
    // validations again
    if (!discount.code) return res.status(400).json({ message: 'Mã không được trống' });
    if (!['percent','fixed'].includes(discount.type)) return res.status(400).json({ message: 'Loại mã không hợp lệ' });
    if (discount.type === 'percent' && (discount.value < 0 || discount.value > 100))
      return res.status(400).json({ message: 'Phần trăm phải từ 0-100' });

    const updated = await discount.save();
    res.json(updated);
  } catch (error) {
    if (error.code === 11000) {
      return res.status(400).json({ message: 'Mã đã tồn tại' });
    }
    res.status(400).json({ message: error.message });
  }
};

// Admin: Delete discount
const deleteDiscount = async (req, res) => {
  try {
    const discount = await Discount.findById(req.params.id);
    if (!discount) {
      return res.status(404).json({ message: 'Không tìm thấy mã' });
    }
    await discount.deleteOne();
    res.json({ message: 'Đã xoá' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// User: Get available discounts for current user
const getAvailableDiscounts = async (req, res) => {
  try {
    const userId = req.user.id;
    const now = new Date();
    
    // Get all active discounts
    const allDiscounts = await Discount.find({
      isActive: true,
      $or: [
        { startAt: { $exists: false } },
        { startAt: null },
        { startAt: { $lte: now } }
      ],
      $or: [
        { endAt: { $exists: false } },
        { endAt: null },
        { endAt: { $gte: now } }
      ]
    });
    
    // Filter by user eligibility
    const eligible = [];
    for (const discount of allDiscounts) {
      // Check global usage limit
      if (discount.usageLimit && discount.usedCount >= discount.usageLimit) continue;
      
      // Check user eligibility
      const isEligible = await isUserEligible(discount, userId);
      if (!isEligible) continue;
      
      // Check per-user limit
      if (discount.perUserLimit) {
        const userUsageCount = await DiscountUsage.countDocuments({
          discount: discount._id,
          user: userId
        });
        if (userUsageCount >= discount.perUserLimit) continue;
      }
      
      eligible.push(discount);
    }
    
    res.json(eligible);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// User: Validate discount code
const validateDiscount = async (req, res) => {
  try {
    const { code } = req.body;
    const userId = req.user.id;
    
    const discount = await Discount.findOne({ 
      code: String(code || '').toUpperCase(),
      isActive: true 
    });
    
    if (!discount) {
      return res.status(404).json({ message: 'Mã không tồn tại hoặc không hoạt động' });
    }
    
    const now = new Date();
    
    // Check time validity
    if (discount.startAt && now < discount.startAt) {
      return res.status(400).json({ message: 'Chưa đến thời gian áp dụng' });
    }
    if (discount.endAt && now > discount.endAt) {
      return res.status(400).json({ message: 'Mã đã hết hạn' });
    }
    
    // Check global usage limit
    if (discount.usageLimit && discount.usedCount >= discount.usageLimit) {
      return res.status(400).json({ message: 'Mã đã đạt giới hạn sử dụng' });
    }
    
    // Check user eligibility
    const isEligible = await isUserEligible(discount, userId);
    if (!isEligible) {
      return res.status(403).json({ message: 'Bạn không đủ điều kiện sử dụng mã này' });
    }
    
    // Check per-user limit
    if (discount.perUserLimit) {
      const userUsageCount = await DiscountUsage.countDocuments({
        discount: discount._id,
        user: userId
      });
      if (userUsageCount >= discount.perUserLimit) {
        return res.status(400).json({ message: 'Bạn đã sử dụng hết lượt cho mã này' });
      }
    }
    
    res.json({
      valid: true,
      discount: {
        code: discount.code,
        type: discount.type,
        value: discount.value,
        minOrder: discount.minOrder,
        maxDiscount: discount.maxDiscount
      }
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// User: Preview discount application on cart
const previewDiscount = async (req, res) => {
  try {
    const { code, items = [], subtotal = 0 } = req.body;
    const userId = req.user?.id;
    
    const coupon = await Discount.findOne({ 
      code: String(code || '').toUpperCase(), 
      isActive: true 
    });
    
    if (!coupon) {
      return res.status(404).json({ message: 'Mã không tồn tại hoặc không hoạt động' });
    }

    const now = new Date();
    if (coupon.startAt && now < coupon.startAt) {
      return res.status(400).json({ message: 'Chưa đến thời gian áp dụng' });
    }
    if (coupon.endAt && now > coupon.endAt) {
      return res.status(400).json({ message: 'Mã đã hết hạn' });
    }

    if (coupon.usageLimit && coupon.usedCount >= coupon.usageLimit) {
      return res.status(400).json({ message: 'Mã đã đạt giới hạn sử dụng' });
    }
    
    // Check user eligibility if userId provided
    if (userId) {
      const isEligible = await isUserEligible(coupon, userId);
      if (!isEligible) {
        return res.status(403).json({ message: 'Bạn không đủ điều kiện sử dụng mã này' });
      }
      
      if (coupon.perUserLimit) {
        const userUsageCount = await DiscountUsage.countDocuments({
          discount: coupon._id,
          user: userId
        });
        if (userUsageCount >= coupon.perUserLimit) {
          return res.status(400).json({ message: 'Bạn đã sử dụng hết lượt cho mã này' });
        }
      }
    }

    // scope validation
    const productIds = items.map(i => i.product).filter(Boolean);
    const categoryIds = items.map(i => i.category).filter(Boolean);

    const inProducts = !coupon.applicableProducts?.length ||
      productIds.some(id => coupon.applicableProducts.map(x => String(x)).includes(String(id)));
    const inCategories = !coupon.applicableCategories?.length ||
      categoryIds.some(id => coupon.applicableCategories.map(x => String(x)).includes(String(id)));

    const excludedProducts = coupon.excludeProducts?.length &&
      productIds.some(id => coupon.excludeProducts.map(x => String(x)).includes(String(id)));
    const excludedCategories = coupon.excludeCategories?.length &&
      categoryIds.some(id => coupon.excludeCategories.map(x => String(x)).includes(String(id)));

    if ((!inProducts && !inCategories) || excludedProducts || excludedCategories) {
      return res.status(400).json({ message: 'Mã không áp dụng cho giỏ hàng này' });
    }

    if (coupon.minOrder && subtotal < coupon.minOrder) {
      return res.status(400).json({ message: `Chưa đạt giá trị tối thiểu ${coupon.minOrder.toLocaleString()}₫` });
    }

    const discountValue = calculateDiscountAmount(coupon, subtotal);

    res.json({
      code: coupon.code,
      discount: discountValue,
      subtotal,
      total: Math.max(0, subtotal - discountValue),
    });
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

module.exports = {
  getDiscounts,
  createDiscount,
  updateDiscount,
  deleteDiscount,
  getAvailableDiscounts,
  validateDiscount,
  previewDiscount,
  isUserEligible,
  calculateDiscountAmount,
};