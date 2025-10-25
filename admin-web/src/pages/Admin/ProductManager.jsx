import React, { useEffect, useMemo, useState } from 'react';
import {
  Table,
  Card,
  Space,
  Button,
  Input,
  Modal,
  Form,
  InputNumber,
  Image,
  Typography,
  Select,
  message,
  Popconfirm,
  Tag,
  Switch,
  Row,
  Col,
  Divider,
  Tooltip
} from 'antd';
import {
  PlusOutlined,
  EditOutlined,
  DeleteOutlined,
  SearchOutlined,
  ReloadOutlined,
  ShoppingOutlined,
  CheckCircleOutlined,
  StopOutlined,
  AlertOutlined
} from '@ant-design/icons';
import { ProductService } from '../../services/api/products';

const { Title, Text } = Typography;
const { TextArea } = Input;
const { Option } = Select;

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0
});

const LOW_STOCK_THRESHOLD = 10;

const friendlyNumber = (value) => new Intl.NumberFormat('vi-VN').format(Number(value) || 0);

const ProductManager = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form] = Form.useForm();
  const [previewUrl, setPreviewUrl] = useState('');
  const [categories, setCategories] = useState([]);
  const [searchText, setSearchText] = useState('');
  const [categoryFilter, setCategoryFilter] = useState('all');
  const [statusFilter, setStatusFilter] = useState('all');

  const [viewportWidth, setViewportWidth] = useState(
    typeof window !== 'undefined' ? window.innerWidth : 1200
  );

  useEffect(() => {
    if (typeof window === 'undefined') return undefined;
    const handleResize = () => setViewportWidth(window.innerWidth);
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    const loadCategories = async () => {
      const categoriesData = await ProductService.getCategories();
      if (categoriesData.EC === 0) {
        setCategories(categoriesData.DT || []);
      }
    };
    loadCategories();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, []);

  const parseImages = (text) => {
    if (!text) return [];
    return text
      .split(/\r?\n|,/)
      .map((item) => item.trim())
      .filter(Boolean);
  };

  const mapProductForTable = (product, fallbackIndex = 0) => {
    if (!product) return product;
    const categoryObj = typeof product.category === 'object' ? product.category : null;
    const categoryId = categoryObj ? (categoryObj._id || categoryObj.id) : product.category;
    const primaryImage = Array.isArray(product.images) && product.images.length
      ? product.images[0]
      : (product.image || '');
    const categoryName = categoryObj?.name || categories.find((c) => c._id === categoryId)?.name || '';

    return {
      ...product,
      id: product._id || product.id || `product-${fallbackIndex}`,
      quantity: product.quantity ?? product.stock ?? 0,
      stock: product.quantity ?? product.stock ?? 0,
      image: primaryImage,
      categoryId,
      categoryName,
      isActive: typeof product.isActive === 'boolean' ? product.isActive : true
    };
  };

  const fetchProducts = async () => {
    setLoading(true);
    const res = await ProductService.getProducts();
    if (res.EC === 0 && Array.isArray(res.DT?.products)) {
      const mapped = res.DT.products.map((p, index) => mapProductForTable(p, index));
      setProducts(mapped);
    } else {
      message.error(res.EM || 'Không thể tải sản phẩm');
      setProducts([]);
    }
    setLoading(false);
  };

  const showModal = (product = null) => {
    setEditingProduct(product);
    if (product) {
      const initial = {
        name: product.name,
        sku: product.sku || '',
        description: product.description || '',
        brand: product.brand || '',
        category: product.categoryId || (
          typeof product.category === 'object'
            ? (product.category?._id || product.category?.id)
            : product.category
        ),
        price: product.price,
        quantity: product.quantity ?? product.stock ?? 0,
        isActive: typeof product.isActive === 'boolean' ? product.isActive : true,
        imagesText: Array.isArray(product.images)
          ? product.images.join('\n')
          : (product.image ? product.image : '')
      };
      form.setFieldsValue(initial);
      setPreviewUrl(parseImages(initial.imagesText)[0] || '');
    } else {
      form.resetFields();
      setPreviewUrl('');
    }
    setModalVisible(true);
  };

  const handleModalOk = () => {
    form.submit();
  };

  const handleModalCancel = () => {
    setModalVisible(false);
    setEditingProduct(null);
    form.resetFields();
    setPreviewUrl('');
  };

  const onFinish = async (values) => {
    try {
      const images = parseImages(values.imagesText);
      const payload = {
        name: values.name?.trim(),
        sku: values.sku?.trim(),
        description: values.description?.trim(),
        images,
        brand: values.brand?.trim() || undefined,
        category: values.category,
        price: Number(values.price) || 0,
        quantity: Number(values.quantity) || 0,
        isActive: !!values.isActive
      };

      if (!payload.name || !payload.sku || !payload.description || !payload.category || !images.length) {
        message.error('Vui lòng nhập đầy đủ tên, SKU, mô tả, danh mục và ít nhất 1 ảnh');
        return;
      }

      if (editingProduct) {
        const res = await ProductService.updateProduct(
          editingProduct.id || editingProduct._id,
          payload
        );
        if (res.EC === 0) {
          const targetId = editingProduct.id || editingProduct._id;
          const normalized = mapProductForTable(
            res.DT,
            products.findIndex((p) => p.id === targetId)
          );
          setProducts((prev) => prev.map((item) => (
            (item.id || item._id) === targetId ? normalized : item
          )));
          message.success('Cập nhật sản phẩm thành công');
        } else {
          message.error(res.EM || 'Cập nhật thất bại');
        }
      } else {
        const res = await ProductService.createProduct(payload);
        if (res.EC === 0) {
          const normalized = mapProductForTable(res.DT, products.length);
          setProducts((prev) => [...prev, normalized]);
          message.success('Thêm sản phẩm thành công');
        } else {
          message.error(res.EM || 'Thêm sản phẩm thất bại');
        }
      }

      handleModalCancel();
    } catch (error) {
      message.error('Có lỗi xảy ra khi lưu sản phẩm');
    }
  };

  const handleDelete = async (productId) => {
    try {
      const res = await ProductService.deleteProduct(productId);
      if (res.EC === 0) {
        setProducts((prev) => prev.filter((product) => (product.id || product._id) !== productId));
        message.success('Xóa sản phẩm thành công');
      } else {
        message.error(res.EM || 'Không thể xóa sản phẩm');
      }
    } catch (error) {
      message.error('Không thể xóa sản phẩm');
    }
  };

  const handleValuesChange = (_, all) => {
    if (typeof all.imagesText === 'string') {
      const first = parseImages(all.imagesText)[0] || '';
      setPreviewUrl(first);
    }
  };

  const statistics = useMemo(() => {
    const total = products.length;
    const active = products.filter((item) => item.isActive).length;
    const inactive = total - active;
    const lowStock = products.filter((item) => (item.quantity ?? 0) < LOW_STOCK_THRESHOLD).length;
    return { total, active, inactive, lowStock };
  }, [products]);

  const filteredProducts = useMemo(() => {
    const keyword = searchText.trim().toLowerCase();
    return products.filter((product) => {
      const matchesSearch = !keyword || [product.name, product.sku, product.brand]
        .filter(Boolean)
        .some((field) => field.toLowerCase().includes(keyword));
      const matchesCategory = categoryFilter === 'all' || product.categoryId === categoryFilter;
      const matchesStatus = (() => {
        if (statusFilter === 'all') return true;
        if (statusFilter === 'active') return product.isActive;
        if (statusFilter === 'inactive') return !product.isActive;
        if (statusFilter === 'lowStock') return (product.quantity ?? 0) < LOW_STOCK_THRESHOLD;
        return true;
      })();
      return matchesSearch && matchesCategory && matchesStatus;
    });
  }, [products, searchText, categoryFilter, statusFilter]);

  const summaryCards = useMemo(() => ([
    {
      key: 'total',
      title: 'Tổng sản phẩm',
      value: friendlyNumber(statistics.total),
      icon: <ShoppingOutlined />,
      color: '#1d39c4',
      description: 'Tất cả SKU trong hệ thống'
    },
    {
      key: 'active',
      title: 'Đang bán',
      value: friendlyNumber(statistics.active),
      icon: <CheckCircleOutlined />,
      color: '#52c41a',
      description: 'Sản phẩm đang hiển thị'
    },
    {
      key: 'inactive',
      title: 'Ngừng bán',
      value: friendlyNumber(statistics.inactive),
      icon: <StopOutlined />,
      color: '#fa541c',
      description: 'Đang ẩn khỏi cửa hàng'
    },
    {
      key: 'lowStock',
      title: 'Sắp hết hàng',
      value: friendlyNumber(statistics.lowStock),
      icon: <AlertOutlined />,
      color: '#faad14',
      description: `Số lượng < ${LOW_STOCK_THRESHOLD}`
    }
  ]), [statistics]);

  const isMobile = viewportWidth < 768;
  const modalWidth = Math.min(viewportWidth - 32, 820);

  const columns = useMemo(() => ([
    {
      title: '#',
      dataIndex: 'index',
      key: 'index',
      width: 60,
      align: 'center',
      render: (_, __, index) => index + 1
    },
    {
      title: 'Ảnh',
      dataIndex: 'image',
      key: 'image',
      width: 80,
      render: (image) => (
        image ? (
          <Image
            src={image}
            alt="product"
            width={56}
            height={56}
            style={{ objectFit: 'cover', borderRadius: 8 }}
            fallback=""
            preview={false}
          />
        ) : (
          <div
            style={{
              width: 56,
              height: 56,
              borderRadius: 8,
              background: '#f0f0f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              fontSize: 12,
              color: '#999'
            }}
          >
            N/A
          </div>
        )
      )
    },
    {
      title: 'Tên sản phẩm',
      dataIndex: 'name',
      key: 'name',
      width: 240,
      ellipsis: true,
      sorter: (a, b) => (a.name || '').localeCompare(b.name || ''),
      render: (value, record) => (
        <Space direction="vertical" size={0}>
          <Text strong>{value}</Text>
          <Text type="secondary">SKU: {record.sku || 'Chưa cập nhật'}</Text>
        </Space>
      )
    },
    {
      title: 'Danh mục',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 180,
      render: (name) => (
        <Tag color="blue">{name || 'Chưa gán danh mục'}</Tag>
      )
    },
    {
      title: 'Giá',
      dataIndex: 'price',
      key: 'price',
      align: 'right',
      width: 140,
      sorter: (a, b) => (a.price ?? 0) - (b.price ?? 0),
      render: (price) => currencyFormatter.format(price ?? 0)
    },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      key: 'quantity',
      align: 'center',
      width: 130,
      sorter: (a, b) => (a.quantity ?? 0) - (b.quantity ?? 0),
      render: (quantity) => {
        const qty = quantity ?? 0;
        if (qty < LOW_STOCK_THRESHOLD) {
          return <Tag color="orange">{qty} - Sắp hết</Tag>;
        }
        return <Text>{qty}</Text>;
      }
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 140,
      render: (isActive) => (
        <Tag color={isActive ? 'green' : 'red'}>{isActive ? 'Đang bán' : 'Ngừng bán'}</Tag>
      )
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 170,
      fixed: 'right',
      render: (_, record) => (
        <Space size={8} wrap>
          <Button icon={<EditOutlined />} onClick={() => showModal(record)}>
            Sửa
          </Button>
          <Popconfirm
            title="Bạn có chắc chắn muốn xóa sản phẩm này?"
            onConfirm={() => handleDelete(record.id || record._id)}
            okText="Xóa"
            cancelText="Hủy"
          >
            <Button icon={<DeleteOutlined />} danger>
              Xóa
            </Button>
          </Popconfirm>
        </Space>
      )
    }
  ]), []);

  return (
    <div style={{ padding: 24, display: 'flex', flexDirection: 'column', gap: 24 }}>
      <Card
        bordered={false}
        style={{
          borderRadius: 16,
          background: 'linear-gradient(135deg, #f5f9ff 0%, #ffffff 100%)'
        }}
      >
        <Space direction="vertical" size={8} style={{ width: '100%' }}>
          <Space size={12} align="center" wrap>
            <Title level={3} style={{ margin: 0, color: '#1d39c4' }}>Quản lý sản phẩm</Title>
            <Tag color="blue" style={{ borderRadius: 16 }}>
              Cập nhật lần cuối: {new Date().toLocaleDateString('vi-VN')}
            </Tag>
          </Space>
          <Text type="secondary">
            Theo dõi tồn kho, tình trạng bán và cập nhật thông tin sản phẩm một cách trực quan.
          </Text>
        </Space>
      </Card>

      <Row gutter={[16, 16]}>
        {summaryCards.map((card) => (
          <Col xs={24} sm={12} xl={6} key={card.key}>
            <Card bordered={false} style={{ borderRadius: 16, height: '100%' }}>
              <Space align="start" style={{ width: '100%', justifyContent: 'space-between' }}>
                <Space direction="vertical" size={2}>
                  <Text type="secondary" style={{ textTransform: 'uppercase', fontSize: 12 }}>
                    {card.title}
                  </Text>
                  <Title level={3} style={{ margin: 0, color: card.color }}>{card.value}</Title>
                  <Text type="secondary">{card.description}</Text>
                </Space>
                <div
                  style={{
                    width: 44,
                    height: 44,
                    borderRadius: '50%',
                    background: `${card.color}1a`,
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: card.color,
                    fontSize: 20
                  }}
                >
                  {card.icon}
                </div>
              </Space>
            </Card>
          </Col>
        ))}
      </Row>

      <Card bordered={false} style={{ borderRadius: 16 }}>
        <Space
          direction={isMobile ? 'vertical' : 'horizontal'}
          style={{ width: '100%', justifyContent: 'space-between' }}
          size={isMobile ? 16 : 12}
        >
          <Space wrap size={12}>
            <Input
              allowClear
              placeholder="Tìm kiếm theo tên, SKU hoặc thương hiệu"
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={(event) => setSearchText(event.target.value)}
              style={{ width: isMobile ? '100%' : 280 }}
              size={isMobile ? 'middle' : 'large'}
            />
            <Select
              style={{ width: isMobile ? '100%' : 200 }}
              value={categoryFilter}
              onChange={setCategoryFilter}
              size={isMobile ? 'middle' : 'large'}
              placeholder="Chọn danh mục"
            >
              <Option value="all">Tất cả danh mục</Option>
              {categories.map((cat) => (
                <Option key={cat._id} value={cat._id}>{cat.name}</Option>
              ))}
            </Select>
            <Select
              style={{ width: isMobile ? '100%' : 200 }}
              value={statusFilter}
              onChange={setStatusFilter}
              size={isMobile ? 'middle' : 'large'}
            >
              <Option value="all">Tất cả trạng thái</Option>
              <Option value="active">Đang bán</Option>
              <Option value="inactive">Ngừng bán</Option>
              <Option value="lowStock">Sắp hết hàng</Option>
            </Select>
            <Tooltip title="Đặt lại bộ lọc">
              <Button onClick={() => { setCategoryFilter('all'); setStatusFilter('all'); setSearchText(''); }}>
                Đặt lại
              </Button>
            </Tooltip>
          </Space>
          <Space size={12} wrap>
            <Button icon={<ReloadOutlined />} onClick={fetchProducts} loading={loading}>
              Tải lại
            </Button>
            <Button
              type="primary"
              icon={<PlusOutlined />}
              size={isMobile ? 'middle' : 'large'}
              onClick={() => showModal()}
            >
              {isMobile ? 'Thêm sản phẩm' : 'Thêm sản phẩm mới'}
            </Button>
          </Space>
        </Space>
      </Card>

      <Card bordered={false} style={{ borderRadius: 16 }}>
        <Table
          columns={columns}
          dataSource={filteredProducts}
          loading={loading}
          size={isMobile ? 'small' : 'middle'}
          rowKey={(record) => record.id || record._id}
          pagination={{
            showSizeChanger: true,
            showTotal: (total) => `Tổng ${total} sản phẩm`,
            pageSizeOptions: ['10', '20', '50', '100']
          }}
          scroll={{ x: 960 }}
          sticky
        />
      </Card>

      <Modal
        title={editingProduct ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới'}
        open={modalVisible}
        onOk={handleModalOk}
        onCancel={handleModalCancel}
        width={modalWidth}
        centered
        destroyOnClose
        maskClosable={!editingProduct}
      >
        <Form
          form={form}
          layout="vertical"
          onFinish={onFinish}
          onValuesChange={handleValuesChange}
          initialValues={{ isActive: true }}
        >
          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="name"
                label="Tên sản phẩm"
                rules={[{ required: true, message: 'Vui lòng nhập tên sản phẩm' }]}
              >
                <Input placeholder="Ví dụ: Sữa tươi A" />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="sku"
                label="SKU"
                rules={[{ required: true, message: 'Vui lòng nhập SKU' }]}
              >
                <Input placeholder="VD: SKU-001" />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="price"
                label="Giá"
                rules={[{ required: true, message: 'Vui lòng nhập giá sản phẩm' }]}
              >
                <InputNumber
                  style={{ width: '100%' }}
                  min={0}
                  formatter={(value) => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
                  parser={(value) => value.replace(/\$\s?|,(?=\d)/g, '')}
                />
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item
                name="quantity"
                label="Số lượng trong kho"
                rules={[{ required: true, message: 'Vui lòng nhập số lượng' }]}
              >
                <InputNumber style={{ width: '100%' }} min={0} />
              </Form.Item>
            </Col>
          </Row>

          <Row gutter={16}>
            <Col xs={24} md={12}>
              <Form.Item
                name="category"
                label="Danh mục"
                rules={[{ required: true, message: 'Vui lòng chọn danh mục' }]}
              >
                <Select
                  showSearch
                  placeholder="Chọn danh mục"
                  optionFilterProp="children"
                  loading={!categories.length}
                >
                  {categories.map((cat) => (
                    <Option key={cat._id} value={cat._id}>{cat.name}</Option>
                  ))}
                </Select>
              </Form.Item>
            </Col>
            <Col xs={24} md={12}>
              <Form.Item name="brand" label="Thương hiệu">
                <Input placeholder="Tên thương hiệu" />
              </Form.Item>
            </Col>
          </Row>

          <Form.Item
            name="description"
            label="Mô tả"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả sản phẩm' }]}
          >
            <TextArea rows={isMobile ? 3 : 4} placeholder="Mô tả ngắn gọn về sản phẩm" />
          </Form.Item>

          <Form.Item
            name="imagesText"
            label="Hình ảnh (mỗi dòng một URL)"
            rules={[{ required: true, message: 'Vui lòng nhập ít nhất 1 ảnh' }]}
          >
            <TextArea rows={isMobile ? 3 : 4} placeholder="https://...jpg\nhttps://...jpg" />
          </Form.Item>

          {previewUrl ? (
            <Card size="small" style={{ marginBottom: 16, borderRadius: 12 }}>
              <Space direction="vertical" size={8}>
                <Text type="secondary">Xem trước ảnh đầu tiên</Text>
                <Image src={previewUrl} alt="preview" width={220} style={{ objectFit: 'cover' }} />
              </Space>
            </Card>
          ) : null}

          <Divider style={{ margin: '12px 0' }} />

          <Form.Item name="isActive" label="Kích hoạt" valuePropName="checked">
            <Switch checkedChildren="Bật" unCheckedChildren="Tắt" />
          </Form.Item>
        </Form>
      </Modal>
    </div>
  );
};

export default ProductManager;
