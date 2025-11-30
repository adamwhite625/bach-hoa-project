import React, { useState, useEffect, useMemo, useCallback } from 'react';
import {
  Table,
  Card,
  Space,
  Button,
  Select,
  Input,
  DatePicker,
  Typography,
  message,
  Descriptions,
  Badge,
  Drawer,
  Row,
  Col,
  Statistic,
  Empty,
  Steps,
  Divider,
  Popconfirm,
  Skeleton,
  Tag
} from 'antd';
import {
  SearchOutlined,
  EyeOutlined,
  DeleteOutlined,
  ReloadOutlined,
  ShoppingCartOutlined,
  CheckCircleOutlined,
  ClockCircleOutlined,
  DollarCircleOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import isBetween from 'dayjs/plugin/isBetween';
import { OrderService } from '../../services/api/orders';
import { useNavigate } from 'react-router-dom';

dayjs.extend(isBetween);

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

const SkeletonTable = () => (
  <Space direction="vertical" style={{ width: '100%' }} size="middle">
    <Skeleton active paragraph={{ rows: 2 }} />
    <Skeleton active paragraph={{ rows: 2 }} />
  </Space>
);

const formatNumber = (value) => new Intl.NumberFormat('vi-VN').format(Number(value) || 0);
const formatCurrency = (value) => new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0
}).format(Number(value) || 0);

const OrderManager = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [selectedOrder, setSelectedOrder] = useState(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const [filters, setFilters] = useState({
    status: 'all',
    dateRange: [],
    search: ''
  });
  const navigate = useNavigate();

  const fetchOrders = useCallback(async () => {
    setLoading(true);
    const res = await OrderService.list();
    if (res.EC === 0) {
      setOrders(res.DT);
    } else {
      message.error(res.EM || 'Không thể tải đơn hàng');
      setOrders([]);
    }
    setLoading(false);
  }, []);

  useEffect(() => {
    fetchOrders();
  }, [fetchOrders]);

  const handleStatusChange = async (orderId, newStatus) => {
    const hide = message.loading('Đang cập nhật trạng thái...', 0);
    try {
      const res = await OrderService.updateStatus(orderId, newStatus);
      hide();
      if (res.EC === 0) {
        // Fetch fresh data to ensure consistency
        await fetchOrders();
        
        // Update selected order if drawer is open
        if (selectedOrder?.id === orderId) {
          const updatedOrderData = res.DT || {};
          setSelectedOrder(prev => prev ? {
            ...prev,
            ...updatedOrderData,
            id: updatedOrderData._id || updatedOrderData.id || orderId,
            status: newStatus
          } : prev);
        }
        
        message.success('Cập nhật trạng thái thành công');
      } else {
        message.error(res.EM || 'Không thể cập nhật trạng thái đơn hàng');
      }
    } catch (error) {
      hide();
      console.error('Update status error:', error);
      message.error('Không thể cập nhật trạng thái đơn hàng');
    }
  };

  const handleDeleteOrder = async (orderId) => {
    try {
      const res = await OrderService.remove(orderId);
      if (res.EC === 0) {
        setOrders(prev => prev.filter(order => order.id !== orderId));
        message.success('Xóa đơn hàng thành công');
        if (selectedOrder?.id === orderId) {
          setDrawerVisible(false);
          setSelectedOrder(null);
        }
      } else {
        message.error(res.EM || 'Không thể xóa đơn hàng');
      }
    } catch (error) {
      message.error('Không thể xóa đơn hàng');
    }
  };

  const filteredOrders = useMemo(() => {
    const { status, dateRange, search } = filters;
    return orders.filter(order => {
      const matchesStatus = status === 'all' || order.status === status;
      const matchesSearch = !search || [order.id, order.customerName, order.email, order.phone]
        .filter(Boolean)
        .some((field) => String(field).toLowerCase().includes(search.trim().toLowerCase()))
      ;
      const matchesDate = !dateRange?.length
        || (order.createdAt && dayjs(order.createdAt).isBetween(dateRange[0], dateRange[1], 'day', '[]'));
      return matchesStatus && matchesSearch && matchesDate;
    });
  }, [orders, filters]);

  const metrics = useMemo(() => {
    if (!orders.length) {
      return {
        totalOrders: 0,
        totalRevenue: 0,
        pending: 0,
        completed: 0,
        cancelled: 0
      };
    }
    return orders.reduce((acc, order) => {
      acc.totalOrders += 1;
      acc.totalRevenue += order.total || 0;
      if (order.status === 'pending') acc.pending += 1;
      if (order.status === 'completed') acc.completed += 1;
      if (order.status === 'cancelled') acc.cancelled += 1;
      return acc;
    }, { totalOrders: 0, totalRevenue: 0, pending: 0, completed: 0, cancelled: 0 });
  }, [orders]);

  const summaryCards = useMemo(() => ([
    {
      key: 'totalOrders',
      title: 'Tổng đơn hàng',
      value: metrics.totalOrders,
      icon: <ShoppingCartOutlined style={{ color: '#1d39c4' }} />,
      formatter: formatNumber
    },
    {
      key: 'totalRevenue',
      title: 'Doanh thu lũy kế',
      value: metrics.totalRevenue,
      icon: <DollarCircleOutlined style={{ color: '#52c41a' }} />,
      formatter: formatCurrency
    },
    {
      key: 'pending',
      title: 'Đơn đang xử lý',
      value: metrics.pending,
      icon: <ClockCircleOutlined style={{ color: '#faad14' }} />,
      formatter: formatNumber
    },
    {
      key: 'completed',
      title: 'Đơn hoàn thành',
      value: metrics.completed,
      icon: <CheckCircleOutlined style={{ color: '#13c2c2' }} />,
      formatter: formatNumber
    }
  ]), [metrics]);

  const columns = [
    {
      title: 'Mã đơn hàng',
      dataIndex: 'id',
      key: 'id',
      render: (text, record) => (
        <span
          style={{ color: '#1677ff', cursor: 'pointer', fontWeight:500 }}
          onClick={() => {
            setSelectedOrder(record);
            setDrawerVisible(true);
          }}
        >
          {text}
        </span>
      )
    },
    {
      title: 'Khách hàng',
      dataIndex: 'customerName',
      key: 'customerName',
      render: (name, record) => (
        <Space direction="vertical" size={0}>
          <span style={{ fontWeight: 500 }}>{name || '—'}</span>
          <Text type="secondary" style={{ fontSize: 12 }}>
            {record.email || 'Không có email'}
          </Text>
        </Space>
      )
    },
    {
      title: 'Tổng tiền',
      dataIndex: 'total',
      key: 'total',
      render: (total) => `${total.toLocaleString()}₫`,
      sorter: (a, b) => a.total - b.total,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status, record) => (
        <Select
          value={status}
          onChange={(value) => handleStatusChange(record.id, value)}
          onClick={(e) => e.stopPropagation()}
          style={{ width: 140 }}
        >
          <Option value="Pending">Chờ xử lý</Option>
          <Option value="Processing">Đang xử lý</Option>
          <Option value="Shipped">Đang giao</Option>
          <Option value="Delivered">Hoàn thành</Option>
          <Option value="Cancelled">Đã hủy</Option>
        </Select>
      ),
      filters: [
        { text: 'Chờ xử lý', value: 'Pending' },
        { text: 'Đang xử lý', value: 'Processing' },
        { text: 'Đang giao', value: 'Shipped' },
        { text: 'Hoàn thành', value: 'Delivered' },
        { text: 'Đã hủy', value: 'Cancelled' },
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Ngày đặt',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (value) => value ? dayjs(value).format('DD/MM/YYYY HH:mm') : '—',
      sorter: (a, b) => dayjs(a.createdAt).unix() - dayjs(b.createdAt).unix(),
    },
    {
      title: 'Thao tác',
      key: 'action',
      render: (_, record) => (
        <Space onClick={(e) => e.stopPropagation()}>
          <Button
            icon={<EyeOutlined />}
            onClick={(e) => {
              e.stopPropagation();
              setSelectedOrder(record);
              setDrawerVisible(true);
            }}
          >
            Chi tiết
          </Button>
          <Popconfirm
            title="Xác nhận xóa đơn hàng"
            description="Bạn chắc chắn muốn xóa đơn hàng này?"
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true }}
            onConfirm={() => handleDeleteOrder(record.id)}
          >
            <Button icon={<DeleteOutlined />} danger>
              Xóa
            </Button>
          </Popconfirm>
        </Space>
      ),
    },
  ];

  const getStatusBadge = (status) => {
    const normalizedStatus = status?.toLowerCase();
    const statusMap = {
      pending: { status: 'warning', text: 'Chờ xử lý' },
      processing: { status: 'processing', text: 'Đang xử lý' },
      shipped: { status: 'processing', text: 'Đang giao' },
      delivered: { status: 'success', text: 'Hoàn thành' },
      cancelled: { status: 'error', text: 'Đã hủy' }
    };
    return statusMap[normalizedStatus] || { status: 'default', text: status };
  };

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 24 }}>
      <Card
        bordered={false}
        style={{
          borderRadius: 16,
          background: 'linear-gradient(135deg, #f0f5ff 0%, #ffffff 100%)'
        }}
      >
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          <Space align="center" size={12} wrap>
            <ShoppingCartOutlined style={{ fontSize: 24, color: '#1d39c4' }} />
            <Title level={3} style={{ margin: 0, color: '#1d39c4' }}>Quản lý đơn hàng</Title>
            <Tag color="blue" style={{ borderRadius: 16 }}>
              Tổng {formatNumber(metrics.totalOrders)} đơn
            </Tag>
          </Space>
          <Text type="secondary">
            Theo dõi trạng thái xử lý và chi tiết từng đơn để đảm bảo luồng giao hàng thông suốt.
          </Text>
          <Space size={8} wrap>
            <Tag color="processing">Đang xử lý: {formatNumber(metrics.pending)}</Tag>
            <Tag color="success">Hoàn thành: {formatNumber(metrics.completed)}</Tag>
            <Tag color="red">Đã hủy: {formatNumber(metrics.cancelled)}</Tag>
          </Space>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {summaryCards.map((card) => (
          <Col key={card.key} xs={24} sm={12} xl={6}>
            <Card bordered={false} style={{ borderRadius: 16, boxShadow: '0 4px 12px rgba(0,0,0,0.05)' }}>
              <Statistic
                title={card.title}
                value={card.value}
                prefix={card.icon}
                valueStyle={{ fontSize: 22, fontWeight: 600 }}
                formatter={(value) => (card.formatter ? card.formatter(value) : value)}
              />
            </Card>
          </Col>
        ))}
      </Row>

      <Card bordered={false} style={{ borderRadius: 18 }}>
        <Space direction="vertical" size="large" style={{ width: '100%' }}>
          <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap', gap: 12 }}>
            <Space size={12} wrap>
              <Title level={4} style={{ margin: 0 }}>Danh sách đơn hàng</Title>
              <Tag color="blue">
                Hiển thị {formatNumber(filteredOrders.length)} / {formatNumber(orders.length)} đơn
              </Tag>
            </Space>
            <Space size={12} wrap>
              <Button icon={<ReloadOutlined />} onClick={fetchOrders} loading={loading}>
                Tải lại
              </Button>
            </Space>
          </Space>

          <Space wrap size={12}>
            <Input
              placeholder="Tìm theo mã đơn, tên khách, email..."
              prefix={<SearchOutlined />}
              style={{ width: 260 }}
              value={filters.search}
              onChange={e => setFilters({ ...filters, search: e.target.value })}
              allowClear
            />
            <Select
              style={{ width: 180 }}
              value={filters.status}
              onChange={value => setFilters({ ...filters, status: value })}
            >
              <Option value="all">Tất cả trạng thái</Option>
              <Option value="Pending">Chờ xử lý</Option>
              <Option value="Processing">Đang xử lý</Option>
              <Option value="Shipped">Đang giao</Option>
              <Option value="Delivered">Hoàn thành</Option>
              <Option value="Cancelled">Đã hủy</Option>
            </Select>
            <RangePicker
              value={filters.dateRange?.length ? filters.dateRange : null}
              onChange={dates => setFilters({ ...filters, dateRange: dates || [] })}
            />
          </Space>

          <Table
            columns={columns}
            dataSource={filteredOrders}
            loading={loading}
            locale={{ emptyText: loading ? <SkeletonTable /> : <Empty description="Không có đơn hàng" /> }}
            rowKey="id"
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} đơn hàng`
            }}
            onRow={(record) => ({
              onClick: () => {
                setSelectedOrder(record);
                setDrawerVisible(true);
              },
              style: { cursor: 'pointer' }
            })}
          />
        </Space>
      </Card>

      {/* Order Details Drawer */}
      <Drawer
        title="Chi tiết đơn hàng"
        placement="right"
        width={640}
        onClose={() => {
          setDrawerVisible(false);
          setSelectedOrder(null);
        }}
        open={drawerVisible}
      >
        {selectedOrder && (
          <Space direction="vertical" size="large" style={{ width: '100%' }}>
            <Descriptions column={1} bordered>
              <Descriptions.Item label="Mã đơn hàng">
                {selectedOrder.id}
              </Descriptions.Item>
              <Descriptions.Item label="Trạng thái">
                <Badge
                  status={getStatusBadge(selectedOrder.status).status}
                  text={getStatusBadge(selectedOrder.status).text}
                />
              </Descriptions.Item>
              <Descriptions.Item label="Khách hàng">
                {selectedOrder.customerName}
              </Descriptions.Item>
              <Descriptions.Item label="Email">
                {selectedOrder.email}
              </Descriptions.Item>
              <Descriptions.Item label="Số điện thoại">
                {selectedOrder.phone}
              </Descriptions.Item>
              <Descriptions.Item label="Địa chỉ giao hàng">
                {typeof selectedOrder.shippingAddress === 'object' 
                  ? `${selectedOrder.shippingAddress.address || ''}, ${selectedOrder.shippingAddress.city || ''}`.trim().replace(/^,\s*|,\s*$/g, '') || 'Không có địa chỉ'
                  : selectedOrder.shippingAddress || 'Không có địa chỉ'}
              </Descriptions.Item>
              <Descriptions.Item label="Phương thức thanh toán">
                {selectedOrder.paymentMethod}
              </Descriptions.Item>
              <Descriptions.Item label="Ngày đặt">
                {selectedOrder.createdAt ? dayjs(selectedOrder.createdAt).format('DD/MM/YYYY HH:mm') : '—'}
              </Descriptions.Item>
            </Descriptions>

            <Steps
              size="small"
              current={(() => {
                const normalizedStatus = selectedOrder.status?.toLowerCase();
                if (normalizedStatus === 'cancelled') return 0;
                const steps = ['pending', 'processing', 'shipped', 'delivered'];
                return Math.max(steps.indexOf(normalizedStatus), 0);
              })()}
              items={selectedOrder.status?.toLowerCase() === 'cancelled' ? [
                { title: 'Đã hủy', status: 'error' }
              ] : [
                { title: 'Chờ xử lý' },
                { title: 'Đang xử lý' },
                { title: 'Đang giao' },
                { title: 'Hoàn thành' }
              ]}
            />

            <Divider style={{ margin: '16px 0' }}>Sản phẩm</Divider>

            <Table
              title={() => <strong>Sản phẩm đặt mua</strong>}
              dataSource={selectedOrder.items}
              pagination={false}
              rowKey={(r) => r.productId || r.id}
              columns={[
                {
                  title: 'Sản phẩm',
                  dataIndex: 'name',
                  key: 'name',
                  render: (text, rec) => (
                    <span
                      style={{ color:'#1677ff', cursor:'pointer' }}
                      onClick={() => rec.productId && navigate(`/products/${rec.productId}`)}
                    >
                      {text}
                    </span>
                  )
                },
                {
                  title: 'SL',
                  dataIndex: 'quantity',
                  key: 'quantity',
                  width: 70
                },
                {
                  title: 'Đơn giá',
                  dataIndex: 'price',
                  key: 'price',
                  render: (price) => `${price.toLocaleString()}₫`,
                },
                {
                  title: 'Thành tiền',
                  dataIndex: 'subtotal',
                  key: 'subtotal',
                  render: (subtotal) => `${subtotal.toLocaleString()}₫`,
                },
              ]}
              summary={() => (
                <Table.Summary>
                  <Table.Summary.Row>
                    <Table.Summary.Cell colSpan={3}>
                      <strong>Tổng tiền</strong>
                    </Table.Summary.Cell>
                    <Table.Summary.Cell>
                      <strong>{selectedOrder.total.toLocaleString()}₫</strong>
                    </Table.Summary.Cell>
                  </Table.Summary.Row>
                </Table.Summary>
              )}
            />
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default OrderManager;
