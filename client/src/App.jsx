import { useState, useEffect } from 'react';
import { BrowserRouter, useNavigate, useSearchParams, useLocation } from 'react-router-dom';
import './App.css';
import Cart from './components/Cart';
import Topbar from './components/Topbar';
import Header from './components/Header';
import Hero from './components/Hero';
import HomeCategories from './components/HomeCategories';
import ProductGrid from './components/ProductGrid';
import CategoryView from './components/CategoryView';
import ProductDetail from './components/ProductDetail';
import UserProfile from './components/UserProfile';
import Favorites from './components/Favorites';
import Login from './components/Login';
import Footer from './components/Footer';
import Toast from './components/Toast';
import { authStorage } from './services/api';

const products = [
  { id: 1, name: "Nike Air Max 90 Red Edition", price: 1250000, oldPrice: 1500000, img: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1542291026-7eec264c27ff", "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a"] },
  { id: 2, name: "Adidas Forum Low Classic White", price: 1850000, img: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1523275335684-37898b6baf30", "https://images.unsplash.com/photo-1512374382149-4332c6c75d61"] },
  { id: 3, name: "New Balance 530 Grey Matter", price: 2150000, img: "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1549298916-b41d501d3772"] },
  { id: 4, name: "Nike Dunk Low Retro Panda", price: 2450000, img: "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa"] },
  { id: 5, name: "Vans Old Skool Black/White", price: 1100000, img: "https://images.unsplash.com/photo-1595341888016-a392ef81b7de?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1595341888016-a392ef81b7de"] }
];

function AppContent() {
  const [isLoggedIn, setIsLoggedIn] = useState(() => authStorage.isAuthenticated());
  const [toasts, setToasts] = useState([]);
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const location = useLocation();

  // Sync isLoggedIn state with authStorage
  useEffect(() => {
    const checkAuth = () => {
      setIsLoggedIn(authStorage.isAuthenticated());
    };
    checkAuth();
  }, []);

  // Get productId from path /product/:id
  const isProductDetail = location.pathname.startsWith('/product/');
  const productId = isProductDetail 
    ? location.pathname.split('/product/')[1] 
    : searchParams.get('productId');

  // Get view from URL query param
  const currentView = isProductDetail ? 'detail' : (searchParams.get('view') || 'home');
  const categoryName = searchParams.get('category') || '';

  const addToast = (message, type = 'info') => {
    const id = Date.now();
    setToasts(prev => [...prev, { id, message, type }]);
  };

  const removeToast = (id) => {
    setToasts(prev => prev.filter(toast => toast.id !== id));
  };

  const handleLogin = () => {
    setIsLoggedIn(true);
    navigate('/');
  };

  const handleLogout = () => {
    authStorage.clearUser();
    authStorage.removeToken();
    setIsLoggedIn(false);
    navigate('/');
  };

  const viewDetail = (productId) => {
    navigate(`/product/${productId}`);
  };

  const handleProfileClick = () => {
    if (!isLoggedIn) {
      navigate('/?view=login');
    } else {
      navigate('/?view=profile');
    }
  };

  const handleCartClick = () => {
    if (!isLoggedIn) {
      navigate('/?view=login');
    } else {
      navigate('/?view=cart');
    }
  };

  const renderContent = () => {
    switch (currentView) {
      case 'home':
        return (
          <>
            <Hero toggleView={(view, cat) => navigate(`/?view=category&category=${encodeURIComponent(cat)}`)} />
            <HomeCategories toggleView={(view, cat) => navigate(`/?view=category&category=${encodeURIComponent(cat)}`)} />
            <ProductGrid toggleView={(view, cat) => navigate(`/?view=category&category=${encodeURIComponent(cat)}`)} />
          </>
        );
      case 'category':
        return <CategoryView />;
      case 'detail':
        return <ProductDetail productId={productId} toggleView={() => navigate(-1)} addToast={addToast} />;
      case 'profile':
        return <UserProfile onClose={() => navigate('/')} onLogout={handleLogout} isLoggedIn={isLoggedIn} addToast={addToast} />;
      case 'favorites':
        return <Favorites toggleView={(view) => navigate(`/${view}`)} />;
      case 'cart':
        return <Cart isLoggedIn={isLoggedIn} addToast={addToast} />;
      case 'login':
        return <Login toggleView={handleLogin} addToast={addToast} />;
      default:
        return null;
    }
  };

  return (
    <div className="bg-gray-50 text-gray-800">
      <ToastContainer toasts={toasts} removeToast={removeToast} />
      <Topbar toggleView={(view, cat) => navigate(`/?view=category&category=${encodeURIComponent(cat)}`)} />
      <Header currentView={currentView} toggleView={(view, cat) => navigate(`/?view=category&category=${encodeURIComponent(cat)}`)} isLoggedIn={isLoggedIn} onProfileClick={handleProfileClick} onLogout={handleLogout} onCartClick={handleCartClick} />
      {renderContent()}
      <Footer />
    </div>
  );
}

const ToastContainer = ({ toasts, removeToast }) => {
  return (
    <div className="toast-container">
      {toasts.map(toast => (
        <Toast
          key={toast.id}
          message={toast.message}
          type={toast.type}
          onClose={() => removeToast(toast.id)}
        />
      ))}
    </div>
  );
};

function App() {
  return (
    <BrowserRouter>
      <AppContent />
    </BrowserRouter>
  );
}

export default App;
