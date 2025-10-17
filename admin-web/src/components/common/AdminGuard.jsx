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
  if (user && user.role && user.role !== 'admin') {
    return <Navigate to="/admin/login" replace />;
  }
  return children;
};

export default AdminGuard;
