const Product = require('../models/productModel');
const cloudinary = require('../config/cloudinary');
const streamifier = require('streamifier');

// @desc    Search products with filters and sorting
// @route   GET /api/products/search
// @access  Public
const searchProducts = async (req, res) => {
    try {
        const { 
            keyword,
            category,
            minPrice,
            maxPrice,
            sortBy = 'createdAt',
            sortOrder = 'desc',
            page = 1,
            limit = 10
        } = req.query;

        // Build query
        const query = { isActive: true };

        // Search by keyword in name or description
        if (keyword) {
            query.$or = [
                { name: { $regex: keyword, $options: 'i' } },
                { description: { $regex: keyword, $options: 'i' } }
            ];
        }

        // Filter by category
        if (category) {
            query.category = category;
        }

        // Filter by price range
        if (minPrice !== undefined || maxPrice !== undefined) {
            query.price = {};
            if (minPrice !== undefined) query.price.$gte = Number(minPrice);
            if (maxPrice !== undefined) query.price.$lte = Number(maxPrice);
        }

        // Calculate skip for pagination
        const skip = (page - 1) * limit;

        // Build sort object
        const sort = {};
        sort[sortBy] = sortOrder === 'desc' ? -1 : 1;

        // Execute query with pagination
        const products = await Product.find(query)
            .populate('category', 'name')
            .sort(sort)
            .skip(skip)
            .limit(Number(limit));

        // Get total count for pagination
        const total = await Product.countDocuments(query);

        res.json({
            products,
            page: Number(page),
            pages: Math.ceil(total / limit),
            total
        });
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

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
            name, sku, description, images: images || [], brand, category, price, quantity, user: req.user._id,
        });

        // If files uploaded via multer memory storage
        if (req.files && req.files.length > 0) {
            const uploadPromises = req.files.map(file => {
                return new Promise((resolve, reject) => {
                    const stream = cloudinary.uploader.upload_stream({ folder: 'products' }, (error, result) => {
                        if (error) return reject(error);
                        resolve(result.secure_url);
                    });
                    streamifier.createReadStream(file.buffer).pipe(stream);
                });
            });

            try {
                const uploadedUrls = await Promise.all(uploadPromises);
                product.images = product.images.concat(uploadedUrls);
            } catch (err) {
                console.error('Cloudinary product images upload error:', err);
            }
        }
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

// @route   PUT /api/products/:id
const updateProduct = async (req, res) => {
    try {
        const { name, sku, description, images, brand, category, price, quantity, isActive } = req.body;
        
        const product = await Product.findById(req.params.id);

        if (product) {
            // Cập nhật các trường có trong body
            product.name = name || product.name;
            product.sku = sku || product.sku;
            product.description = description || product.description;
            product.images = images || product.images;

            // If new files uploaded, upload to Cloudinary and append
            if (req.files && req.files.length > 0) {
                const uploadPromises = req.files.map(file => {
                    return new Promise((resolve, reject) => {
                        const stream = cloudinary.uploader.upload_stream({ folder: 'products' }, (error, result) => {
                            if (error) return reject(error);
                            resolve(result.secure_url);
                        });
                        streamifier.createReadStream(file.buffer).pipe(stream);
                    });
                });

                try {
                    const uploadedUrls = await Promise.all(uploadPromises);
                    product.images = product.images.concat(uploadedUrls);
                } catch (err) {
                    console.error('Cloudinary product images upload error:', err);
                }
            }
            product.brand = brand || product.brand;
            product.category = category || product.category;
            product.price = price !== undefined ? price : product.price;
            product.quantity = quantity !== undefined ? quantity : product.quantity;
            product.isActive = isActive !== undefined ? isActive : product.isActive;

            const updatedProduct = await product.save();
            res.json(updatedProduct);
        } else {
            res.status(404).json({ message: 'Product not found' });
        }
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

// @route   DELETE /api/products/:id
const deleteProduct = async (req, res) => {
    try {
        const product = await Product.findById(req.params.id);

        if (product) {
            // Chỉ ẩn sản phẩm thay vì xóa
            product.isActive = false;
            await product.save();

            res.json({ message: 'Product deleted (set inactive)' });
        } else {
            res.status(404).json({ message: 'Product not found' });
        }
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

module.exports = { 
    getProducts, 
    getProductById, 
    createProduct, 
    createProductReview, 
    updateProduct, 
    deleteProduct,
    searchProducts 
};
