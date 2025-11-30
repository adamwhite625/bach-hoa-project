const mongoose = require('mongoose');

const notificationSchema = mongoose.Schema({
  user: { 
    type: mongoose.Schema.Types.ObjectId, 
    required: true, 
    ref: 'User' 
  },
  type: { 
    type: String, 
    enum: ['order_status', 'order_created', 'promotion', 'system'], 
    default: 'order_status' 
  },
  title: { type: String, required: true },
  message: { type: String, required: true },
  data: { type: mongoose.Schema.Types.Mixed }, // Extra data (orderId, etc.)
  isRead: { type: Boolean, default: false },
  readAt: { type: Date },
}, { timestamps: true });

const Notification = mongoose.model('Notification', notificationSchema);
module.exports = Notification;
