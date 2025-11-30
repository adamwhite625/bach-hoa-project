import api from '../../services/axios.customize';
import { API_ENDPOINTS } from '../../config/api';

const normalizeOrder = (o = {}) => {
  const id = o._id || o.id;
  
  // Handle user field (populated by backend)
  const user = o.user || {};
  const customerName = o.customerName 
    || (user.firstName && user.lastName ? `${user.firstName} ${user.lastName}`.trim() : '')
    || o.shippingAddress?.fullName
    || '';
  const email = o.email || user.email || '';
  const phone = o.phone || o.shippingAddress?.phone || '';
  
  // Handle shipping address
  const shippingAddress = typeof o.shippingAddress === 'object' 
    ? o.shippingAddress 
    : { address: o.shippingAddress || '' };
  
  return {
    id,
    _id: id,
    customerName,
    email,
    phone,
    total: o.totalPrice || o.total || o.totalAmount || 0,
    status: o.status || 'pending',
    paymentMethod: o.paymentMethod || o.payment || 'COD',
    createdAt: o.createdAt || o.date || o.created_at || new Date().toISOString(),
    items: Array.isArray(o.orderItems || o.items) ? (o.orderItems || o.items).map(it => ({
      id: it._id || it.id || it.productId || it.product?._id,
      productId: it.product?._id || it.productId || it.id || it._id,
      name: it.name || it.product?.name || '',
      quantity: it.quantity || it.qty || 0,
      price: it.price || it.unitPrice || 0,
      image: it.image || it.product?.image || '',
      subtotal: it.subtotal || ((it.price || it.unitPrice || 0) * (it.quantity || it.qty || 0))
    })) : [],
    shippingAddress,
    user: {
      id: user._id || user.id,
      firstName: user.firstName,
      lastName: user.lastName,
      email: user.email
    }
  };
};

export const OrderService = {
  async list(params = {}) {
    try {
      const res = await api.get(API_ENDPOINTS.ORDERS.LIST, { params });
      if (res?.EC === 0) {
        const data = res.DT;
        const orders = Array.isArray(data?.orders) ? data.orders : Array.isArray(data) ? data : [];
        return { ...res, DT: orders.map(normalizeOrder) };
      }
      if (Array.isArray(res)) return { EC: 0, DT: res.map(normalizeOrder), EM: 'OK' };
      return { EC: res?.EC ?? -1, DT: [], EM: res?.EM || 'Không thể tải đơn hàng' };
    } catch (e) {
      return { EC: -1, DT: [], EM: 'Lỗi mạng khi tải đơn hàng' };
    }
  },
  async detail(id) {
    try {
      const res = await api.get(API_ENDPOINTS.ORDERS.DETAIL(id));
      if (res?.EC === 0) return { ...res, DT: normalizeOrder(res.DT) };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.EM || 'Không thể lấy chi tiết đơn hàng' };
    } catch (e) {
      return { EC: -1, DT: null, EM: 'Lỗi mạng khi lấy chi tiết đơn hàng' };
    }
  },
  async updateStatus(id, status) {
    try {
      const res = await api.patch(API_ENDPOINTS.ORDERS.DETAIL(id), { status });
      if (res?.EC === 0) return { ...res, DT: normalizeOrder(res.DT) };
      return { EC: res?.EC ?? -1, DT: null, EM: res?.EM || 'Cập nhật trạng thái thất bại' };
    } catch (e) {
      return { EC: -1, DT: null, EM: 'Lỗi mạng khi cập nhật trạng thái' };
    }
  },
  async remove(id) {
    try {
      const res = await api.delete(API_ENDPOINTS.ORDERS.DETAIL(id));
      if (res?.EC === 0) return res;
      return { EC: res?.EC ?? -1, DT: null, EM: res?.EM || 'Xóa đơn hàng thất bại' };
    } catch (e) {
      return { EC: -1, DT: null, EM: 'Lỗi mạng khi xóa đơn hàng' };
    }
  }
};
