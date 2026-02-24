import React from 'react';

const Topbar = ({ toggleView }) => {
  return (
    <div className="bg-black text-white text-xs py-2 px-12 flex justify-between items-center hidden md:flex">
      <div>Chào mừng bạn đến với Giày Xshop!</div>
      <div className="flex gap-4">
        <a href="#" className="hover:text-red-500">Hệ thống cửa hàng</a>
        <a href="#" className="hover:text-red-500">Kiểm tra đơn hàng</a>
        <button
          onClick={() => toggleView('login')}
          className="hover:text-red-500 cursor-pointer"
        >
          Đăng nhập
        </button>
      </div>
    </div>
  );
};

export default Topbar;