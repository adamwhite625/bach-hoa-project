import React, { useState, useEffect } from 'react';
import {
  Table,
  Card,
  Space,
  Button,
  Tag,
  Select,
  Input,
  DatePicker,
  Modal,
  Typography,
  message,
  Descriptions,
  Badge,
  Drawer
} from 'antd';
import {
  SearchOutlined,
  EditOutlined,
  EyeOutlined,
  DeleteOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import { OrderService } from '../../services/api/orders';
import { useNavigate } from 'react-router-dom';

const { Title } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

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

  useEffect(() => {
    fetchOrders();
  }, []);

  const fetchOrders = async () => {
    setLoading(true);
    const res = await OrderService.list();
    if (res.EC === 0) {
      setOrders(res.DT);
    } else {
      message.error(res.EM || 'Không thể tải đơn hàng');
      setOrders([]);
    }
    setLoading(false);
  };

  const handleStatusChange = async (orderId, newStatus) => {
    const res = await OrderService.updateStatus(orderId, newStatus);
    if (res.EC === 0) {
      setOrders(prev => prev.map(o => o.id === orderId ? { ...o, status: newStatus } : o));
      message.success('Cập nhật trạng thái thành công');
    } else {
      message.error(res.EM || 'Không thể cập nhật trạng thái đơn hàng');
    }
  };

  const handleDeleteOrder = (orderId) => {
    Modal.confirm({
      title: 'Xác nhận xóa đơn hàng',
      content: 'Bạn có chắc chắn muốn xóa đơn hàng này không?',
      okText: 'Xóa',
      cancelText: 'Hủy',
      okButtonProps: { danger: true },
      onOk: async () => {
        try {
          const res = await OrderService.remove(orderId);
          if (res.EC === 0) {
            setOrders(prev => prev.filter(order => order.id !== orderId));
            message.success('Xóa đơn hàng thành công');
          } else message.error(res.EM || 'Không thể xóa đơn hàng');
        } catch (error) {
          message.error('Không thể xóa đơn hàng');
        }
      }
    });
  };

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
          style={{ width: 140 }}
        >
          <Option value="pending">Chờ xử lý</Option>
          <Option value="processing">Đang xử lý</Option>
          <Option value="shipping">Đang giao</Option>
          <Option value="completed">Hoàn thành</Option>
          <Option value="cancelled">Đã hủy</Option>
        </Select>
      ),
      filters: [
        { text: 'Chờ xử lý', value: 'pending' },
        { text: 'Đang xử lý', value: 'processing' },
        { text: 'Đang giao', value: 'shipping' },
        { text: 'Hoàn thành', value: 'completed' },
        { text: 'Đã hủy', value: 'cancelled' },
      ],
      onFilter: (value, record) => record.status === value,
    },
    {
      title: 'Ngày đặt',
      dataIndex: 'createdAt',
      key: 'createdAt',
      sorter: (a, b) => dayjs(a.createdAt).unix() - dayjs(b.createdAt).unix(),
    },
    {
      title: 'Thao tác',
      key: 'action',
      render: (_, record) => (
        <Space>
          <Button
            icon={<EyeOutlined />}
            onClick={() => {
              setSelectedOrder(record);
              setDrawerVisible(true);
            }}
          >
            Chi tiết
          </Button>
          <Button
            icon={<DeleteOutlined />}
            danger
            onClick={() => handleDeleteOrder(record.id)}
          >
            Xóa
          </Button>
        </Space>
      ),
    },
  ];

  const getStatusBadge = (status) => {
    const statusMap = {
      pending: { status: 'warning', text: 'Chờ xử lý' },
      processing: { status: 'processing', text: 'Đang xử lý' },
      shipping: { status: 'processing', text: 'Đang giao' },
      completed: { status: 'success', text: 'Hoàn thành' },
      cancelled: { status: 'error', text: 'Đã hủy' }
    };
    return statusMap[status] || { status: 'default', text: status };
  };

  return (
    <div style={{ padding: '24px' }}>
      <Card>
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <Title level={2}>Quản lý đơn hàng</Title>

          {/* Filters */}
          <Space wrap>
            <Input
              placeholder="Tìm kiếm đơn hàng..."
              prefix={<SearchOutlined />}
              style={{ width: 250 }}
              value={filters.search}
              onChange={e => setFilters({ ...filters, search: e.target.value })}
            />
            <Select
              style={{ width: 150 }}
              placeholder="Trạng thái"
              value={filters.status}
              onChange={value => setFilters({ ...filters, status: value })}
            >
              <Option value="all">Tất cả</Option>
              <Option value="pending">Chờ xử lý</Option>
              <Option value="processing">Đang xử lý</Option>
              <Option value="shipping">Đang giao</Option>
              <Option value="completed">Hoàn thành</Option>
              <Option value="cancelled">Đã hủy</Option>
            </Select>
            <RangePicker
              value={filters.dateRange}
              onChange={dates => setFilters({ ...filters, dateRange: dates })}
            />
          </Space>

          {/* Orders Table */}
          <Table
            columns={columns}
            dataSource={orders}
            loading={loading}
            rowKey="id"
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `Tổng số ${total} đơn hàng`,
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
        onClose={() => setDrawerVisible(false)}
        visible={drawerVisible}
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
                {selectedOrder.shippingAddress}
              </Descriptions.Item>
              <Descriptions.Item label="Phương thức thanh toán">
                {selectedOrder.paymentMethod}
              </Descriptions.Item>
            </Descriptions>

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
