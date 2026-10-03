import API from '../api/axios';

const BASE = '/databases';

export const databaseApi = {
  getAll: (params = {}) => API.get(BASE, { params }),
  getById: (id) => API.get(`${BASE}/${id}`),
  create: (data) => API.post(BASE, data),
  update: (id, data) => API.put(`${BASE}/${id}`, data),
  remove: (id) => API.delete(`${BASE}/${id}`),
  testConnection: (id) => API.post(`${BASE}/${id}/test`),
  enable: (id) => API.patch(`${BASE}/${id}/enable`),
  disable: (id) => API.patch(`${BASE}/${id}/disable`),
};
