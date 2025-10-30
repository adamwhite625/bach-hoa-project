const express = require('express');
const router = express.Router();
const { getUserProfile, updateUserProfile, getUsers, deleteUser } = require('../controllers/userController');
const { protect, admin } = require('../middlewares/authMiddleware');
const uploadMemory = require('../middlewares/multerMemory');

// Accept avatar upload as a single file named 'avatar' (stored in memory and uploaded to Cloudinary)
router.route('/profile').get(protect, getUserProfile).put(protect, uploadMemory.single('avatar'), updateUserProfile);
router.route('/').get(protect, admin, getUsers);
router.route('/:id').delete(protect, admin, deleteUser);

module.exports = router;