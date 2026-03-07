import React, { useState } from 'react';
import './Login.css';
import { authAPI, authStorage } from '../services/api';

const Login = ({ toggleView, addToast }) => {
  const [isLogin, setIsLogin] = useState(true);

  return (
    <div className="login-page">
      <div className="login-container">
        <div className="login-box">
          <div className="login-header">
            <h2>{isLogin ? 'Đăng Nhập' : 'Đăng Ký'}</h2>
            <p>{isLogin ? 'Chào mừng trở lại!' : 'Tạo tài khoản mới'}</p>
          </div>

          <div className="login-tabs">
            <button
              className={`tab ${isLogin ? 'active' : ''}`}
              onClick={() => setIsLogin(true)}
            >
              Đăng nhập
            </button>
            <button
              className={`tab ${!isLogin ? 'active' : ''}`}
              onClick={() => setIsLogin(false)}
            >
              Đăng ký
            </button>
          </div>

          {isLogin ? (
            <LoginForm toggleView={toggleView} addToast={addToast} />
          ) : (
            <RegisterForm setIsLogin={setIsLogin} toggleView={toggleView} addToast={addToast} />
          )}
        </div>
      </div>
    </div>
  );
};

// Login Form Component
const LoginForm = ({ toggleView, addToast }) => {
  const [formData, setFormData] = useState({
    email: '',
    password: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!formData.email) {
      newErrors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
    }

    if (!formData.password) {
      newErrors.password = 'Mật khẩu là bắt buộc';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const response = await authAPI.login(formData.email, formData.password);
      if (response.code === 1000 && response.result?.token) {
        // Store token
        authStorage.setToken(response.result.token);
        
        // Fetch user profile after login
        try {
          const profileResponse = await fetch('http://localhost:8080/profile', {
            headers: {
              'Authorization': `Bearer ${response.result.token}`,
              'Content-Type': 'application/json',
            },
          });
          
          // Handle 401 Unauthorized
          if (profileResponse.status === 401) {
            authStorage.removeToken();
            window.location.href = '/?view=login';
            return;
          }
          
          const profileData = await profileResponse.json();
          if (profileData.result) {
            authStorage.setUser(profileData.result);
          }
        } catch (profileError) {
          console.error('Failed to fetch profile:', profileError);
        }

        addToast('Đăng nhập thành công!', 'success');
        toggleView(); // Call toggleView to update parent state
      } else {
        setErrors({ submit: response.message || 'Đăng nhập thất bại' });
        addToast(response.message || 'Đăng nhập thất bại', 'error');
      }
    } catch (error) {
      setErrors({ submit: error.message || 'Có lỗi xảy ra, vui lòng thử lại' });
      addToast(error.message || 'Có lỗi xảy ra, vui lòng thử lại', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="login-email">Email</label>
        <input
          type="email"
          id="login-email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="nh@example.com"
        />
        {errors.email && <span className="error-message">{errors.email}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="login-password">Mật khẩu</label>
        <div className="password-wrapper">
          <input
            type={showPassword ? 'text' : 'password'}
            id="login-password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Nhập mật khẩu"
          />
          <button
            type="button"
            className="toggle-password"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? (
              <i className="fas fa-eye-slash"></i>
            ) : (
              <i className="fas fa-eye"></i>
            )}
          </button>
        </div>
        {errors.password && <span className="error-message">{errors.password}</span>}
      </div>

      {errors.submit && <div className="error-message submit-error">{errors.submit}</div>}

      <button type="submit" className="submit-btn" disabled={loading}>
        {loading ? 'Đang đăng nhập...' : 'Đăng nhập'}
      </button>
    </form>
  );
};

// Register Form Component
const RegisterForm = ({ setIsLogin, toggleView, addToast }) => {
  const [formData, setFormData] = useState({
    name: '',
    email: '',
    password: ''
  });
  const [showPassword, setShowPassword] = useState(false);
  const [errors, setErrors] = useState({});
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value
    }));
    if (errors[name]) {
      setErrors(prev => ({
        ...prev,
        [name]: ''
      }));
    }
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const newErrors = {};

    if (!formData.name.trim()) {
      newErrors.name = 'Họ tên là bắt buộc';
    } else if (formData.name.trim().length < 2) {
      newErrors.name = 'Họ tên phải có ít nhất 2 ký tự';
    }

    if (!formData.email) {
      newErrors.email = 'Email là bắt buộc';
    } else if (!/\S+@\S+\.\S+/.test(formData.email)) {
      newErrors.email = 'Email không hợp lệ';
    }

    if (!formData.password) {
      newErrors.password = 'Mật khẩu là bắt buộc';
    } else if (formData.password.length < 8) {
      newErrors.password = 'Mật khẩu phải có ít nhất 8 ký tự';
    }

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    setLoading(true);
    try {
      const response = await authAPI.register(formData.name, formData.email, formData.password);
      if (response.code === 1000) {
        addToast('Đăng ký thành công! Vui lòng đăng nhập', 'success');
        setIsLogin(true);
      } else {
        setErrors({ submit: response.message || 'Đăng ký thất bại' });
        addToast(response.message || 'Đăng ký thất bại', 'error');
      }
    } catch (error) {
      setErrors({ submit: error.message || 'Có lỗi xảy ra, vui lòng thử lại' });
      addToast(error.message || 'Có lỗi xảy ra, vui lòng thử lại', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <form className="login-form" onSubmit={handleSubmit}>
      <div className="form-group">
        <label htmlFor="register-name">Họ và tên</label>
        <input
          type="text"
          id="register-name"
          name="name"
          value={formData.name}
          onChange={handleChange}
          placeholder="Nguyễn Văn A"
        />
        {errors.name && <span className="error-message">{errors.name}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="register-email">Email</label>
        <input
          type="email"
          id="register-email"
          name="email"
          value={formData.email}
          onChange={handleChange}
          placeholder="nh@example.com"
        />
        {errors.email && <span className="error-message">{errors.email}</span>}
      </div>

      <div className="form-group">
        <label htmlFor="register-password">Mật khẩu</label>
        <div className="password-wrapper">
          <input
            type={showPassword ? 'text' : 'password'}
            id="register-password"
            name="password"
            value={formData.password}
            onChange={handleChange}
            placeholder="Nhập mật khẩu"
          />
          <button
            type="button"
            className="toggle-password"
            onClick={() => setShowPassword(!showPassword)}
          >
            {showPassword ? (
              <i className="fas fa-eye-slash"></i>
            ) : (
              <i className="fas fa-eye"></i>
            )}
          </button>
        </div>
        {errors.password && <span className="error-message">{errors.password}</span>}
      </div>

      {errors.submit && <div className="error-message submit-error">{errors.submit}</div>}

      <button type="submit" className="submit-btn" disabled={loading}>
        {loading ? 'Đang đăng ký...' : 'Đăng ký'}
      </button>

      <p className="login-link">
        Đã có tài khoản? <button type="button" onClick={() => setIsLogin(true)}>Đăng nhập</button>
      </p>
    </form>
  );
};

export default Login;
