import React, { useState, useEffect } from 'react';
import { productAPI, cartAPI, authStorage } from '../services/api';

const ProductDetail = ({ productId, toggleView, addToast }) => {
  const [product, setProduct] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [selectedColor, setSelectedColor] = useState(null);
  const [selectedSize, setSelectedSize] = useState(null);
  const [mainImage, setMainImage] = useState(null);
  const [addingToCart, setAddingToCart] = useState(false);

  useEffect(() => {
    const fetchProductDetail = async () => {
      try {
        setLoading(true);
        const response = await productAPI.getProductDetail(productId);
        const productData = response.result;
        
        setProduct(productData);
        
        // Set defaults from first variant
        if (productData.colors && productData.colors.length > 0) {
          setSelectedColor(productData.colors[0]);
        }
        if (productData.sizes && productData.sizes.length > 0) {
          setSelectedSize(productData.sizes[0]);
        }
        setMainImage(productData.image || (productData.variantImages && productData.variantImages[0]));
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchProductDetail();
  }, [productId]);

  const handleAddToCart = async () => {
    // Check login
    if (!authStorage.isAuthenticated()) {
      addToast('Vui lòng đăng nhập để thêm vào giỏ hàng', 'warning');
      toggleView('login');
      return;
    }

    // Check selected color and size
    if (!selectedColor || !selectedSize) {
      addToast('Vui lòng chọn màu và size', 'warning');
      return;
    }

    try {
      setAddingToCart(true);
      
      // Find the variant ID for selected color and size
      const variant = product.prices?.find(
        p => p.color === selectedColor && p.size === selectedSize
      );

      console.log('Selected variant:', variant);
      console.log('Product prices:', product.prices);

      if (!variant) {
        addToast('Kết hợp màu và size này không có sẵn', 'error');
        return;
      }

      // Check if productVariantId exists
      if (!variant.productVariantId) {
        addToast('Dữ liệu sản phẩm không hợp lệ', 'error');
        return;
      }

      // Check stock
      if (variant.stock <= 0) {
        addToast('Size này đã hết hàng', 'error');
        return;
      }

      console.log('Adding to cart:', { productVariantId: variant.productVariantId, quantity: 1 });
      await cartAPI.addToCart(variant.productVariantId, 1);
      addToast('Đã thêm vào giỏ hàng!', 'success');
    } catch (err) {
      console.error('Add to cart error:', err);
      if (err.message?.includes('already in your cart')) {
        addToast('Sản phẩm này đã có trong giỏ hàng', 'warning');
      } else if (err.message?.includes('out of stock')) {
        addToast('Sản phẩm đã hết hàng', 'error');
      } else if (err.message?.includes('exceeds stock')) {
        addToast('Số lượng vượt quá tồn kho', 'error');
      } else {
        addToast(err.message || 'Không thể thêm vào giỏ hàng. Vui lòng thử lại!', 'error');
      }
    } finally {
      setAddingToCart(false);
    }
  };

  const changeMainImg = (src) => {
    setMainImage(src);
  };

  const selectSize = (size) => {
    setSelectedSize(size);
  };

  const selectColor = (color) => {
    setSelectedColor(color);
    // Reset size when color changes, will auto-select first available size
    setSelectedSize(null);
  };

  // Get available sizes for selected color
  const getAvailableSizesForColor = (color) => {
    if (!product || !product.prices || !color) return product?.sizes || [];
    
    // Filter sizes that have stock for this color
    const availableSizes = product.prices
      .filter(p => p.color === color && p.stock > 0)
      .map(p => p.size);
    
    return [...new Set(availableSizes)];
  };

  // Auto-select first available size when color changes
  useEffect(() => {
    if (selectedColor && !selectedSize && product?.sizes) {
      const availableSizes = getAvailableSizesForColor(selectedColor);
      if (availableSizes.length > 0) {
        // Select first available size that's in the product.sizes list
        const firstValidSize = product.sizes.find(s => availableSizes.includes(s));
        if (firstValidSize) {
          setSelectedSize(firstValidSize);
        }
      }
    }
  }, [selectedColor, product]);

  const getVariantStock = (color, size) => {
    if (!product || !product.prices) return 0;
    const variant = product.prices.find(p => p.color === color && p.size === size);
    return variant ? variant.stock : 0;
  };

  const getVariantPrice = (color, size) => {
    if (!product || !product.prices) return product?.price;
    const variant = product.prices.find(p => p.color === color && p.size === size);
    return variant ? variant.price : product?.price;
  };

  // Collect all unique images (product image + variant images)
  const getAllImages = () => {
    const images = [];
    if (product?.image) images.push(product.image);
    if (product?.variantImages) {
      product.variantImages.forEach(img => {
        if (!images.includes(img)) images.push(img);
      });
    }
    return images;
  };

  if (loading) {
    return (
      <div className="max-w-[1200px] mx-auto px-12 py-8 flex items-center justify-center min-h-[400px]">
        <div className="text-center">
          <div className="animate-spin rounded-full h-16 w-16 border-b-2 border-red-600 mx-auto mb-4"></div>
          <p className="text-gray-500">Đang tải thông tin sản phẩm...</p>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-[1200px] mx-auto px-12 py-8">
        <div className="text-center text-red-600">
          <h2 className="text-2xl font-bold mb-2">Không tìm thấy sản phẩm</h2>
          <p>{error || 'Sản phẩm không tồn tại'}</p>
          <button 
            onClick={() => toggleView('home')}
            className="mt-4 px-6 py-2 bg-red-600 text-white rounded hover:bg-red-700"
          >
            Quay lại trang chủ
          </button>
        </div>
      </div>
    );
  }

  const allImages = getAllImages();

  return (
    <div className="max-w-[1200px] mx-auto px-12 py-8 animate-fade-in">
      {/* Breadcrumb */}
      <nav className="text-xs text-gray-500 mb-8 uppercase tracking-wider">
        <a href="#" onClick={(e) => { e.preventDefault(); toggleView('home'); }} className="hover:text-red-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <a href="#" onClick={(e) => { e.preventDefault(); toggleView('category', product?.category?.name || 'Sản phẩm'); }} className="hover:text-red-600">{product?.category?.name || 'Danh mục'}</a>
        <span className="mx-2">/</span>
        <span className="text-gray-800 font-bold">{product.name}</span>
      </nav>

      <div className="flex flex-col lg:flex-row gap-12 bg-white p-6 rounded-lg shadow-sm mb-12">
        {/* Left: Gallery */}
        <div className="w-full lg:w-1/2 flex flex-col gap-4">
          <div className="border border-gray-100 rounded-lg overflow-hidden">
            <img 
              id="main-product-img" 
              src={mainImage} 
              alt={product.name} 
              className="w-full h-auto object-cover aspect-square transition-opacity duration-300" 
            />
          </div>
          <div className="flex gap-4 flex-wrap">
            {allImages.map((img, index) => (
              <div
                key={index}
                className={`w-20 h-20 border-2 ${mainImage === img ? 'thumb-active border-red-600' : 'border-gray-100'} rounded overflow-hidden cursor-pointer hover:border-red-600 transition`}
                onClick={() => changeMainImg(img)}
              >
                <img src={img} className="w-full h-full object-cover" />
              </div>
            ))}
          </div>
        </div>

        {/* Right: Info & Selectors */}
        <div className="w-full lg:w-1/2 space-y-6">
          <div>
            <h1 className="text-2xl md:text-3xl font-bold text-gray-900 mb-2 uppercase">{product.name}</h1>
            <div className="flex items-center gap-4">
              <span className="text-3xl font-bold text-red-600">
                {(getVariantPrice(selectedColor, selectedSize) || product.price || 0).toLocaleString()}₫
              </span>
              {product.oldPrice && <span className="text-lg text-gray-400 line-through">{product.oldPrice.toLocaleString()}₫</span>}
            </div>
            {product.totalStock > 0 ? (
              <p className="text-sm text-green-600 mt-2">
                <i className="fas fa-check-circle mr-1"></i>
                Còn hàng: {product.totalStock} sản phẩm
              </p>
            ) : (
              <p className="text-sm text-red-600 mt-2">
                <i className="fas fa-times-circle mr-1"></i>
                Hết hàng
              </p>
            )}
          </div>

          {/* Color Selector */}
          {product.colors && product.colors.length > 0 && (
            <div>
              <p className="text-sm font-bold uppercase mb-3">
                Màu sắc: <span className="text-red-600 ml-1">{selectedColor || 'Chưa chọn'}</span>
              </p>
              <div className="flex gap-3 flex-wrap">
                {product.colors.map((color) => (
                  <button
                    key={color}
                    onClick={() => selectColor(color)}
                    className={`px-4 py-2 border-2 rounded-md text-sm font-medium transition ${
                      selectedColor === color
                        ? 'border-red-600 bg-red-50 text-red-600'
                        : 'border-gray-200 hover:border-red-600'
                    }`}
                  >
                    {color}
                  </button>
                ))}
              </div>
            </div>
          )}

          {/* Size Selector */}
          {product.sizes && product.sizes.length > 0 && selectedColor && (
            <div>
              <p className="text-sm font-bold uppercase mb-3">Chọn Kích cỡ (Size):</p>
              <div className="grid grid-cols-4 md:grid-cols-6 gap-2">
                {product.sizes.map((size) => {
                  const stock = getVariantStock(selectedColor, size);
                  const isOutOfStock = stock === 0;
                  return (
                    <button
                      key={size}
                      onClick={() => !isOutOfStock && selectSize(size)}
                      disabled={isOutOfStock}
                      className={`border py-2 text-sm transition ${
                        isOutOfStock
                          ? 'border-gray-100 text-gray-300 cursor-not-allowed bg-gray-50'
                          : selectedSize === size
                          ? 'border-red-600 bg-red-50 text-red-600 font-bold'
                          : 'border-gray-200 hover:border-red-600'
                      }`}
                    >
                      {size}
                      {isOutOfStock && <span className="block text-[10px]">Hết</span>}
                    </button>
                  );
                })}
              </div>
            </div>
          )}

          {!selectedColor && (
            <p className="text-sm text-gray-500 italic">Vui lòng chọn màu trước khi chọn size</p>
          )}

          <div className="border-t border-b py-4">
            <p className="text-sm text-gray-600 mb-2">{product.description || 'Không có mô tả chi tiết'}</p>
          </div>

          <div className="flex flex-col sm:flex-row gap-4">
            <button 
              onClick={handleAddToCart}
              disabled={addingToCart || product.totalStock === 0}
              className="flex-1 bg-red-600 hover:bg-red-700 text-white font-bold py-4 rounded-md uppercase tracking-wider transition shadow-lg disabled:bg-gray-300 disabled:cursor-not-allowed flex items-center justify-center gap-2"
            >
              <i className="fas fa-shopping-cart"></i>
              {addingToCart ? 'ĐANG THÊM...' : (product.totalStock > 0 ? 'THÊM VÀO GIỎ HÀNG' : 'HẾT HÀNG')}
            </button>
          </div>
        </div>
      </div>

      {/* Description Section */}
      <div className="bg-white p-6 md:p-10 rounded-lg shadow-sm">
        {/* Brand and Category */}
        <div className="flex gap-6 text-sm mb-6 pb-4 border-b border-gray-200">
          {product.brand && (
            <div className="flex items-center gap-2">
              <span className="text-gray-500">Thương hiệu:</span>
              <span className="font-semibold text-gray-800">{product.brand.name}</span>
            </div>
          )}
          {product.category && (
            <div className="flex items-center gap-2">
              <span className="text-gray-500">Danh mục:</span>
              <span className="font-semibold text-gray-800">{product.category.name}</span>
            </div>
          )}
        </div>
        <div className="border-b border-gray-200 mb-8">
          <h2 className="text-xl font-bold uppercase border-b-2 border-black inline-block pb-4">Mô tả sản phẩm</h2>
        </div>
        <div className="prose max-w-none text-gray-600 leading-relaxed">
          <p>{product.description || 'Không có mô tả chi tiết cho sản phẩm này.'}</p>
        </div>
      </div>
    </div>
  );
};

export default ProductDetail;
