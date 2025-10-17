import React, { useState, useEffect } from 'react';
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
  Switch
} from 'antd';
import { PlusOutlined, EditOutlined, DeleteOutlined, SearchOutlined } from '@ant-design/icons';
import { ProductService } from '../../services/api/products';

const { Title } = Typography;
const { TextArea } = Input;
const { Option } = Select;

const currencyFormatter = new Intl.NumberFormat('vi-VN', {
  style: 'currency',
  currency: 'VND',
  maximumFractionDigits: 0
});

const ProductManager = () => {
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);
  const [modalVisible, setModalVisible] = useState(false);
  const [editingProduct, setEditingProduct] = useState(null);
  const [form] = Form.useForm();
  const [previewUrl, setPreviewUrl] = useState('');
  const [categories, setCategories] = useState([]);
  const [searchText, setSearchText] = useState('');

  // Track viewport for responsive behaviors
  const [vw, setVw] = useState(typeof window !== 'undefined' ? window.innerWidth : 1200);

  useEffect(() => {
    if (typeof window === 'undefined') return undefined;
    const handleResize = () => setVw(window.innerWidth);
    handleResize();
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  useEffect(() => {
    const fetchCategories = async () => {
      const categoriesData = await ProductService.getCategories();
      if (categoriesData.EC === 0) {
        setCategories(categoriesData.DT);
      }
    };
    fetchCategories();
  }, []);

  useEffect(() => {
    fetchProducts();
  }, []);

  const parseImages = (text) => {
    if (!text) return [];
    return text
      .split(/\r?\n|,/)
      .map(s => s.trim())
      .filter(Boolean);
  };

  const isMobile = vw < 768;
  const modalWidth = Math.min(vw - 32, 800);

  const mapProductForTable = (product, fallbackIndex = 0) => {
    if (!product) return product;
    const categoryObj = typeof product.category === 'object' ? product.category : null;
    const categoryId = categoryObj ? (categoryObj._id || categoryObj.id) : product.category;
    const primaryImage = Array.isArray(product.images) && product.images.length ? product.images[0] : (product.image || '');
    const categoryName = categoryObj?.name || categories.find(c => c._id === categoryId)?.name || '';
    return {
      ...product,
      id: product._id || product.id || `product-${fallbackIndex}`,
      quantity: product.quantity ?? product.stock ?? 0,
      stock: product.quantity ?? product.stock ?? 0,
      image: primaryImage,
      categoryId,
      categoryName
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
      // map normalized product -> form fields
      const initial = {
        name: product.name,
        sku: product.sku || '',
        description: product.description || '',
        brand: product.brand || '',
        category: product.categoryId || (typeof product.category === 'object' ? (product.category?._id || product.category?.id) : product.category),
        price: product.price,
        quantity: product.quantity ?? product.stock ?? 0,
        isActive: typeof product.isActive === 'boolean' ? product.isActive : true,
        imagesText: Array.isArray(product.images) ? product.images.join('\n') : (product.image ? product.image : '')
      };
      form.setFieldsValue(initial);
      setPreviewUrl(initial.imagesText?.split(/\r?\n|,/)[0] || '');
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
        isActive: !!values.isActive,
      };

      // basic front validation
      if (!payload.name || !payload.sku || !payload.description || !payload.category || !images.length) {
        message.error('Vui lòng nhập đầy đủ: tên, SKU, mô tả, danh mục, và ít nhất 1 ảnh');
        return;
      }

      if (editingProduct) {
        const res = await ProductService.updateProduct(editingProduct.id || editingProduct._id, payload);
        if (res.EC === 0) {
          const targetId = editingProduct.id || editingProduct._id;
          const normalized = mapProductForTable(res.DT, products.findIndex(p => p.id === targetId));
          setProducts(products.map(product => {
            const currentId = product.id || product._id;
            return currentId === targetId ? normalized : product;
          }));
          message.success('Cập nhật sản phẩm thành công');
        } else message.error(res.EM || 'Cập nhật thất bại');
      } else {
        const res = await ProductService.createProduct(payload);
        if (res.EC === 0) {
          const normalized = mapProductForTable(res.DT, products.length);
          setProducts([...products, normalized]);
          message.success('Thêm sản phẩm thành công');
        } else message.error(res.EM || 'Thêm sản phẩm thất bại');
      }
      handleModalCancel();
    } catch (error) {
      message.error('Có lỗi xảy ra');
    }
  };

  const handleDelete = async (productId) => {
    try {
      const res = await ProductService.deleteProduct(productId);
      if (res.EC === 0) {
        setProducts(products.filter(product => (product.id || product._id) !== productId));
        message.success('Xóa sản phẩm thành công');
      } else message.error(res.EM || 'Không thể xóa sản phẩm');
    } catch (error) { message.error('Không thể xóa sản phẩm'); }
  };

  const handleValuesChange = (_, all) => {
    if (typeof all.imagesText === 'string') {
      const first = parseImages(all.imagesText)[0] || '';
      setPreviewUrl(first);
    }
  };

  const columns = [
    {
      title: '#',
      dataIndex: 'index',
      key: 'index',
      width: 60,
      align: 'center',
      render: (_, __, index) => index + 1,
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
            style={{ objectFit: 'cover', borderRadius: 6 }}
            fallback=""
            preview={false}
          />
        ) : (
          <div style={{ width: 56, height: 56, borderRadius: 6, background: '#f0f0f0', display: 'flex', alignItems: 'center', justifyContent: 'center', fontSize: 12, color: '#999' }}>
            N/A
          </div>
        )
      ),
    },
    {
      title: 'Tên sản phẩm',
      dataIndex: 'name',
      key: 'name',
      width: 230,
      ellipsis: true,
      sorter: (a, b) => (a.name || '').localeCompare(b.name || ''),
      filteredValue: searchText ? [searchText] : null,
      onFilter: (value, record) => {
        const keyword = value.toLowerCase();
        return (
          (record.name || '').toLowerCase().includes(keyword) ||
          (record.sku || '').toLowerCase().includes(keyword)
        );
      },
    },
    {
      title: 'SKU',
      dataIndex: 'sku',
      key: 'sku',
      width: 140,
      ellipsis: true,
      render: (sku) => sku || '-',
    },
    {
      title: 'Thương hiệu',
      dataIndex: 'brand',
      key: 'brand',
      width: 140,
      ellipsis: true,
      responsive: ['lg'],
      render: (brand) => brand || '-',
    },
    {
      title: 'Danh mục',
      dataIndex: 'categoryName',
      key: 'categoryName',
      width: 160,
      responsive: ['sm'],
      filters: categories.map(c => ({ text: c.name, value: c._id })),
      onFilter: (value, record) => record.categoryId === value,
      render: (name, record) => name || record?.category?.name || '-',
    },
    {
      title: 'Giá',
      dataIndex: 'price',
      key: 'price',
      width: 130,
      align: 'right',
      sorter: (a, b) => (a.price ?? 0) - (b.price ?? 0),
      render: (price) => currencyFormatter.format(price ?? 0),
    },
    {
      title: 'Số lượng',
      dataIndex: 'quantity',
      key: 'quantity',
      width: 120,
      align: 'center',
      sorter: (a, b) => (a.quantity ?? 0) - (b.quantity ?? 0),
      render: (quantity) => quantity ?? 0,
    },
    {
      title: 'Trạng thái',
      dataIndex: 'isActive',
      key: 'isActive',
      width: 120,
      responsive: ['lg'],
      render: (isActive) => {
        const color = isActive ? 'green' : 'red';
        const text = isActive ? 'Đang bán' : 'Ngừng bán';
        return <Tag color={color}>{text}</Tag>;
      },
      filters: [
        { text: 'Đang bán', value: true },
        { text: 'Ngừng bán', value: false },
      ],
      onFilter: (value, record) => Boolean(record.isActive) === value,
    },
    {
      title: 'Thao tác',
      key: 'action',
      width: 160,
      render: (_, record) => (
        <Space>
          <Button
            icon={<EditOutlined />}
            onClick={() => showModal(record)}
          >
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
      ),
    },
  ];

  return (
    <div style={{ padding: '24px' }}>
      <Card className="admin-product-card">
        <div className="admin-product-toolbar">
          <div className="left">
            <Title level={2} style={{ margin: 0, fontSize: isMobile ? 22 : 28 }}>Quản lý sản phẩm</Title>
            <Input
              allowClear
              placeholder="Tìm kiếm..."
              prefix={<SearchOutlined />}
              value={searchText}
              onChange={e => setSearchText(e.target.value)}
              className="admin-product-search"
              style={{ width: isMobile ? '100%' : 280 }}
              size={isMobile ? 'middle' : 'large'}
            />
          </div>
          <div className="right">
            <Button
              type="primary"
              icon={<PlusOutlined />}
              size={isMobile ? 'middle' : 'large'}
              onClick={() => showModal()}
            >
              {isMobile ? 'Thêm' : 'Thêm sản phẩm'}
            </Button>
          </div>
        </div>
        <div style={{ marginTop: 18 }}>
          <Table
            columns={columns}
            dataSource={products}
            loading={loading}
            size={isMobile ? 'small' : 'middle'}
            rowKey={(r) => r.id || r._id}
            pagination={{
              showSizeChanger: true,
              showTotal: (total) => `Tổng ${total} sản phẩm`,
              pageSizeOptions: ['10','20','50','100']
            }}
            scroll={{ x: 900 }}
            sticky
          />
        </div>
      </Card>

      <Modal
        title={editingProduct ? 'Sửa sản phẩm' : 'Thêm sản phẩm mới'}
        open={modalVisible}
        /* visible prop fallback for any residual plugins expecting it */
        visible={modalVisible}
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
          <Form.Item
            name="name"
            label="Tên sản phẩm"
            rules={[{ required: true, message: 'Vui lòng nhập tên sản phẩm' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="sku"
            label="SKU"
            rules={[{ required: true, message: 'Vui lòng nhập SKU' }]}
          >
            <Input />
          </Form.Item>

          <Form.Item
            name="price"
            label="Giá"
            rules={[{ required: true, message: 'Vui lòng nhập giá sản phẩm' }]}
          >
            <InputNumber
              style={{ width: '100%' }}
              min={0}
              formatter={value => `${value}`.replace(/\B(?=(\d{3})+(?!\d))/g, ',')}
              parser={value => value.replace(/\$\s?|(,*)/g, '')}
            />
          </Form.Item>

          <Form.Item
            name="quantity"
            label="Số lượng trong kho"
            rules={[{ required: true, message: 'Vui lòng nhập số lượng' }]}
          >
            <InputNumber style={{ width: '100%' }} min={0} />
          </Form.Item>

          <Form.Item
            name="category"
            label="Danh mục"
            rules={[{ required: true, message: 'Vui lòng chọn danh mục' }]}
          >
            <Select
              showSearch
              placeholder="Chọn danh mục"
              optionFilterProp="label"
              loading={!categories.length}
            >
              {categories.map(cat => (
                <Option key={cat._id} value={cat._id} label={cat.name}>{cat.name}</Option>
              ))}
            </Select>
          </Form.Item>

          <Form.Item name="brand" label="Thương hiệu">
            <Input />
          </Form.Item>

          <Form.Item
            name="description"
            label="Mô tả"
            rules={[{ required: true, message: 'Vui lòng nhập mô tả sản phẩm' }]}
          >
            <TextArea rows={isMobile ? 3 : 4} />
          </Form.Item>

          <Form.Item
            name="imagesText"
            label="Hình ảnh (mỗi dòng một URL)"
            rules={[{ required: true, message: 'Vui lòng nhập ít nhất 1 ảnh' }]}
          >
            <TextArea rows={isMobile ? 3 : 4} placeholder="https://...jpg\nhttps://...jpg" />
          </Form.Item>

          {previewUrl ? (
            <div style={{ marginBottom: 16 }}>
              <Image src={previewUrl} alt="preview" width={200} style={{ objectFit: 'cover' }} />
            </div>
          ) : null}

          <Form.Item name="isActive" label="Kích hoạt" valuePropName="checked">
            <Switch checkedChildren="Bật" unCheckedChildren="Tắt" />
          </Form.Item>
        </Form>
  </Modal>
    </div>
  );
};

export default ProductManager;
