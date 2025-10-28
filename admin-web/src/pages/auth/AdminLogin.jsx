import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { Form, Input, Button, Card, Typography, notification, Alert, Checkbox } from 'antd';
import { UserOutlined, LockOutlined } from '@ant-design/icons';
import { handleLogin } from '../../services/api';

const { Title, Text } = Typography;

const AdminLogin = () => {
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const rememberedEmail = useMemo(() => localStorage.getItem('remember_email') || '', []);
  const [remember, setRemember] = useState(() => !!rememberedEmail);
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    // Kiểm tra auth khi component mount
    const token = localStorage.getItem('access_token');
    if (!token) return;
    try {
      const u = JSON.parse(localStorage.getItem('user') || 'null');
      if (u?.role === 'Admin') navigate('/admin', { replace: true });
    } catch {}
  }, [navigate]);

  const onFinish = async (values) => {
    setLoading(true);
    setError('');
    let loginSuccess = false;
    try {
      const email = String(values.email || '').trim();
      const password = String(values.password || '');
      const res = await handleLogin(email, password);
      console.log('Login response:', res);

     if (res.EC === 0) {
        if (res.user.role !== 'Admin') {
          setError('Tài khoản không có quyền truy cập admin');
          localStorage.removeItem('access_token');
          localStorage.removeItem('user');
          setLoading(false);
          return;
        }
        localStorage.setItem('access_token', res.access_token);
        localStorage.setItem('user', JSON.stringify(res.user));
        loginSuccess = true;
     }
   }
   catch (err) {
      console.error(err);
      setError(err.message || 'Đăng nhập thất bại');
      notification.error({ message: 'Đăng nhập thất bại', description: err.message || 'Vui lòng thử lại' });
    } finally {
      setLoading(false);
    }
    if (remember) {
      localStorage.setItem('remember_email', values.email);
    } else {
      localStorage.removeItem('remember_email');
    }
    const from = location.state?.from?.pathname || '/admin';
    navigate(from, { replace: true });
  }


  return (
    <div
      style={{
        minHeight: '100vh',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        padding: '32px 12px',
        background: '#f5f7fb',
      }}
    >
      {/* Khung viền gradient bao quanh */}
      <div
        style={{
          background: 'linear-gradient(135deg, #6366f1, #22d3ee)',
          padding: 1,
          borderRadius: 16,
          boxShadow: '0 16px 40px rgba(0,0,0,0.08)',
          minWidth: 440,
        }}
      >
        <Card
          style={{
            borderRadius: 15,
            border: '1px solid #eef0f6',
            overflow: 'hidden',
            width: '100%',
          }}
          bodyStyle={{ padding: 22 }}
        >
          <div style={{ textAlign: 'center', marginBottom: 12 }}>
            <Title level={4} style={{ marginBottom: 4 }}>Bach Hoa Admin</Title>
            <Text type="secondary" style={{ fontSize: 12 }}>
              Đăng nhập để vào trang quản trị
            </Text>
          </div>

          {error ? (
            <Alert
              type="error"
              showIcon
              message="Đăng nhập thất bại"
              description={error}
              style={{ marginBottom: 12 }}
            />
          ) : null}

          <Form
            layout="vertical"
            size="middle"
            onFinish={onFinish}
            initialValues={{ email: rememberedEmail }}
          >
            <Form.Item
              name="email"
              label="Email"
              rules={[
                { required: true, message: 'Nhập email' },
                { type: 'email', message: 'Email không hợp lệ' },
              ]}
              style={{ marginBottom: 12 }}
            >
              <Input
                prefix={<UserOutlined />}
                placeholder="admin@admin.com"
                autoComplete="email"
                allowClear
                disabled={loading}
                autoFocus
              />
            </Form.Item>

            <Form.Item
              name="password"
              label="Mật khẩu"
              rules={[
                { required: true, message: 'Nhập mật khẩu' },
                { min: 6, message: 'Tối thiểu 6 ký tự' },
              ]}
              style={{ marginBottom: 12 }}
            >
              <Input.Password
                prefix={<LockOutlined />}
                placeholder="••••••"
                autoComplete="current-password"
                allowClear
                disabled={loading}
              />
            </Form.Item>

            <div
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                alignItems: 'center',
                marginBottom: 10,
              }}
            >
              <Checkbox
                checked={remember}
                onChange={(e) => setRemember(e.target.checked)}
                disabled={loading}
              >
                Ghi nhớ email
              </Checkbox>
              <Link to="#" onClick={(e) => e.preventDefault()} style={{ fontSize: 12 }}>
                Liên hệ hỗ trợ
              </Link>
            </div>

            <Form.Item style={{ marginBottom: 0 }}>
              <Button type="primary" htmlType="submit" loading={loading} block size="middle">
                Đăng nhập
              </Button>
            </Form.Item>
          </Form>

          <div style={{ textAlign: 'center', marginTop: 14 }}>
            <Text type="secondary" style={{ fontSize: 12 }}>
              © {new Date().getFullYear()} Bach Hoa
            </Text>
          </div>
        </Card>
      </div>
    </div>
  );
};

export default AdminLogin;