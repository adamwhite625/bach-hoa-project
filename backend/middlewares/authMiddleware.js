const jwt = require('jsonwebtoken');
const User = require('../models/userModel');

const protect = async (req, res, next) => {
    let token;
    try {
        if (req.headers.authorization && req.headers.authorization.startsWith('Bearer')) {
            token = req.headers.authorization.split(' ')[1];

            const decoded = jwt.verify(token, process.env.JWT_SECRET);

            // Support both payload shapes:
            // - old: { id: '...' }
            // - new: { user: { id: '...', ... } }
            const userId = decoded?.user?.id || decoded?.id;
            if (!userId) {
                return res.status(401).json({ message: 'Not authorized, token invalid' });
            }

            req.user = await User.findById(userId).select('-password');
            next();
        } else {
            res.status(401).json({ message: 'Not authorized, no token' });
        }
    } catch (error) {
        console.error(error);
        res.status(401).json({ message: 'Not authorized, token failed' });
    }
};

const admin = (req, res, next) => {
    try {
        if (req.user && req.user.role && req.user.role.toString().toLowerCase() === 'admin') {
            next();
        } else {
            res.status(403).json({ message: 'Not authorized as an admin' });
        }
    } catch (e) {
        res.status(403).json({ message: 'Not authorized as an admin' });
    }
};

module.exports = { protect, admin };
