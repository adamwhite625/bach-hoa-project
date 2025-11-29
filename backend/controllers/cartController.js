const Cart = require('../models/cartModel');
const Product = require('../models/productModel');

// Đầu file cartController.js, thêm:
const isSaleActive = (sale) => {
  if (!sale?.active) return false;
  const now = new Date();
  if (sale.startAt && now < new Date(sale.startAt)) return false;
  if (sale.endAt && now > new Date(sale.endAt)) return false;
  return true;
};

const getEffectivePrice = (product) => {
  const basePrice = Number(product.price || 0);
  if (!isSaleActive(product.sale)) return basePrice;
  
  const { type, value } = product.sale;
  if (type === 'percent') {
    return Math.max(0, Math.round(basePrice * (1 - value / 100)));
  }
  return Math.max(0, basePrice - Number(value || 0));
};

// @desc    Add/Update item to cart
// @route   POST /api/carts
// @access  Private
const addToCart = async (req, res) => {
  try {
    const { productId, quantity } = req.body;

    const requestedQuantity = Number(quantity) || 0;
    if (requestedQuantity <= 0) {
      return res.status(400).json({ message: 'Số lượng phải lớn hơn 0' });
    }

    // Validate product exists and has enough stock
    const product = await Product.findById(productId);
    if (!product) {
      return res.status(404).json({ message: 'Sản phẩm không tồn tại' });
    }
    if (!product.isActive) {
      return res.status(400).json({ message: 'Sản phẩm tạm ngưng kinh doanh' });
    }

    // Find or create cart for user
    let cart = await Cart.findOne({ user: req.user._id });
    if (!cart) {
      cart = new Cart({ user: req.user._id, items: [] });
    }

    // Check if product already in cart
    const itemIndex = cart.items.findIndex(item => item.product.toString() === productId);
    const existingQuantity = itemIndex > -1 ? cart.items[itemIndex].quantity : 0;
    const newQuantity = existingQuantity + requestedQuantity;

    if (newQuantity > product.quantity) {
      return res.status(400).json({ message: 'Số lượng sản phẩm trong kho không đủ' });
    }

    if (itemIndex > -1) {
      cart.items[itemIndex].quantity = newQuantity;
    } else {
      cart.items.push({ product: productId, quantity: requestedQuantity });
    }

    await cart.save();

    const populatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image images quantity sale');
    
    const enrichedItems = populatedCart.items.map(item => {
      const product = item.product;
      if (!product) return null;
      
      const effectivePrice = getEffectivePrice(product);
      
      return {
        _id: item._id,
        product: {
          _id: product._id,
          name: product.name,
          image: product.image,
          images: product.images || [],
          price: product.price,
          effectivePrice,
          stock: product.quantity,
          sale: isSaleActive(product.sale) ? product.sale : null,
        },
        quantity: item.quantity,
        itemTotal: effectivePrice * item.quantity,
        outOfStock: item.quantity > product.quantity,
        availableStock: product.quantity,
      };
    }).filter(item => item !== null);
    
    res.status(200).json({
      items: enrichedItems,
      totalPrice: enrichedItems.reduce((sum, i) => sum + i.itemTotal, 0),
    });
  } catch (error) {
    console.error('Add to cart error:', error);
    res.status(500).json({ message: 'Lỗi server' });
  }
};


// @desc    Get user cart
// @route   GET /api/carts
const getCart = async (req, res) => {
  try {
    const cart = await Cart.findOne({ user: req.user._id })
      .populate('items.product', 'name price image images quantity sale'); // ← Đã có 'quantity'
    
    if (!cart) {
      return res.json({ items: [], totalPrice: 0 });
    }

    const enrichedItems = cart.items.map(item => {
      const product = item.product;
      
      // Skip nếu sản phẩm đã bị xóa
      if (!product) return null;
      
      const effectivePrice = getEffectivePrice(product);
      
      return {
        _id: item._id,
        product: {
          _id: product._id,
          name: product.name,
          image: product.image,
          images: product.images || [],
          price: product.price,
          effectivePrice,
          stock: product.quantity, // ← Số lượng tồn kho
          sale: isSaleActive(product.sale) ? product.sale : null,
        },
        quantity: item.quantity,
        itemTotal: effectivePrice * item.quantity,
        // Cảnh báo nếu số lượng trong giỏ > tồn kho
        outOfStock: item.quantity > product.quantity,
        availableStock: product.quantity, // ← Để frontend dễ xử lý
      };
    }).filter(item => item !== null);

    const totalPrice = enrichedItems.reduce((sum, item) => sum + item.itemTotal, 0);

    res.json({
      items: enrichedItems,
      totalPrice,
    });
  } catch (error) {
    console.error('Cart error:', error);
    res.status(500).json({ message: 'Lỗi server' });
  }
};

// @desc    Update cart item quantity
// @route   PUT /api/carts/:itemId
// @access  Private
const updateCartItem = async (req, res) => {
  try {
    const { quantity } = req.body;
    const cart = await Cart.findOne({ user: req.user._id });
    
    if (!cart) {
      return res.status(404).json({ message: 'Không tìm thấy giỏ hàng' });
    }

    const itemIndex = cart.items.findIndex(item => item._id.toString() === req.params.itemId);
    if (itemIndex === -1) {
      return res.status(404).json({ message: 'Không tìm thấy sản phẩm trong giỏ hàng' });
    }

    // Validate product stock
    const product = await Product.findById(cart.items[itemIndex].product);
    if (!product || product.quantity < quantity) {
      return res.status(400).json({ message: 'Số lượng sản phẩm trong kho không đủ' });
    }

    cart.items[itemIndex].quantity = quantity;
    await cart.save();

    const updatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image images quantity sale');
    
    // Apply enrichment
    const enrichedItems = updatedCart.items.map(item => {
      const product = item.product;
      if (!product) return null;
      const effectivePrice = getEffectivePrice(product);
      return {
        _id: item._id,
        product: {
          _id: product._id,
          name: product.name,
          image: product.image,
          images: product.images || [],
          price: product.price,
          effectivePrice,
          stock: product.quantity,
          sale: isSaleActive(product.sale) ? product.sale : null,
        },
        quantity: item.quantity,
        itemTotal: effectivePrice * item.quantity,
        outOfStock: item.quantity > product.quantity,
        availableStock: product.quantity,
      };
    }).filter(item => item !== null);
    
    res.json({
      items: enrichedItems,
      totalPrice: enrichedItems.reduce((sum, i) => sum + i.itemTotal, 0),
    });
  } catch (error) {
    console.error('Update cart error:', error);
    res.status(500).json({ message: 'Lỗi server' });
  }
};

// @desc    Remove item from cart
// @route   DELETE /api/carts/:itemId
// @access  Private
const removeFromCart = async (req, res) => {
  try {
    const cart = await Cart.findOne({ user: req.user._id });
    
    if (!cart) {
      return res.status(404).json({ message: 'Không tìm thấy giỏ hàng' });
    }

    cart.items = cart.items.filter(item => item._id.toString() !== req.params.itemId);
    await cart.save();

    const updatedCart = await Cart.findById(cart._id)
      .populate('items.product', 'name price image images quantity sale');
    
    // Apply enrichment
    const enrichedItems = updatedCart.items.map(item => {
      const product = item.product;
      if (!product) return null;
      const effectivePrice = getEffectivePrice(product);
      return {
        _id: item._id,
        product: {
          _id: product._id,
          name: product.name,
          image: product.image,
          images: product.images || [],
          price: product.price,
          effectivePrice,
          stock: product.quantity,
          sale: isSaleActive(product.sale) ? product.sale : null,
        },
        quantity: item.quantity,
        itemTotal: effectivePrice * item.quantity,
        outOfStock: item.quantity > product.quantity,
        availableStock: product.quantity,
      };
    }).filter(item => item !== null);
    
    res.json({
      items: enrichedItems,
      totalPrice: enrichedItems.reduce((sum, i) => sum + i.itemTotal, 0),
    });
  } catch (error) {
    console.error('Remove from cart error:', error);
    res.status(500).json({ message: 'Lỗi server' });
  }
};

module.exports = {
    addToCart,
    getCart,
    updateCartItem,
    removeFromCart
};