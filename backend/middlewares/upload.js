const multer = require('multer');
const path = require('path');

// Storage configuration: save in ./uploads/avatars with original filename prefixed by timestamp
const storage = multer.diskStorage({
    destination: function (req, file, cb) {
        cb(null, path.join(__dirname, '..', 'uploads', 'avatars'));
    },
    filename: function (req, file, cb) {
        const ext = path.extname(file.originalname);
        const name = path.basename(file.originalname, ext).replace(/\s+/g, '_');
        cb(null, Date.now() + '_' + name + ext);
    }
});

// File filter - accept images only
function fileFilter (req, file, cb) {
    if (file.mimetype.startsWith('image/')) {
        cb(null, true);
    } else {
        cb(new Error('Only image files are allowed!'), false);
    }
}

const upload = multer({ storage, fileFilter });

module.exports = upload;
