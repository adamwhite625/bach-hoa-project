import { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';

export const useAuth = () => {
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [user, setUser] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    // Kiểm tra token trong localStorage
    const token = localStorage.getItem('access_token');
    const userStr = localStorage.getItem('user');
    
    if (token && userStr) {
      setIsAuthenticated(true);
      setUser(JSON.parse(userStr));
      // TODO: Có thể thêm gọi API để lấy thông tin user đầy đủ ở đây
      // fetchUserProfile();
    }
  }, []);

  const login = (response) => {
    try {      
      if (response.EC === 0) {
        // Lưu token
        localStorage.setItem('access_token', response.access_token);
        
        // Lưu thông tin user vào localStorage
        let userInfo;
        if (response.user) {
          userInfo = response.user;
        } else {
          // Fallback: decode JWT to extract role/email if present
          try {
            const payload = JSON.parse(atob(response.access_token.split('.')[1] || ''));
            userInfo = { email: payload.email || 'User', role: payload.role, _id: payload._id };
          } catch {
            userInfo = { email: response.email || 'User' };
          }
        }
        localStorage.setItem('user', JSON.stringify(userInfo));
        
        // Cập nhật state
        setIsAuthenticated(true);
        setUser(userInfo);
        
        return true;
      }
      return false;
    } catch (error) {
      return false;
    }
  };

  const logout = async () => {
    try {
      // Nếu cần gọi API logout ở đây
      localStorage.removeItem('access_token');
      setIsAuthenticated(false);
      setUser(null);
      navigate('/login');
    } catch (error) {
      // Ignore logout errors
    }
  };

  return {
    isAuthenticated,
    user,
    login,
    logout
  };
};
