import { useState } from 'react';
import './App.css';
import Topbar from './components/Topbar';
import Header from './components/Header';
import Hero from './components/Hero';
import HomeCategories from './components/HomeCategories';
import ProductGrid from './components/ProductGrid';
import CategoryView from './components/CategoryView';
import ProductDetail from './components/ProductDetail';
import Footer from './components/Footer';

const products = [
  { id: 1, name: "Nike Air Max 90 Red Edition", price: 1250000, oldPrice: 1500000, img: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1542291026-7eec264c27ff", "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa", "https://images.unsplash.com/photo-1595950653106-6c9ebd614d3a"] },
  { id: 2, name: "Adidas Forum Low Classic White", price: 1850000, img: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1523275335684-37898b6baf30", "https://images.unsplash.com/photo-1512374382149-4332c6c75d61"] },
  { id: 3, name: "New Balance 530 Grey Matter", price: 2150000, img: "https://images.unsplash.com/photo-1549298916-b41d501d3772?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1549298916-b41d501d3772"] },
  { id: 4, name: "Nike Dunk Low Retro Panda", price: 2450000, img: "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa"] },
  { id: 5, name: "Vans Old Skool Black/White", price: 1100000, img: "https://images.unsplash.com/photo-1595341888016-a392ef81b7de?auto=format&fit=crop&w=600&q=80", gallery: ["https://images.unsplash.com/photo-1595341888016-a392ef81b7de"] }
];

function App() {
  const [currentView, setCurrentView] = useState('home');
  const [selectedCategory, setSelectedCategory] = useState('');
  const [selectedProduct, setSelectedProduct] = useState(null);

  const toggleView = (view, categoryName = '') => {
    setCurrentView(view);
    if (view === 'category') {
      setSelectedCategory(categoryName);
    } else if (view === 'detail') {
      // Assuming categoryName is productId for detail
      const product = products.find(p => p.id === categoryName);
      setSelectedProduct(product);
    }
    window.scrollTo(0, 0);
  };

  const viewDetail = (productId) => {
    toggleView('detail', productId);
  };

  const renderContent = () => {
    switch (currentView) {
      case 'home':
        return (
          <>
            <Hero toggleView={toggleView} />
            <HomeCategories toggleView={toggleView} />
            <ProductGrid products={products} onViewDetail={viewDetail} toggleView={toggleView} />
          </>
        );
      case 'category':
        return <CategoryView categoryName={selectedCategory} products={products.concat(products)} onViewDetail={viewDetail} toggleView={toggleView} />;
      case 'detail':
        return <ProductDetail product={selectedProduct} toggleView={toggleView} />;
      default:
        return null;
    }
  };

  return (
    <div className="bg-gray-50 text-gray-800">
      <Topbar />
      <Header currentView={currentView} toggleView={toggleView} />
      {renderContent()}
      <Footer />
    </div>
  );
}

export default App;
