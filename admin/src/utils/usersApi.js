import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/users';

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
 * Get users with pagination and optional email search
 * @param {number} page - Page number (0-indexed)
 * @param {number} size - Number of items per page
 * @param {string} sortBy - Sort field
 * @param {string} email - Email search filter (optional)
 * @returns {Promise<{items: Array, page: number, size: number, totalItems: number, totalPages: number, hasNext: boolean, hasPrevious: boolean}>}
 */
export const getUsers = async (page = 0, size = 10, sortBy = 'createdAt', email = '') => {
  const params = { page, size, sortBy };
  if (email && email.trim()) {
    params.email = email.trim();
  }
  const response = await api.get('', { params });
  return response.data.result;
};

/**
 * Delete a user
 * @param {string} id - User ID
 * @returns {Promise<void>}
 */
export const deleteUser = async (id) => {
  await api.delete(`/${id}`);
};
