import React from 'react';

const Footer = () => {
  return (
    <>
      <section className="bg-gray-100 py-10 mt-12 border-y border-gray-200">
        <div className="max-w-[1200px] mx-auto px-12 grid grid-cols-1 md:grid-cols-4 gap-8">
          <div className="flex items-center gap-4"><i className="fas fa-truck text-3xl text-red-600"></i><div><h4 className="font-bold uppercase text-sm">Giao hàng miễn phí</h4><p className="text-xs text-gray-500">Đơn hàng trên 2 triệu</p></div></div>
          <div className="flex items-center gap-4"><i className="fas fa-undo text-3xl text-red-600"></i><div><h4 className="font-bold uppercase text-sm">Đổi trả 7 ngày</h4><p className="text-xs text-gray-500">Hỗ trợ đổi size nhanh</p></div></div>
          <div className="flex items-center gap-4"><i className="fas fa-shield-alt text-3xl text-red-600"></i><div><h4 className="font-bold uppercase text-sm">Bảo hành 6 tháng</h4><p className="text-xs text-gray-500">Keo chỉ trọn đời</p></div></div>
          <div className="flex items-center gap-4"><i className="fas fa-headset text-3xl text-red-600"></i><div><h4 className="font-bold uppercase text-sm">Hỗ trợ 24/7</h4><p className="text-xs text-gray-500">0999.888.XXX</p></div></div>
        </div>
      </section>

      <footer className="bg-white pt-12 pb-6 border-t border-gray-200">
        <div className="max-w-[1200px] mx-auto px-12 grid grid-cols-1 md:grid-cols-4 gap-12 mb-10 text-sm">
          <div><div className="text-2xl font-bold text-black mb-6">GIAY<span className="text-red-600">X</span>SHOP</div><p>Hệ thống giày Sneaker hàng đầu Việt Nam.</p></div>
          <div><h4 className="font-bold uppercase mb-6">Chính sách</h4><ul className="space-y-3"><li>Bảo mật</li><li>Đổi trả</li></ul></div>
          <div><h4 className="font-bold uppercase mb-6">Hỗ trợ</h4><ul className="space-y-3"><li>Hướng dẫn chọn size</li><li>Hệ thống cửa hàng</li></ul></div>
          <div><h4 className="font-bold uppercase mb-6">Đăng ký</h4><div className="flex"><input type="text" placeholder="Email..." className="bg-gray-100 px-4 py-2 w-full" /><button className="bg-black text-white px-4 py-2 font-bold">GỬI</button></div></div>
        </div>
        <p className="text-center text-xs text-gray-400">&copy; 2024 Giày Xshop. All rights reserved.</p>
      </footer>
    </>
  );
};

export default Footer;