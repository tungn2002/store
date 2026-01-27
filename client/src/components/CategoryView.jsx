import React from 'react';
import ProductCard from './ProductCard';

const CategoryView = ({ categoryName, products, onViewDetail, toggleView }) => {
  return (
    <div className="max-w-[1200px] mx-auto px-12 py-8">
      <nav className="text-xs text-gray-500 mb-6 uppercase tracking-wider">
        <a href="javascript:void(0)" onClick={() => toggleView('home')} className="hover:text-red-600">Trang chủ</a>
        <span className="mx-2">/</span>
        <span className="text-gray-800 font-bold">{categoryName}</span>
      </nav>

      <div className="flex flex-col md:flex-row gap-8">
        <aside className="w-full md:w-1/4 lg:w-1/5 space-y-8">
          <div className="filter-section">
            <h4>Danh mục sản phẩm</h4>
            <ul className="text-sm space-y-3 text-gray-600">
              <li><a href="#" className="hover:text-red-600 flex justify-between">Giày Nike <span>(24)</span></a></li>
              <li><a href="#" className="hover:text-red-600 flex justify-between">Giày Adidas <span>(18)</span></a></li>
              <li><a href="#" className="hover:text-red-600 flex justify-between">Giày Jordan <span>(12)</span></a></li>
            </ul>
          </div>
          <div className="filter-section">
            <h4>Lọc theo giá</h4>
            <div className="space-y-4">
              <input type="range" className="w-full accent-red-600" min="0" max="5000000" step="100000" />
              <div className="flex justify-between text-xs font-bold"><span>0₫</span><span>5,000,000₫</span></div>
              <button className="w-full bg-black text-white py-2 text-xs font-bold uppercase hover:bg-red-600 transition">Lọc giá</button>
            </div>
          </div>
        </aside>

        <div className="flex-1">
          <div className="flex justify-between items-center mb-6 bg-white p-4 shadow-sm rounded">
            <h1 className="text-xl font-bold uppercase">{categoryName}</h1>
          </div>
          <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
            {products.map(product => (
              <ProductCard key={product.id} product={product} onViewDetail={onViewDetail} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CategoryView;