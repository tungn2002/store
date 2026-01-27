import React from 'react';

const ProductCard = ({ product, onViewDetail }) => {
  return (
    <div className="product-card bg-white rounded shadow-sm border border-transparent hover:border-gray-200 transition relative cursor-pointer" onClick={() => onViewDetail(product.id)}>
      <div className="relative overflow-hidden group">
        <img src={product.img} alt={product.name} className="w-full aspect-square object-cover" />
        <div className="product-action absolute bottom-0 left-0 right-0 bg-black bg-opacity-70 flex justify-center py-2 space-x-4 opacity-0 transform translate-y-4 transition-all duration-300">
          <button className="text-white hover:text-red-500 bg-transparent border-none"><i className="fas fa-cart-plus"></i></button>
          <button className="text-white hover:text-red-500 bg-transparent border-none"><i className="far fa-heart"></i></button>
        </div>
      </div>
      <div className="p-3 text-center">
        <h3 className="text-sm font-medium mb-2 truncate px-2">{product.name}</h3>
        <div className="flex justify-center items-center gap-2">
          <span className="text-red-600 font-bold">{product.price.toLocaleString()}₫</span>
          {product.oldPrice && <span className="text-gray-400 text-xs line-through">{product.oldPrice.toLocaleString()}₫</span>}
        </div>
      </div>
    </div>
  );
};

export default ProductCard;