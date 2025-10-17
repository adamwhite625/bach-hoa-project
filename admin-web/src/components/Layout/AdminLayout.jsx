import React, { useMemo, useState, useEffect, useCallback } from 'react';
import { Layout, Menu, Breadcrumb, theme, Button, Popconfirm, Space, Typography } from 'antd';
import {
  DashboardOutlined,
  ShoppingOutlined,
  AppstoreOutlined,
  ShoppingCartOutlined,
  BarChartOutlined,
  SettingOutlined,
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  ArrowLeftOutlined
} from '@ant-design/icons';
import { useNavigate, useLocation, Outlet } from 'react-router-dom';
import '../../styles/admin.css';

const { Header, Sider, Content, Footer } = Layout;

const navItems = [
  { key: '/admin', icon: <DashboardOutlined />, label: 'Dashboard' },
  { key: '/admin/products', icon: <ShoppingOutlined />, label: 'Sản phẩm' },
  { key: '/admin/categories', icon: <AppstoreOutlined />, label: 'Danh mục' },
  { key: '/admin/orders', icon: <ShoppingCartOutlined />, label: 'Đơn hàng' },
  { key: '/admin/reports', icon: <BarChartOutlined />, label: 'Báo cáo', disabled: true },
  { key: '/admin/settings', icon: <SettingOutlined />, label: 'Cài đặt', disabled: true }
];

const AdminLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [collapsed, setCollapsed] = useState(false);
  const { token } = theme.useToken();

  // Responsive collapse
  useEffect(() => {
    const handleResize = () => {
      if (window.innerWidth < 992) setCollapsed(true);
    };
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  const selectedKeys = useMemo(() => {
    const path = location.pathname;
    const found = navItems.find(i => path === i.key || path.startsWith(i.key + '/'));
    return found ? [found.key] : ['/admin'];
  }, [location.pathname]);

  const onMenuClick = useCallback(({ key }) => {
    if (!key.startsWith('/admin')) return;
    navigate(key);
  }, [navigate]);

  const breadcrumbItems = useMemo(() => {
    const segments = location.pathname.replace(/(^\/|\/$)/g,'').split('/');
    const acc = [];
    return segments.map((seg, idx) => {
      acc.push(seg);
      const url = '/' + acc.join('/');
      const nav = navItems.find(n => n.key === url);
      return { title: nav?.label || (seg === 'admin' ? 'Admin' : seg) };
    });
  }, [location.pathname]);

  return (
    <Layout className="admin-root">
      <Sider
        collapsible
        collapsed={collapsed}
        trigger={null}
        width={230}
        className="admin-sider"
      >
        <div className="admin-logo">{collapsed ? 'AD' : 'Admin Panel'}</div>
        <Menu
          theme="dark"
          mode="inline"
          selectedKeys={selectedKeys}
          items={navItems}
          onClick={onMenuClick}
          className="admin-menu"
        />
      </Sider>
      <Layout>
        <Header className="admin-header" style={{ background: token.colorBgElevated }}>
          <div className="admin-header-left">
            <Button
              type="text"
              icon={collapsed ? <MenuUnfoldOutlined /> : <MenuFoldOutlined />}
              onClick={() => setCollapsed(c => !c)}
              className="collapse-btn"
            />
            <Breadcrumb items={breadcrumbItems} className="admin-breadcrumb" />
          </div>
          <div className="admin-header-right">
            <Space size={12}>
              <Typography.Text type="secondary" style={{ fontSize: 13 }}>
                {(() => { try { return JSON.parse(localStorage.getItem('user')||'null')?.email || 'Admin'; } catch { return 'Admin'; } })()}
              </Typography.Text>
              <Popconfirm
                title="Đăng xuất"
                description="Bạn chắc chắn muốn đăng xuất?"
                okText="Đăng xuất"
                cancelText="Hủy"
                onConfirm={() => {
                  try { localStorage.removeItem('access_token'); localStorage.removeItem('user'); } catch {}
                  navigate('/admin/login', { replace: true });
                }}
              >
                <Button danger>Đăng xuất</Button>
              </Popconfirm>
            </Space>
          </div>
        </Header>
        <Content className="admin-content">
          <div className="admin-content-inner">
            <Outlet />
          </div>
        </Content>
        <Footer className="admin-footer">© {new Date().getFullYear()} Admin Panel • E-commerce</Footer>
      </Layout>
    </Layout>
  );
};

export default AdminLayout;
