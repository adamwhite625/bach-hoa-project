import React, { useEffect, useMemo, useState } from 'react';
import {
  Card,
  Table,
  Button,
  Space,
  Typography,
  Tag,
  Modal,
  Form,
  Input,
  InputNumber,
  Switch,
  DatePicker,
  Select,
  message,
  Tooltip,
  Popconfirm
} from 'antd';
import { Segmented } from 'antd';
import {
  GiftOutlined,
  PlusOutlined,
  ReloadOutlined,
  EditOutlined,
  DeleteOutlined
} from '@ant-design/icons';
import dayjs from 'dayjs';
import DiscountService from '../../services/api/discounts';

const { Title, Text } = Typography;
const { RangePicker } = DatePicker;
const { Option } = Select;

const friendlyDate = (value) => {
  if (!value) return 'Không xác định';
  const d = dayjs(value);
  return d.isValid() ? d.format('DD/MM/YYYY HH:mm') : 'Không xác định';
};

const discountTypeLabel = {
  percent: 'Theo phần trăm',
  fixed: 'Theo số tiền'
};

const determineStatus = (discount) => {
  if (!discount.isActive) return 'inactive';
  const now = dayjs();
  if (discount.startAt && dayjs(discount.startAt).isAfter(now)) return 'upcoming';
  if (discount.endAt && dayjs(discount.endAt).isBefore(now)) return 'expired';
  return 'active';
};

const statusMeta = {
  active: { color: 'green', text: 'Đang áp dụng' },
  upcoming: { color: 'blue', text: 'Sắp diễn ra' },
  expired: { color: 'volcano', text: 'Hết hạn' },
  inactive: { color: 'default', text: 'Tạm tắt' }
};

const DiscountManager = () => {
  const [form] = Form.useForm();
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [modalOpen, setModalOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [submitting, setSubmitting] = useState(false);
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState('all');
  const typeWatch = Form.useWatch('type', form);

  const fetchDiscounts = async (showToast = false) => {
    setLoading(true);
    const res = await DiscountService.list();
    if (res.EC === 0) {
      setList(res.DT);
      if (showToast) message.success('Đã tải lại mã giảm giá');
    } else {
      message.error(res.EM || 'Không thể tải mã giảm giá');
      setList([]);
    }
    setLoading(false);
  };

  useEffect(() => {
    fetchDiscounts();
  }, []);

  const stats = useMemo(() => {
    const total = list.length;
    const active = list.filter((item) => determineStatus(item) === 'active').length;
    const upcoming = list.filter((item) => determineStatus(item) === 'upcoming').length;
    const expired = list.filter((item) => determineStatus(item) === 'expired').length;
    return { total, active, upcoming, expired };
  }, [list]);

  const filteredList = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    return list.filter((item) => {
      const status = determineStatus(item);
      const matchesStatus = statusFilter === 'all' || status === statusFilter;
      const matchesSearch = !keyword
        || item.code?.toLowerCase().includes(keyword)
        || item.description?.toLowerCase().includes(keyword);
      return matchesStatus && matchesSearch;
    });
  }, [list, search, statusFilter]);

  const openCreateModal = () => {
    setEditing(null);
    form.resetFields();
    setModalOpen(true);
  };

  const openEditModal = (record) => {
    setEditing(record);
    form.setFieldsValue({
      code: record.code,
      description: record.description,
      type: record.type,
      value: record.value,
      minOrder: record.minOrder,
      maxDiscount: record.maxDiscount,
      usageLimit: record.usageLimit,
      isActive: record.isActive,
      validRange: [record.startAt ? dayjs(record.startAt) : null, record.endAt ? dayjs(record.endAt) : null]
    });
    setModalOpen(true);
  };

  const handleDelete = async (record) => {
    const id = record?._id;
    if (!id) {
      message.error('Không tìm thấy ID mã giảm giá');
      return;
    }
    const res = await DiscountService.remove(id);
    if (res.EC === 0) {
      message.success('Đã xóa mã giảm giá');
      fetchDiscounts();
    } else {
      message.error(res.EM || 'Xóa mã giảm giá thất bại');
    }
  };

  const handleSubmit = async (values) => {
    const payload = {
      code: values.code?.trim(),
      description: values.description?.trim(),
      type: values.type,
      value: Number(values.value) || 0,
      minOrder: values.minOrder != null ? Number(values.minOrder) : undefined,
      maxDiscount: values.maxDiscount != null ? Number(values.maxDiscount) : undefined,
      usageLimit: values.usageLimit != null ? Number(values.usageLimit) : undefined,
      isActive: values.isActive,
      startAt: values.validRange?.[0] ? values.validRange[0].toISOString() : undefined,
      endAt: values.validRange?.[1] ? values.validRange[1].toISOString() : undefined
    };

    setSubmitting(true);
    const res = editing
      ? await DiscountService.update(editing._id, payload)
      : await DiscountService.create(payload);
    setSubmitting(false);

    if (res.EC === 0) {
      message.success(editing ? 'Cập nhật mã giảm giá thành công' : 'Tạo mã giảm giá thành công');
      setModalOpen(false);
      setEditing(null);
      form.resetFields();
      fetchDiscounts();
    } else {
      message.error(res.EM || (editing ? 'Cập nhật mã giảm giá thất bại' : 'Tạo mã giảm giá thất bại'));
    }
  };

  const columns = [
    {
      title: 'Mã',
      dataIndex: 'code',
      key: 'code',
      render: (value) => <Tag color="blue" style={{ fontSize: 13, padding: '2px 10px' }}>{value}</Tag>
    },
    {
      title: 'Mô tả',
      dataIndex: 'description',
      key: 'description',
      ellipsis: true,
      render: (value) => value || <Text type="secondary">Chưa có mô tả</Text>
    },
    {
      title: 'Loại',
      dataIndex: 'type',
      key: 'type',
      render: (type) => discountTypeLabel[type] || type
    },
    {
      title: 'Giá trị',
      dataIndex: 'value',
      key: 'value',
      render: (value, record) => (
        record.type === 'percent'
          ? `${value}%`
          : `${Number(value).toLocaleString('vi-VN')}₫`
      )
    },
    {
      title: 'Giới hạn sử dụng',
      key: 'usage',
      render: (_, record) => {
        if (!record.usageLimit) return `${record.usedCount || 0} lần`;
        return `${record.usedCount || 0}/${record.usageLimit}`;
      }
    },
    {
      title: 'Thời gian hiệu lực',
      key: 'valid',
      render: (_, record) => {
        if (!record.startAt && !record.endAt) return <Text type="secondary">Không giới hạn</Text>;
        return (
          <Space direction="vertical" size={0}>
            {record.startAt ? <span>Từ: {friendlyDate(record.startAt)}</span> : null}
            {record.endAt ? <span>Đến: {friendlyDate(record.endAt)}</span> : null}
          </Space>
        );
      }
    },
    {
      title: 'Trạng thái',
      key: 'status',
      render: (_, record) => {
        const status = determineStatus(record);
        const meta = statusMeta[status] || statusMeta.active;
        return <Tag color={meta.color}>{meta.text}</Tag>;
      }
    },
    {
      title: 'Thao tác',
      key: 'actions',
      width: 170,
      render: (_, record) => (
        <Space size={8}>
          <Button icon={<EditOutlined />} onClick={() => openEditModal(record)}>
            Sửa
          </Button>
          <Popconfirm
            title="Xóa mã giảm giá này?"
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
          background: 'linear-gradient(135deg, #fff0f6 0%, #ffffff 100%)'
        }}
      >
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          <Space size={12} align="center" wrap>
            <Title level={3} style={{ margin: 0, color: '#c41d7f' }}>Quản lý mã giảm giá</Title>
            <Tag color="magenta" style={{ borderRadius: 16 }}>
              Tổng cộng {stats.total} mã
            </Tag>
          </Space>
          <Text type="secondary">
            Theo dõi, tạo mới và điều chỉnh mã giảm giá để tối ưu chương trình khuyến mãi.
          </Text>
        </Space>
      </Card>

      <Space wrap size={16}>
        <Card bordered={false} style={{ borderRadius: 16, minWidth: 200 }}>
          <Space direction="vertical" size={0}>
            <Text type="secondary">Đang áp dụng</Text>
            <Title level={4} style={{ margin: 0 }}>{stats.active}</Title>
          </Space>
        </Card>
        <Card bordered={false} style={{ borderRadius: 16, minWidth: 200 }}>
          <Space direction="vertical" size={0}>
            <Text type="secondary">Sắp diễn ra</Text>
            <Title level={4} style={{ margin: 0 }}>{stats.upcoming}</Title>
          </Space>
        </Card>
        <Card bordered={false} style={{ borderRadius: 16, minWidth: 200 }}>
          <Space direction="vertical" size={0}>
            <Text type="secondary">Đã hết hạn</Text>
            <Title level={4} style={{ margin: 0 }}>{stats.expired}</Title>
          </Space>
        </Card>
      </Space>

      <Card bordered={false} style={{ borderRadius: 16 }}>
        <Space direction="vertical" size={16} style={{ width: '100%' }}>
          <Space style={{ width: '100%', justifyContent: 'space-between', flexWrap: 'wrap' }}>
            <Space size={12} wrap>
              <Input
                allowClear
                placeholder="Tìm kiếm mã hoặc mô tả"
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                style={{ width: 240 }}
              />
              <Segmented
                options={[
                  { label: 'Tất cả', value: 'all' },
                  { label: 'Đang áp dụng', value: 'active' },
                  { label: 'Sắp diễn ra', value: 'upcoming' },
                  { label: 'Đã hết hạn', value: 'expired' },
                  { label: 'Tạm tắt', value: 'inactive' }
                ]}
                value={statusFilter}
                onChange={setStatusFilter}
              />
              <Tooltip title="Tải lại danh sách">
                <Button icon={<ReloadOutlined />} onClick={() => fetchDiscounts(true)} loading={loading}>
                  Tải lại
                </Button>
              </Tooltip>
            </Space>
            <Button type="primary" icon={<PlusOutlined />} onClick={openCreateModal}>
              Thêm mã giảm giá
            </Button>
          </Space>

          <Table
            rowKey={(record) => record._id}
            loading={loading}
            columns={columns}
            dataSource={filteredList}
            pagination={{ pageSize: 10, showTotal: (total) => `Tổng ${total} mã giảm giá` }}
          />
        </Space>
      </Card>

      <Modal
        title={editing ? 'Sửa mã giảm giá' : 'Thêm mã giảm giá'}
        open={modalOpen}
        onOk={() => form.submit()}
        onCancel={() => {
          setModalOpen(false);
          setEditing(null);
          form.resetFields();
        }}
        okText={editing ? 'Cập nhật' : 'Tạo mới'}
        cancelText="Hủy"
        confirmLoading={submitting}
        destroyOnClose
        centered
      >
        <Form layout="vertical" form={form} onFinish={handleSubmit} initialValues={{ type: 'percent', isActive: true }}>
          <Form.Item
            name="code"
            label="Mã giảm giá"
            rules={[{ required: true, message: 'Nhập mã giảm giá' }]}
          >
            <Input prefix={<GiftOutlined />} placeholder="Ví dụ: WELCOME10" />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <Input.TextArea rows={3} placeholder="Thông tin mô tả chương trình" />
          </Form.Item>
          <Form.Item name="type" label="Loại giảm giá" rules={[{ required: true }]}>
            <Segmented
              block
              options={[
                { label: 'Theo %', value: 'percent' },
                { label: 'Số tiền', value: 'fixed' }
              ]}
            />
          </Form.Item>
          <Form.Item
            name="value"
            label="Giá trị"
            rules={[{ required: true, message: 'Nhập giá trị' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              formatter={(val) => {
                if (!val && val !== 0) return '';
                const base = `${val}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',');
                return typeWatch === 'percent' ? `${base}%` : `${base} ₫`;
              }}
              parser={(val) => (val || '').replace(/[^0-9.-]/g, '')}
            />
          </Form.Item>
          <Form.Item name="minOrder" label="Giá trị đơn tối thiểu">
            <InputNumber style={{ width: '100%' }} min={0}
              formatter={(val) => (val || val === 0) ? `${(`${val}`).replace(/\B(?=(\d{3})+(?!\d))/g, ',')} ₫` : ''}
              parser={(val) => (val || '').replace(/[^0-9.-]/g, '')}
            />
          </Form.Item>
          <Form.Item name="maxDiscount" label="Giảm tối đa">
            <InputNumber style={{ width: '100%' }} min={0}
              formatter={(val) => (val || val === 0) ? `${(`${val}`).replace(/\B(?=(\d{3})+(?!\d))/g, ',')} ₫` : ''}
              parser={(val) => (val || '').replace(/[^0-9.-]/g, '')}
            />
          </Form.Item>
          <Form.Item name="usageLimit" label="Giới hạn sử dụng">
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>
          <Form.Item name="validRange" label="Thời gian hiệu lực">
            <RangePicker
              style={{ width: '100%' }}
              showTime
              format="DD/MM/YYYY HH:mm"
              presets={[
                { label: 'Hôm nay', value: [dayjs().startOf('day'), dayjs().endOf('day')] },
                { label: '7 ngày', value: [dayjs().startOf('day'), dayjs().add(7, 'day').endOf('day')] },
                { label: '30 ngày', value: [dayjs().startOf('day'), dayjs().add(30, 'day').endOf('day')] },
              ]}
            />
          </Form.Item>
          <Form.Item name="isActive" label="Kích hoạt" valuePropName="checked">
            <Switch checkedChildren="Bật" unCheckedChildren="Tắt" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default DiscountManager;
