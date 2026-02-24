import React, { useState } from 'react';
import './UserProfile.css';

const UserProfile = ({ onClose }) => {
  const [activeMenu, setActiveMenu] = useState('personal-info');

  return (
    <div className="user-profile-page">
      <div className="user-profile-container">
        <div className="user-profile-header">
          <h2>Trang Cá Nhân</h2>
          <button className="back-btn" onClick={onClose}>← Quay lại</button>
        </div>
        <div className="user-profile-content">
          {/* Left Menu */}
          <div className="menu-left">
            <ul className="menu-list">
              <li
                className={`menu-item ${activeMenu === 'personal-info' ? 'active' : ''}`}
                onClick={() => setActiveMenu('personal-info')}
              >
                Thông Tin Cá Nhân
              </li>
              <li
                className={`menu-item ${activeMenu === 'orders' ? 'active' : ''}`}
                onClick={() => setActiveMenu('orders')}
              >
                Đơn Hàng
              </li>
            </ul>
          </div>

          {/* Right Content */}
          <div className="content-right">
            {activeMenu === 'personal-info' ? (
              <PersonalInfoForm />
            ) : (
              <OrdersTable />
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

// Personal Information Form Component
const PersonalInfoForm = () => {
  const [formData, setFormData] = useState({
    name: 'Nguyễn Văn A',
    email: 'nguyenvana@example.com',
    phonenumber: '0123456789',
    dob: '1990-01-01',
    gender: 'male',
    address: '123 Đường ABC, Quận XYZ, TP.HCM',
    password_old: '',
    password_new: '',
    password_access: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = (e) => {
    e.preventDefault();
    // Handle form submission here
    console.log('Form submitted:', formData);
  };

  return (
    <div className="form-container">
      <h3>Thông Tin Cá Nhân</h3>
      <form onSubmit={handleSubmit}>
        <div className="form-row">
          <div className="form-group">
            <label htmlFor="name">Họ và tên:</label>
            <input
              type="text"
              id="name"
              name="name"
              value={formData.name}
              onChange={handleChange}
              placeholder="Nhập họ và tên"
            />
          </div>
          <div className="form-group">
            <label htmlFor="email">Email:</label>
            <input
              type="email"
              id="email"
              name="email"
              value={formData.email}
              onChange={handleChange}
              placeholder="Nhập địa chỉ email"
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="phonenumber">Số điện thoại:</label>
            <input
              type="tel"
              id="phonenumber"
              name="phonenumber"
              value={formData.phonenumber}
              onChange={handleChange}
              placeholder="Nhập số điện thoại"
            />
          </div>
          <div className="form-group">
            <label htmlFor="dob">Ngày sinh:</label>
            <input
              type="date"
              id="dob"
              name="dob"
              value={formData.dob}
              onChange={handleChange}
            />
          </div>
        </div>

        <div className="form-row">
          <div className="form-group">
            <label htmlFor="gender">Giới tính:</label>
            <select
              id="gender"
              name="gender"
              value={formData.gender}
              onChange={handleChange}
            >
              <option value="">Chọn giới tính</option>
              <option value="male">Nam</option>
              <option value="female">Nữ</option>
              <option value="other">Khác</option>
            </select>
          </div>
          <div className="form-group">
            <label htmlFor="address">Địa chỉ:</label>
            <input
              type="text"
              id="address"
              name="address"
              value={formData.address}
              onChange={handleChange}
              placeholder="Nhập địa chỉ đầy đủ"
            />
          </div>
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-save">Lưu thay đổi</button>
        </div>

        <div className="password-section">
          <h4>Thay đổi mật khẩu</h4>
          <div className="form-row">
            <div className="form-group">
              <label htmlFor="password_old">Mật khẩu cũ:</label>
              <input
                type="password"
                id="password_old"
                name="password_old"
                value={formData.password_old}
                onChange={handleChange}
                placeholder="Nhập mật khẩu cũ"
              />
            </div>
            <div className="form-group">
              <label htmlFor="password_new">Mật khẩu mới:</label>
              <input
                type="password"
                id="password_new"
                name="password_new"
                value={formData.password_new}
                onChange={handleChange}
                placeholder="Nhập mật khẩu mới"
              />
            </div>
          </div>

          <div className="form-group">
            <label htmlFor="password_access">Xác nhận mật khẩu:</label>
            <input
              type="password"
              id="password_access"
              name="password_access"
              value={formData.password_access}
              onChange={handleChange}
              placeholder="Nhập lại mật khẩu mới"
            />
          </div>

          <div className="form-actions">
            <button type="submit" className="btn-save">Lưu thay đổi</button>
          </div>
        </div>
      </form>
    </div>
  );
};

// Orders Table Component
const OrdersTable = () => {
  const [showOrderDetailPopup, setShowOrderDetailPopup] = useState(null);

  const orders = [
    {
      id: 1,
      date: '2023-01-15',
      total: 1500000,
      status: 'Hoàn thành',
      note: 'Giao hàng giờ hành chính',
      products: [
        { name: 'Nike Air Max 90 Red Edition', size: '42', color: 'Red', quantity: 1, price: 1250000 },
        { name: 'Adidas Forum Low Classic White', size: '40', color: 'White', quantity: 1, price: 250000 }
      ]
    },
    {
      id: 2,
      date: '2023-02-20',
      total: 2300000,
      status: 'Đang chờ',
      note: 'Khách yêu cầu gọi trước khi giao',
      products: [
        { name: 'Nike Dunk Low Retro Panda', size: '43', color: 'Black/White', quantity: 1, price: 2300000 }
      ]
    },
    {
      id: 3,
      date: '2023-03-10',
      total: 850000,
      status: 'Hoàn thành',
      note: '',
      products: [
        { name: 'Vans Old Skool Classic Black', size: '39', color: 'Black', quantity: 1, price: 850000 }
      ]
    },
    {
      id: 4,
      date: '2023-04-05',
      total: 3200000,
      status: 'Đang chờ',
      note: 'Giao tại văn phòng làm việc',
      products: [
        { name: 'Nike Air Max 90 Red Edition', size: '42', color: 'Red', quantity: 2, price: 1250000 },
        { name: 'Adidas Forum Low Classic White', size: '41', color: 'White', quantity: 1, price: 700000 }
      ]
    },
    {
      id: 5,
      date: '2023-05-12',
      total: 1750000,
      status: 'Hoàn thành',
      note: '',
      products: [
        { name: 'Nike Dunk Low Retro Panda', size: '40', color: 'Black/White', quantity: 1, price: 1750000 }
      ]
    }
  ];

  const openOrderDetailPopup = (order) => {
    setShowOrderDetailPopup(order);
  };

  const handleCancelOrder = (orderId) => {
    if (window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này?')) {
      // Handle cancel order logic here
      console.log('Cancel order:', orderId);
      alert('Đã hủy đơn hàng thành công');
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  return (
    <div className="orders-container">
      <h3>Danh Sách Đơn Hàng</h3>
      <table className="orders-table">
        <thead>
          <tr>
            <th>Mã đơn hàng</th>
            <th>Ngày đặt</th>
            <th>Sản phẩm</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
            <th>Hành động</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.date}</td>
              <td>
                <button
                  onClick={() => openOrderDetailPopup(order)}
                  className="btn-detail"
                >
                  Chi tiết
                </button>
              </td>
              <td>{order.total.toLocaleString('vi-VN')} ₫</td>
              <td className={`status ${order.status.toLowerCase().replace(/\s+/g, '-')}`}>
                {order.status}
              </td>
              <td>
                {order.status === 'Đang chờ' && (
                  <button
                    onClick={() => handleCancelOrder(order.id)}
                    className="btn-cancel-order"
                  >
                    Hủy
                  </button>
                )}
              </td>
            </tr>
          ))}
        </tbody>
      </table>

      {/* Order Detail Popup */}
      {showOrderDetailPopup && (
        <div className="popup-overlay" onClick={() => setShowOrderDetailPopup(null)}>
          <div className="popup-content" onClick={(e) => e.stopPropagation()}>
            <div className="popup-header">
              <h3>Chi tiết đơn hàng #{showOrderDetailPopup.id}</h3>
              <button className="popup-close" onClick={() => setShowOrderDetailPopup(null)}>×</button>
            </div>
            <div className="popup-body">
              <table className="order-detail-table">
                <thead>
                  <tr>
                    <th>Tên sản phẩm</th>
                    <th>Size</th>
                    <th>Color</th>
                    <th>Số lượng</th>
                    <th>Đơn giá</th>
                  </tr>
                </thead>
                <tbody>
                  {showOrderDetailPopup.products.map((product, index) => (
                    <tr key={index}>
                      <td>{product.name}</td>
                      <td>{product.size}</td>
                      <td>{product.color}</td>
                      <td>{product.quantity}</td>
                      <td>{formatCurrency(product.price)}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              {showOrderDetailPopup.note && (
                <div className="order-note">
                  <strong>Ghi chú:</strong> {showOrderDetailPopup.note}
                </div>
              )}
              <div className="order-total">
                <strong>Tổng cộng:</strong> {formatCurrency(showOrderDetailPopup.total)}
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;