import React from 'react';

const Hero = ({ toggleView }) => {
  return (
    <section className="relative h-[300px] md:h-[550px] bg-gray-200 overflow-hidden">
      <div className="absolute inset-0 bg-cover bg-center" style={{ backgroundImage: "url('https://images.unsplash.com/photo-1556906781-9a412961c28c?ixlib=rb-4.0.3&auto=format&fit=crop&w=1920&q=80')" }}>
        <div className="absolute inset-0 bg-black bg-opacity-30"></div>
      </div>
      <div className="relative max-w-[1200px] mx-auto h-full flex flex-col justify-center px-12 text-white">
        <h2 className="text-2xl md:text-5xl font-bold mb-4 uppercase">New Collection 2024</h2>
        <p className="text-lg mb-8 max-w-lg">Khám phá những mẫu Sneaker mới nhất, dẫn đầu xu hướng thời trang thế giới tại Giày Xshop.</p>
        <div>
          <button onClick={() => toggleView('category', 'Bộ sưu tập mới')} className="bg-red-600 hover:bg-red-700 text-white px-8 py-3 rounded-md font-bold transition inline-block">MUA NGAY</button>
        </div>
      </div>
    </section>
  );
};

export default Hero;