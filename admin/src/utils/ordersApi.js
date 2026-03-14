import axios from 'axios';

const ORDERS_API_URL = 'http://localhost:8080/orders';

const getToken = () => localStorage.getItem('token');

const createApi = (baseURL) => {
  const api = axios.create({ baseURL });
  api.interceptors.request.use((config) => {
    const token = getToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  });
  return api;
};

const ordersApi = createApi(ORDERS_API_URL);

// Get all orders for admin with pagination
export const getAllOrders = async (page = 0, size = 10) => {
  const response = await ordersApi.get('/admin/all', { params: { page, size } });
  return response.data.result;
};

// Get order details by ID (includes items)
export const getOrderById = async (orderId) => {
  const response = await ordersApi.get(`/${orderId}`);
  return response.data.result;
};

export default ordersApi;
