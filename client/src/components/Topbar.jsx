import React from 'react';

const Topbar = ({ toggleView }) => {
  return (
    <div className="bg-black text-white text-xs py-2 px-12 flex justify-between items-center hidden md:flex">
      <div>Chào mừng bạn đến với ZFashion!</div>
      <div className="flex gap-4">
        <a href="#" className="hover:text-red-500">Hệ thống cửa hàng</a>
      </div>
    </div>
  );
};

export default Topbar;