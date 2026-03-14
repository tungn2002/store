import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import './UserProfile.css';
import { profileAPI, authStorage, ordersAPI } from '../services/api';

const UserProfile = ({ onClose, onLogout, isLoggedIn, addToast }) => {
  const navigate = useNavigate();
  const [activeMenu, setActiveMenu] = useState('personal-info');

  useEffect(() => {
    // Redirect to login if not authenticated
    if (!isLoggedIn && !authStorage.isAuthenticated()) {
      navigate('/?view=login');
    }
  }, [isLoggedIn, navigate]);

  const handleLogout = () => {
    onLogout();
    navigate('/');
  };

  return (
    <div className="user-profile-page">
      <div className="user-profile-container">
        <div className="user-profile-header">
          <h2>Trang Cá Nhân</h2>
          <div className="header-actions">
            <button className="logout-btn" onClick={handleLogout}>Đăng xuất</button>
            <button className="back-btn" onClick={onClose}>← Quay lại</button>
          </div>
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
              <PersonalInfoForm addToast={addToast} />
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
const PersonalInfoForm = ({ addToast }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    phoneNumber: '',
    dateOfBirth: '',
    gender: '',
    address: '',
    password_old: '',
    password_new: '',
    password_access: ''
  });
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);

  // Fetch profile data on mount
  useEffect(() => {
    const fetchProfile = async () => {
      try {
        const response = await profileAPI.getProfile();
        if (response.code === 1000 && response.result) {
          const profile = response.result;
          setFormData(prev => ({
            ...prev,
            name: profile.name || '',
            email: profile.email || '',
            phoneNumber: profile.phoneNumber || '',
            dateOfBirth: profile.dateOfBirth || '',
            gender: profile.gender || '',
            address: profile.address || ''
          }));
          // Update stored user info
          authStorage.setUser(profile);
        }
      } catch (error) {
        console.error('Failed to fetch profile:', error);
        addToast('Không thể tải thông tin cá nhân', 'error');
      } finally {
        setLoading(false);
      }
    };

    fetchProfile();
  }, [addToast]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSubmitting(true);

    try {
      const profileData = {
        name: formData.name,
        email: formData.email,
        phoneNumber: formData.phoneNumber,
        dateOfBirth: formData.dateOfBirth || null,
        gender: formData.gender || null,
        address: formData.address
      };

      const response = await profileAPI.updateProfile(profileData);
      if (response.code === 1000) {
        addToast('Cập nhật thông tin thành công!', 'success');
        // Update stored user info
        if (response.result) {
          authStorage.setUser(response.result);
        }
      } else {
        addToast(response.message || 'Cập nhật thất bại', 'error');
      }
    } catch (error) {
      addToast(error.message || 'Có lỗi xảy ra khi cập nhật', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  if (loading) {
    return <div className="loading-container">Đang tải thông tin...</div>;
  }

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
            <label htmlFor="phoneNumber">Số điện thoại:</label>
            <input
              type="tel"
              id="phoneNumber"
              name="phoneNumber"
              value={formData.phoneNumber}
              onChange={handleChange}
              placeholder="Nhập số điện thoại"
            />
          </div>
          <div className="form-group">
            <label htmlFor="dateOfBirth">Ngày sinh:</label>
            <input
              type="date"
              id="dateOfBirth"
              name="dateOfBirth"
              value={formData.dateOfBirth}
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
              <option value="MALE">Nam</option>
              <option value="FEMALE">Nữ</option>
              <option value="OTHER">Khác</option>
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
          <button type="submit" className="btn-save" disabled={submitting}>
            {submitting ? 'Đang lưu...' : 'Lưu thay đổi'}
          </button>
        </div>

        <div className="password-section">
          <h4>Thay đổi mật khẩu</h4>
          <PasswordChangeForm addToast={addToast} />
        </div>
      </form>
    </div>
  );
};

// Password Change Form Component
const PasswordChangeForm = ({ addToast }) => {
  const [passwordData, setPasswordData] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: ''
  });
  const [submitting, setSubmitting] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setPasswordData(prev => ({
      ...prev,
      [name]: value
    }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    
    if (passwordData.newPassword !== passwordData.confirmPassword) {
      addToast('Mật khẩu mới không khớp', 'error');
      return;
    }

    if (passwordData.newPassword.length < 8) {
      addToast('Mật khẩu mới phải có ít nhất 8 ký tự', 'error');
      return;
    }

    setSubmitting(true);
    try {
      const response = await profileAPI.changePassword(
        passwordData.oldPassword,
        passwordData.newPassword
      );
      if (response.code === 1000) {
        addToast('Đổi mật khẩu thành công!', 'success');
        setPasswordData({ oldPassword: '', newPassword: '', confirmPassword: '' });
      } else {
        addToast(response.message || 'Đổi mật khẩu thất bại', 'error');
      }
    } catch (error) {
      addToast(error.message || 'Có lỗi xảy ra khi đổi mật khẩu', 'error');
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <form onSubmit={handleSubmit}>
      <div className="form-row">
        <div className="form-group">
          <label htmlFor="oldPassword">Mật khẩu cũ:</label>
          <input
            type="password"
            id="oldPassword"
            name="oldPassword"
            value={passwordData.oldPassword}
            onChange={handleChange}
            placeholder="Nhập mật khẩu cũ"
          />
        </div>
        <div className="form-group">
          <label htmlFor="newPassword">Mật khẩu mới:</label>
          <input
            type="password"
            id="newPassword"
            name="newPassword"
            value={passwordData.newPassword}
            onChange={handleChange}
            placeholder="Nhập mật khẩu mới"
          />
        </div>
      </div>

      <div className="form-group">
        <label htmlFor="confirmPassword">Xác nhận mật khẩu:</label>
        <input
          type="password"
          id="confirmPassword"
          name="confirmPassword"
          value={passwordData.confirmPassword}
          onChange={handleChange}
          placeholder="Nhập lại mật khẩu mới"
        />
      </div>

      <div className="form-actions">
        <button type="submit" className="btn-save" disabled={submitting}>
          {submitting ? 'Đang đổi...' : 'Đổi mật khẩu'}
        </button>
      </div>
    </form>
  );
};

// Orders Table Component
const OrdersTable = () => {
  const [orders, setOrders] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showOrderDetailPopup, setShowOrderDetailPopup] = useState(null);
  const [loadingOrderDetail, setLoadingOrderDetail] = useState(false);
  const [pagination, setPagination] = useState({
    page: 0,
    size: 10,
    totalItems: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false
  });

  const fetchOrders = async (page = 0, size = 10) => {
    try {
      setLoading(true);
      const response = await ordersAPI.getMyOrders(page, size);
      if (response.code === 1000 && response.result) {
        const result = response.result;
        setOrders(result.items || []);
        setPagination({
          page: result.page,
          size: result.size,
          totalItems: result.totalItems,
          totalPages: result.totalPages,
          hasNext: result.hasNext,
          hasPrevious: result.hasPrevious
        });
      }
    } catch (error) {
      console.error('Failed to fetch orders:', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const openOrderDetailPopup = async (order) => {
    // Show loading state
    setShowOrderDetailPopup({ ...order, items: null, loading: true });
    
    try {
      setLoadingOrderDetail(true);
      const response = await ordersAPI.getOrderById(order.id);
      if (response.code === 1000 && response.result) {
        setShowOrderDetailPopup({ ...response.result, loading: false });
      }
    } catch (error) {
      console.error('Failed to fetch order details:', error);
    } finally {
      setLoadingOrderDetail(false);
    }
  };

  const handlePageChange = (newPage) => {
    if (newPage >= 0 && newPage < pagination.totalPages) {
      fetchOrders(newPage, pagination.size);
    }
  };

  const formatCurrency = (amount) => {
    return new Intl.NumberFormat('vi-VN', {
      style: 'currency',
      currency: 'VND'
    }).format(amount);
  };

  const getStatusClass = (status) => {
    switch (status) {
      case 'PAID':
        return 'status-paid';
      case 'PENDING':
        return 'status-pending';
      case 'CANCEL':
        return 'status-cancel';
      default:
        return '';
    }
  };

  const getStatusText = (status) => {
    switch (status) {
      case 'PAID':
        return 'Hoàn thành';
      case 'PENDING':
        return 'Đang chờ';
      case 'CANCEL':
        return 'Đã hủy';
      default:
        return status;
    }
  };

  if (loading) {
    return <div className="loading-container">Đang tải đơn hàng...</div>;
  }

  return (
    <div className="orders-container">
      <h3>Danh Sách Đơn Hàng</h3>
      {orders.length === 0 ? (
        <div className="empty-orders">
          <p>Chưa có đơn hàng nào</p>
        </div>
      ) : (
        <>
          <table className="orders-table">
            <thead>
              <tr>
                <th>Mã đơn hàng</th>
                <th>Ngày đặt</th>
                <th>Tổng tiền</th>
                <th>Trạng thái</th>
                <th>Hành động</th>
              </tr>
            </thead>
            <tbody>
              {orders.map(order => (
                <tr key={order.id}>
                  <td>#{order.id}</td>
                  <td>{new Date(order.createdAt).toLocaleDateString('vi-VN')}</td>
                  <td>{formatCurrency(order.totalAmount)}</td>
                  <td className={`status ${getStatusClass(order.status)}`}>
                    {getStatusText(order.status)}
                  </td>
                  <td>
                    <button
                      onClick={() => openOrderDetailPopup(order)}
                      className="btn-detail"
                    >
                      Chi tiết
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>

          {/* Pagination */}
          <div className="pagination">
            <button
              onClick={() => handlePageChange(pagination.page - 1)}
              disabled={!pagination.hasPrevious}
              className="btn-page"
            >
              ← Trước
            </button>
            <span className="page-info">
              Trang {pagination.page + 1} / {pagination.totalPages}
            </span>
            <button
              onClick={() => handlePageChange(pagination.page + 1)}
              disabled={!pagination.hasNext}
              className="btn-page"
            >
              Sau →
            </button>
          </div>
        </>
      )}

      {/* Order Detail Popup */}
      {showOrderDetailPopup && (
        <div className="popup-overlay" onClick={() => setShowOrderDetailPopup(null)}>
          <div className="popup-content" onClick={(e) => e.stopPropagation()}>
            <div className="popup-header">
              <h3>Chi tiết đơn hàng #{showOrderDetailPopup.id}</h3>
              <button className="popup-close" onClick={() => setShowOrderDetailPopup(null)}>×</button>
            </div>
            <div className="popup-body">
              {showOrderDetailPopup.loading ? (
                <div className="loading-container">Đang tải chi tiết đơn hàng...</div>
              ) : (
                <>
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
                      {(showOrderDetailPopup.items || []).map((item, index) => (
                        <tr key={index}>
                          <td>{item.productName}</td>
                          <td>{item.size || '-'}</td>
                          <td>{item.color || '-'}</td>
                          <td>{item.quantity}</td>
                          <td>{formatCurrency(item.sellingPrice)}</td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                  <div className="order-total">
                    <strong>Tổng cộng:</strong> {formatCurrency(showOrderDetailPopup.totalAmount)}
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default UserProfile;