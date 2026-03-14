const API_BASE_URL = 'http://localhost:8080';

// Helper to get auth token from localStorage
const getToken = () => {
  return localStorage.getItem('access_token');
};

// Helper to set auth token
const setToken = (token) => {
  localStorage.setItem('access_token', token);
};

// Helper to remove auth token and clear user data
const removeToken = () => {
  localStorage.removeItem('access_token');
  localStorage.removeItem('user_info');
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
    // Handle 401 Unauthorized - token expired or invalid
    if (response.status === 401) {
      removeToken();
      // Redirect to login page
      window.location.href = '/?view=login';
      throw new Error('Session expired. Please login again.');
    }
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
    }, false); // Không cần auth cho register
    return data;
  },

  login: async (email, password) => {
    const data = await apiCall('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }, false); // Không cần auth cho login
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

  getProductVariants: async (productId, page = 0, size = 100) => {
    const data = await apiCall(`/products/${productId}/variants?page=${page}&size=${size}`, {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },
};

// Brand API
export const brandAPI = {
  getAllBrands: async () => {
    const data = await apiCall('/brands/all', {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },
};

// Category API
export const categoryAPI = {
  getAllCategories: async () => {
    const data = await apiCall('/categories/all', {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },
};

// Search API (Elasticsearch)
export const searchAPI = {
  searchProducts: async (params) => {
    const queryParams = new URLSearchParams();
    if (params.query) queryParams.append('query', params.query);
    if (params.minPrice) queryParams.append('minPrice', params.minPrice);
    if (params.maxPrice) queryParams.append('maxPrice', params.maxPrice);
    if (params.brand) queryParams.append('brand', params.brand);
    if (params.category) queryParams.append('category', params.category);
    if (params.page !== undefined) queryParams.append('page', params.page);
    if (params.size !== undefined) queryParams.append('size', params.size);
    if (params.sortBy) queryParams.append('sortBy', params.sortBy);
    if (params.sortDirection) queryParams.append('sortDirection', params.sortDirection);

    const data = await apiCall(`/search?${queryParams.toString()}`, {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },

  suggestProducts: async (prefix) => {
    const data = await apiCall(`/search/suggest?prefix=${encodeURIComponent(prefix)}`, {
      method: 'GET',
    }, false); // Không cần auth
    return data;
  },
};

// Cart API
export const cartAPI = {
  getCart: async () => {
    const data = await apiCall('/cart', {
      method: 'GET',
    }, true); // Cần auth
    return data;
  },

  getCartProductVariants: async (cartId) => {
    const data = await apiCall(`/cart/items/${cartId}/variants`, {
      method: 'GET',
    }, true); // Cần auth
    return data;
  },

  addToCart: async (productVariantId, quantity = 1) => {
    const data = await apiCall('/cart/items', {
      method: 'POST',
      body: JSON.stringify({ productVariantId, quantity }),
    }, true); // Cần auth
    return data;
  },

  updateCartItem: async (cartId, quantity) => {
    const data = await apiCall(`/cart/items/${cartId}`, {
      method: 'PUT',
      body: JSON.stringify({ quantity }),
    }, true); // Cần auth
    return data;
  },

  updateCartItemVariant: async (cartId, productVariantId) => {
    const data = await apiCall(`/cart/items/${cartId}/variant?productVariantId=${productVariantId}`, {
      method: 'PUT',
    }, true); // Cần auth
    return data;
  },

  deleteCartItem: async (cartId) => {
    const data = await apiCall(`/cart/items/${cartId}`, {
      method: 'DELETE',
    }, true); // Cần auth
    return data;
  },

  clearCart: async () => {
    const data = await apiCall('/cart', {
      method: 'DELETE',
    }, true); // Cần auth
    return data;
  },
};

// Checkout API
export const checkoutAPI = {
  createCheckoutSession: async (items, successUrl, cancelUrl) => {
    const data = await apiCall(`/checkout?successUrl=${encodeURIComponent(successUrl)}&cancelUrl=${encodeURIComponent(cancelUrl)}`, {
      method: 'POST',
      body: JSON.stringify({ items }),
    }, true); // Cần auth
    return data;
  },
};

// Orders API
export const ordersAPI = {
  getMyOrders: async (page = 0, size = 10) => {
    const data = await apiCall(`/orders/my-orders?page=${page}&size=${size}`, {
      method: 'GET',
    }, true); // Cần auth
    return data;
  },

  getOrderById: async (orderId) => {
    const data = await apiCall(`/orders/${orderId}`, {
      method: 'GET',
    }, true); // Cần auth
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
    removeToken();
  },

  isAuthenticated: () => {
    return !!getToken();
  },
};

export default {
  authAPI,
  profileAPI,
  productAPI,
  brandAPI,
  categoryAPI,
  searchAPI,
  cartAPI,
  checkoutAPI,
  ordersAPI,
  authStorage,
};
