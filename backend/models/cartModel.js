const mongoose = require('mongoose');

const cartItemSchema = mongoose.Schema({
    product: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: 'Product'
    },
    quantity: {
        type: Number,
        required: true,
        min: 1
    }
});

const cartSchema = mongoose.Schema({
    user: {
        type: mongoose.Schema.Types.ObjectId,
        required: true,
        ref: 'User'
    },
    items: [cartItemSchema],
    totalPrice: {
        type: Number,
        required: true,
        default: 0
    }
}, {
    timestamps: true
});

// Middleware để tính totalPrice trước khi lưu
cartSchema.pre('save', async function(next) {
    if (this.items && this.items.length > 0) {
        const populatedCart = await this.constructor.findOne({ _id: this._id })
            .populate('items.product', 'price');
        
        if (populatedCart) {
            this.totalPrice = populatedCart.items.reduce((total, item) => {
                return total + (item.product.price * item.quantity);
            }, 0);
        }
    } else {
        this.totalPrice = 0;
    }
    next();
});

const Cart = mongoose.model('Cart', cartSchema);
module.exports = Cart;