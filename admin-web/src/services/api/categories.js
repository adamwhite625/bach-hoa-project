import axios from '../axios.customize';
import { API_ENDPOINTS } from '../../config/api';

export const CategoryService = {
  list: async () => {
    try {
      const res = await axios.get(API_ENDPOINTS.CATEGORIES.LIST);
      if (Array.isArray(res)) return { EC: 0, DT: res, EM: 'Success' };
      if (res?.EC === 0 && Array.isArray(res.DT)) return { EC: 0, DT: res.DT, EM: res.EM || 'Success' };
      if (res?.EC === 0 && res?.DT?.categories) return { EC: 0, DT: res.DT.categories, EM: res.EM || 'Success' };
      return { EC: res?.EC ?? -1, DT: [], EM: res?.EM || 'Lỗi lấy danh mục' };
    } catch {
      return { EC: -1, DT: [], EM: 'Lỗi kết nối danh mục' };
    }
  },
  create: async ({ name, description, image }) => {
    try {
      const payload = { name: name?.trim(), description: description?.trim(), image: image?.trim() };
      const res = await axios.post(API_ENDPOINTS.CATEGORIES.CREATE, payload);
      if (res?._id || res?.id) return { EC: 0, DT: res, EM: 'Tạo danh mục thành công' };
      if (res?.EC === 0 && res?.DT) return { EC: 0, DT: res.DT, EM: res.EM || 'Tạo danh mục thành công' };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.message || res?.EM || 'Tạo danh mục thất bại' };
    } catch (e) {
      return { EC: -1, DT: null, EM: 'Lỗi kết nối tạo danh mục' };
    }
  },
  update: async (id, { name, description, image }) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID danh mục' };
    try {
      const payload = { name: name?.trim(), description: description?.trim(), image: image?.trim() };
      const res = await axios.put(API_ENDPOINTS.CATEGORIES.UPDATE(id), payload);
      if (res?._id || res?.id) return { EC: 0, DT: res, EM: 'Cập nhật danh mục thành công' };
      if (res?.EC === 0 && res?.DT) return { EC: 0, DT: res.DT, EM: res.EM || 'Cập nhật danh mục thành công' };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.message || res?.EM || 'Cập nhật danh mục thất bại' };
    } catch {
      return { EC: -1, DT: null, EM: 'Lỗi kết nối cập nhật danh mục' };
    }
  },
  remove: async (id) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID danh mục' };
    try {
      const res = await axios.delete(API_ENDPOINTS.CATEGORIES.DELETE(id));
      if (res?.EC === 0 || res?.success || res?.status === 'success') {
        return { EC: 0, DT: null, EM: res?.EM || 'Xóa danh mục thành công' };
      }
      if (res?.message && !Object.prototype.hasOwnProperty.call(res, 'EC')) {
        return { EC: 0, DT: null, EM: res.message };
      }
      if (res === '' || res == null) return { EC: 0, DT: null, EM: 'Xóa danh mục thành công' };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.message || res?.EM || 'Xóa danh mục thất bại' };
    } catch {
      return { EC: -1, DT: null, EM: 'Lỗi kết nối xóa danh mục' };
    }
  }
};

export default CategoryService;
