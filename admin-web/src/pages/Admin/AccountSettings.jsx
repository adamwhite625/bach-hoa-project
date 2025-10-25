import React, { useEffect, useState, useCallback } from 'react';
import {
  Card,
  Space,
  Typography,
  Form,
  Input,
  Button,
  Divider,
  message,
  Popconfirm,
  Skeleton
} from 'antd';
import { SettingOutlined, SaveOutlined, DeleteOutlined } from '@ant-design/icons';
import { useNavigate } from 'react-router-dom';
import UserService from '../../services/api/users';

const { Title, Text } = Typography;

const buildPayload = (values = {}) => {
  const {
    firstName,
    lastName,
    email,
    phone,
    newPassword,
    confirmPassword
  } = values;
  const payload = {
    firstName: firstName?.trim(),
    lastName: lastName?.trim(),
    email: email?.trim(),
    phone: phone?.trim()
  };
  if (newPassword) payload.password = newPassword;
  if (confirmPassword) delete payload.confirmPassword;
  return payload;
};

const AccountSettings = () => {
  const [form] = Form.useForm();
  const [profile, setProfile] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const navigate = useNavigate();

  const fetchProfile = useCallback(async () => {
    setLoading(true);
    const res = await UserService.profile();
    if (res.EC === 0 && res.DT) {
      setProfile(res.DT);
      form.setFieldsValue({
        firstName: res.DT.firstName,
        lastName: res.DT.lastName,
        email: res.DT.email,
        phone: res.DT.phone || ''
      });
    } else {
      message.error(res.EM || 'Không thể tải thông tin tài khoản');
    }
    setLoading(false);
  }, [form]);

  useEffect(() => {
    fetchProfile();
  }, [fetchProfile]);

  const handleSubmit = async (values) => {
    if (values.newPassword && values.newPassword !== values.confirmPassword) {
      message.error('Mật khẩu xác nhận không khớp');
      return;
    }
    setSaving(true);
    const payload = buildPayload(values);
    const res = await UserService.updateProfile(payload);
    setSaving(false);
    if (res.EC === 0 && res.DT) {
      message.success('Cập nhật tài khoản thành công');
      setProfile(res.DT);
      form.resetFields(['newPassword', 'confirmPassword']);
      const updatedLocal = {
        ...(JSON.parse(localStorage.getItem('user') || '{}')),
        email: res.DT.email,
        role: res.DT.role,
        firstName: res.DT.firstName,
        lastName: res.DT.lastName,
        phone: res.DT.phone
      };
      localStorage.setItem('user', JSON.stringify(updatedLocal));
    } else {
      message.error(res.EM || 'Cập nhật tài khoản thất bại');
    }
  };

  const handleDelete = async () => {
    if (!profile?._id) return;
    setDeleting(true);
    const res = await UserService.deleteById(profile._id);
    setDeleting(false);
    if (res.EC === 0) {
      message.success('Tài khoản đã được xóa');
      try {
        localStorage.removeItem('access_token');
        localStorage.removeItem('user');
      } catch (_) {
        // ignore storage errors
      }
      navigate('/admin/login', { replace: true });
    } else {
      message.error(res.EM || 'Không thể xóa tài khoản');
    }
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
        <Space direction="vertical" size={6} style={{ width: '100%' }}>
          <Space align="center" size={12}>
            <SettingOutlined style={{ fontSize: 20, color: '#1d39c4' }} />
            <Title level={3} style={{ margin: 0, color: '#1d39c4' }}>Cài đặt tài khoản</Title>
          </Space>
          <Text type="secondary">Quản lý thông tin đăng nhập của quản trị viên và cập nhật bảo mật.</Text>
        </Space>
      </Card>

      <Card bordered={false} style={{ borderRadius: 16 }}>
        {loading ? (
          <Skeleton active paragraph={{ rows: 6 }} />
        ) : (
          <Form
            layout="vertical"
            form={form}
            onFinish={handleSubmit}
            initialValues={{ firstName: '', lastName: '', email: '', phone: '' }}
          >
            <Title level={4}>Thông tin cơ bản</Title>
            <Space direction="horizontal" size={16} wrap>
              <Form.Item
                name="firstName"
                label="Tên"
                rules={[{ required: true, message: 'Vui lòng nhập tên' }]}
              >
                <Input placeholder="Tên" />
              </Form.Item>
              <Form.Item
                name="lastName"
                label="Họ"
                rules={[{ required: true, message: 'Vui lòng nhập họ' }]}
              >
                <Input placeholder="Họ" />
              </Form.Item>
            </Space>
            <Form.Item
              name="email"
              label="Email"
              rules={[{ required: true, message: 'Vui lòng nhập email' }, { type: 'email', message: 'Email không hợp lệ' }]}
            >
              <Input placeholder="admin@example.com" />
            </Form.Item>
            <Form.Item name="phone" label="Số điện thoại">
              <Input placeholder="Số điện thoại" />
            </Form.Item>

            <Divider orientation="left">Đổi mật khẩu</Divider>
            <Space direction="horizontal" size={16} wrap>
              <Form.Item name="newPassword" label="Mật khẩu mới" hasFeedback>
                <Input.Password placeholder="Nhập mật khẩu mới" />
              </Form.Item>
              <Form.Item name="confirmPassword" label="Xác nhận mật khẩu" dependencies={["newPassword"]} hasFeedback>
                <Input.Password placeholder="Nhập lại mật khẩu" />
              </Form.Item>
            </Space>

            <Button type="primary" icon={<SaveOutlined />} htmlType="submit" loading={saving}>
              Lưu thay đổi
            </Button>
          </Form>
        )}
      </Card>

      <Card bordered={false} style={{ borderRadius: 16, border: '1px solid #ffe1e1', background: '#fff1f0' }}>
        <Space direction="vertical" style={{ width: '100%' }}>
          <Title level={4} style={{ color: '#cf1322', marginBottom: 0 }}>Xóa tài khoản quản trị</Title>
          <Text type="secondary">
            Hành động này không thể hoàn tác. Sau khi xóa, bạn sẽ bị đăng xuất và mất quyền truy cập admin.
          </Text>
          <Popconfirm
            title="Bạn chắc chắn muốn xóa tài khoản admin này?"
            okText="Xóa"
            okButtonProps={{ danger: true, loading: deleting }}
            cancelText="Hủy"
            onConfirm={handleDelete}
          >
            <Button danger icon={<DeleteOutlined />} loading={deleting}>
              Xóa tài khoản quản trị
            </Button>
          </Popconfirm>
        </Space>
      </Card>
    </div>
  );
};

export default AccountSettings;
