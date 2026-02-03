import React, { useState, useEffect } from 'react';

const Favorites = ({ toggleView }) => {
  // Sample favorite products - in a real app this would come from state/context/localStorage
  const [favoriteProducts, setFavoriteProducts] = useState([
    {
      id: 1,
      name: "Nike Air Max 90 Red Edition",
      price: 1250000,
      oldPrice: 1500000,
      img: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
      rating: 4.5
    },
    {
      id: 2,
      name: "Adidas Forum Low Classic White",
      price: 1850000,
      oldPrice: 2000000,
      img: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
      rating: 4.2
    },
    {
      id: 4,
      name: "Nike Dunk Low Retro Panda",
      price: 2450000,
      oldPrice: 2600000,
      img: "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80",
      rating: 4.8
    }
  ]);

  // Function to handle removing a product from favorites
  const removeFromFavorites = (productId) => {
    setFavoriteProducts(prev => prev.filter(product => product.id !== productId));
  };

  // Function to handle viewing product details
  const viewProductDetail = (productId) => {
    toggleView('detail', productId);
  };

  // Render star ratings
  const renderRating = (rating) => {
    const stars = [];
    const fullStars = Math.floor(rating);
    const hasHalfStar = rating % 1 >= 0.5;

    for (let i = 0; i < fullStars; i++) {
      stars.push(<span key={i} className="text-yellow-400">★</span>);
    }

    if (hasHalfStar) {
      stars.push(<span key="half" className="text-yellow-400">☆</span>);
    }

    const emptyStars = 5 - fullStars - (hasHalfStar ? 1 : 0);
    for (let i = 0; i < emptyStars; i++) {
      stars.push(<span key={`empty-${i}`} className="text-gray-300">★</span>);
    }

    return <div className="flex">{stars}</div>;
  };

  return (
    <div className="max-w-[1200px] mx-auto px-12 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-2xl font-bold uppercase">Sản phẩm yêu thích</h1>
        <button
          onClick={() => toggleView('home')}
          className="text-red-600 hover:text-red-800 flex items-center"
        >
          ← Quay lại trang chủ
        </button>
      </div>

      {favoriteProducts.length === 0 ? (
        <div className="text-center py-12">
          <div className="text-5xl mb-4">❤️</div>
          <h2 className="text-xl font-semibold mb-2">Danh sách yêu thích trống</h2>
          <p className="text-gray-600 mb-6">Bạn chưa thêm sản phẩm nào vào danh sách yêu thích</p>
          <button
            onClick={() => toggleView('home')}
            className="bg-red-600 text-white px-6 py-3 rounded-md hover:bg-red-700 transition-colors"
          >
            Mua sắm ngay
          </button>
        </div>
      ) : (
        <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
          {favoriteProducts.map((product) => (
            <div key={product.id} className="product-card bg-white rounded shadow-sm border border-transparent hover:border-gray-200 transition relative cursor-pointer" onClick={() => viewProductDetail(product.id)}>
              <div className="relative overflow-hidden group">
                <img src={product.img} alt={product.name} className="w-full aspect-square object-cover" />
                <div className="product-action absolute bottom-0 left-0 right-0 bg-black bg-opacity-70 flex justify-center py-2 space-x-4 opacity-0 transform translate-y-4 transition-all duration-300 group-hover:opacity-100 group-hover:translate-y-0">
                  <button
                    className="text-white hover:text-red-500 bg-transparent border-none"
                    onClick={(e) => {
                      e.stopPropagation();
                      // Add to cart functionality would go here
                    }}
                  >
                    <i className="fas fa-cart-plus"></i>
                  </button>
                  <button
                    className="text-white hover:text-red-500 bg-transparent border-none"
                    onClick={(e) => {
                      e.stopPropagation();
                      removeFromFavorites(product.id);
                    }}
                    title="Xóa khỏi yêu thích"
                  >
                    <i className="fas fa-heart"></i>
                  </button>
                </div>
              </div>
              <div className="p-3 text-center">
                <h3 className="text-sm font-medium mb-2 truncate px-2">{product.name}</h3>
                <div className="flex justify-center items-center gap-2">
                  <span className="text-red-600 font-bold">{product.price.toLocaleString('vi-VN')}₫</span>
                  {product.oldPrice && <span className="text-gray-400 text-xs line-through">{product.oldPrice.toLocaleString('vi-VN')}₫</span>}
                </div>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default Favorites;