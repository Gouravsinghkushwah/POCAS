import axios from 'axios';

const API_BASE_URL = process.env.REACT_APP_API_URL || 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 10000, // 10 second timeout
});

// Add response interceptor for error handling
api.interceptors.response.use(
  (response) => response,
  (error) => {
    console.error('API Error:', error);
    if (error.code === 'ECONNREFUSED') {
      console.error('Backend server is not running. Please start the Spring Boot application.');
    }
    return Promise.reject(error);
  }
);

// Customer APIs
export const customerAPI = {
  getAll: () => api.get('/customers'),
  getById: (id) => api.get(`/customers/${id}`),
  create: (data) => api.post('/customers', data),
  getTotalCount: () => api.get('/customers/total-count').then(response => response.data),
};

// Account APIs
export const accountAPI = {
  getAll: () => api.get('/accounts').then(response => response.data.data),
  getById: (id) => api.get(`/accounts/${id}`).then(response => response.data.data),
  create: (data) => api.post('/accounts', data).then(response => response.data.data),
};

// Daily Collection APIs
export const collectionAPI = {
  getAll: () => api.get('/daily-collections/all').then(response => response.data.data),
  getAllPaginated: (page, size, search) => 
    api.get(`/daily-collections/paginated?page=${page - 1}&size=${size}&search=${encodeURIComponent(search || '')}`).then(response => response.data),
  getByAccount: (accountId) => api.get(`/daily-collections/account/${accountId}`).then(response => response.data.data),
  getByCustomer: (customerId) => api.get(`/daily-collections/customer/${customerId}`).then(response => response.data.data),
  create: (data) => api.post('/daily-collections', data).then(response => response.data.data),
  getMonthlySummary: (accountId, month, year) => 
    api.get(`/daily-collections/account/${accountId}/month-summary?month=${month}&year=${year}`).then(response => response.data.data),
  getMonthlyAccountSummary: (accountId, month, year) => 
    api.get(`/daily-collections/account/${accountId}/month-summary-detailed?month=${month}&year=${year}`).then(response => response.data.data),
  getPaymentStatus: (accountId) => 
    api.get(`/daily-collections/collectionAccount/${accountId}/payment-status`).then(response => response.data.data),
  getMonthlyPaymentSummary: (accountId, month, year) => 
    api.get(`/daily-collections/collectionAccount/${accountId}/monthly-payment-summary?month=${month}&year=${year}`).then(response => response.data.data),
};

export default api;
