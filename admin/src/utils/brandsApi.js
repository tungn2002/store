import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/brands';

/**
 * Get token from localStorage
 */
const getToken = () => {
  return localStorage.getItem('token');
};

/**
 * Create axios instance with auth header
 */
const api = axios.create({
  baseURL: API_BASE_URL,
});

// Add auth header to every request
api.interceptors.request.use((config) => {
  const token = getToken();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

/**
 * Get brands with pagination
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Number of items per page
 * @param {string} sortBy - Sort field
 * @returns {Promise<{items: Array, page: number, size: number, totalItems: number, totalPages: number, hasNext: boolean, hasPrevious: boolean}>}
 */
export const getBrands = async (page = 0, size = 10, sortBy = 'id') => {
  const response = await api.get('', {
    params: { page, size, sortBy },
  });
  return response.data.result;
};

/**
 * Create a new brand
 * @param {Object} brandData - Brand data with name
 * @returns {Promise<{id: number, name: string}>}
 */
export const createBrand = async (brandData) => {
  const response = await api.post('', brandData, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return response.data.result;
};

/**
 * Update an existing brand
 * @param {number} id - Brand ID
 * @param {Object} brandData - Brand data with name
 * @returns {Promise<{id: number, name: string}>}
 */
export const updateBrand = async (id, brandData) => {
  const response = await api.put(`/${id}`, brandData, {
    headers: {
      'Content-Type': 'application/json',
    },
  });
  return response.data.result;
};

/**
 * Delete a brand
 * @param {number} id - Brand ID
 * @returns {Promise<void>}
 */
export const deleteBrand = async (id) => {
  await api.delete(`/${id}`);
};
