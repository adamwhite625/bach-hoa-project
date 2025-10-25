const Discount = require('../models/discountModel');

const normalizeBody = (body = {}) => {
  const data = { ...body };
  if (typeof data.code === 'string') data.code = data.code.trim().toUpperCase();
  if (data.description === '') data.description = undefined;
  return data;
};

const getDiscounts = async (req, res) => {
  try {
    const discounts = await Discount.find({}).sort({ createdAt: -1 });
    res.json(discounts);
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

const createDiscount = async (req, res) => {
  try {
    const payload = normalizeBody(req.body);
    const discount = new Discount(payload);
    const created = await discount.save();
    res.status(201).json(created);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const updateDiscount = async (req, res) => {
  try {
    const payload = normalizeBody(req.body);
    const discount = await Discount.findById(req.params.id);
    if (!discount) {
      return res.status(404).json({ message: 'Discount not found' });
    }

    Object.assign(discount, payload);
    const updated = await discount.save();
    res.json(updated);
  } catch (error) {
    res.status(400).json({ message: error.message });
  }
};

const deleteDiscount = async (req, res) => {
  try {
    const discount = await Discount.findById(req.params.id);
    if (!discount) {
      return res.status(404).json({ message: 'Discount not found' });
    }
    await discount.deleteOne();
    res.json({ message: 'Discount deleted' });
  } catch (error) {
    res.status(500).json({ message: error.message });
  }
};

module.exports = {
  getDiscounts,
  createDiscount,
  updateDiscount,
  deleteDiscount
};
