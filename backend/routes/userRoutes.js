const express = require("express");
const router = express.Router();
const {
  getUserProfile,
  updateUserProfile,
  getUsers,
  deleteUser,
  updateShippingAddress,
  getShippingAddress,
} = require("../controllers/userController");
const { protect, admin } = require("../middlewares/authMiddleware");
const uploadMemory = require("../middlewares/multerMemory");

// Accept avatar upload as a single file named 'avatar' (stored in memory and uploaded to Cloudinary)
router
  .route("/profile")
  .get(protect, getUserProfile)
  .put(protect, uploadMemory.single("avatar"), updateUserProfile);

// Shipping address routes
router
  .route("/shipping-address")
  .get(protect, getShippingAddress)
  .put(protect, updateShippingAddress);

// Admin routes
router.route("/").get(protect, admin, getUsers);

router.route("/:id").delete(protect, admin, deleteUser);

module.exports = router;
