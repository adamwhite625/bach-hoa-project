import axios from '../axios.customize';
import { API_ENDPOINTS } from '../../config/api';

const toImageArray = (list) => {
  if (!Array.isArray(list)) return [];
  return list
    .map((item) => {
      if (typeof item === 'string') return item;
      if (item?.url) return item.url;
      return null;
    })
    .filter(Boolean);
};

// chuẩn hoá sản phẩm từ API về định dạng dùng trong UI
const normalizeProduct = (p) => {
  const gallery = toImageArray(p.images);
  const detailGallery = toImageArray(p.detailImages);
  const primaryImage = p.image || p.thumbnail || gallery[0] || '';

  return {
    _id: p._id || p.id,
    name: p.name || p.title || 'Sản phẩm',
    sku: p.sku || p.SKU || '',
    brand: p.brand || '',
    price: Number(p.price) || 0,
    description: p.description || '',
    image: primaryImage,
    images: gallery.length ? gallery : (primaryImage ? [primaryImage] : []),
    detailImages: detailGallery,
    category: p.category || p.categoryId || null,
    stock: p.stock ?? p.quantity ?? 0,
    quantity: p.quantity ?? p.stock ?? 0,
    createdAt: p.createdAt || p.updatedAt || null,
    ratings: p.ratings || p.rating || 0,
    isNew: p.isNew || false,
    isBestSeller: p.isBestSeller || false,
    isActive: typeof p.isActive === 'boolean' ? p.isActive : true,
    status: p.status || (typeof p.isActive === 'boolean' ? (p.isActive ? 'active' : 'inactive') : undefined),
  };
};

const success = (DT, EM = 'Success') => ({ EC: 0, DT, EM });
const failure = (EM = 'Lỗi kết nối', EC = -1, DT = null) => ({ EC, DT, EM });

const extractArray = (payload) => {
  if (Array.isArray(payload)) return payload;
  if (Array.isArray(payload?.products)) return payload.products;
  if (Array.isArray(payload?.data)) return payload.data;
  if (Array.isArray(payload?.DT)) return payload.DT;
  if (Array.isArray(payload?.DT?.products)) return payload.DT.products;
  return null;
};

const extractSingle = (payload) => {
  if (!payload) return null;
  if (Array.isArray(payload)) return payload[0] ?? null;
  if (payload?.DT && !Array.isArray(payload.DT)) return payload.DT;
  return payload;
};

const hasId = (item) => Boolean(item && (item._id || item.id));

export const ProductService = {
  getProducts: async (params = {}) => {
    try {
      const url = API_ENDPOINTS.PRODUCTS.LIST_Q ? API_ENDPOINTS.PRODUCTS.LIST_Q(params) : API_ENDPOINTS.PRODUCTS.LIST;
      const res = await axios.get(url);
      const raw = extractArray(res);
      if (raw) {
        const normalized = raw.map(normalizeProduct);
        const total = res?.total ?? normalized.length;
        return success({ products: normalized, total }, res?.EM || 'Success');
      }
      const message = res?.message || res?.EM || 'Lỗi lấy sản phẩm';
      return failure(message, res?.EC ?? -1, { products: [], total: 0 });
    } catch {
      return failure('Lỗi kết nối sản phẩm', -1, { products: [], total: 0 });
    }
  },
  getProductById: async (id) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID' };
    try {
      const res = await axios.get(API_ENDPOINTS.PRODUCTS.DETAIL(id));
      if (res?.message && !hasId(res)) return failure(res.message, res?.EC ?? -1, null);
      const payload = extractSingle(res);
      if (hasId(payload)) return success(normalizeProduct(payload), res?.EM || 'Success');
      return failure('Không tìm thấy sản phẩm', res?.EC ?? -1, null);
    } catch {
      return failure('Lỗi kết nối sản phẩm');
    }
  },
  getCategories: async () => {
    try {
      const res = await axios.get(API_ENDPOINTS.CATEGORIES.LIST);
      const raw = extractArray(res);
      if (raw) return success(raw, res?.EM || 'Success');
      if (Array.isArray(res?.categories)) return success(res.categories, res?.EM || 'Success');
      const message = res?.message || res?.EM || 'Lỗi lấy danh mục';
      return failure(message, res?.EC ?? -1, []);
    } catch {
      return failure('Lỗi kết nối danh mục', -1, []);
    }
  },
  createProduct: async (payload) => {
    try {
      const res = await axios.post(API_ENDPOINTS.PRODUCTS.CREATE, payload);
      if (res?.message && !hasId(res)) return failure(res.message, res?.EC ?? -1, null);
      if (hasId(res)) return success(normalizeProduct(res), res?.EM || 'Tạo thành công');
      if (hasId(res?.DT)) return success(normalizeProduct(res.DT), res?.EM || 'Tạo thành công');
      return failure(res?.message || res?.EM || 'Tạo thất bại', res?.EC ?? -1, null);
    } catch {
      return failure('Lỗi kết nối tạo sản phẩm');
    }
  },
  updateProduct: async (id, payload) => {
    try {
      const res = await axios.put(API_ENDPOINTS.PRODUCTS.UPDATE(id), payload);
      console.log(API_ENDPOINTS.PRODUCTS.UPDATE(id), payload);
      if (res?.message && !hasId(res)) return failure(res.message, res?.EC ?? -1, null);
      if (hasId(res)) return success(normalizeProduct(res), res?.EM || 'Cập nhật thành công');
      if (hasId(res?.DT)) return success(normalizeProduct(res.DT), res?.EM || 'Cập nhật thành công');
      return failure(res?.message || res?.EM || 'Cập nhật thất bại', res?.EC ?? -1, null);
    } catch {
      return failure('Lỗi kết nối cập nhật sản phẩm');
    }
  },
  deleteProduct: async (id) => {
    try {
      const res = await axios.delete(API_ENDPOINTS.PRODUCTS.DELETE(id));
      if (res?.message && !res?.success && !res?.status) return failure(res.message, res?.EC ?? -1, null);
      if (res?.EC === 0 || res?.success || res === true) return success(null, res?.EM || 'Xóa thành công');
      if (res == null || res === '') return success(null, 'Xóa thành công');
      return failure(res?.message || res?.EM || 'Xóa thất bại', res?.EC ?? -1, null);
    } catch {
      return failure('Lỗi kết nối xóa sản phẩm');
    }
  }
};