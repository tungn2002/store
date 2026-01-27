import React from 'react';
import ProductCard from './ProductCard';

const ProductGrid = ({ products, onViewDetail, toggleView }) => {
  return (
    <main className="max-w-[1200px] mx-auto px-12 py-8">
      <div className="flex justify-between items-end mb-8 border-b border-gray-200 pb-4">
        <h2 className="text-2xl font-bold uppercase border-b-2 border-red-600 -mb-5 pb-4">Sản phẩm mới nhất</h2>
        <button onClick={() => toggleView('category', 'Sản phẩm mới')} className="text-red-600 font-medium hover:underline">Xem tất cả <i className="fas fa-arrow-right text-xs"></i></button>
      </div>
      <div className="grid grid-cols-2 md:grid-cols-4 lg:grid-cols-5 gap-6">
        {products.map(product => (
          <ProductCard key={product.id} product={product} onViewDetail={onViewDetail} />
        ))}
      </div>
    </main>
  );
};

export default ProductGrid;