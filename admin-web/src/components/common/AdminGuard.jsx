import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';

const getStoredUser = () => {
  try { return JSON.parse(localStorage.getItem('user') || 'null'); } catch { return null; }
};

const isAuthenticated = () => !!localStorage.getItem('access_token');

export const AdminGuard = ({ children }) => {
  const location = useLocation();

  if (!isAuthenticated()) {
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }
  
  const user = getStoredUser();
  if (!user || !user.role || user.role !== 'Admin') { // Sửa thành 'Admin' viết hoa
    console.log('Invalid admin role:', user?.role);
    localStorage.removeItem('access_token'); // Xóa token nếu role không hợp lệ
    localStorage.removeItem('user');
    return <Navigate to="/admin/login" state={{ from: location }} replace />;
  }
  
  return children;
};

export default AdminGuard;
