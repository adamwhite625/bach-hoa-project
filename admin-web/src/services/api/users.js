import axios from '../axios.customize';
import { API_ENDPOINTS } from '../../config/api';

export const UserService = {
  list: async () => {
    try {
      const res = await axios.get(API_ENDPOINTS.USERS.LIST);
      if (Array.isArray(res)) return { EC: 0, DT: res, EM: 'Success' };
      if (Array.isArray(res?.DT)) return { EC: 0, DT: res.DT, EM: res?.EM || 'Success' };
      if (Array.isArray(res?.data)) return { EC: 0, DT: res.data, EM: res?.EM || 'Success' };
      return { EC: res?.EC ?? -1, DT: [], EM: res?.message || res?.EM || 'Lỗi lấy người dùng' };
    } catch (e) {
      return { EC: -1, DT: [], EM: 'Lỗi kết nối người dùng' };
    }
  },
  profile: async () => {
    try {
      const res = await axios.get(API_ENDPOINTS.USERS.PROFILE);
      if (res?.EC === 0 && res?.DT) return { EC: 0, DT: res.DT, EM: res?.EM || 'Success' };
      return { EC: 0, DT: res, EM: res?.EM || 'Success' };
    } catch (error) {
      return { EC: -1, DT: null, EM: error?.response?.data?.message || 'Không thể lấy thông tin tài khoản' };
    }
  },
  updateProfile: async (payload = {}) => {
    try {
      const res = await axios.put(API_ENDPOINTS.USERS.PROFILE, payload);
      if (res?.EC === 0 && res?.DT) return { EC: 0, DT: res.DT, EM: res?.EM || 'Cập nhật thành công' };
      if (res?._id || res?.id) return { EC: 0, DT: res, EM: 'Cập nhật thành công' };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.message || res?.EM || 'Cập nhật thất bại' };
    } catch (error) {
      return { EC: -1, DT: null, EM: error?.response?.data?.message || 'Cập nhật thất bại' };
    }
  },
  deleteById: async (id) => {
    if (!id) return { EC: -1, DT: null, EM: 'Thiếu ID người dùng' };
    try {
      const res = await axios.delete(API_ENDPOINTS.USERS.DELETE(id));
      if (res?.EC === 0 || res?.message === 'User removed' || res === true) {
        return { EC: 0, DT: null, EM: res?.EM || res?.message || 'Xóa người dùng thành công' };
      }
      return { EC: res?.EC ?? -1, DT: null, EM: res?.message || res?.EM || 'Xóa người dùng thất bại' };
    } catch (error) {
      return { EC: -1, DT: null, EM: error?.response?.data?.message || 'Xóa người dùng thất bại' };
    }
  }
};

export default UserService;
