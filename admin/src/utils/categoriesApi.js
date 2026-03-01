import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/categories';

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
 * Get categories with pagination
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Number of items per page
 * @param {string} sortBy - Sort field
 * @returns {Promise<{items: Array, page: number, size: number, totalItems: number, totalPages: number, hasNext: boolean, hasPrevious: boolean}>}
 */
export const getCategories = async (page = 0, size = 10, sortBy = 'id') => {
  const response = await api.get('', {
    params: { page, size, sortBy },
  });
  return response.data.result;
};

/**
 * Create a new category
 * @param {FormData} formData - Form data with name and image file
 * @returns {Promise<{id: number, name: string, image: string}>}
 */
export const createCategory = async (formData) => {
  const response = await api.post('', formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.result;
};

/**
 * Update an existing category
 * @param {number} id - Category ID
 * @param {FormData} formData - Form data with name and optional image file
 * @returns {Promise<{id: number, name: string, image: string}>}
 */
export const updateCategory = async (id, formData) => {
  const response = await api.put(`/${id}`, formData, {
    headers: {
      'Content-Type': 'multipart/form-data',
    },
  });
  return response.data.result;
};

/**
 * Delete a category
 * @param {number} id - Category ID
 * @returns {Promise<void>}
 */
export const deleteCategory = async (id) => {
  await api.delete(`/${id}`);
};
