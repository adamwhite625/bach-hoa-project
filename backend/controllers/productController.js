const Product = require('../models/productModel');
const cloudinary = require('../config/cloudinary');
const streamifier = require('streamifier');

const normalizeImagePayload = (list = []) => {
    if (!Array.isArray(list)) return [];
    return list
        .filter(Boolean)
        .map((item) => {
            if (typeof item === 'string') return { url: item };
            if (item?.url) return { url: item.url };
            return null;
        })
        .filter(Boolean);
};

// @desc    Search products with filters and sorting
// @route   GET /api/products/search
// @access  Public
const escapeRegex = (str = '') => str.replace(/[.*+?^${}()|[\]\\]/g, '\\$&');

const searchProducts = async (req, res) => {
    try {
        const {
            keyword = '',
            category,
            minPrice,
            maxPrice,
            sortBy = 'createdAt',
            sortOrder = 'desc',
            page = 1,
            limit = 10
        } = req.query;

        const pageNum = Math.max(1, parseInt(page, 10) || 1);
        const limitNum = Math.min(100, Math.max(1, parseInt(limit, 10) || 10));

        const allowedSort = ['createdAt', 'price', 'rating', 'name'];
        const finalSortBy = allowedSort.includes(sortBy) ? sortBy : 'createdAt';
        const finalSortOrder = sortOrder === 'asc' ? 1 : -1;

        const query = { isActive: true };

        if (keyword.trim()) {
            const safe = escapeRegex(keyword.trim());
            query.name = { $regex: safe, $options: 'i' };
        }

        if (category) {
            query.category = category;
        }

        if (minPrice !== undefined || maxPrice !== undefined) {
            query.price = {};
            if (minPrice !== undefined) query.price.$gte = Number(minPrice);
            if (maxPrice !== undefined) query.price.$lte = Number(maxPrice);
        }

        const skip = (pageNum - 1) * limitNum;
        const sort = { [finalSortBy]: finalSortOrder };

        const [products, total] = await Promise.all([
            Product.find(query)
                .populate('category', 'name')
                .sort(sort)
                .skip(skip)
                .limit(limitNum),
            Product.countDocuments(query)
        ]);

        res.json({
            products,
            page: pageNum,
            pages: Math.ceil(total / limitNum),
            total,
            limit: limitNum,
            sortBy: finalSortBy,
            sortOrder: finalSortOrder === 1 ? 'asc' : 'desc'
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
        const { name, sku, description, images, detailImages, brand, category, price, quantity } = req.body;
        const product = new Product({
            name,
            sku,
            description,
            image: req.body.image,
            images: normalizeImagePayload(images),
            detailImages: normalizeImagePayload(detailImages),
            brand,
            category,
            price,
            quantity,
            user: req.user._id,
        });

        if (!product.image) {
            const fallbackImage = product.images?.[0]?.url || product.detailImages?.[0]?.url || '';
            if (fallbackImage) {
                product.image = fallbackImage;
            }
        }

        if (!product.image && product.images?.length) {
            product.image = product.images[0].url;
        }

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
                product.images = product.images.concat(uploadedUrls.map((url) => ({ url })));
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
        const { name, sku, description, images, detailImages, brand, category, price, quantity, isActive } = req.body;
        
        const product = await Product.findById(req.params.id);

        if (product) {
            // Cập nhật các trường có trong body
            product.name = name || product.name;
            product.sku = sku || product.sku;
            product.description = description || product.description;
            if (images !== undefined) {
                product.images = normalizeImagePayload(images);
            }
            if (req.body.image) {
                product.image = req.body.image;
            }
            if (detailImages !== undefined) {
                product.detailImages = normalizeImagePayload(detailImages);
            }

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
                    product.images = product.images.concat(uploadedUrls.map((url) => ({ url })));
                } catch (err) {
                    console.error('Cloudinary product images upload error:', err);
                }
            }
            product.brand = brand || product.brand;
            product.category = category || product.category;
            product.price = price !== undefined ? price : product.price;
            product.quantity = quantity !== undefined ? quantity : product.quantity;
            product.isActive = isActive !== undefined ? isActive : product.isActive;

            if (product.images?.length) {
                product.image = product.images[0].url;
            } else if (!product.image && product.detailImages?.length) {
                product.image = product.detailImages[0].url;
            }

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
