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
        </div>

        <div className="form-actions">
          <button type="submit" className="btn-save">Lưu thay đổi</button>
          <button type="button" className="btn-cancel">Hủy</button>
        </div>
      </form>
    </div>
  );
};

// Orders Table Component
const OrdersTable = () => {
  const orders = [
    { id: 1, date: '2023-01-15', total: 1500000, status: 'Hoàn thành' },
    { id: 2, date: '2023-02-20', total: 2300000, status: 'Đang xử lý' },
    { id: 3, date: '2023-03-10', total: 850000, status: 'Đã hủy' },
    { id: 4, date: '2023-04-05', total: 3200000, status: 'Giao hàng' },
    { id: 5, date: '2023-05-12', total: 1750000, status: 'Hoàn thành' }
  ];

  return (
    <div className="orders-container">
      <h3>Danh Sách Đơn Hàng</h3>
      <table className="orders-table">
        <thead>
          <tr>
            <th>Mã đơn hàng</th>
            <th>Ngày đặt</th>
            <th>Tổng tiền</th>
            <th>Trạng thái</th>
          </tr>
        </thead>
        <tbody>
          {orders.map(order => (
            <tr key={order.id}>
              <td>{order.id}</td>
              <td>{order.date}</td>
              <td>{order.total.toLocaleString('vi-VN')} ₫</td>
              <td className={`status ${order.status.toLowerCase().replace(/\s+/g, '-')}`}>
                {order.status}
              </td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  );
};

export default UserProfile;