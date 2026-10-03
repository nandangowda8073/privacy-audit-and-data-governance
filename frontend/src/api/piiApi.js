import axiosInstance from './axios';

export const piiApi = {
  // Detection
  detectText: (text, source = 'TEXT_INPUT') =>
    axiosInstance.post('/api/pii/detect', { text, source }),

  detectFile: (file) => {
    const form = new FormData();
    form.append('file', file);
    return axiosInstance.post('/api/pii/file', form, {
      headers: { 'Content-Type': 'multipart/form-data' },
    });
  },

  // Dashboard data
  getStats:   () => axiosInstance.get('/api/pii/stats'),
  getHistory: () => axiosInstance.get('/api/pii/history'),
  getTypes:   () => axiosInstance.get('/api/pii/types'),
  getCategories: () => axiosInstance.get('/api/pii/categories'),

  // Custom patterns
  getPatterns:          () => axiosInstance.get('/api/pii/custom-pattern'),
  createPattern:  (dto) => axiosInstance.post('/api/pii/custom-pattern', dto),
  updatePattern: (id, dto) => axiosInstance.put(`/api/pii/custom-pattern/${id}`, dto),
  deletePattern:  (id) => axiosInstance.delete(`/api/pii/custom-pattern/${id}`),
};
