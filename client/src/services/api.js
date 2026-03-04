const API_BASE_URL = 'http://localhost:8080';

// Helper to get auth token from localStorage
const getToken = () => {
  return localStorage.getItem('access_token');
};

// Helper to set auth token
const setToken = (token) => {
  localStorage.setItem('access_token', token);
};

// Helper to remove auth token
const removeToken = () => {
  localStorage.removeItem('access_token');
};

// Generic API call helper
const apiCall = async (endpoint, options = {}, requireAuth = true) => {
  const token = getToken();

  const headers = {
    'Content-Type': 'application/json',
    ...(requireAuth && token && { 'Authorization': `Bearer ${token}` }),
    ...options.headers,
  };

  const response = await fetch(`${API_BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  const data = await response.json();

  if (!response.ok) {
    throw new Error(data.message || 'API call failed');
  }

  return data;
};

// Auth API
export const authAPI = {
  register: async (name, email, password) => {
    const data = await apiCall('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ name, email, password }),
    });
    return data;
  },

  login: async (email, password) => {
    const data = await apiCall('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    });
    if (data.result && data.result.token) {
      setToken(data.result.token);
    }
    return data;
  },

  logout: async () => {
    const token = getToken();
    if (token) {
      await apiCall('/auth/logout', {
        method: 'POST',
        body: JSON.stringify({ token }),
      });
    }
    removeToken();
  },
};

// Profile API
export const profileAPI = {
  getProfile: async () => {
    const data = await apiCall('/profile', {
      method: 'GET',
    });
    return data;
  },

  updateProfile: async (profileData) => {
    const data = await apiCall('/profile', {
      method: 'PUT',
      body: JSON.stringify(profileData),
    });
    return data;
  },

  changePassword: async (oldPassword, newPassword) => {
    const data = await apiCall('/profile/change-password', {
      method: 'PUT',
      body: JSON.stringify({
        oldPassword,
        newPassword,
      }),
    });
    return data;
  },
};

// Product API
export const productAPI = {
  getLatest5Products: async () => {
    const data = await apiCall('/products/latest', {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },

  getProductDetail: async (productId) => {
    const data = await apiCall(`/products/${productId}/detail`, {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },
};

// Storage helpers for user info
export const authStorage = {
  setToken,
  getToken,
  removeToken,
  
  setUser: (user) => {
    localStorage.setItem('user_info', JSON.stringify(user));
  },
  
  getUser: () => {
    const user = localStorage.getItem('user_info');
    return user ? JSON.parse(user) : null;
  },
  
  clearUser: () => {
    localStorage.removeItem('user_info');
  },
  
  isAuthenticated: () => {
    return !!getToken();
  },
};

export default {
  authAPI,
  profileAPI,
  productAPI,
  authStorage,
};
