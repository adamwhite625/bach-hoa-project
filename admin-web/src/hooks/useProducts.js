import { useState, useEffect } from 'react';
import { useSearchParams } from 'react-router-dom';
import { ProductService } from '../services/api/products';

export const useProducts = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [totalPages, setTotalPages] = useState(1);
  
  const [filters, setFilters] = useState({
    category: searchParams.get('category') || 'all',
    minPrice: searchParams.get('minPrice') || '',
    maxPrice: searchParams.get('maxPrice') || '',
    search: searchParams.get('search') || '',
    sort: searchParams.get('sort') || 'newest',
  page: parseInt(searchParams.get('page')) || 1,
  refresh: 0
  });

  const fetchProducts = async () => {
    setLoading(true);
    setError(null);
    try {
      const queryParams = {
        page: filters.page,
        limit: 12,
        sort: filters.sort,
        ...(filters.category !== 'all' && { category: filters.category }),
        ...(filters.minPrice && { minPrice: filters.minPrice }),
        ...(filters.maxPrice && { maxPrice: filters.maxPrice }),
        ...(filters.search && { search: filters.search })
      };

  const data = await ProductService.getProducts(queryParams);
  console.debug('Products API response:', data);
      if (data.EC === 0 && data.DT) {
        setProducts(data.DT.products || []);
        setTotalPages(Math.ceil((data.DT.total || 0) / 12));
        
        // Chỉ cập nhật URL parameters nếu có giá trị
        const newParams = {};
        if (filters.category !== 'all') newParams.category = filters.category;
        if (filters.minPrice) newParams.minPrice = filters.minPrice;
        if (filters.maxPrice) newParams.maxPrice = filters.maxPrice;
        if (filters.search) newParams.search = filters.search;
        if (filters.sort !== 'newest') newParams.sort = filters.sort;
        if (filters.page > 1) newParams.page = filters.page.toString();
        
        setSearchParams(newParams);
      } else {
        setError(data.EM || 'Không thể tải danh sách sản phẩm');
        setProducts([]);
        setTotalPages(0);
      }
    } catch (err) {
      setError('Đã có lỗi xảy ra khi tải danh sách sản phẩm');
      setProducts([]);
      setTotalPages(0);
    } finally {
      setLoading(false);
    }
  };

  const fetchCategories = async () => {
    try {
      const response = await ProductService.getCategories();
      
      if (response.EC === 0 && Array.isArray(response.DT)) {
        setCategories(response.DT);
      } else {
        setCategories([]);
      }
    } catch (err) {
      setCategories([]);
    }
  };

  // Fetch categories with retry logic
  useEffect(() => {
    let retryCount = 0;
    const maxRetries = 3;
    const retryDelay = 1000; // 1 second

    const initCategories = async () => {
      while (retryCount < maxRetries) {
        try {
          await fetchCategories();
          break; // Nếu thành công, thoát loop
        } catch (err) {
          retryCount++;
          if (retryCount === maxRetries) {
            console.error('Đã thử lại tối đa, không thể tải danh mục.');
            break;
          }
          await new Promise(resolve => setTimeout(resolve, retryDelay * retryCount));
        }
      }
    };

    initCategories();
  }, []); // Chỉ chạy một lần khi mount

  useEffect(() => {
    const controller = new AbortController();
    
    const loadProducts = async () => {
      try {
        await fetchProducts();
      } catch (err) {
        if (!controller.signal.aborted) {
          console.error('Lỗi khi tải sản phẩm:', err);
        }
      }
    };

    loadProducts();

    return () => controller.abort(); // Cleanup khi unmount hoặc filters thay đổi
  }, [
    filters.category,
    filters.minPrice,
    filters.maxPrice,
    filters.search,
    filters.sort,
  filters.page,
  filters.refresh
  ]);

  const updateFilters = (newFilters) => {
    console.log('updateFilters called with:', newFilters);
    setFilters(prev => {
      const updated = {
        ...prev,
        ...newFilters,
        page: 1 // Reset page when filters change
      };
      console.log('Updated filters:', updated);
      return updated;
    });
  };

  const clearFilters = () => {
    setFilters({
      category: 'all',
      minPrice: '',
      maxPrice: '',
      search: '',
      sort: 'newest',
      page: 1
    });
  };

  return {
    products,
    categories,
    loading,
    error,
    filters,
    totalPages,
    updateFilters,
    clearFilters,
    setFilters
  };
};
