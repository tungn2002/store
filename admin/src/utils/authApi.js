import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/auth';

/**
 * Call login API
 * @param {string} email - User email
 * @param {string} password - User password
 * @returns {Promise<{token: string}>} Authentication response with token
 */
export const login = async (email, password) => {
  const response = await axios.post(`${API_BASE_URL}/login`, {
    email,
    password,
  });
  
  // API returns: { result: { token: "..." } }
  return response.data.result;
};

/**
 * Call logout API
 * @param {string} token - JWT token to invalidate
 * @returns {Promise<void>}
 */
export const logout = async (token) => {
  await axios.post(`${API_BASE_URL}/logout`, {
    token,
  });
};
