import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import { cartAPI, productAPI, authStorage, checkoutAPI } from '../services/api';
import './Cart.css';

const Cart = ({ isLoggedIn, addToast }) => {
  const navigate = useNavigate();
  const [cartItems, setCartItems] = useState([]);
  const [selectedItems, setSelectedItems] = useState({}); // Client-side selection only
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [showSizeColorPopup, setShowSizeColorPopup] = useState(null);
  const [selectedSizeColor, setSelectedSizeColor] = useState({});
  const [productVariants, setProductVariants] = useState([]);
  const [updateTrigger, setUpdateTrigger] = useState(0);

  // Handle Stripe redirect (success/cancel) - just show toast, webhook handles status update
  useEffect(() => {
    const urlParams = new URLSearchParams(window.location.search);
    const orderId = urlParams.get('order_id');
    const sessionId = urlParams.get('session_id');
    const paymentStatus = urlParams.get('payment');

    if (orderId) {
      if (sessionId && paymentStatus === 'success') {
        addToast('Thanh toán thành công!', 'success');
      } else if (paymentStatus === 'cancel') {
        addToast('Đơn hàng đã hủy. Kho sẽ được cập nhật.', 'info');
      }
      // Clear URL params
      window.history.replaceState({}, document.title, '/?view=cart');
    }
  }, []);

  // Redirect to login if not authenticated
  useEffect(() => {
    if (!isLoggedIn && !authStorage.isAuthenticated()) {
      navigate('/?view=login');
    }
  }, [isLoggedIn, navigate]);

  // Log cart items changes
  useEffect(() => {
    console.log('Cart items updated:', cartItems);
  }, [cartItems]);

  // Fetch cart data from API
  const fetchCart = async () => {
    try {
      setLoading(true);
      const response = await cartAPI.getCartItems();
      if (response && response.result) {
        const items = response.result || [];
        setCartItems(items);
        // Initialize all items as NOT selected by default (client-side only)
        const initialSelection = {};
        items.forEach(item => {
          initialSelection[item.cartId] = false;
        });
        setSelectedItems(initialSelection);
      }
    } catch (err) {
      setError(err.message || 'Không thể tải giỏ hàng');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, []);

  // Calculate selected total based on client-side selection
  const calculateSelectedTotal = () => {
    let total = 0;
    cartItems.forEach(item => {
      if (selectedItems[item.cartId]) {
        total += item.price * item.quantity;
      }
    });
    return total;
  };

  const selectedTotal = calculateSelectedTotal();
  const finalTotal = selectedTotal;
  const selectedCount = Object.values(selectedItems).filter(v => v).length;

  // Format currency function
  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  // Handle quantity changes - cannot be 0
  const updateQuantity = async (cartId, newQuantity) => {
    if (newQuantity < 1) {
      addToast('Số lượng phải lớn hơn 0', 'error');
      return;
    }

    try {
      // Call API to update quantity
      const response = await cartAPI.updateCartItem(cartId, newQuantity);

      if (response && response.result) {
        // Update local state with API response
        setCartItems(items =>
          items.map(item =>
            item.cartId === cartId
              ? {
                  ...item,
                  quantity: newQuantity
                }
              : item
          )
        );

        addToast('Đã cập nhật số lượng', 'success');
      }
    } catch (err) {
      addToast(err.message || 'Không thể cập nhật số lượng', 'error');
    }
  };

  // Toggle item selection (client-side only)
  const toggleItemSelection = (cartId) => {
    setSelectedItems(prev => ({
      ...prev,
      [cartId]: !prev[cartId]
    }));
  };

  // Toggle all items selection (client-side only)
  const toggleSelectAll = () => {
    const allSelected = cartItems.every(item => selectedItems[item.cartId]);
    const newSelection = {};
    cartItems.forEach(item => {
      newSelection[item.cartId] = !allSelected;
    });
    setSelectedItems(newSelection);
  };

  // Remove item from cart
  const removeItem = async (cartId) => {
    if (!confirm('Bạn có chắc chắn muốn xóa sản phẩm này khỏi giỏ hàng?')) {
      return;
    }

    try {
      await cartAPI.deleteCartItem(cartId);
      setCartItems(items => items.filter(item => item.cartId !== cartId));
      // Remove from selection
      setSelectedItems(prev => {
        const newSelection = { ...prev };
        delete newSelection[cartId];
        return newSelection;
      });
      addToast('Đã xóa sản phẩm khỏi giỏ hàng', 'success');
    } catch (err) {
      addToast(err.message || 'Không thể xóa sản phẩm', 'error');
    }
  };

  // Open size/color popup with all variants
  const openSizeColorPopup = async (item) => {
    setSelectedSizeColor({
      cartId: item.cartId,
      productVariantId: item.productVariantId,
      size: item.size,
      color: item.color
    });

    // Fetch all variants for this product from cart item
    try {
      const response = await cartAPI.getCartProductVariants(item.cartId);
      if (response && response.result) {
        const variants = response.result.map(v => ({
          id: v.id,
          size: v.size,
          color: v.color,
          price: v.price,
          stockQuantity: v.stockQuantity,
          image: v.image
        }));
        setProductVariants(variants);
      }
    } catch (err) {
      console.error('Failed to fetch product variants:', err);
      setProductVariants([]);
    }

    setShowSizeColorPopup(true);
  };

  // Save size/color changes - check if variant already exists in cart and call API
  const saveSizeColorChanges = async () => {
    // Find the selected variant details
    const selectedVariant = productVariants.find(
      v => v.size === selectedSizeColor.size && v.color === selectedSizeColor.color
    );
    
    if (!selectedVariant) {
      addToast('Variant không tồn tại!', 'error');
      return;
    }
    
    // Check if current selection
    if (selectedVariant.id === selectedSizeColor.productVariantId) {
      setShowSizeColorPopup(false);
      return;
    }
    
    // Check if the selected variant already exists in cart (excluding current item)
    const existingItem = cartItems.find(item => 
      item.productVariantId === selectedVariant.id && 
      item.cartId !== selectedSizeColor.cartId
    );
    
    if (existingItem) {
      addToast(`Sản phẩm với Size ${selectedSizeColor.size} - Color ${selectedSizeColor.color} đã có trong giỏ hàng!`, 'error');
      return;
    }
    
    try {
      // Call API to update cart item variant
      const response = await cartAPI.updateCartItemVariant(
        selectedSizeColor.cartId, 
        selectedVariant.id
      );
      
      if (response && response.result) {
        // Update local state with API response
        const updatedItems = cartItems.map(item => {
          if (item.cartId === selectedSizeColor.cartId) {
            return {
              ...item,
              size: selectedSizeColor.size,
              color: selectedSizeColor.color,
              productVariantId: selectedVariant.id,
              price: selectedVariant.price || item.price,
              variantImage: selectedVariant.image || item.variantImage,
              subtotal: (selectedVariant.price || item.price) * item.quantity
            };
          }
          return item;
        });
        
        setCartItems(updatedItems);
        setShowSizeColorPopup(false);
        addToast('Đã cập nhật size/color thành công!', 'success');
      }
    } catch (err) {
      console.error('Failed to update cart variant:', err);
      addToast(err.message || 'Không thể cập nhật size/color. Vui lòng thử lại.', 'error');
    }
  };

  // Handle checkout
  const handleCheckout = async () => {
    if (selectedCount === 0) {
      addToast('Vui lòng chọn ít nhất một sản phẩm để thanh toán', 'error');
      return;
    }

    try {
      // Prepare checkout request with items wrapper
      const checkoutData = {
        items: cartItems
          .filter(item => selectedItems[item.cartId])
          .map(item => ({
            productVariantId: item.productVariantId,
            quantity: item.quantity
          }))
      };

      // Call checkout API with success/cancel URLs
      const baseUrl = window.location.origin;
      const successUrl = `${baseUrl}/?view=cart&payment=success`;
      const cancelUrl = `${baseUrl}/?view=cart&payment=cancel`;

      const response = await fetch(`http://localhost:8080/checkout?successUrl=${encodeURIComponent(successUrl)}&cancelUrl=${encodeURIComponent(cancelUrl)}`, {
        method: 'POST',
        headers: {
          'Authorization': `Bearer ${authStorage.getToken()}`,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(checkoutData)
      });

      // Handle 401 Unauthorized - token expired
      if (response.status === 401) {
        authStorage.removeToken();
        addToast('Phiên đăng nhập hết hạn. Vui lòng đăng nhập lại.', 'error');
        window.location.href = '/?view=login';
        return;
      }

      const result = await response.json();

      if (result.result && result.result.checkoutUrl) {
        addToast('Đang chuyển hướng đến trang thanh toán...', 'info');
        // Redirect to Stripe Checkout
        window.location.href = result.result.checkoutUrl;
      } else {
        addToast(result.message || 'Không thể tạo phiên thanh toán!', 'error');
      }
    } catch (err) {
      console.error('Checkout error:', err);
      addToast(err.message || 'Lỗi khi tạo phiên thanh toán!', 'error');
    }
  };

  if (loading) {
    return (
      <div className="max-w-[95%] mx-auto px-4 py-8 text-center">
        <p className="text-xl text-gray-600">Đang tải giỏ hàng...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="max-w-[95%] mx-auto px-4 py-8 text-center">
        <p className="text-xl text-red-600">{error}</p>
        <button
          onClick={() => toggleView('home')}
          className="mt-4 bg-red-600 text-white px-6 py-3 rounded-md hover:bg-red-700"
        >
          Quay lại trang chủ
        </button>
      </div>
    );
  }

  return (
    <div className="max-w-[95%] mx-auto px-4 md:px-6 lg:px-8 py-8">
      <div className="flex justify-between items-center mb-6">
        <h1 className="text-3xl font-bold">Giỏ hàng</h1>
        <button
          onClick={() => navigate('/')}
          className="text-gray-600 hover:text-gray-900"
        >
          ← Quay lại
        </button>
      </div>

      {cartItems.length === 0 ? (
        <div className="text-center py-12 bg-white rounded-lg shadow-md">
          <p className="text-xl text-gray-600">Giỏ hàng của bạn đang trống</p>
          <button
            onClick={() => navigate('/')}
            className="mt-4 bg-red-600 text-white px-6 py-3 rounded-md hover:bg-red-700 transition-colors"
          >
            Mua sắm ngay
          </button>
        </div>
      ) : (
        <div className="bg-white rounded-lg shadow-md overflow-hidden">
          {/* Cart header with select all */}
          <div className="border-b p-4 flex items-center bg-gray-50">
            <input
              type="checkbox"
              checked={cartItems.length > 0 && cartItems.every(item => selectedItems[item.cartId])}
              onChange={toggleSelectAll}
              className="mr-4 h-5 w-5 text-red-600 rounded focus:ring-red-500"
            />
            <span className="font-semibold text-lg">Sản phẩm ({cartItems.length})</span>
          </div>

          {/* Cart items */}
          <div className="divide-y">
            {cartItems.map((item) => (
              <div key={item.cartId} className="p-4 flex flex-col md:flex-row items-start hover:bg-gray-50 transition-colors">
                <input
                  type="checkbox"
                  checked={!!selectedItems[item.cartId]}
                  onChange={() => toggleItemSelection(item.cartId)}
                  className="mr-4 mt-1 h-5 w-5 text-red-600 rounded focus:ring-red-500"
                />

                <div className="flex-1 flex flex-col md:flex-row">
                  <img
                    src={item.variantImage || 'https://via.placeholder.com/100'}
                    alt={item.productName}
                    className="w-24 h-24 object-cover rounded-md mb-4 md:mb-0 md:mr-6"
                  />

                  <div className="flex-1">
                    <h3 className="font-semibold text-lg">
                      {item.productName || `Sản phẩm #${item.productVariantId}`}
                    </h3>
                    <div className="flex flex-wrap items-center mt-2 gap-2">
                      <span className="bg-gray-100 px-3 py-1 rounded-full text-sm">
                        Size: {item.size || 'Không có'}
                      </span>
                      <span className="bg-gray-100 px-3 py-1 rounded-full text-sm">
                        Color: {item.color || 'Không có'}
                      </span>
                      <button
                        onClick={() => openSizeColorPopup(item)}
                        className="text-blue-600 hover:text-blue-800 text-sm underline ml-2"
                      >
                        Thay đổi
                      </button>
                    </div>

                    <div className="mt-4 flex items-center">
                      <div className="flex items-center border border-gray-300 rounded-md">
                        <button
                          onClick={() => updateQuantity(item.cartId, item.quantity - 1)}
                          className="px-3 py-1 text-gray-600 hover:bg-gray-100 disabled:opacity-50"
                          disabled={item.quantity <= 1}
                        >
                          −
                        </button>
                        <span className="px-4 py-1 min-w-[50px] text-center font-medium">{item.quantity}</span>
                        <button
                          onClick={() => updateQuantity(item.cartId, item.quantity + 1)}
                          className="px-3 py-1 text-gray-600 hover:bg-gray-100"
                        >
                          +
                        </button>
                      </div>

                      <button
                        onClick={() => removeItem(item.cartId)}
                        className="ml-4 text-red-600 hover:text-red-800 font-medium"
                      >
                        🗑 Xóa
                      </button>
                    </div>
                  </div>
                </div>

                <div className="mt-4 md:mt-0 md:ml-4 text-right">
                  <div className="font-semibold text-lg text-red-600">
                    {(item.price * item.quantity).toLocaleString('vi-VN')}₫
                  </div>
                  <div className="text-sm text-gray-500">
                    {item.price.toLocaleString('vi-VN')}₫ x {item.quantity}
                  </div>
                </div>
              </div>
            ))}
          </div>

          {/* Selected items summary */}
          {selectedCount > 0 && (
            <div className="border-t border-b p-4 bg-blue-50">
              <div className="flex items-center justify-between">
                <div className="flex items-center">
                  <span className="text-blue-800 font-medium">
                    ✓ Đã chọn {selectedCount} sản phẩm
                  </span>
                </div>
                <div className="text-right">
                  <span className="text-blue-800 font-medium">
                    Tạm tính: {formatCurrency(selectedTotal)}
                  </span>
                </div>
              </div>
            </div>
          )}

          {/* Cart summary */}
          <div className="border-t p-6 bg-gray-50">
            <div className="space-y-3 mb-6">
              {selectedCount > 0 && (
                <div className="flex justify-between items-center text-blue-600 font-medium">
                  <span>✓ Tạm tính ({selectedCount} sản phẩm đã chọn):</span>
                  <span>{formatCurrency(selectedTotal)}</span>
                </div>
              )}
              <div className="flex justify-between items-center">
                <span className="text-lg font-semibold">Tổng cộng:</span>
                <span className="text-2xl font-bold text-red-600">
                  {selectedCount > 0 ? formatCurrency(finalTotal) : '0₫'}
                </span>
              </div>
              {selectedCount > 0 && (
                <div className="text-sm text-gray-500 text-right">
                  (Bao gồm {selectedCount} sản phẩm đã chọn)
                </div>
              )}
              {selectedCount === 0 && (
                <div className="text-sm text-gray-500 text-right">
                  (Chọn sản phẩm để tính tổng)
                </div>
              )}
            </div>

            <button
              onClick={handleCheckout}
              disabled={selectedCount === 0}
              className={`w-full py-4 rounded-md transition-colors font-bold text-lg ${
                selectedCount === 0
                  ? 'bg-gray-300 text-gray-500 cursor-not-allowed'
                  : 'bg-red-600 text-white hover:bg-red-700'
              }`}
            >
              MUA HÀNG ({selectedCount} sản phẩm)
            </button>
          </div>
        </div>
      )}

      {/* Size/Color Selection Popup Modal */}
      {showSizeColorPopup && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex items-center justify-center z-50 p-4">
          <div className="bg-white rounded-lg max-w-2xl w-full p-6 shadow-xl max-h-[80vh] overflow-y-auto">
            <h3 className="text-xl font-bold mb-4 text-gray-800">Chọn Kích thước & Màu sắc</h3>
            <p className="text-sm text-gray-500 mb-4">
              Sản phẩm: <span className="font-semibold">{cartItems.find(i => i.cartId === selectedSizeColor.cartId)?.productName || 'Unknown'}</span>
            </p>

            {/* Color Selector */}
            <div className="mb-6">
              <label className="block text-gray-700 mb-3 font-medium">Màu sắc:</label>
              <div className="grid grid-cols-3 md:grid-cols-4 gap-2">
                {[...new Set(productVariants.map(v => v.color).filter(Boolean))].map((color) => {
                  const isSelected = selectedSizeColor.color === color;
                  const hasStock = productVariants.some(v => v.color === color && v.stockQuantity > 0);
                  return (
                    <button
                      key={color}
                      onClick={() => hasStock && setSelectedSizeColor({...selectedSizeColor, color})}
                      disabled={!hasStock}
                      className={`border py-3 px-2 text-sm transition ${
                        !hasStock
                          ? 'border-gray-100 text-gray-300 cursor-not-allowed bg-gray-50'
                          : isSelected
                          ? 'border-red-600 bg-red-50 text-red-600 font-bold'
                          : 'border-gray-200 hover:border-red-600'
                      }`}
                    >
                      {color}
                      {!hasStock && <span className="block text-[10px]">Hết</span>}
                    </button>
                  );
                })}
              </div>
            </div>

            {/* Size Selector (only show if color is selected) */}
            {selectedSizeColor.color && (
              <div className="mb-6">
                <label className="block text-gray-700 mb-3 font-medium">Kích thước:</label>
                <div className="grid grid-cols-4 md:grid-cols-6 gap-2">
                  {productVariants
                    .filter(v => v.color === selectedSizeColor.color)
                    .map((variant) => {
                      const isSelected = selectedSizeColor.size === variant.size;
                      const isOutOfStock = variant.stockQuantity === 0;
                      return (
                        <button
                          key={variant.size}
                          onClick={() => !isOutOfStock && setSelectedSizeColor({...selectedSizeColor, size: variant.size})}
                          disabled={isOutOfStock}
                          className={`border py-2 text-sm transition ${
                            isOutOfStock
                              ? 'border-gray-100 text-gray-300 cursor-not-allowed bg-gray-50'
                              : isSelected
                              ? 'border-red-600 bg-red-50 text-red-600 font-bold'
                              : 'border-gray-200 hover:border-red-600'
                          }`}
                        >
                          {variant.size}
                          {isOutOfStock && <span className="block text-[10px]">Hết</span>}
                        </button>
                      );
                    })}
                </div>
              </div>
            )}

            {!selectedSizeColor.color && (
              <p className="text-sm text-gray-500 italic mb-4">Vui lòng chọn màu trước khi chọn size</p>
            )}

            {/* Current selection info */}
            {selectedSizeColor.size && selectedSizeColor.color && (
              <div className="mb-4 p-3 bg-green-50 border border-green-200 rounded">
                <p className="text-sm text-green-800">
                  <i className="fas fa-check-circle mr-2"></i>
                  Đã chọn: <strong>Size {selectedSizeColor.size}</strong> - <strong>{selectedSizeColor.color}</strong>
                </p>
              </div>
            )}

            <div className="flex justify-end space-x-3 mt-6 pt-4 border-t">
              <button
                onClick={() => setShowSizeColorPopup(false)}
                className="px-4 py-2 border border-gray-300 rounded-md hover:bg-gray-100 transition-colors"
              >
                Hủy
              </button>
              <button
                onClick={saveSizeColorChanges}
                disabled={!selectedSizeColor.size || !selectedSizeColor.color}
                className="px-4 py-2 bg-red-600 text-white rounded-md hover:bg-red-700 transition-colors disabled:bg-gray-300 disabled:cursor-not-allowed"
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
