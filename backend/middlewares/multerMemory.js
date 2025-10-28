const multer = require('multer');

// Use memory storage so we can stream buffer to Cloudinary
const storage = multer.memoryStorage();
const upload = multer({ storage });

module.exports = upload;
