import axios from 'axios';

const API = axios.create({
  baseURL: 'http://localhost:8085/api/v1',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to attach Bearer token
API.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to catch 401 unauthorized and 403 forbidden
API.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('user');
      if (window.location.pathname !== '/login' && window.location.pathname !== '/register') {
        window.location.href = '/login';
      }
    }
    if (error.response && error.response.status === 403) {
      // Dispatch a global event components can listen to if needed
      window.dispatchEvent(new CustomEvent('rbac:access-denied', { detail: error.response.data?.message || 'Access denied' }));
    }
    return Promise.reject(error);
  }
);

export default API;
