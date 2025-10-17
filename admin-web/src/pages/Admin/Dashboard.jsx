import React, { useState, useEffect } from 'react';
import { 
  Row, 
  Col, 
  Card, 
  Statistic, 
  Table, 
  Button, 
  Typography,
  Space,
  Badge 
} from 'antd';
import { 
  ShoppingOutlined, 
  UserOutlined, 
  DollarOutlined,
  ShoppingCartOutlined,
  ArrowUpOutlined,
  ArrowDownOutlined,
  DashboardOutlined
} from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import { OrderService } from '../../services/api/orders';

const { Title } = Typography;

const Dashboard = () => {
  const [loading, setLoading] = useState(true);
  const [statistics, setStatistics] = useState({
    totalSales: 0,
    totalOrders: 0,
    totalUsers: 0,
    totalProducts: 0,
  });
  const [recentOrders, setRecentOrders] = useState([]);
  const navigate = useNavigate();

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      const res = await OrderService.list({ limit: 5 });
      if (res.EC === 0) {
        const orders = res.DT.slice(0,5);
        setRecentOrders(orders.map(o => ({
          key: o.id,
          id: o.id,
            customer: o.customerName || o.customer || '',
            total: o.total,
            status: o.status,
            date: o.createdAt?.substring(0,10)
        })));
        setStatistics(s => ({
          ...s,
          totalOrders: res.DT.length,
          totalSales: res.DT.reduce((sum, o) => sum + (o.total||0), 0)
        }));
      } else {
        setRecentOrders([]);
      }
      setLoading(false);
    };
    load();
  }, []);

  const orderColumns = [
    {
      title: 'Mã đơn',
      dataIndex: 'id',
      key: 'id',
      render: (text) => <span style={{ color: '#1890ff', fontWeight: 500 }}>{text}</span>
    },
    {
      title: 'Khách hàng',
      dataIndex: 'customer',
      key: 'customer',
    },
    {
      title: 'Tổng tiền',
      dataIndex: 'total',
      key: 'total',
      render: (value) => (
        <span style={{ color: '#e74c3c', fontWeight: 500 }}>
          {value.toLocaleString('vi-VN')}₫
        </span>
      )
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => {
        const statusMap = {
          completed: { color: 'green', text: 'Hoàn thành' },
          pending: { color: 'gold', text: 'Chờ xử lý' },
          shipping: { color: 'blue', text: 'Đang giao' }
        };
        const { color, text } = statusMap[status] || { color: 'default', text: status };
        return <Badge color={color} text={text} />;
      }
    },
    {
      title: 'Ngày đặt',
      dataIndex: 'date',
      key: 'date',
    }
  ];

  return (
    <div className="fade-in">
      <div className="text-center mb-32">
        <DashboardOutlined style={{ fontSize: '48px', color: '#1890ff', marginBottom: '16px' }} />
        <Title level={2} className="responsive-title" style={{ color: "#1890ff", margin: 0 }}>
          Bảng điều khiển Admin
        </Title>
      </div>

          {/* Statistics Cards */}
          <Row gutter={[16, 16]} className="mb-32">
            <Col xs={24} sm={12} lg={6}>
              <Card className="responsive-card slide-up" style={{ animationDelay: '0.1s' }}>
                <Statistic
                  title="Doanh thu"
                  value={statistics.totalSales}
                  formatter={value => `${value.toLocaleString('vi-VN')}₫`}
                  prefix={<DollarOutlined style={{ color: '#52c41a' }} />}
                  valueStyle={{ color: '#52c41a', fontWeight: 600 }}
                />
                <div style={{ marginTop: 8 }}>
                  <ArrowUpOutlined style={{ color: '#52c41a' }} />
                  <span style={{ color: '#52c41a', marginLeft: 4 }}>12.5%</span>
                  <span style={{ color: '#666', marginLeft: 8 }}>so với tháng trước</span>
                </div>
              </Card>
            </Col>
            
            <Col xs={24} sm={12} lg={6}>
              <Card className="responsive-card slide-up" style={{ animationDelay: '0.2s' }}>
                <Statistic
                  title="Đơn hàng"
                  value={statistics.totalOrders}
                  prefix={<ShoppingCartOutlined style={{ color: '#1890ff' }} />}
                  valueStyle={{ color: '#1890ff', fontWeight: 600 }}
                />
                <div style={{ marginTop: 8 }}>
                  <ArrowUpOutlined style={{ color: '#52c41a' }} />
                  <span style={{ color: '#52c41a', marginLeft: 4 }}>8.2%</span>
                  <span style={{ color: '#666', marginLeft: 8 }}>so với tháng trước</span>
                </div>
              </Card>
            </Col>
            
            <Col xs={24} sm={12} lg={6}>
              <Card className="responsive-card slide-up" style={{ animationDelay: '0.3s' }}>
                <Statistic
                  title="Người dùng"
                  value={statistics.totalUsers}
                  prefix={<UserOutlined style={{ color: '#722ed1' }} />}
                  valueStyle={{ color: '#722ed1', fontWeight: 600 }}
                />
                <div style={{ marginTop: 8 }}>
                  <ArrowUpOutlined style={{ color: '#52c41a' }} />
                  <span style={{ color: '#52c41a', marginLeft: 4 }}>15.3%</span>
                  <span style={{ color: '#666', marginLeft: 8 }}>so với tháng trước</span>
                </div>
              </Card>
            </Col>
            
            <Col xs={24} sm={12} lg={6}>
              <Card className="responsive-card slide-up" style={{ animationDelay: '0.4s' }}>
                <Statistic
                  title="Sản phẩm"
                  value={statistics.totalProducts}
                  prefix={<ShoppingOutlined style={{ color: '#fa8c16' }} />}
                  valueStyle={{ color: '#fa8c16', fontWeight: 600 }}
                />
                <div style={{ marginTop: 8 }}>
                  <ArrowUpOutlined style={{ color: '#52c41a' }} />
                  <span style={{ color: '#52c41a', marginLeft: 4 }}>3.1%</span>
                  <span style={{ color: '#666', marginLeft: 8 }}>so với tháng trước</span>
                </div>
              </Card>
            </Col>
          </Row>

          {/* Recent Orders */}
          <Card 
            className="responsive-card slide-up" 
            style={{ animationDelay: '0.5s' }}
            title={
              <span style={{ fontSize: '18px', fontWeight: 600, color: '#1890ff' }}>
                Đơn hàng gần đây
              </span>
            }
            extra={
              <Button 
                type="primary" 
                onClick={() => navigate('/admin/orders')}
                className="responsive-button"
              >
                Xem tất cả
              </Button>
            }
          >
            <div className="responsive-table-container">
              <Table
                columns={orderColumns}
                dataSource={recentOrders}
                loading={loading}
                pagination={false}
                scroll={{ x: 600 }}
                className="responsive-table"
              />
            </div>
          </Card>

          {/* Quick Actions */}
          <Card 
            className="responsive-card slide-up" 
            style={{ animationDelay: '0.6s', marginTop: '24px' }}
            title={
              <span style={{ fontSize: '18px', fontWeight: 600, color: '#1890ff' }}>
                Thao tác nhanh
              </span>
            }
          >
            <Row gutter={[16, 16]}>
              <Col xs={24} sm={12} md={8}>
                <Button
                  type="primary"
                  size="large"
                  className="responsive-button"
                  style={{
                    background: "linear-gradient(90deg, #52c41a 0%, #73d13d 100%)",
                    border: "none",
                    fontWeight: 600,
                    borderRadius: 8,
                  }}
                  onClick={() => navigate('/admin/products')}
                >
                  Quản lý sản phẩm
                </Button>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Button
                  type="primary"
                  size="large"
                  className="responsive-button"
                  style={{
                    background: "linear-gradient(90deg, #1890ff 0%, #40a9ff 100%)",
                    border: "none",
                    fontWeight: 600,
                    borderRadius: 8,
                  }}
                  onClick={() => navigate('/admin/orders')}
                >
                  Quản lý đơn hàng
                </Button>
              </Col>
              <Col xs={24} sm={12} md={8}>
                <Button
                  type="primary"
                  size="large"
                  className="responsive-button"
                  style={{
                    background: "linear-gradient(90deg, #722ed1 0%, #9254de 100%)",
                    border: "none",
                    fontWeight: 600,
                    borderRadius: 8,
                  }}
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
