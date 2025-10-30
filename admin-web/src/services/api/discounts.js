import axios from '../axios.customize';
import { API_ENDPOINTS } from '../../config/api';

const normalize = (item = {}) => ({
  _id: item._id || item.id,
  code: item.code || '',
  description: item.description || '',
  type: item.type || 'percentage',
  value: Number(item.value) || 0,
  minOrderValue: Number.isFinite(item.minOrderValue) ? Number(item.minOrderValue) : null,
  maxDiscountAmount: Number.isFinite(item.maxDiscountAmount) ? Number(item.maxDiscountAmount) : null,
  usageLimit: Number.isFinite(item.usageLimit) ? Number(item.usageLimit) : null,
  usedCount: Number(item.usedCount) || 0,
  startDate: item.startDate || item.start_date || null,
  endDate: item.endDate || item.end_date || null,
  isActive: typeof item.isActive === 'boolean' ? item.isActive : true,
  createdAt: item.createdAt || item.created_at || null,
  updatedAt: item.updatedAt || item.updated_at || null
});

const normalizeArray = (payload) => {
  if (!payload) return [];
  if (Array.isArray(payload)) return payload.map(normalize);
  if (Array.isArray(payload?.DT)) return payload.DT.map(normalize);
  if (Array.isArray(payload?.data)) return payload.data.map(normalize);
  if (Array.isArray(payload?.discounts)) return payload.discounts.map(normalize);
  return [];
};

const normalizeSingle = (payload) => {
  if (!payload) return null;
  if (payload?.DT && !Array.isArray(payload.DT)) return normalize(payload.DT);
  return normalize(payload);
};

export const DiscountService = {
  list: async () => {
    try {
      const res = await axios.get(API_ENDPOINTS.DISCOUNTS.LIST);
      return { EC: 0, DT: normalizeArray(res), EM: res?.EM || 'Success' };
    } catch {
      return { EC: -1, DT: [], EM: 'Không thể tải mã giảm giá' };
    }
  },
  create: async (payload) => {
    try {
      const res = await axios.post(API_ENDPOINTS.DISCOUNTS.CREATE, payload);
      return { EC: 0, DT: normalizeSingle(res), EM: res?.EM || 'Tạo mã thành công' };
    } catch (error) {
      return { EC: error?.response?.status || -1, DT: null, EM: error?.response?.data?.message || 'Tạo mã thất bại' };
    }
  },
  update: async (id, payload) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID mã giảm giá' };
    try {
      const res = await axios.put(API_ENDPOINTS.DISCOUNTS.UPDATE(id), payload);
      return { EC: 0, DT: normalizeSingle(res), EM: res?.EM || 'Cập nhật mã thành công' };
    } catch (error) {
      return { EC: error?.response?.status || -1, DT: null, EM: error?.response?.data?.message || 'Cập nhật mã thất bại' };
    }
  },
  remove: async (id) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID mã giảm giá' };
    try {
      await axios.delete(API_ENDPOINTS.DISCOUNTS.DELETE(id));
      return { EC: 0, DT: null, EM: 'Xóa mã giảm giá thành công' };
    } catch (error) {
      return { EC: error?.response?.status || -1, DT: null, EM: error?.response?.data?.message || 'Xóa mã thất bại' };
    }
  }
};

export default DiscountService;
