const Category = require('../models/categoryModel');

const createCategory = async (req, res) => {
    const { name, description, image } = req.body;
    try {
        const category = new Category({ name, description, image });
        const createdCategory = await category.save();
        res.status(201).json(createdCategory);
    } catch (error) {
        res.status(400).json({ message: error.message });
    }
};

const getCategories = async (req, res) => {
    try {
        const categories = await Category.find({});
        res.json(categories);
    } catch (error) {
        res.status(500).json({ message: error.message });
    }
};

module.exports = { createCategory, getCategories };