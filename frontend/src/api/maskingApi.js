import axiosInstance from './axios';

export const maskingApi = {
  getPolicies: () => axiosInstance.get('/api/masking/policies'),
  getPolicyById: (id) => axiosInstance.get(`/api/masking/policies/${id}`),
  createPolicy: (data) => axiosInstance.post('/api/masking/policies', data),
  updatePolicy: (id, data) => axiosInstance.put(`/api/masking/policies/${id}`, data),
  deletePolicy: (id) => axiosInstance.delete(`/api/masking/policies/${id}`),
  preview: (data) => axiosInstance.post('/api/masking/preview', data),
};
