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

// const protect = async (req, res, next) => {
//     let token;
    
//     try {
//         // Check Authorization header exists
//         if (!req.headers.authorization || !req.headers.authorization.startsWith('Bearer')) {
//             return res.status(401).json({ message: 'Not authorized, no token' });
//         }

//         // Extract token (remove "Bearer " prefix)
//         const parts = req.headers.authorization.split(' ');
        
//         // Validate we have exactly 2 parts
//         if (parts.length !== 2) {
//             return res.status(401).json({ message: 'Not authorized, malformed header' });
//         }
        
//         token = parts[1];
        
//         // Validate token is not empty or literal strings
//         if (!token || token === 'null' || token === 'undefined' || token.startsWith('Bearer')) {
//             console.error('Invalid token detected:', token);
//             return res.status(401).json({ message: 'Not authorized, invalid token format' });
//         }

//         // Log for debugging (remove in production)
//         console.log('Token received (first 30 chars):', token.substring(0, 30) + '...');

//         // Verify token
//         const decoded = jwt.verify(token, process.env.JWT_SECRET);
//         console.log('Decoded payload:', decoded);

//         // Support both payload shapes
//         const userId = decoded?.user?.id || decoded?.id;
//         if (!userId) {
//             return res.status(401).json({ message: 'Not authorized, invalid token payload' });
//         }

//         // Find user
//         req.user = await User.findById(userId).select('-password');
        
//         if (!req.user) {
//             return res.status(401).json({ message: 'Not authorized, user not found' });
//         }

//         next();
//     } catch (error) {
//         console.error('Auth error details:', {
//             name: error.name,
//             message: error.message,
//             tokenPreview: token ? token.substring(0, 50) + '...' : 'No token'
//         });

//         // Specific error handling
//         if (error.name === 'TokenExpiredError') {
//             return res.status(401).json({ message: 'Token expired, please login again' });
//         }
//         if (error.name === 'JsonWebTokenError') {
//             return res.status(401).json({ 
//                 message: 'Invalid token format',
//                 details: error.message 
//             });
//         }
        
//         res.status(401).json({ message: 'Not authorized, token failed' });
//     }
// };

const admin = (req, res, next) => {
    if (req.user && req.user.role.toLowerCase() === 'admin') {
        next();
    } else {
        res.status(403).json({ message: 'Not authorized as an admin' });
    }
};

module.exports = { protect, admin };
