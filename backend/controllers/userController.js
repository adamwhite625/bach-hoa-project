const User = require("../models/userModel");
const cloudinary = require("../config/cloudinary");
const streamifier = require("streamifier");

const getUserProfile = async (req, res) => {
  const user = await User.findById(req.user._id);
  if (user) {
    res.json({
      _id: user._id,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email,
      role: user.role,
      gender: user.gender,
      avatar: user.avatar,
      shippingAddress: user.shippingAddress,
    });
  } else {
    res.status(404).json({ message: "User not found" });
  }
};

const updateUserProfile = async (req, res) => {
  const user = await User.findById(req.user._id);
  if (user) {
    user.firstName = req.body.firstName || user.firstName;
    user.lastName = req.body.lastName || user.lastName;
    user.email = req.body.email || user.email;
    user.gender = req.body.gender || user.gender;
    // If an avatar file was uploaded (multerMemory puts buffer on req.file)
    if (req.file && req.file.buffer) {
      // Upload buffer to Cloudinary
      const uploadFromBuffer = (buffer) => {
        return new Promise((resolve, reject) => {
          const stream = cloudinary.uploader.upload_stream(
            { folder: "avatars" },
            (error, result) => {
              if (error) return reject(error);
              resolve(result);
            }
          );
          streamifier.createReadStream(buffer).pipe(stream);
        });
      };

      try {
        const result = await uploadFromBuffer(req.file.buffer);
        user.avatar = result.secure_url;
      } catch (err) {
        console.error("Cloudinary avatar upload error:", err);
      }
    }
    if (req.body.password) {
      user.password = req.body.password;
    }
    const updatedUser = await user.save();
    res.json({
      _id: updatedUser._id,
      firstName: updatedUser.firstName,
      lastName: updatedUser.lastName,
      email: updatedUser.email,
      role: updatedUser.role,
      gender: updatedUser.gender,
      avatar: updatedUser.avatar,
    });
  } else {
    res.status(404).json({ message: "User not found" });
  }
};

const getUsers = async (req, res) => {
  const users = await User.find({});
  res.json(users);
};

const deleteUser = async (req, res) => {
  const user = await User.findById(req.params.id);
  if (user) {
    await user.deleteOne();
    res.json({ message: "User removed" });
  } else {
    res.status(404).json({ message: "User not found" });
  }
};

// Lấy địa chỉ giao hàng
const getShippingAddress = async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    if (!user.shippingAddress) {
      return res.status(404).json({
        message: "Chưa có địa chỉ giao hàng",
        hasShippingAddress: false,
      });
    }

    res.json({
      hasShippingAddress: true,
      shippingAddress: user.shippingAddress,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

// Cập nhật địa chỉ giao hàng
const updateShippingAddress = async (req, res) => {
  try {
    const user = await User.findById(req.user._id);
    if (!user) {
      return res.status(404).json({ message: "User not found" });
    }

    const { fullName, phone, address, city } = req.body;

    // Validate thông tin
    if (!fullName || !phone || !address || !city) {
      return res.status(400).json({
        message: "Vui lòng điền đầy đủ thông tin địa chỉ giao hàng",
      });
    }

    user.shippingAddress = {
      fullName,
      phone,
      address,
      city,
    };

    await user.save();
    res.json({
      message: "Cập nhật địa chỉ giao hàng thành công",
      shippingAddress: user.shippingAddress,
    });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getUserProfile,
  updateUserProfile,
  getUsers,
  deleteUser,
  updateShippingAddress,
  getShippingAddress,
};
