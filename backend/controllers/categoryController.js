const Category = require('../models/categoryModel');
const Product = require('../models/productModel');
const asyncHandler = require('express-async-handler');
const cloudinary = require('../config/cloudinary');
const streamifier = require('streamifier');

// @desc    Get all categories
// @route   GET /api/categories
// @access  Public
const getCategories = asyncHandler(async (req, res) => {
    const categories = await Category.find({});
    res.json(categories);
});

// @desc    Create a new category
// @route   POST /api/categories
// @access  Private/Admin
const createCategory = asyncHandler(async (req, res) => {
    const { name, description } = req.body;
    
    // Check if category exists
    const existingCategory = await Category.findOne({ name });
    if (existingCategory) {
        res.status(400);
        throw new Error('Danh mục đã tồn tại');
    }

    let imageUrl;
    
    // Upload image to cloudinary if exists
    if (req.file) {
        try {
            const result = await new Promise((resolve, reject) => {
                const uploadStream = cloudinary.uploader.upload_stream(
                    {
                        folder: 'categories',
                    },
                    (error, result) => {
                        if (error) reject(error);
                        else resolve(result);
                    }
                );
                streamifier.createReadStream(req.file.buffer).pipe(uploadStream);
            });
            imageUrl = result.secure_url;
        } catch (error) {
            res.status(500);
            throw new Error('Lỗi upload ảnh: ' + error.message);
        }
    }

    // Create category
    const category = await Category.create({
        name,
        description,
        ...(imageUrl && { image: imageUrl })
    });

    res.status(201).json(category);
});

// @desc    Update category
// @route   PUT /api/categories/:id
// @access  Private/Admin
const updateCategory = asyncHandler(async (req, res) => {
    const { name, description, image } = req.body;
    const category = await Category.findById(req.params.id);

    if (!category) {
        res.status(404);
        throw new Error('Danh mục không tồn tại');
    }

    // Check if new name conflicts with existing category
    if (name && name !== category.name) {
        const existingCategory = await Category.findOne({ name });
        if (existingCategory) {
            res.status(400);
            throw new Error('Tên danh mục đã tồn tại');
        }
    }

    category.name = name || category.name;
    category.description = description || category.description;
    category.image = image || category.image;

    const updatedCategory = await category.save();
    res.json(updatedCategory);
});

// @desc    Delete category
// @route   DELETE /api/categories/:id
// @access  Private/Admin
const deleteCategory = asyncHandler(async (req, res) => {
    const category = await Category.findById(req.params.id);

    if (!category) {
        res.status(404);
        throw new Error('Danh mục không tồn tại');
    }

    // Check if category has products
    const productsCount = await Product.countDocuments({ category: req.params.id });
    if (productsCount > 0) {
        res.status(400);
        throw new Error('Không thể xóa danh mục đang có sản phẩm. Vui lòng xóa hoặc chuyển các sản phẩm sang danh mục khác trước.');
    }

    await category.deleteOne();
    res.json({ message: 'Đã xóa danh mục thành công' });
});

// @desc    Get products by category ID
// @route   GET /api/categories/:id/products
// @access  Public
const getCategoryProducts = asyncHandler(async (req, res) => {
    const { page = 1, limit = 10, sortBy = 'createdAt', sortOrder = 'desc' } = req.query;
    
    const category = await Category.findById(req.params.id);
    if (!category) {
        res.status(404);
        throw new Error('Danh mục không tồn tại');
    }

    const skip = (page - 1) * limit;
    const sort = {};
    sort[sortBy] = sortOrder === 'desc' ? -1 : 1;

    const products = await Product.find({ 
        category: req.params.id,
        isActive: true 
    })
        .sort(sort)
        .skip(skip)
        .limit(Number(limit))
        .populate('category', 'name');

    const total = await Product.countDocuments({ 
        category: req.params.id,
        isActive: true 
    });

    res.json({
        products,
        page: Number(page),
        pages: Math.ceil(total / limit),
        total
    });
});

module.exports = { createCategory, getCategories, deleteCategory, updateCategory, getCategoryProducts };