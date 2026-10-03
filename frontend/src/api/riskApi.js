import axiosInstance from './axios';

export const riskApi = {
  getAll: () => axiosInstance.get('/api/risk-assessments'),
  getById: (id) => axiosInstance.get(`/api/risk-assessments/${id}`),
  getByRiskLevel: (level) => axiosInstance.get(`/api/risk-assessments/risk/${level}`),
  getByPiiType: (type) => axiosInstance.get(`/api/risk-assessments/pii/${type}`),
  getByScanId: (scanId) => axiosInstance.get(`/api/risk-assessments/scan/${scanId}`),
  getSummary: () => axiosInstance.get('/api/risk-assessments/summary'),
};
