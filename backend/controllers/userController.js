const User = require('../models/userModel');

const getUserProfile = async (req, res) => {
    const user = await User.findById(req.user._id);
    if (user) {
        res.json({
            _id: user._id,
            firstName: user.firstName,
            lastName: user.lastName,
            email: user.email,
            role: user.role,
            addresses: user.addresses,
        });
    } else {
        res.status(404).json({ message: 'User not found' });
    }
};

const updateUserProfile = async (req, res) => {
    const user = await User.findById(req.user._id);
    if (user) {
        user.firstName = req.body.firstName || user.firstName;
        user.lastName = req.body.lastName || user.lastName;
        user.email = req.body.email || user.email;
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
        });
    } else {
        res.status(404).json({ message: 'User not found' });
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
        res.json({ message: 'User removed' });
    } else {
        res.status(404).json({ message: 'User not found' });
    }
};

module.exports = { getUserProfile, updateUserProfile, getUsers, deleteUser };