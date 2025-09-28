const mongoose = require('mongoose');

const reviewSchema = mongoose.Schema({
    user: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'User' },
    name: { type: String, required: true },
    rating: { type: Number, required: true }, // 1 to 5 stars
    comment: { type: String, required: true }
}, { timestamps: true });

const productSchema = mongoose.Schema({
    name: { type: String, required: true, trim: true },
    sku: { type: String, required: true, unique: true },
    description: { type: String, required: true },
    images: [{ type: String, required: true }],
    brand: { type: String },
    category: { type: mongoose.Schema.Types.ObjectId, required: true, ref: 'Category' },
    price: { type: Number, required: true, default: 0 },
    quantity: { type: Number, required: true, default: 0 },
    reviews: [reviewSchema],
    rating: { type: Number, required: true, default: 0 },
    numReviews: { type: Number, required: true, default: 0 },
    isActive: { type: Boolean, default: true }
}, { timestamps: true });

const Product = mongoose.model('Product', productSchema);
module.exports = Product;
