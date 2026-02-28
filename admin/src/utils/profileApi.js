import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/profile';

/**
 * Get user profile
 * @param {string} token - JWT token
 * @returns {Promise<{id: string, name: string, email: string, phoneNumber: string, dateOfBirth: string, gender: string, address: string}>}
 */
export const getProfile = async (token) => {
  const response = await axios.get(API_BASE_URL, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data.result;
};

/**
 * Update user profile
 * @param {string} token - JWT token
 * @param {Object} profileData - Profile data to update
 * @param {string} profileData.name - User name
 * @param {string} profileData.email - User email
 * @param {string} [profileData.phoneNumber] - Phone number
 * @param {string} [profileData.dateOfBirth] - Date of birth
 * @param {string} [profileData.gender] - Gender
 * @param {string} [profileData.address] - Address
 * @returns {Promise<{id: string, name: string, email: string, phoneNumber: string, dateOfBirth: string, gender: string, address: string}>}
 */
export const updateProfile = async (token, profileData) => {
  const response = await axios.put(API_BASE_URL, profileData, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data.result;
};
