import React from 'react';

const Header = ({ currentView, toggleView }) => {
  return (
    <header className="bg-white shadow-sm sticky-header">
      <div className="px-12 py-4 flex justify-between items-center">
        <button className="md:hidden text-2xl" onClick={() => toggleView('home')}><i className="fas fa-bars"></i></button>

        <div className="text-3xl font-bold tracking-tighter text-black cursor-pointer" onClick={() => toggleView('home')}>
          GIAY<span className="text-red-600">X</span>SHOP
        </div>

        <nav className="hidden md:flex space-x-8 font-medium uppercase text-sm">
          <a href="javascript:void(0)" onClick={() => toggleView('home')} className={`nav-item hover:text-red-600 transition ${currentView === 'home' ? 'text-red-600 border-b-2 border-red-600' : ''}`}>Trang chủ</a>
          <a href="javascript:void(0)" onClick={() => toggleView('category', 'Giày Nam')} className="nav-item hover:text-red-600 transition">Giày Nam</a>
          <a href="javascript:void(0)" onClick={() => toggleView('category', 'Giày Nữ')} className="nav-item hover:text-red-600 transition">Giày Nữ</a>
          <a href="javascript:void(0)" onClick={() => toggleView('category', 'Phụ kiện')} className="nav-item hover:text-red-600 transition">Phụ kiện</a>
          <a href="javascript:void(0)" className="hover:text-red-600 transition">Tin tức</a>
          <a href="javascript:void(0)" onClick={() => toggleView('category', 'Sale Off')} className="hover:text-red-600 transition text-red-500 font-bold">Sale Off</a>
        </nav>

        <div className="flex items-center space-x-5 text-xl">
          <a href="#" className="hover:text-red-600"><i className="fas fa-search"></i></a>
          <a href="javascript:void(0)" onClick={() => toggleView('profile')} className="hover:text-red-600 hidden md:inline cursor-pointer"><i className="far fa-user"></i></a>
          <a href="javascript:void(0)" onClick={() => toggleView('favorites')} className="hover:text-red-600 hidden md:inline cursor-pointer"><i className="far fa-heart"></i></a>
          <a href="javascript:void(0)" onClick={() => toggleView('cart')} className="relative hover:text-red-600 cursor-pointer">
            <i className="fas fa-shopping-cart"></i>
            <span className="absolute -top-2 -right-2 bg-red-600 text-white text-[10px] rounded-full w-4 h-4 flex items-center justify-center">2</span>
          </a>
        </div>
      </div>
    </header>
  );
};

export default Header;