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
  }
};

export default UserService;
