import React, { useState, useEffect } from 'react';
import { brandAPI, categoryAPI } from '../services/api';
import ProductCard from './ProductCard';

const CategoryView = ({ categoryName, products, onViewDetail, toggleView }) => {
  const [brands, setBrands] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState('');
  const [sortBy, setSortBy] = useState('relevance');

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

  // Filter and sort products
  const filteredProducts = products
    .filter(product => product.name.toLowerCase().includes(searchTerm.toLowerCase()))
    .sort((a, b) => {
      if (sortBy === 'price-asc') {
        return a.price - b.price;
      } else if (sortBy === 'price-desc') {
        return b.price - a.price;
      }
      return 0;
    });

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto px-12 py-8">
        <nav className="text-xs text-gray-500 mb-6 uppercase tracking-wider">
          <a href="#" onClick={(e) => { e.preventDefault(); toggleView('home'); }} className="hover:text-red-600">Trang chủ</a>
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
        <a href="#" onClick={(e) => { e.preventDefault(); toggleView('home'); }} className="hover:text-red-600">Trang chủ</a>
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
            <div className="flex gap-4">
              <input
                type="text"
                placeholder="Tìm kiếm sản phẩm..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-red-600"
              />
              <div className="flex items-center gap-2">
                <span className="text-sm font-medium text-gray-700">Lọc theo:</span>
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
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {filteredProducts.length > 0 ? (
              filteredProducts.map(product => (
                <ProductCard key={product.id} product={product} onViewDetail={onViewDetail} />
              ))
            ) : (
              <div className="col-span-full text-center py-20 text-gray-500">
                Không tìm thấy sản phẩm nào
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CategoryView;