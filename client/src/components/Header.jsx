import React, { useState, useRef, useEffect } from 'react';
import { authStorage } from '../services/api';

const Header = ({ currentView, toggleView, isLoggedIn, onProfileClick, onLogout, onCartClick }) => {
  const [showUserMenu, setShowUserMenu] = useState(false);
  const menuRef = useRef(null);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setShowUserMenu(false);
      }
    };

    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  const handleUserClick = () => {
    if (!authStorage.isAuthenticated()) {
      // Chưa đăng nhập → mở login
      toggleView('login');
    } else {
      // Đã đăng nhập → hiện menu
      setShowUserMenu(!showUserMenu);
    }
  };

  const handleProfileClick = () => {
    onProfileClick();
    setShowUserMenu(false);
  };

  const handleLogoutClick = () => {
    onLogout();
    setShowUserMenu(false);
  };
  return (
    <header className="bg-white shadow-sm sticky-header">
      <div className="px-12 py-4 flex justify-between items-center">
        <button className="md:hidden text-2xl" onClick={() => toggleView('home')}><i className="fas fa-bars"></i></button>

        <div className="text-3xl font-bold tracking-tighter text-black cursor-pointer" onClick={() => toggleView('home')}>
          <span className="text-red-600">ZF</span>ASHION
        </div>

        <nav className="hidden md:flex space-x-8 font-medium uppercase text-sm">
          <a href="#" onClick={(e) => { e.preventDefault(); toggleView('home'); }} className={`nav-item hover:text-red-600 transition ${currentView === 'home' ? 'text-red-600 border-b-2 border-red-600' : ''}`}>Trang chủ</a>
          <a href="#" onClick={(e) => { e.preventDefault(); toggleView('category', 'Sản phẩm'); }} className="nav-item hover:text-red-600 transition">Sản phẩm</a>
          <a href="#" onClick={(e) => { e.preventDefault(); toggleView('category', 'Dịch vụ'); }} className="nav-item hover:text-red-600 transition">Dịch vụ</a>
          <a href="#" className="hover:text-red-600 transition">Tin tức</a>
          <a href="#" onClick={(e) => { e.preventDefault(); toggleView('category', 'Sale Off'); }} className="hover:text-red-600 transition text-red-500 font-bold">Sale Off</a>
        </nav>

        <div className="flex items-center space-x-5 text-xl">
          <a href="#" className="hover:text-red-600"><i className="fas fa-search"></i></a>
          <div className="relative" ref={menuRef}>
            <a
              href="#"
              onClick={(e) => { e.preventDefault(); handleUserClick(); }}
              className="hover:text-red-600 hidden md:inline cursor-pointer"
            >
              <i className="far fa-user"></i>
            </a>
            {showUserMenu && (
              <div className="user-dropdown-menu">
                <button onClick={handleProfileClick}>
                  <i className="fas fa-user"></i> Thông tin cá nhân
                </button>
                <button onClick={handleLogoutClick} className="logout-item">
                  <i className="fas fa-sign-out-alt"></i> Đăng xuất
                </button>
              </div>
            )}
          </div>
          <a href="#" onClick={(e) => { e.preventDefault(); onCartClick(); }} className="relative hover:text-red-600 cursor-pointer">
            <i className="fas fa-shopping-cart"></i>
            <span className="absolute -top-2 -right-2 bg-red-600 text-white text-[10px] rounded-full w-4 h-4 flex items-center justify-center">2</span>
          </a>
        </div>
      </div>
    </header>
  );
};

export default Header;