import React, { useEffect, useMemo, useState } from 'react';
import {
  Row,
  Col,
  Card,
  Statistic,
  Table,
  Space,
  Button,
  Typography,
  Tag,
  Badge,
  Empty,
  List,
  Spin,
  DatePicker,
  Select
} from 'antd';
import {
  RiseOutlined,
  ShoppingCartOutlined,
  DashboardOutlined,
  DollarOutlined,
  InboxOutlined,
  UserOutlined,
  ShoppingOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { OrderService } from '../../services/api/orders';
import UserService from '../../services/api/users';
import { ProductService } from '../../services/api/products';
import {
  ResponsiveContainer,
  ComposedChart,
  Area,
  Line,
  CartesianGrid,
  XAxis,
  YAxis,
  Tooltip as RechartsTooltip,
  Legend,
  BarChart,
  Bar
} from 'recharts';

const { Title, Text } = Typography;

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0
});

const numberFormatter = new Intl.NumberFormat('vi-VN');

const statusMeta = Object.freeze({
  pending: { color: 'gold', label: 'Đang xử lý' },
  confirmed: { color: 'blue', label: 'Đã xác nhận' },
  shipped: { color: 'cyan', label: 'Đang giao' },
  delivered: { color: 'green', label: 'Đã giao' },
  completed: { color: 'green', label: 'Hoàn tất' },
  cancelled: { color: 'red', label: 'Đã hủy' },
  refunded: { color: 'purple', label: 'Hoàn tiền' }
});

const formatCurrency = (value) => currencyFormatter.format(Number.isFinite(Number(value)) ? Number(value) : 0);

const formatNumber = (value) => numberFormatter.format(Number.isFinite(Number(value)) ? Number(value) : 0);

const friendlyDate = (value) => {
  const date = value ? new Date(value) : null;
  if (!date || Number.isNaN(date.getTime())) return 'Không xác định';
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const createDailyBuckets = (days) => {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const buckets = [];
  const map = {};
  for (let i = days - 1; i >= 0; i -= 1) {
    const bucketDate = new Date(today);
    bucketDate.setDate(today.getDate() - i);
    const key = bucketDate.toISOString().slice(0, 10);
    const label = bucketDate.toLocaleDateString('vi-VN', { day: '2-digit', month: '2-digit' });
    const bucket = { key, label };
    buckets.push(bucket);
    map[key] = bucket;
  }
  const start = new Date(today);
  start.setDate(start.getDate() - (days - 1));
  start.setHours(0, 0, 0, 0);
  return { buckets, map, start };
};

const buildOrderTrend = (orders, days = 7, customRange = null) => {
  let startDate, endDate;
  
  if (customRange && customRange[0] && customRange[1]) {
    startDate = new Date(customRange[0]);
    startDate.setHours(0, 0, 0, 0);
    endDate = new Date(customRange[1]);
    endDate.setHours(23, 59, 59, 999);
    const diffTime = Math.abs(endDate - startDate);
    const diffDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
    days = diffDays;
  }
  
  const { buckets, map, start } = createDailyBuckets(days);
  const end = new Date();
  end.setHours(23, 59, 59, 999);
  buckets.forEach((bucket) => {
    bucket.revenue = 0;
    bucket.orders = 0;
  });
  orders.forEach((order) => {
    const raw = order.createdAt || order.created_at || order.date;
    if (!raw) return;
    const parsed = new Date(raw);
    if (Number.isNaN(parsed.getTime())) return;
    if (parsed < start || parsed > end) return;
    parsed.setHours(0, 0, 0, 0);
    const key = parsed.toISOString().slice(0, 10);
    const bucket = map[key];
    if (!bucket) return;
    bucket.revenue += Number(order.total) || 0;
    bucket.orders += 1;
  });
  return buckets.map(({ label, revenue, orders: orderCount }) => ({
    date: label,
    revenue: Math.round(revenue),
    orders: orderCount
  }));
};

const buildUserTrend = (users, days = 7, customRange = null) => {
  let actualDays = days;
  
  if (customRange && customRange[0] && customRange[1]) {
    const startOverride = new Date(customRange[0]);
    startOverride.setHours(0, 0, 0, 0);
    const endOverride = new Date(customRange[1]);
    endOverride.setHours(23, 59, 59, 999);
    const diffTime = Math.abs(endOverride - startOverride);
    actualDays = Math.ceil(diffTime / (1000 * 60 * 60 * 24)) + 1;
  }
  
  const { buckets, map, start } = createDailyBuckets(actualDays);
  const end = new Date();
  end.setHours(23, 59, 59, 999);
  buckets.forEach((bucket) => {
    bucket.users = 0;
  });
  users.forEach((user) => {
    const raw = user.createdAt || user.created_at || user.joinedAt || user.date || user.createdOn;
    if (!raw) return;
    const parsed = new Date(raw);
    if (Number.isNaN(parsed.getTime())) return;
    if (parsed < start || parsed > end) return;
    parsed.setHours(0, 0, 0, 0);
    const key = parsed.toISOString().slice(0, 10);
    const bucket = map[key];
    if (!bucket) return;
    bucket.users += 1;
  });
  return buckets.map(({ label, users: count }) => ({
    date: label,
    users: count
  }));
};

const USE_DASHBOARD_MOCK = false;

const daysAgoIso = (days) => {
  const date = new Date();
  date.setHours(9, 0, 0, 0);
  date.setDate(date.getDate() - days);
  return date.toISOString();
};

const MOCK_ORDERS = [
  { id: 'ODR001', customer: 'Nguyễn Văn A', total: 2150000, status: 'completed', createdAt: daysAgoIso(0) },
  { id: 'ODR002', customer: 'Trần Thị B', total: 1820000, status: 'confirmed', createdAt: daysAgoIso(1) },
  { id: 'ODR003', customer: 'Phạm Văn C', total: 960000, status: 'pending', createdAt: daysAgoIso(2) },
  { id: 'ODR004', customer: 'Lê Thu D', total: 3240000, status: 'delivered', createdAt: daysAgoIso(3) },
  { id: 'ODR005', customer: 'Đỗ Minh E', total: 1450000, status: 'shipped', createdAt: daysAgoIso(4) },
  { id: 'ODR006', customer: 'Vũ Hoàng F', total: 2730000, status: 'completed', createdAt: daysAgoIso(5) },
  { id: 'ODR007', customer: 'Hoàng Mai G', total: 760000, status: 'cancelled', createdAt: daysAgoIso(6) }
];

const MOCK_USERS = [
  { id: 'USR001', name: 'Nguyễn Văn T', email: 't.nguyen@example.com', role: 'customer', createdAt: daysAgoIso(0) },
  { id: 'USR002', name: 'Lê Thị H', email: 'h.le@example.com', role: 'customer', createdAt: daysAgoIso(1) },
  { id: 'USR003', name: 'Phạm Quốc I', email: 'i.pham@example.com', role: 'customer', createdAt: daysAgoIso(1) },
  { id: 'USR004', name: 'Trần Thanh J', email: 'j.tran@example.com', role: 'customer', createdAt: daysAgoIso(2) },
  { id: 'USR005', name: 'Đinh Phương K', email: 'k.dinh@example.com', role: 'customer', createdAt: daysAgoIso(4) },
  { id: 'USR006', name: 'Ngô Thị L', email: 'l.ngo@example.com', role: 'customer', createdAt: daysAgoIso(6) }
];

const MOCK_PRODUCTS = [
  { id: 'PRD001', name: 'Gạo thơm đặc biệt' },
  { id: 'PRD002', name: 'Sữa tươi hữu cơ' },
  { id: 'PRD003', name: 'Dầu ăn cao cấp' },
  { id: 'PRD004', name: 'Nước mắm truyền thống' },
  { id: 'PRD005', name: 'Bột giặt sinh học' },
  { id: 'PRD006', name: 'Khăn giấy đa năng' },
  { id: 'PRD007', name: 'Trà thảo mộc' },
  { id: 'PRD008', name: 'Cafe nguyên chất' },
  { id: 'PRD009', name: 'Nước giải khát vị cam' },
  { id: 'PRD010', name: 'Bánh quy dinh dưỡng' }
];

const Dashboard = () => {
  const navigate = useNavigate();
  const [recentOrders, setRecentOrders] = useState([]);
  const [recentUsers, setRecentUsers] = useState([]);
  const [orderTrendData, setOrderTrendData] = useState([]);
  const [userTrendData, setUserTrendData] = useState([]);
  const [statistics, setStatistics] = useState({
    totalSales: 0,
    averageOrderValue: 0,
    totalOrders: 0,
    pendingOrders: 0,
    totalUsers: 0,
    usersThisWeek: 0,
    totalProducts: 0
  });
  const [ordersLoading, setOrdersLoading] = useState(true);
  const [usersLoading, setUsersLoading] = useState(true);
  const [productsLoading, setProductsLoading] = useState(true);
  const [orderError, setOrderError] = useState(null);
  const [userError, setUserError] = useState(null);
  const [productError, setProductError] = useState(null);
  const [dateRange, setDateRange] = useState('7days');
  const [customDateRange, setCustomDateRange] = useState(null);
  const [userDateRange, setUserDateRange] = useState('7days');
  const [customUserDateRange, setCustomUserDateRange] = useState(null);

  useEffect(() => {
    let active = true;

    const loadOrders = async () => {
      if (USE_DASHBOARD_MOCK) {
        const orders = MOCK_ORDERS;
        const sorted = [...orders].sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        const normalized = sorted
          .slice(0, 6)
          .map((order, index) => ({
            key: order.id || index,
            id: order.id,
            customer: order.customer,
            total: Number(order.total) || 0,
            status: order.status,
            createdAt: order.createdAt
          }));
        const totalSales = orders.reduce((sum, order) => sum + (Number(order.total) || 0), 0);
        const totalOrders = orders.length;
        const pendingOrders = orders.filter((order) => (order.status || '').toLowerCase() === 'pending').length;
        const averageOrderValue = totalOrders ? totalSales / totalOrders : 0;

        setRecentOrders(normalized);
        const trendDays = dateRange === '7days' ? 7 : dateRange === '30days' ? 30 : dateRange === '90days' ? 90 : 7;
        setOrderTrendData(buildOrderTrend(orders, trendDays, dateRange === 'custom' ? customDateRange : null));
        setStatistics((prev) => ({
          ...prev,
          totalSales,
          totalOrders,
          pendingOrders,
          averageOrderValue
        }));
        setOrdersLoading(false);
        setOrderError(null);
        return;
      }
      setOrdersLoading(true);
      setOrderError(null);
      const response = await OrderService.list({ limit: 50, sort: 'desc' });
      if (!active) return;

      if (response.EC === 0 || Array.isArray(response.DT) || Array.isArray(response)) {
        const rawOrders = Array.isArray(response.DT) ? response.DT : Array.isArray(response) ? response : [];
        const orders = rawOrders.map(o => ({
          ...o,
          total: o.totalPrice || o.total || 0,
          createdAt: o.createdAt || o.created_at || new Date().toISOString()
        }));
        
        const sorted = [...orders].sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
        const normalized = sorted.slice(0, 6).map((order) => ({
          key: order._id || order.id || Math.random().toString(36).slice(2),
          id: order._id || order.id,
          customer: order.shippingAddress?.fullName || order.user?.firstName + ' ' + order.user?.lastName || 'Khách lẻ',
          total: Number(order.total) || 0,
          status: (order.status || 'Pending').toLowerCase(),
          createdAt: order.createdAt
        }));

        const totalSales = orders.reduce((sum, order) => sum + (Number(order.total) || 0), 0);
        const totalOrders = orders.length;
        const pendingOrders = orders.filter((order) => (order.status || '').toLowerCase() === 'pending').length;
        const averageOrderValue = totalOrders ? totalSales / totalOrders : 0;

        setRecentOrders(normalized);
        setStatistics((prev) => ({
          ...prev,
          totalSales,
          totalOrders,
          pendingOrders,
          averageOrderValue
        }));
        const trendDays = dateRange === '7days' ? 7 : dateRange === '30days' ? 30 : dateRange === '90days' ? 90 : 7;
        setOrderTrendData(buildOrderTrend(orders, trendDays, dateRange === 'custom' ? customDateRange : null));
      } else {
        setRecentOrders([]);
        setOrderError(response.EM || 'Không thể tải đơn hàng');
        setOrderTrendData([]);
      }

      setOrdersLoading(false);
    };

    const loadUsers = async () => {
      if (USE_DASHBOARD_MOCK) {
        const users = MOCK_USERS;
        const normalized = users
          .map((user, index) => ({
            key: user.id || index,
            id: user.id,
            name: user.name,
            email: user.email,
            role: user.role || 'user',
            createdAt: user.createdAt
          }))
          .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));
        const now = Date.now();
        const usersThisWeek = normalized.filter((user) => {
          if (!user.createdAt) return false;
          const createdTime = new Date(user.createdAt).getTime();
          if (Number.isNaN(createdTime)) return false;
          const diffDays = (now - createdTime) / (1000 * 60 * 60 * 24);
          return diffDays <= 7;
        }).length;

        setRecentUsers(normalized.slice(0, 5));
        const userTrendDays = userDateRange === '7days' ? 7 : userDateRange === '30days' ? 30 : userDateRange === '90days' ? 90 : 7;
        setUserTrendData(buildUserTrend(normalized, userTrendDays, userDateRange === 'custom' ? customUserDateRange : null));
        setStatistics((prev) => ({
          ...prev,
          totalUsers: normalized.length,
          usersThisWeek
        }));
        setUsersLoading(false);
        setUserError(null);
        return;
      }
      setUsersLoading(true);
      setUserError(null);
      const response = await UserService.list();
      if (!active) return;

      if (response.EC === 0) {
        const users = Array.isArray(response.DT) ? response.DT : [];
        const normalized = users
          .map((user) => {
            const createdAt = user.createdAt || user.created_at || user.joinedAt || user.date || null;
            const name = (user.fullName || `${user.firstName || ''} ${user.lastName || ''}`.trim() || user.name || user.username || user.email || '').trim();
            return {
              key: user._id || user.id || user.email || Math.random().toString(36).slice(2),
              id: user._id || user.id,
              name: name || 'Người dùng',
              email: user.email || 'Không có email',
              role: user.role || user.type || 'user',
              createdAt
            };
          })
          .sort((a, b) => new Date(b.createdAt || 0) - new Date(a.createdAt || 0));

        const now = Date.now();
        const usersThisWeek = normalized.filter((user) => {
          if (!user.createdAt) return false;
          const createdTime = new Date(user.createdAt).getTime();
          if (Number.isNaN(createdTime)) return false;
          const diffDays = (now - createdTime) / (1000 * 60 * 60 * 24);
          return diffDays <= 7;
        }).length;

        setRecentUsers(normalized.slice(0, 5));
        setStatistics((prev) => ({
          ...prev,
          totalUsers: normalized.length,
          usersThisWeek
        }));
        const userTrendDays = userDateRange === '7days' ? 7 : userDateRange === '30days' ? 30 : userDateRange === '90days' ? 90 : 7;
        setUserTrendData(buildUserTrend(normalized, userTrendDays, userDateRange === 'custom' ? customUserDateRange : null));
      } else {
        setRecentUsers([]);
        setUserError(response.EM || 'Không thể tải người dùng');
        setUserTrendData([]);
      }

      setUsersLoading(false);
    };

    const loadProducts = async () => {
      if (USE_DASHBOARD_MOCK) {
        setStatistics((prev) => ({
          ...prev,
          totalProducts: MOCK_PRODUCTS.length
        }));
        setProductsLoading(false);
        setProductError(null);
        return;
      }
      setProductsLoading(true);
      setProductError(null);
      const response = await ProductService.getProducts({ page: 1, limit: 1 });
      if (!active) return;

      if (response.EC === 0 && response.DT) {
        const totalProducts = response.DT.total ?? response.DT.products?.length ?? 0;
        setStatistics((prev) => ({
          ...prev,
          totalProducts
        }));
      } else {
        setProductError(response.EM || 'Không thể tải sản phẩm');
      }

      setProductsLoading(false);
    };

    loadOrders();
    loadUsers();
    loadProducts();

    return () => {
      active = false;
    };
  }, [dateRange, customDateRange, userDateRange, customUserDateRange]);

  const hasOrderTrendData = useMemo(
    () => orderTrendData.some((item) => (item.orders || 0) > 0 || (item.revenue || 0) > 0),
    [orderTrendData]
  );

  const hasUserTrendData = useMemo(
    () => userTrendData.some((item) => (item.users || 0) > 0),
    [userTrendData]
  );

  const statTiles = useMemo(
    () => [
      {
        key: 'revenue',
        title: 'Tổng doanh thu',
        value: statistics.totalSales,
        formatter: formatCurrency,
        icon: <DollarOutlined />,
        theme: '#1d39c4',
        description: ordersLoading ? 'Đang tính toán...' : 'Doanh thu tích lũy từ đơn hàng',
        route: '/admin/orders'
      },
      {
        key: 'orders',
        title: 'Đơn hàng',
        value: statistics.totalOrders,
        formatter: formatNumber,
        icon: <ShoppingCartOutlined />,
        theme: '#08979c',
        description: ordersLoading ? 'Đang tải...' : `Đang chờ: ${formatNumber(statistics.pendingOrders)}`,
        route: '/admin/orders'
      },
      {
        key: 'users',
        title: 'Người dùng',
        value: statistics.totalUsers,
        formatter: formatNumber,
        icon: <UserOutlined />,
        theme: '#722ed1',
        description: userError ? 'Không thể tải người dùng' : (usersLoading ? 'Đang tải...' : `+${formatNumber(statistics.usersThisWeek)} trong 7 ngày`),
        route: '/admin/users'
      },
      {
        key: 'products',
        title: 'Sản phẩm',
        value: statistics.totalProducts,
        formatter: formatNumber,
        icon: <ShoppingOutlined />,
        theme: '#fa8c16',
        description: productError ? 'Không thể tải sản phẩm' : (productsLoading ? 'Đang tải...' : 'SKU đang mở bán'),
        route: '/admin/products'
      }
    ],
    [statistics, ordersLoading, usersLoading, productsLoading, userError, productError]
  );

  const orderColumns = useMemo(
    () => [
      {
        title: 'Mã đơn',
        dataIndex: 'id',
        key: 'id',
        render: (value) => <Tag color="blue">#{value || 'N/A'}</Tag>
      },
      {
        title: 'Khách hàng',
        dataIndex: 'customer',
        key: 'customer'
      },
      {
        title: 'Tổng tiền',
        dataIndex: 'total',
        key: 'total',
        align: 'right',
        render: (value) => <span style={{ fontWeight: 600, color: '#cf1322' }}>{formatCurrency(value)}</span>
      },
      {
        title: 'Trạng thái',
        dataIndex: 'status',
        key: 'status',
        render: (status) => {
          const meta = statusMeta[(status || '').toLowerCase()] || { color: 'default', label: status || 'Chưa xác định' };
          return <Badge color={meta.color} text={meta.label} />;
        }
      },
      {
        title: 'Ngày đặt',
        dataIndex: 'createdAt',
        key: 'createdAt',
        render: (value) => <span>{friendlyDate(value)}</span>
      }
    ],
    []
  );

  return (
    <div style={{ display: 'flex', flexDirection: 'column', gap: 24 }}>
      <Card
        bordered={false}
        style={{ borderRadius: 14, background: 'linear-gradient(135deg, #f5f9ff 0%, #ffffff 100%)' }}
      >
        <Space align="start" size={16} style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
          <Space direction="vertical" size={4}>
            <Title level={3} style={{ margin: 0, color: '#1d39c4' }}>Xin chào, quản trị viên!</Title>
            <Text style={{ color: '#597ef7' }}>
              Tổng quan số liệu cập nhật đến {new Date().toLocaleDateString('vi-VN')}
            </Text>
          </Space>
          <Tag icon={<RiseOutlined />} color="blue" style={{ borderRadius: 20, padding: '4px 12px' }}>
            Hệ thống hoạt động ổn định
          </Tag>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {statTiles.map((tile) => (
          <Col xs={24} sm={12} lg={6} key={tile.key}>
            <Card
              hoverable={Boolean(tile.route)}
              bordered={false}
              onClick={() => tile.route && navigate(tile.route)}
              style={{
                borderRadius: 14,
                height: '100%',
                boxShadow: '0 10px 24px rgba(52,106,255,0.08)',
                cursor: tile.route ? 'pointer' : 'default'
              }}
            >
              <Space align="start" size={16} style={{ width: '100%', justifyContent: 'space-between' }}>
                <Space direction="vertical" size={6}>
                  <Text style={{ color: '#8c8c8c', textTransform: 'uppercase', fontSize: 13 }}>
                    {tile.title}
                  </Text>
                  <Statistic
                    value={tile.value}
                    formatter={(val) => tile.formatter(val)}
                    valueStyle={{ color: tile.theme, fontWeight: 700, fontSize: 28 }}
                  />
                  <Text style={{ color: '#8c8c8c', fontSize: 12 }}>{tile.description}</Text>
                </Space>
                <div
                  style={{
                    width: 48,
                    height: 48,
                    borderRadius: '50%',
                    background: `${tile.theme}1a`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    fontSize: 22,
                    color: tile.theme
                  }}
                >
                  {tile.icon}
                </div>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} lg={16}>
          <Card
            title={
              <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
                <Space><DollarOutlined /> <span style={{ fontWeight: 600 }}>Hiệu suất bán hàng</span></Space>
                <Space size={8}>
                  <Select
                    value={dateRange}
                    onChange={(value) => {
                      setDateRange(value);
                      if (value !== 'custom') {
                        setCustomDateRange(null);
                      }
                    }}
                    style={{ width: 140 }}
                    options={[
                      { label: '7 ngày', value: '7days' },
                      { label: '30 ngày', value: '30days' },
                      { label: '90 ngày', value: '90days' },
                      { label: 'Tùy chỉnh', value: 'custom' }
                    ]}
                  />
                  {dateRange === 'custom' && (
                    <DatePicker.RangePicker
                      value={customDateRange}
                      onChange={(dates) => setCustomDateRange(dates)}
                      format="DD/MM/YYYY"
                      placeholder={['Từ ngày', 'Đến ngày']}
                    />
                  )}
                </Space>
              </Space>
            }
            bordered={false}
            style={{ borderRadius: 14, height: '100%' }}
          >
            {ordersLoading ? (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 280 }}>
                <Spin />
              </div>
            ) : hasOrderTrendData ? (
              <div style={{ height: 280 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <ComposedChart data={orderTrendData} margin={{ top: 10, right: 20, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                    <YAxis
                      yAxisId="left"
                      tick={{ fontSize: 12 }}
                      tickFormatter={(value) => formatNumber(value)}
                    />
                    <YAxis
                      yAxisId="right"
                      orientation="right"
                      allowDecimals={false}
                      width={40}
                      tick={{ fontSize: 12 }}
                      tickFormatter={(value) => formatNumber(value)}
                    />
                    <RechartsTooltip
                      formatter={(value, name) => {
                        if (name === 'Doanh thu') return [formatCurrency(value), name];
                        return [formatNumber(value), name];
                      }}
                    />
                    <Legend />
                    <defs>
                      <linearGradient id="revenueGradient" x1="0" y1="0" x2="0" y2="1">
                        <stop offset="5%" stopColor="#1d39c4" stopOpacity={0.4} />
                        <stop offset="95%" stopColor="#1d39c4" stopOpacity={0.05} />
                      </linearGradient>
                    </defs>
                    <Area
                      yAxisId="left"
                      type="monotone"
                      dataKey="revenue"
                      name="Doanh thu"
                      stroke="#1d39c4"
                      fill="url(#revenueGradient)"
                      strokeWidth={2}
                    />
                    <Line
                      yAxisId="right"
                      type="monotone"
                      dataKey="orders"
                      name="Đơn hàng"
                      stroke="#fa8c16"
                      strokeWidth={2}
                      dot={{ r: 3 }}
                      activeDot={{ r: 5 }}
                    />
                  </ComposedChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <Empty description="Chưa có dữ liệu đơn hàng" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
        <Col xs={24} lg={8}>
          <Card
            title={
              <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
                <Space><UserOutlined /> <span style={{ fontWeight: 600 }}>Người dùng mới</span></Space>
                <Space size={8}>
                  <Select
                    value={userDateRange}
                    onChange={(value) => {
                      setUserDateRange(value);
                      if (value !== 'custom') {
                        setCustomUserDateRange(null);
                      }
                    }}
                    style={{ width: 140 }}
                    options={[
                      { label: '7 ngày', value: '7days' },
                      { label: '30 ngày', value: '30days' },
                      { label: '90 ngày', value: '90days' },
                      { label: 'Tùy chỉnh', value: 'custom' }
                    ]}
                  />
                  {userDateRange === 'custom' && (
                    <DatePicker.RangePicker
                      value={customUserDateRange}
                      onChange={(dates) => setCustomUserDateRange(dates)}
                      format="DD/MM/YYYY"
                      placeholder={['Từ ngày', 'Đến ngày']}
                    />
                  )}
                </Space>
              </Space>
            }
            bordered={false}
            style={{ borderRadius: 14, height: '100%' }}
          >
            {usersLoading ? (
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', height: 280 }}>
                <Spin />
              </div>
            ) : hasUserTrendData ? (
              <div style={{ height: 280 }}>
                <ResponsiveContainer width="100%" height="100%">
                  <BarChart data={userTrendData} margin={{ top: 10, right: 10, left: 0, bottom: 0 }}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#f0f0f0" />
                    <XAxis dataKey="date" tick={{ fontSize: 12 }} />
                    <YAxis allowDecimals={false} tick={{ fontSize: 12 }} />
                    <RechartsTooltip formatter={(value) => [formatNumber(value), 'Người dùng']} />
                    <Bar dataKey="users" name="Người dùng mới" fill="#722ed1" radius={[6, 6, 0, 0]} />
                  </BarChart>
                </ResponsiveContainer>
              </div>
            ) : (
              <Empty description="Chưa có dữ liệu người dùng" image={Empty.PRESENTED_IMAGE_SIMPLE} />
            )}
          </Card>
        </Col>
      </Row>

      <Row gutter={[16, 16]}>
        <Col xs={24} xl={16}>
          <Card
            title={<Space><ShoppingCartOutlined /> <span style={{ fontWeight: 600 }}>Đơn hàng gần đây</span></Space>}
            extra={
              <Button type="link" onClick={() => navigate('/admin/orders')}>
                Xem tất cả
              </Button>
            }
            bordered={false}
            style={{ borderRadius: 14, height: '100%' }}
          >
            {orderError ? (
              <Empty description={orderError} image={Empty.PRESENTED_IMAGE_SIMPLE} />
            ) : (
              <Table
                columns={orderColumns}
                dataSource={recentOrders}
                loading={ordersLoading}
                pagination={false}
                scroll={{ x: 720 }}
                locale={{ emptyText: ordersLoading ? 'Đang tải...' : 'Chưa có đơn hàng nào' }}
              />
            )}
          </Card>
        </Col>

        <Col xs={24} xl={8}>
          <Space direction="vertical" size={16} style={{ width: '100%' }}>
            <Card
              title={<Space><DashboardOutlined /> <span style={{ fontWeight: 600 }}>Tóm tắt nhanh</span></Space>}
              bordered={false}
              style={{ borderRadius: 14 }}
            >
              <Space direction="vertical" size={16} style={{ width: '100%' }}>
                <Card size="small" bordered={false} style={{ background: '#f6ffed', borderRadius: 12 }}>
                  <Space align="start">
                    <DollarOutlined style={{ color: '#237804', fontSize: 20 }} />
                    <Space direction="vertical" size={0}>
                      <Text strong>Doanh thu trung bình</Text>
                      <Text type="secondary">
                        {ordersLoading ? 'Đang tải...' : formatCurrency(statistics.averageOrderValue)}
                      </Text>
                    </Space>
                  </Space>
                </Card>

                <Card size="small" bordered={false} style={{ background: '#e6f4ff', borderRadius: 12 }}>
                  <Space align="start">
                    <InboxOutlined style={{ color: '#0958d9', fontSize: 20 }} />
                    <Space direction="vertical" size={0}>
                      <Text strong>Đơn chờ xử lý</Text>
                      <Text type="secondary">
                        {ordersLoading ? 'Đang tải...' : formatNumber(statistics.pendingOrders)}
                      </Text>
                    </Space>
                  </Space>
                </Card>

                <Card size="small" bordered={false} style={{ background: '#f9f0ff', borderRadius: 12 }}>
                  <Space align="start">
                    <UserOutlined style={{ color: '#722ed1', fontSize: 20 }} />
                    <Space direction="vertical" size={0}>
                      <Text strong>Người dùng mới</Text>
                      <Text type="secondary">
                        {usersLoading ? 'Đang tải...' : `${formatNumber(statistics.usersThisWeek)} trong 7 ngày`}
                      </Text>
                    </Space>
                  </Space>
                </Card>
              </Space>
            </Card>

            <Card
              title={<Space><UserOutlined /> <span style={{ fontWeight: 600 }}>Người dùng mới nhất</span></Space>}
              bordered={false}
              style={{ borderRadius: 14 }}
            >
              {userError ? (
                <Empty description={userError} image={Empty.PRESENTED_IMAGE_SIMPLE} />
              ) : (
                <List
                  dataSource={recentUsers}
                  loading={usersLoading}
                  split
                  locale={{ emptyText: usersLoading ? 'Đang tải...' : 'Chưa có người dùng mới' }}
                  renderItem={(item) => (
                    <List.Item key={item.key}>
                      <List.Item.Meta
                        title={<Text strong>{item.name}</Text>}
                        description={<Text type="secondary">{item.email}</Text>}
                      />
                      <Text type="secondary" style={{ fontSize: 12 }}>
                        {friendlyDate(item.createdAt)}
                      </Text>
                    </List.Item>
                  )}
                />
              )}
            </Card>
          </Space>
        </Col>
      </Row>

      <Card
        title={<Space><RiseOutlined /> <span style={{ fontWeight: 600 }}>Thao tác nhanh</span></Space>}
        bordered={false}
        style={{ borderRadius: 14 }}
      >
        <Row gutter={[16, 16]}>
          <Col xs={24} md={8}>
            <Button
              type="primary"
              size="large"
              block
              style={{ background: 'linear-gradient(90deg, #52c41a 0%, #73d13d 100%)', border: 'none', borderRadius: 10 }}
              onClick={() => navigate('/admin/products')}
            >
              Quản lý sản phẩm
            </Button>
          </Col>
          <Col xs={24} md={8}>
            <Button
              type="primary"
              size="large"
              block
              style={{ background: 'linear-gradient(90deg, #1890ff 0%, #40a9ff 100%)', border: 'none', borderRadius: 10 }}
              onClick={() => navigate('/admin/orders')}
            >
              Quản lý đơn hàng
            </Button>
          </Col>
          <Col xs={24} md={8}>
            <Button
              type="primary"
              size="large"
              block
              style={{ background: 'linear-gradient(90deg, #722ed1 0%, #9254de 100%)', border: 'none', borderRadius: 10 }}
            >
              Báo cáo thống kê
            </Button>
          </Col>
        </Row>
      </Card>
    </div>
  );
};

export default Dashboard;