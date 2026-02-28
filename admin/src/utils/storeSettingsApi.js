import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/store-settings';

/**
 * Get store settings
 * @param {string} token - JWT token
 * @returns {Promise<{id: string, name: string, address: string, createdAt: string, updatedAt: string}>}
 */
export const getStoreSettings = async (token) => {
  const response = await axios.get(API_BASE_URL, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data.result;
};

/**
 * Update store settings
 * @param {string} token - JWT token
 * @param {Object} settingsData - Settings data to update
 * @param {string} settingsData.name - Store name
 * @param {string} settingsData.address - Store address
 * @returns {Promise<{id: string, name: string, address: string, createdAt: string, updatedAt: string}>}
 */
export const updateStoreSettings = async (token, settingsData) => {
  const response = await axios.put(API_BASE_URL, settingsData, {
    headers: {
      Authorization: `Bearer ${token}`,
    },
  });

  return response.data.result;
};
