import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { productAPI } from '../services/api';
import './ProductGrid.css';

const ProductGrid = () => {
  const navigate = useNavigate();
  const [products, setProducts] = useState([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchProducts = async () => {
      try {
        const response = await productAPI.getLatest5Products();
        console.log('Latest products response:', response);
        if (response.code === 1000 && response.result) {
          console.log('Products count:', response.result.length);
          setProducts(response.result);
        }
      } catch (error) {
        console.error('Failed to fetch latest products:', error);
      } finally {
        setLoading(false);
      }
    };

    fetchProducts();
  }, []);

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const handleProductClick = (productId) => {
    navigate(`/product/${productId}`);
  };

  if (loading) {
    return (
      <main className="max-w-[1200px] mx-auto px-12 py-8">
        <div className="flex justify-between items-end mb-8 border-b border-gray-200 pb-4">
          <h2 className="text-2xl font-bold uppercase border-b-2 border-red-600 -mb-5 pb-4">Sản phẩm mới nhất</h2>
          <button onClick={() => navigate('/?view=category')} className="text-red-600 font-medium hover:underline">Xem tất cả <i className="fas fa-arrow-right text-xs"></i></button>
        </div>
        <div className="flex justify-center items-center py-20">
          <div className="text-gray-500">Đang tải sản phẩm...</div>
        </div>
      </main>
    );
  }

  if (products.length === 0) {
    return null;
  }

  return (
    <main className="max-w-[1200px] mx-auto px-12 py-8">
      <div className="flex justify-between items-end mb-8 border-b border-gray-200 pb-4">
        <h2 className="text-2xl font-bold uppercase border-b-2 border-red-600 -mb-5 pb-4">Sản phẩm mới nhất</h2>
        <button onClick={() => navigate('/?view=category')} className="text-red-600 font-medium hover:underline">Xem tất cả <i className="fas fa-arrow-right text-xs"></i></button>
      </div>
      <div className="latest-products-grid">
        {products.map(product => (
          <div
            key={product.id}
            className="latest-product-card"
            onClick={() => handleProductClick(product.id)}
          >
            <div className="product-image-wrapper">
              <img
                src={product.image || 'https://via.placeholder.com/300x300?text=No+Image'}
                alt={product.name}
                className="product-image"
              />
            </div>
            <div className="product-info">
              <h3 className="product-name">{product.name}</h3>
              {product.price && (
                <p className="product-price">{formatCurrency(product.price)}</p>
              )}
            </div>
          </div>
        ))}
      </div>
    </main>
  );
};

export default ProductGrid;
