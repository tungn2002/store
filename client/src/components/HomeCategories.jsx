import React from 'react';

const HomeCategories = ({ toggleView }) => {
  return (
    <section className="py-12 bg-white">
      <div className="max-w-[1200px] mx-auto px-12 grid grid-cols-2 md:grid-cols-4 gap-4">
        <div onClick={() => toggleView('category', 'Giày Nam')} className="relative group cursor-pointer overflow-hidden rounded-lg">
          <img src="https://images.unsplash.com/photo-1600185365483-26d7a4cc7519?auto=format&fit=crop&w=400&q=80" alt="Giày Nam" className="w-full h-48 object-cover group-hover:scale-110 transition duration-500" />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-20">
            <span className="bg-white text-black px-4 py-2 font-bold text-sm uppercase">Giày Nam</span>
          </div>
        </div>
        <div onClick={() => toggleView('category', 'Giày Nữ')} className="relative group cursor-pointer overflow-hidden rounded-lg">
          <img src="https://images.unsplash.com/photo-1543163521-1bf539c55dd2?auto=format&fit=crop&w=400&q=80" alt="Giày Nữ" className="w-full h-48 object-cover group-hover:scale-110 transition duration-500" />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-20">
            <span className="bg-white text-black px-4 py-2 font-bold text-sm uppercase">Giày Nữ</span>
          </div>
        </div>
        <div onClick={() => toggleView('category', 'Sale Off')} className="relative group cursor-pointer overflow-hidden rounded-lg">
          <img src="https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a?auto=format&fit=crop&w=400&q=80" alt="Giày Sale" className="w-full h-48 object-cover group-hover:scale-110 transition duration-500" />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-20">
            <span className="bg-white text-black px-4 py-2 font-bold text-sm uppercase">Giày Sale</span>
          </div>
        </div>
        <div onClick={() => toggleView('category', 'Phụ kiện')} className="relative group cursor-pointer overflow-hidden rounded-lg">
          <img src="https://images.unsplash.com/photo-1562183241-b937e95585b6?auto=format&fit=crop&w=400&q=80" alt="Phụ kiện" className="w-full h-48 object-cover group-hover:scale-110 transition duration-500" />
          <div className="absolute inset-0 flex items-center justify-center bg-black bg-opacity-20">
            <span className="bg-white text-black px-4 py-2 font-bold text-sm uppercase">Phụ kiện</span>
          </div>
        </div>
      </div>
    </section>
  );
};

export default HomeCategories;