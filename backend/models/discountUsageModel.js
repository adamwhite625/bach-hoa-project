const mongoose = require('mongoose');

const discountUsageSchema = new mongoose.Schema({
  discount: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'Discount', 
    required: true 
  },
  user: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'User', 
    required: true 
  },
  order: { 
    type: mongoose.Schema.Types.ObjectId, 
    ref: 'Order' 
  },
  usedAt: { 
    type: Date, 
    default: Date.now 
  },
  discountAmount: { 
    type: Number, 
    required: true 
  },
}, { timestamps: true });

// Index để query nhanh
discountUsageSchema.index({ discount: 1, user: 1 });
discountUsageSchema.index({ user: 1, usedAt: -1 });

module.exports = mongoose.model('DiscountUsage', discountUsageSchema);
