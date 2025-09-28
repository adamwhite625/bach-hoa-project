const Product = require('../models/productModel');

const getProducts = async (req, res) => {
    try {
        const products = await Product.find({ isActive: true }).populate('category', 'name');
        res.json(products);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const getProductById = async (req, res) => {
    try {
        const product = await Product.findById(req.params.id);
        if (product) {
            res.json(product);
        } else {
            res.status(404).json({ message: 'Product not found' });
        }
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

const createProduct = async (req, res) => {
    try {
        const { name, sku, description, images, brand, category, price, quantity } = req.body;
        const product = new Product({
            name, sku, description, images, brand, category, price, quantity, user: req.user._id,
        });
        const createdProduct = await product.save();
        res.status(201).json(createdProduct);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
};

const createProductReview = async (req, res) => {
    const { rating, comment } = req.body;
    const product = await Product.findById(req.params.id);

    if (product) {
        const alreadyReviewed = product.reviews.find(r => r.user.toString() === req.user._id.toString());
        if(alreadyReviewed) {
            return res.status(400).json({ message: 'Product already reviewed' });
        }

        const review = {
            name: `${req.user.firstName} ${req.user.lastName}`,
            rating: Number(rating),
            comment,
            user: req.user._id
        };
        product.reviews.push(review);
        product.numReviews = product.reviews.length;
        product.rating = product.reviews.reduce((acc, item) => item.rating + acc, 0) / product.reviews.length;
        
        await product.save();
        res.status(201).json({ message: 'Review added'});
    } else {
        res.status(404).json({ message: 'Product not found' });
    }
};

module.exports = { getProducts, getProductById, createProduct, createProductReview };
