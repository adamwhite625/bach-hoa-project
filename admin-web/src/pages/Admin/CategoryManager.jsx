import React, { useEffect, useMemo, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Modal,
  Form,
  Input,
  Image,
  Space,
  Typography,
  message,
  Row,
  Col,
  Tag,
  Statistic,
  Tooltip,
  Popconfirm
} from 'antd';
import {
  PlusOutlined,
  ReloadOutlined,
  AppstoreOutlined,
  CheckOutlined,
  CalendarOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons';
import { CategoryService } from '../../services/api/categories';

const { Title, Text } = Typography;
const { TextArea } = Input;

const friendlyDate = (value) => {
  if (!value) return 'Không xác định';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'Không xác định';
  return date.toLocaleDateString('vi-VN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

const CategoryManager = () => {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();
  const [search, setSearch] = useState('');
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const isEditing = Boolean(editing);

  const fetch = async (showNotify = false) => {
    setLoading(true);
    const res = await CategoryService.list();
    if (res.EC === 0) {
      setList(res.DT || []);
      if (showNotify) message.success('Đã tải lại danh mục');
    } else {
      message.error(res.EM || 'Không thể tải danh mục');
      setList([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetch();
  }, []);

  const handleAdd = () => {
    setEditing(null);
    form.resetFields();
    setOpen(true);
  };

  const handleEdit = (record) => {
    setEditing(record);
    form.setFieldsValue({
      name: record.name || '',
      description: record.description || '',
      image: record.image || ''
    });
    setOpen(true);
  };

  const handleDelete = async (record) => {
    const id = record?._id || record?.id;
    if (!id) {
      message.error('Không tìm thấy ID danh mục');
      return;
    }
    try {
      const res = await CategoryService.remove(id);
      if (res.EC === 0) {
        message.success('Xóa danh mục thành công');
        await fetch();
      } else {
        message.error(res.EM || 'Xóa danh mục thất bại');
      }
    } catch {
      message.error('Lỗi xóa danh mục');
    }
  };

  const handleSubmit = async (values) => {
    setSubmitting(true);
    try {
      let res;
      if (isEditing) {
        const id = editing?._id || editing?.id;
        if (!id) {
          message.error('Không tìm thấy ID danh mục');
          return;
        }
        res = await CategoryService.update(id, values);
      } else {
        res = await CategoryService.create(values);
      }

      if (res.EC === 0) {
        message.success(isEditing ? 'Cập nhật danh mục thành công' : 'Tạo danh mục thành công');
        setOpen(false);
        setEditing(null);
        form.resetFields();
        await fetch();
      } else {
        message.error(res.EM || (isEditing ? 'Cập nhật danh mục thất bại' : 'Tạo danh mục thất bại'));
      }
    } catch {
      message.error(isEditing ? 'Lỗi cập nhật danh mục' : 'Lỗi tạo danh mục');
    } finally {
      setSubmitting(false);
    }
  };

  const handleCancel = () => {
    setOpen(false);
    setEditing(null);
    form.resetFields();
  };

  const filteredList = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return list;
    return list.filter((item) => (
      item.name?.toLowerCase().includes(keyword) ||
      item.description?.toLowerCase().includes(keyword)
    ));
  }, [list, search]);

  const statistics = useMemo(() => {
    const total = list.length;
    const withImage = list.filter((item) => Boolean(item.image)).length;
    const withoutImage = total - withImage;
    const recent = list
      .filter((item) => {
        if (!item.createdAt) return false;
        const diffDays = (Date.now() - new Date(item.createdAt).getTime()) / (1000 * 60 * 60 * 24);
        return diffDays <= 30;
      }).length;
    return { total, withImage, withoutImage, recent };
  }, [list]);

  const columns = [
    {
      title: 'Ảnh',
      dataIndex: 'image',
      key: 'image',
      width: 90,
      render: (img, record) => (
        img ? (
          <Image
            src={img}
            width={56}
            height={56}
            style={{ objectFit: 'cover', borderRadius: 10 }}
            preview={false}
            alt={record.name}
          />
        ) : (
          <Tag color="default">No image</Tag>
        )
      )
    },
    {
      title: 'Tên danh mục',
      dataIndex: 'name',
      key: 'name',
      render: (value) => <Text strong>{value}</Text>
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      responsive: ['md'],
      render: (value) => value || <Text type="secondary">Chưa có mô tả</Text>
    },
    {
      title: 'Ngày tạo',
      dataIndex: 'createdAt',
      key: 'createdAt',
      responsive: ['lg'],
      render: (value) => (
        <Space size={6}>
          <CalendarOutlined />
          <span>{friendlyDate(value)}</span>
        </Space>
      )
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 180,
      render: (_, record) => (
        <Space size={8} wrap>
          <Button icon={<EditOutlined />} onClick={() => handleEdit(record)}>
            Sửa
          </Button>
          <Popconfirm
            title="Xóa danh mục này?"
            okText="Xóa"
            cancelText="Hủy"
            placement="left"
            onConfirm={() => handleDelete(record)}
          >
            <Button danger icon={<DeleteOutlined />}>
              Xóa
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ];

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 24 }}>
      <Card
        bordered={false}
        style={{
          borderRadius: 16,
          background: 'linear-gradient(135deg, #fff8f0 0%, #ffffff 100%)'
        }}
      >
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          <Space size={12} align="center" wrap>
            <Title level={3} style={{ margin: 0, color: '#d4380d' }}>Quản lý danh mục</Title>
            <Tag color="orange" style={{ borderRadius: 16 }}>
              Tổng cộng {statistics.total} danh mục
            </Tag>
          </Space>
          <Text type="secondary">
            Quản lý danh mục sản phẩm, giúp khách hàng dễ dàng tìm kiếm và trải nghiệm mua sắm.
          </Text>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        <Col xs={24} sm={12} md={6}>
          <Card bordered={false} style={{ borderRadius: 16 }}>
            <Statistic
              title="Tổng danh mục"
              value={statistics.total}
              prefix={<AppstoreOutlined style={{ color: '#d4380d' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card bordered={false} style={{ borderRadius: 16 }}>
            <Statistic
              title="Có hình ảnh"
              value={statistics.withImage}
              prefix={<CheckOutlined style={{ color: '#52c41a' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card bordered={false} style={{ borderRadius: 16 }}>
            <Statistic
              title="Chưa có ảnh"
              value={statistics.withoutImage}
              prefix={<AppstoreOutlined style={{ color: '#faad14' }} />}
            />
          </Card>
        </Col>
        <Col xs={24} sm={12} md={6}>
          <Card bordered={false} style={{ borderRadius: 16 }}>
            <Statistic
              title="Tạo trong 30 ngày"
              value={statistics.recent}
              prefix={<CalendarOutlined style={{ color: '#1677ff' }} />}
            />
          </Card>
        </Col>
      </Row>

      <Card bordered={false} style={{ borderRadius: 16 }}>
        <Space
          direction="vertical"
          style={{ width: '100%' }}
          size={16}
        >
          <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
            <Space size={12} wrap>
              <Input
                allowClear
                placeholder="Tìm kiếm theo tên hoặc mô tả"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                style={{ width: 260 }}
              />
              <Tooltip title="Tải lại danh sách">
                <Button icon={<ReloadOutlined />} onClick={() => fetch(true)} loading={loading}>
                  Tải lại
                </Button>
              </Tooltip>
            </Space>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              onClick={handleAdd}
            >
              Thêm danh mục
            </Button>
          </Space>

          <Table
            rowKey={(record) => record._id || record.id}
            loading={loading}
            dataSource={filteredList}
            columns={columns}
            pagination={{ pageSize: 10, showTotal: (total) => `Tổng ${total} danh mục` }}
          />
        </Space>
      </Card>

      <Modal
        title={isEditing ? 'Sửa danh mục' : 'Thêm danh mục'}
        open={open}
        okText={isEditing ? 'Cập nhật' : 'Tạo mới'}
        cancelText="Hủy"
        confirmLoading={submitting}
        onOk={() => form.submit()}
        onCancel={handleCancel}
        afterClose={() => form.resetFields()}
        centered
        destroyOnClose
      >
        <Form layout="vertical" form={form} onFinish={handleSubmit}>
          <Form.Item
            name="name"
            label="Tên danh mục"
            rules={[{ required: true, message: 'Nhập tên danh mục' }]}
          >
            <Input placeholder="Ví dụ: Đồ uống, Gia vị..." />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <TextArea rows={3} placeholder="Mô tả ngắn gọn về danh mục" />
          </Form.Item>
          <Form.Item name="image" label="Ảnh (URL)">
            <Input placeholder="https://...jpg" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default CategoryManager;
