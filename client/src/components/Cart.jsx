import React, { useState } from 'react';
import './Cart.css';

const Cart = ({ toggleView }) => {
  // Sample cart data - in a real app this would come from state/context
  const [cartItems, setCartItems] = useState([
    {
      id: 1,
      name: "Nike Air Max 90 Red Edition",
      price: 1250000,
      quantity: 1,
      size: "42",
      color: "Red",
      img: "https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=600&q=80",
      selected: true
    },
    {
      id: 2,
      name: "Adidas Forum Low Classic White",
      price: 1850000,
      quantity: 2,
      size: "40",
      color: "White",
      img: "https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=600&q=80",
      selected: true
    },
    {
      id: 4,
      name: "Nike Dunk Low Retro Panda",
      price: 2450000,
      quantity: 1,
      size: "43",
      color: "Black/White",
      img: "https://images.unsplash.com/photo-1606107557195-0e29a4b5b4aa?auto=format&fit=crop&w=600&q=80",
      selected: false
    }
  ]);

  const [showSizeColorPopup, setShowSizeColorPopup] = useState(null);
  const [selectedSizeColor, setSelectedSizeColor] = useState({});

  // Calculate total
  const selectedItems = cartItems.filter(item => item.selected);
  const subtotal = selectedItems.reduce((sum, item) => sum + (item.price * item.quantity), 0);
  const shipping = 30000; // Fixed shipping cost
  const total = subtotal + shipping;

  // Handle quantity changes
  const updateQuantity = (id, newQuantity) => {
    if (newQuantity < 1) return; // Prevent quantities less than 1

    setCartItems(items =>
      items.map(item =>
        item.id === id ? { ...item, quantity: newQuantity } : item
      )
    );
  };

  // Toggle item selection
  const toggleItemSelection = (id) => {
    setCartItems(items =>
      items.map(item =>
        item.id === id ? { ...item, selected: !item.selected } : item
      )
    );
  };

  // Remove item from cart
  const removeItem = (id) => {
    setCartItems(items => items.filter(item => item.id !== id));
  };

  // Toggle all items selection
  const toggleSelectAll = () => {
    const allSelected = selectedItems.length === cartItems.length;
    setCartItems(items =>
      items.map(item => ({ ...item, selected: !allSelected }))
    );
  };

  // Open size/color popup
  const openSizeColorPopup = (item) => {
    setSelectedSizeColor({
      id: item.id,
      size: item.size,
      color: item.color
    });
    setShowSizeColorPopup(true);
  };

  // Save size/color changes
  const saveSizeColorChanges = () => {
    setCartItems(items =>
      items.map(item =>
        item.id === selectedSizeColor.id
          ? { ...item, size: selectedSizeColor.size, color: selectedSizeColor.color }
          : item
      )
    );
    setShowSizeColorPopup(false);
  };

  // Available sizes and colors for selection
  const sizes = ["36", "37", "38", "39", "40", "41", "42", "43", "44", "45"];
  const colors = ["Red", "Blue", "Green", "Black", "White", "Yellow", "Purple", "Orange"];

  // Format currency function
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  return (
    <div className="max-w-[95%] mx-auto px-4 md:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Giỏ hàng</h1>
        <button
          onClick={() => toggleView('home')}
          className="text-gray-600 hover:text-gray-900"
        >
          ← Quay lại
        </button>
      </div>

      {cartItems.length === 0 ? (
        <div className="text-center py-12">
          <p className="text-xl text-gray-600">Giỏ hàng của bạn đang trống</p>
          <button
            onClick={() => toggleView('home')}
            className="mt-4 bg-red-600 text-white px-6 py-3 rounded-md hover:bg-red-700 transition-colors"
          >
            Mua sắm ngay
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          {/* Cart header with select all */}
          <div className="border-b p-4 flex items-center">
            <input
              type="checkbox"
              checked={selectedItems.length === cartItems.length && cartItems.length > 0}
              onChange={toggleSelectAll}
              className="mr-4 h-5 w-5 text-red-600 rounded focus:ring-red-500"
            />
            <span className="font-semibold">Sản phẩm</span>
          </div>

          {/* Cart items */}
          <div className="divide-y">
            {cartItems.map((item) => (
              <div key={item.id} className="p-4 flex flex-col md:flex-row items-start">
                <input
                  type="checkbox"
                  checked={item.selected}
                  onChange={() => toggleItemSelection(item.id)}
                  className="mr-4 mt-1 h-5 w-5 text-red-600 rounded focus:ring-red-500"
                />

                <div className="flex-1 flex flex-col md:flex-row">
                  <img
                    src={item.img}
                    alt={item.name}
                    className="w-24 h-24 object-cover rounded-md mb-4 md:mb-0 md:mr-6"
                  />

                  <div className="flex-1">
                    <h3 className="font-semibold text-lg">{item.name}</h3>
                    <div className="flex flex-wrap items-center mt-2 gap-2">
                      <span className="bg-gray-100 px-3 py-1 rounded-full text-sm">
                        Size: {item.size}
                      </span>
                      <span className="bg-gray-100 px-3 py-1 rounded-full text-sm">
                        Color: {item.color}
                      </span>
                      <button
                        onClick={() => openSizeColorPopup(item)}
                        className="text-blue-600 hover:text-blue-800 text-sm underline"
                      >
                        Thay đổi
                      </button>
                    </div>

                    <div className="mt-4 flex items-center">
                      <div className="flex items-center border border-gray-300 rounded-md">
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity - 1)}
                          className="px-3 py-1 text-gray-600 hover:bg-gray-100 disabled:opacity-50"
                          disabled={item.quantity <= 1}
                        >
                          -
                        </button>
                        <span className="px-4 py-1 min-w-[40px] text-center">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.id, item.quantity + 1)}
                          className="px-3 py-1 text-gray-600 hover:bg-gray-100"
                        >
                          +
                        </button>
                      </div>

                      <button
                        onClick={() => removeItem(item.id)}
                        className="ml-4 text-red-600 hover:text-red-800"
                      >
                        Xóa
                      </button>
                    </div>
                  </div>
                </div>

                <div className="mt-4 md:mt-0 md:ml-4 text-right font-semibold">
                  {(item.price * item.quantity).toLocaleString('vi-VN')}₫
                </div>
              </div>
            ))}
          </div>

          {/* Cart summary */}
          <div className="border-t p-6 bg-gray-50">
            <div className="flex justify-between items-center mb-4">
              <span className="text-lg font-semibold">Tổng cộng:</span>
              <span className="text-xl font-bold text-red-600">{total.toLocaleString('vi-VN')}₫</span>
            </div>

            <div className="text-sm text-gray-600 mb-2">
              Phí vận chuyển: {shipping.toLocaleString('vi-VN')}₫
            </div>

            <button
              onClick={() => {
                if (selectedItems.length === 0) {
                  alert('Vui lòng chọn ít nhất một sản phẩm để thanh toán');
                  return;
                }
                // In a real app, this would navigate to checkout
                alert('Chuyển đến trang thanh toán');
              }}
              className="w-full bg-red-600 text-white py-4 rounded-md hover:bg-red-700 transition-colors font-bold text-lg"
            >
              MUA HÀNG
            </button>
          </div>
        </div>
      )}

      {/* Size/Color Selection Popup */}
      {showSizeColorPopup && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-md w-full p-6">
            <h3 className="text-xl font-bold mb-4">Chọn Kích thước & Màu sắc</h3>

            <div className="mb-4">
              <label className="block text-gray-700 mb-2">Kích thước</label>
              <select
                value={selectedSizeColor.size}
                onChange={(e) => setSelectedSizeColor({...selectedSizeColor, size: e.target.value})}
                className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-600"
              >
                {sizes.map(size => (
                  <option key={size} value={size}>{size}</option>
                ))}
              </select>
            </div>

            <div className="mb-6">
              <label className="block text-gray-700 mb-2">Màu sắc</label>
              <select
                value={selectedSizeColor.color}
                onChange={(e) => setSelectedSizeColor({...selectedSizeColor, color: e.target.value})}
                className="w-full p-3 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-red-600"
              >
                {colors.map(color => (
                  <option key={color} value={color}>{color}</option>
                ))}
              </select>
            </div>

            <div className="flex justify-end space-x-3">
              <button
                onClick={() => setShowSizeColorPopup(false)}
                className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-100"
              >
                Hủy
              </button>
              <button
                onClick={saveSizeColorChanges}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700"
              >
                Lưu
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Cart;