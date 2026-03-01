import axios from 'axios';

const PRODUCTS_API_URL = 'http://localhost:8080/products';
const CATEGORIES_API_URL = 'http://localhost:8080/categories';
const BRANDS_API_URL = 'http://localhost:8080/brands';

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

const productsApi = createApi(PRODUCTS_API_URL);
const categoriesApi = createApi(CATEGORIES_API_URL);
const brandsApi = createApi(BRANDS_API_URL);

// Products
export const getProducts = async (page = 0, size = 10, sortBy = 'id', name = '', categoryId = '', brandId = '') => {
  const params = { page, size, sortBy };
  if (name) params.name = name;
  if (categoryId) params.categoryId = categoryId;
  if (brandId) params.brandId = brandId;
  const response = await productsApi.get('', { params });
  return response.data.result;
};

export const getProduct = async (id) => {
  const response = await productsApi.get(`/${id}`);
  return response.data.result;
};

export const createProduct = async (formData) => {
  const response = await productsApi.post('', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data.result;
};

export const updateProduct = async (id, formData) => {
  const response = await productsApi.put(`/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data.result;
};

export const deleteProduct = async (id) => {
  await productsApi.delete(`/${id}`);
};

// Product Variants
export const getProductVariants = async (productId, page = 0, size = 10, sortBy = 'id') => {
  const response = await productsApi.get(`/${productId}/variants`, { params: { page, size, sortBy } });
  return response.data.result;
};

export const createProductVariant = async (productId, formData) => {
  const response = await productsApi.post(`/${productId}/variants`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data.result;
};

export const updateProductVariant = async (productId, id, formData) => {
  const response = await productsApi.put(`/${productId}/variants/${id}`, formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data.result;
};

export const deleteProductVariant = async (productId, id) => {
  await productsApi.delete(`/${productId}/variants/${id}`);
};

// Categories
export const getCategories = async (page = 0, size = 100) => {
  const response = await categoriesApi.get('', { params: { page, size } });
  return response.data.result;
};

// Brands
export const getBrands = async (page = 0, size = 100) => {
  const response = await brandsApi.get('', { params: { page, size } });
  return response.data.result;
};
