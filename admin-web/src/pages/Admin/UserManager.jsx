import React, { useCallback, useEffect, useMemo, useRef, useState } from 'react';
import {
  Card,
  Table,
  Input,
  Select,
  Space,
  Button,
  Tag,
  Typography,
  Drawer,
  Descriptions,
  Avatar,
  message
} from 'antd';
import {
  SearchOutlined,
  ReloadOutlined,
  UserOutlined,
  MailOutlined,
  PhoneOutlined,
  CalendarOutlined
} from '@ant-design/icons';
import UserService from '../../services/api/users';

const { Title, Text } = Typography;
const { Option } = Select;

const STATUS_COLORS = {
  active: 'green',
  inactive: 'volcano',
  pending: 'gold',
  banned: 'red',
  blocked: 'red'
};

const ROLE_LABELS = {
  admin: 'Quản trị',
  manager: 'Quản lý',
  staff: 'Nhân viên',
  user: 'Khách hàng',
  customer: 'Khách hàng'
};

const CUSTOMER_TIER_LABELS = {
  new: 'Khách mới',
  regular: 'Thường',
  vip: 'VIP'
};

const CUSTOMER_TIER_COLORS = {
  new: 'cyan',
  regular: 'default',
  vip: 'gold'
};

const STATUS_LABELS = {
  active: 'Hoạt động',
  inactive: 'Ngừng hoạt động',
  pending: 'Chờ duyệt',
  banned: 'Đã khóa',
  blocked: 'Đã khóa'
};

const friendlyDate = (value) => {
  if (!value) return 'Không xác định';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Không xác định';
  return date.toLocaleString('vi-VN', {
    day: '2-digit',
    month: '2-digit',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const normalizeUser = (user = {}) => {
  const id = user._id || user.id || user.userId || user.email || user.username || Math.random().toString(36).slice(2);
  const firstName = user.firstName || user.given_name || '';
  const lastName = user.lastName || user.family_name || '';
  const composedName = `${firstName} ${lastName}`.trim();
  const name = (user.fullName || user.name || composedName || user.username || user.email || 'Người dùng').trim();
  const email = user.email || user.contactEmail || user.contact?.email || '';
  const phone = user.phone || user.phoneNumber || user.contact?.phone || '';
  const role = (user.role || user.type || 'user').toLowerCase();
  let status = (user.status || '').toLowerCase();
  if (!status) status = user.isActive === false ? 'inactive' : 'active';
  const customerTier = user.customerTier || 'new';
  const createdAt = user.createdAt || user.created_at || user.joinedAt || user.date || user.createdOn || null;
  const lastLogin = user.lastLogin || user.lastLoginAt || user.lastActiveAt || null;
  
  // Handle shippingAddress which can be object or string
  let address = '';
  if (typeof user.shippingAddress === 'string') {
    address = user.shippingAddress;
  } else if (user.shippingAddress?.address) {
    const parts = [
      user.shippingAddress.address,
      user.shippingAddress.city
    ].filter(Boolean);
    address = parts.join(', ');
  } else {
    address = user.address || user.location || user.profile?.address || '';
  }

  return {
    key: id,
    id,
    name,
    email,
    phone,
    role,
    customerTier,
    status,
    createdAt,
    lastLogin,
    address,
    avatar: user.avatar || user.photo || user.picture || '',
    raw: user
  };
};

const UserManager = () => {
  const [users, setUsers] = useState([]);
  const [loading, setLoading] = useState(true);
  const [filters, setFilters] = useState({ search: '', role: 'all', status: 'all' });
  const [selectedUser, setSelectedUser] = useState(null);
  const [drawerVisible, setDrawerVisible] = useState(false);
  const isMounted = useRef(false);

  const fetchUsers = useCallback(async (withFeedback = false) => {
    if (!isMounted.current) return;
    setLoading(true);
    try {
      const res = await UserService.list();
      if (!isMounted.current) return;
      if (res.EC === 0) {
        const normalized = Array.isArray(res.DT) ? res.DT.map(normalizeUser) : [];
        setUsers(normalized);
        if (withFeedback) message.success('Đã tải lại danh sách người dùng');
      } else {
        setUsers([]);
        message.error(res.EM || 'Không thể tải người dùng');
      }
    } catch (error) {
      if (!isMounted.current) return;
      setUsers([]);
      message.error('Không thể tải người dùng');
    } finally {
      if (isMounted.current) setLoading(false);
    }
  }, []);

  useEffect(() => {
    isMounted.current = true;
    fetchUsers();
    return () => {
      isMounted.current = false;
    };
  }, [fetchUsers]);

  const roleOptions = useMemo(() => {
    const set = new Set();
    users.forEach((user) => {
      if (user.role) set.add(user.role);
    });
    return Array.from(set);
  }, [users]);

  const statusOptions = useMemo(() => {
    const set = new Set();
    users.forEach((user) => {
      if (user.status) set.add(user.status);
    });
    return Array.from(set);
  }, [users]);

  const filteredUsers = useMemo(() => {
    const search = filters.search.trim().toLowerCase();
    return users.filter((user) => {
      const matchesSearch = !search || [user.name, user.email, user.phone]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(search));
      const matchesRole = filters.role === 'all' || user.role === filters.role;
      const matchesStatus = filters.status === 'all' || user.status === filters.status;
      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, filters]);

  const hasActiveFilters = useMemo(() => (
    Boolean(filters.search) || filters.role !== 'all' || filters.status !== 'all'
  ), [filters]);

  const columns = useMemo(() => [
    {
      title: 'Họ tên',
      dataIndex: 'name',
      key: 'name',
      render: (_, record) => (
        <Space align="start">
          <Avatar size="large" icon={<UserOutlined />} src={record.avatar || undefined} />
          <Space direction="vertical" size={0}>
            <Text strong>{record.name}</Text>
            <Text type="secondary">{record.email || 'Chưa cập nhật email'}</Text>
          </Space>
        </Space>
      ),
      sorter: (a, b) => a.name.localeCompare(b.name)
    },
    {
      title: 'Số điện thoại',
      dataIndex: 'phone',
      key: 'phone',
      render: (value) => value || 'Chưa cập nhật'
    },
    {
      title: 'Vai trò',
      dataIndex: 'role',
      key: 'role',
      render: (role) => (
        <Tag color="blue">{ROLE_LABELS[role] || role || 'Không rõ'}</Tag>
      ),
      filters: roleOptions.map((role) => ({ text: ROLE_LABELS[role] || role, value: role })),
      onFilter: (value, record) => record.role === value
    },
    {
      title: 'Cấp độ',
      dataIndex: 'customerTier',
      key: 'customerTier',
      render: (tier) => (
        <Tag color={CUSTOMER_TIER_COLORS[tier] || 'default'}>
          {CUSTOMER_TIER_LABELS[tier] || tier || 'Không rõ'}
        </Tag>
      )
    },
    {
      title: 'Trạng thái',
      dataIndex: 'status',
      key: 'status',
      render: (status) => (
        <Tag color={STATUS_COLORS[status] || 'default'}>{STATUS_LABELS[status] || status || 'Không rõ'}</Tag>
      ),
      filters: statusOptions.map((status) => ({ text: STATUS_LABELS[status] || status, value: status })),
      onFilter: (value, record) => record.status === value
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      render: (value) => friendlyDate(value),
      sorter: (a, b) => {
        const left = new Date(a.createdAt || 0).getTime();
        const right = new Date(b.createdAt || 0).getTime();
        return left - right;
      }
    },
    {
      title: 'Thao tác',
      key: 'actions',
      render: (_, record) => (
        <Button type="link" onClick={() => {
          setSelectedUser(record);
          setDrawerVisible(true);
        }}>
          Chi tiết
        </Button>
      )
    }
  ], [roleOptions, statusOptions]);

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <Space direction="vertical" style={{ width: '100%' }} size="large">
          <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
            <Title level={2} style={{ margin: 0 }}>Quản lý người dùng</Title>
            <Button icon={<ReloadOutlined />} onClick={() => fetchUsers(true)} loading={loading}>
              Tải lại
            </Button>
          </Space>

          <Space wrap>
            <Input
              allowClear
              style={{ width: 260 }}
              placeholder="Tìm theo tên, email hoặc số điện thoại"
              prefix={<SearchOutlined />}
              value={filters.search}
              onChange={(event) => setFilters((prev) => ({ ...prev, search: event.target.value }))}
            />
            <Select
              style={{ width: 180 }}
              value={filters.role}
              onChange={(value) => setFilters((prev) => ({ ...prev, role: value }))}
            >
              <Option value="all">Tất cả vai trò</Option>
              {roleOptions.map((role) => (
                <Option key={role} value={role}>{ROLE_LABELS[role] || role}</Option>
              ))}
            </Select>
            <Select
              style={{ width: 180 }}
              value={filters.status}
              onChange={(value) => setFilters((prev) => ({ ...prev, status: value }))}
            >
              <Option value="all">Tất cả trạng thái</Option>
              {statusOptions.map((status) => (
                <Option key={status} value={status}>{STATUS_LABELS[status] || status}</Option>
              ))}
            </Select>
            <Button
              type="text"
              disabled={!hasActiveFilters}
              onClick={() => setFilters({ search: '', role: 'all', status: 'all' })}
            >
              Xóa bộ lọc
            </Button>
          </Space>

          <Table
            columns={columns}
            dataSource={filteredUsers}
            loading={loading}
            rowKey="key"
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} người dùng`
            }}
            onRow={(record) => ({
              onClick: () => {
                setSelectedUser(record);
                setDrawerVisible(true);
              },
              style: { cursor: 'pointer' }
            })}
          />
        </Space>
      </Card>

      <Drawer
        title="Thông tin người dùng"
        placement="right"
        width={420}
        onClose={() => {
          setDrawerVisible(false);
          setSelectedUser(null);
        }}
        visible={drawerVisible}
      >
        {selectedUser && (
          <Space direction="vertical" size="middle" style={{ width: '100%' }}>
            <Space align="center" size={16}>
              <Avatar size={64} icon={<UserOutlined />} src={selectedUser.avatar || undefined} />
              <Space direction="vertical" size={0}>
                <Text strong style={{ fontSize: 18 }}>{selectedUser.name}</Text>
                <Tag color={STATUS_COLORS[selectedUser.status] || 'default'}>
                  {STATUS_LABELS[selectedUser.status] || selectedUser.status || 'Không rõ'}
                </Tag>
              </Space>
            </Space>

            <Descriptions column={1} size="small" bordered>
              <Descriptions.Item label="Email">
                <Space size={8}>
                  <MailOutlined />
                  <span>{selectedUser.email || 'Chưa cập nhật'}</span>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Số điện thoại">
                <Space size={8}>
                  <PhoneOutlined />
                  <span>{selectedUser.phone || 'Chưa cập nhật'}</span>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Vai trò">
                {ROLE_LABELS[selectedUser.role] || selectedUser.role || 'Không rõ'}
              </Descriptions.Item>
              <Descriptions.Item label="Cấp độ khách hàng">
                <Tag color={CUSTOMER_TIER_COLORS[selectedUser.customerTier] || 'default'}>
                  {CUSTOMER_TIER_LABELS[selectedUser.customerTier] || selectedUser.customerTier || 'Không rõ'}
                </Tag>
              </Descriptions.Item>
              <Descriptions.Item label="Ngày tạo">
                <Space size={8}>
                  <CalendarOutlined />
                  <span>{friendlyDate(selectedUser.createdAt)}</span>
                </Space>
              </Descriptions.Item>
              <Descriptions.Item label="Hoạt động gần nhất">
                {selectedUser.lastLogin ? friendlyDate(selectedUser.lastLogin) : 'Chưa có dữ liệu'}
              </Descriptions.Item>
              <Descriptions.Item label="Địa chỉ">
                {selectedUser.address || 'Chưa cập nhật'}
              </Descriptions.Item>
            </Descriptions>
          </Space>
        )}
      </Drawer>
    </div>
  );
};

export default UserManager;
