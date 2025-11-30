const express = require('express');
const router = express.Router();
const { protect } = require('../middlewares/authMiddleware');
const {
  getNotifications,
  markAsRead,
  markAllAsRead,
  deleteNotification,
  deleteAllNotifications,
  getUnreadCount
} = require('../controllers/notificationController');

// Get unread count (phải đặt trước /:id để tránh conflict)
router.get('/unread-count', protect, getUnreadCount);

// Get user notifications
router.get('/', protect, getNotifications);

// Mark as read
router.put('/:id/read', protect, markAsRead);

// Mark all as read
router.put('/', protect, markAllAsRead);

// Delete notification
router.delete('/:id', protect, deleteNotification);

// Delete all notifications
router.delete('/', protect, deleteAllNotifications);

module.exports = router;
