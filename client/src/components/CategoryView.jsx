import React, { useState, useEffect, useRef } from 'react';
import { useSearchParams, useNavigate } from 'react-router-dom';
import { brandAPI, categoryAPI, searchAPI } from '../services/api';
import ProductCard from './ProductCard';

const CategoryView = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  
  const categoryName = searchParams.get('category') || 'Sản phẩm';
  
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  
  // Search state
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('relevance');
  const [selectedBrand, setSelectedBrand] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [triggerSearch, setTriggerSearch] = useState(false); // Trigger search manually
  
  // Elasticsearch results
  const [searchResults, setSearchResults] = useState([]);
  const [totalResults, setTotalResults] = useState(0);
  const [currentPage, setCurrentPage] = useState(0);
  const pageSize = 25;

  // Auto-suggest state
  const [suggestions, setSuggestions] = useState([]);
  const [showSuggestions, setShowSuggestions] = useState(false);
  const suggestionRef = useRef(null);

  // Fetch brands and categories on mount
  useEffect(() => {
    const fetchData = async () => {
      try {
        const [brandResponse, categoryResponse] = await Promise.all([
          brandAPI.getAllBrands(),
          categoryAPI.getAllCategories()
        ]);

        if (brandResponse.code === 1000 && brandResponse.result) {
          setBrands(brandResponse.result);
        }

        if (categoryResponse.code === 1000 && categoryResponse.result) {
          setCategories(categoryResponse.result);
        }
      } catch (error) {
        console.error('Failed to fetch brands and categories:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Load all products on mount
  useEffect(() => {
    const loadAllProducts = async () => {
      try {
        const params = {
          query: '',
          page: 0,
          size: pageSize,
          sortBy: 'price',
          sortDirection: 'asc',
        };

        console.log('Loading all products with params:', params);
        const response = await searchAPI.searchProducts(params);
        console.log('Initial products response:', response);

        if (response.code === 1000 && response.result) {
          setSearchResults(response.result.content || []);
          setTotalResults(response.result.totalElements || 0);
          console.log('Loaded', response.result.content?.length, 'products');
        }
      } catch (error) {
        console.error('Failed to load products:', error);
      }
    };

    loadAllProducts();
  }, []);

  // Search products when triggerSearch is true
  useEffect(() => {
    if (!triggerSearch) return;

    const performSearch = async () => {
      console.log('Performing search with params:', { searchTerm, currentPage, sortBy });
      try {
        const params = {
          query: searchTerm || '',
          page: currentPage,
          size: pageSize,
          sortBy: 'price',
          sortDirection: 'asc',
        };

        // Map sort options
        if (sortBy === 'price-asc') {
          params.sortDirection = 'asc';
        } else if (sortBy === 'price-desc') {
          params.sortDirection = 'desc';
        }

        // Add brand filter
        if (selectedBrand) {
          params.brand = selectedBrand;
        }

        // Add category filter
        if (selectedCategory) {
          params.category = selectedCategory;
        }

        console.log('Calling searchAPI with:', params);
        const response = await searchAPI.searchProducts(params);
        console.log('Search response:', response);

        if (response.code === 1000 && response.result) {
          setSearchResults(response.result.content || []);
          setTotalResults(response.result.totalElements || 0);
          console.log('Set results:', response.result.content?.length, 'items');
        } else {
          console.warn('Search response code:', response.code);
        }
      } catch (error) {
        console.error('Search failed:', error);
      } finally {
        setTriggerSearch(false); // Reset trigger after search completes
      }
    };

    performSearch();
  }, [triggerSearch, searchTerm, sortBy, selectedBrand, selectedCategory, currentPage]);

  // Auto-suggest effect
  useEffect(() => {
    if (searchTerm.length >= 2) {
      const fetchSuggestions = async () => {
        try {
          const response = await searchAPI.suggestProducts(searchTerm);
          if (response.code === 1000 && response.result) {
            setSuggestions(response.result);
            setShowSuggestions(true);
          }
        } catch (error) {
          console.error('Failed to fetch suggestions:', error);
        }
      };

      const debounceTimer = setTimeout(fetchSuggestions, 200);
      return () => clearTimeout(debounceTimer);
    } else {
      setSuggestions([]);
      setShowSuggestions(false);
    }
  }, [searchTerm]);

  // Close suggestions when clicking outside
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (suggestionRef.current && !suggestionRef.current.contains(event.target)) {
        setShowSuggestions(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleBrandChange = (brandName) => {
    setSelectedBrand(selectedBrand === brandName ? '' : brandName);
    setCurrentPage(0);
  };

  const handleCategoryChange = (categoryName) => {
    setSelectedCategory(selectedCategory === categoryName ? '' : categoryName);
    setCurrentPage(0);
  };

  const handleSuggestionClick = (suggestion) => {
    setSearchTerm(suggestion.name);
    setShowSuggestions(false);
    setTriggerSearch(true); // Trigger search
  };

  const handleProductClick = (productId) => {
    navigate(`/product/${productId}`);
  };

  const handleSearchClick = () => {
    setTriggerSearch(true); // Trigger search on button click
    setShowSuggestions(false);
  };

  const handleSearchKeyDown = (e) => {
    if (e.key === 'Enter') {
      setTriggerSearch(true); // Trigger search on Enter
      setShowSuggestions(false);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto px-12 py-8">
        <nav className="text-xs text-gray-500 mb-6 uppercase tracking-wider">
          <a href="#" onClick={(e) => { e.preventDefault(); navigate('/'); }} className="hover:text-red-600">Trang chủ</a>
          <span className="mx-2">/</span>
          <span className="text-gray-800 font-bold">{categoryName}</span>
        </nav>
        <div className="flex justify-center items-center py-20">
          <div className="text-gray-500">Đang tải...</div>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-[1200px] mx-auto px-12 py-8">
      <nav className="text-xs text-gray-500 mb-6 uppercase tracking-wider">
        <a href="#" onClick={(e) => { e.preventDefault(); navigate('/'); }} className="hover:text-red-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <span className="text-gray-800 font-bold">{categoryName}</span>
      </nav>

      <div className="flex flex-col md:flex-row gap-8">
        <aside className="w-full md:w-1/4 lg:w-1/5 space-y-8">
          <div className="filter-section">
            <h4>Thương hiệu</h4>
            <ul className="text-sm space-y-3 text-gray-600">
              {brands.length > 0 ? (
                brands.map(brand => (
                  <li key={brand.id} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      id={`brand-${brand.id}`}
                      checked={selectedBrand === brand.name}
                      onChange={() => handleBrandChange(brand.name)}
                      className="w-4 h-4 accent-red-600 cursor-pointer"
                    />
                    <label htmlFor={`brand-${brand.id}`} className="flex-1 cursor-pointer hover:text-red-600">
                      {brand.name}
                    </label>
                  </li>
                ))
              ) : (
                <li className="text-gray-400">Không có thương hiệu</li>
              )}
            </ul>
          </div>
          <div className="filter-section">
            <h4>Danh mục sản phẩm</h4>
            <ul className="text-sm space-y-3 text-gray-600">
              {categories.length > 0 ? (
                categories.map(category => (
                  <li key={category.id} className="flex items-center gap-2">
                    <input
                      type="checkbox"
                      id={`category-${category.id}`}
                      checked={selectedCategory === category.name}
                      onChange={() => handleCategoryChange(category.name)}
                      className="w-4 h-4 accent-red-600 cursor-pointer"
                    />
                    <label htmlFor={`category-${category.id}`} className="flex-1 cursor-pointer hover:text-red-600">
                      {category.name}
                    </label>
                  </li>
                ))
              ) : (
                <li className="text-gray-400">Không có danh mục</li>
              )}
            </ul>
          </div>
        </aside>

        <div className="flex-1">
          <div className="bg-white p-4 shadow-sm rounded mb-6">
            <div className="flex gap-2">
              <div className="flex-1 relative" ref={suggestionRef}>
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm..."
                  value={searchTerm}
                  onChange={(e) => setSearchTerm(e.target.value)}
                  onKeyDown={handleSearchKeyDown}
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600"
                />
                {showSuggestions && suggestions.length > 0 && (
                  <div className="absolute top-full left-0 right-0 mt-1 bg-white border border-gray-300 rounded-lg shadow-lg z-50 max-h-60 overflow-y-auto">
                    {suggestions.map(suggestion => (
                      <div
                        key={suggestion.id}
                        onClick={() => handleSuggestionClick(suggestion)}
                        className="px-4 py-2 hover:bg-gray-100 cursor-pointer flex items-center gap-3"
                      >
                        <img
                          src={suggestion.image || 'https://via.placeholder.com/40x40?text=?'}
                          alt={suggestion.name}
                          className="w-10 h-10 object-cover rounded"
                        />
                        <div className="flex-1">
                          <div className="font-medium text-sm">{suggestion.name}</div>
                          <div className="text-xs text-gray-500">{suggestion.categoryName} • {suggestion.brandName}</div>
                        </div>
                        {suggestion.price && (
                          <div className="text-sm font-semibold text-red-600">
                            {formatCurrency(suggestion.price)}
                          </div>
                        )}
                      </div>
                    ))}
                  </div>
                )}
              </div>
              <button
                onClick={handleSearchClick}
                className="px-6 py-2 bg-red-600 text-white rounded-lg hover:bg-red-700 transition font-medium"
              >
                <i className="fas fa-search"></i> Tìm kiếm
              </button>
              <div className="flex items-center">
                <span className="text-sm font-medium text-gray-700 whitespace-nowrap">Lọc theo:</span>
                <select
                  value={sortBy}
                  onChange={(e) => setSortBy(e.target.value)}
                  className="px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600 bg-white cursor-pointer"
                >
                  <option value="relevance">Liên quan</option>
                  <option value="price-asc">Giá từ nhỏ đến lớn</option>
                  <option value="price-desc">Giá từ lớn đến nhỏ</option>
                </select>
              </div>
            </div>
          </div>

          <div className="flex justify-between items-center mb-6">
            <h1 className="text-xl font-bold uppercase">{categoryName}</h1>
            <span className="text-sm text-gray-500">{totalResults} sản phẩm</span>
          </div>

          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {searchResults.length > 0 ? (
              searchResults.map(product => (
                <ProductCard 
                  key={product.id} 
                  product={{
                    id: product.productId,
                    name: product.name,
                    img: product.image,
                    price: product.price
                  }} 
                  onViewDetail={handleProductClick} 
                />
              ))
            ) : (
              <div className="col-span-full text-center py-20 text-gray-500">
                Không tìm thấy sản phẩm nào
              </div>
            )}
          </div>

          {/* Pagination */}
          {searchResults.length > 0 && (
            <div className="flex justify-center items-center gap-2 mt-8">
              <button
                onClick={() => setCurrentPage(p => Math.max(0, p - 1))}
                disabled={currentPage === 0}
                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
              >
                Trước
              </button>
              <span className="px-4 py-2 text-gray-700">
                Trang {currentPage + 1} / {Math.ceil(totalResults / pageSize)}
              </span>
              <button
                onClick={() => setCurrentPage(p => p + 1)}
                disabled={searchResults.length < pageSize}
                className="px-4 py-2 border border-gray-300 rounded-lg disabled:opacity-50 disabled:cursor-not-allowed hover:bg-gray-100"
              >
                Sau
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default CategoryView;
