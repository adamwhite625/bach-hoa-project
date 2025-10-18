import React, { useEffect, useMemo, useState } from 'react';
import { Card, Table, Button, Modal, Form, Input, Image, Space, Typography, message } from 'antd';
import { PlusOutlined } from '@ant-design/icons';
import { CategoryService } from '../../services/api/categories';

const { Title } = Typography;
const { TextArea } = Input;

const CategoryManager = () => {
  const [list, setList] = useState([]);
  const [loading, setLoading] = useState(false);
  const [open, setOpen] = useState(false);
  const [form] = Form.useForm();

  const fetch = async () => {
    setLoading(true);
    const res = await CategoryService.list();
    if (res.EC === 0) setList(res.DT);
    else message.error(res.EM || 'Không thể tải danh mục');
    setLoading(false);
  };

  useEffect(() => { fetch(); }, []);

  const onCreate = async (values) => {
    const res = await CategoryService.create(values);
    if (res.EC === 0) {
      message.success('Tạo danh mục thành công');
      setOpen(false);
      form.resetFields();
      fetch();
    } else {
      message.error(res.EM || 'Tạo danh mục thất bại');
    }
  };

  const columns = [
    { title: 'Ảnh', dataIndex: 'image', key: 'image', width: 80, render: (img) => img ? <Image src={img} width={48} /> : '-' },
    { title: 'Tên danh mục', dataIndex: 'name', key: 'name' },
    { title: 'Mô tả', dataIndex: 'description', key: 'description', responsive: ['md'] },
    { title: 'Ngày tạo', dataIndex: 'createdAt', key: 'createdAt', responsive: ['lg'], render: (d) => d ? new Date(d).toLocaleString('vi-VN') : '-' },
  ];

  return (
    <div style={{ padding: 24 }}>
      <Card>
        <div className="admin-category-toolbar" style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
          <Title level={2} style={{ margin: 0, fontSize: 24 }}>Danh mục</Title>
          <Button type="primary" icon={<PlusOutlined />} onClick={() => setOpen(true)}>Thêm danh mục</Button>
        </div>
        <div style={{ marginTop: 16 }}>
          <Table rowKey={(r) => r._id || r.id} loading={loading} dataSource={list} columns={columns} pagination={{ pageSize: 10 }} />
        </div>
      </Card>

      <Modal
        title="Thêm danh mục"
        open={open}
        onOk={() => form.submit()}
        onCancel={() => setOpen(false)}
      >
        <Form layout="vertical" form={form} onFinish={onCreate}>
          <Form.Item name="name" label="Tên danh mục" rules={[{ required: true, message: 'Nhập tên danh mục' }]}>
            <Input />
          </Form.Item>
          <Form.Item name="description" label="Mô tả">
            <TextArea rows={3} />
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
