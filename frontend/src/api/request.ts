import axios from 'axios';
import { ElMessage } from 'element-plus';

const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
});

request.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

request.interceptors.response.use(
  (response) => {
    const result = response.data;
    if (result && typeof result.code !== 'undefined' && result.code !== 200) {
      ElMessage.error(result.message || '请求失败');
      return Promise.reject(new Error(result.message || '请求失败'));
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      localStorage.removeItem('userInfo');
      if (window.location.pathname !== '/login') {
        window.location.href = '/login';
      }
      return Promise.reject(error);
    }
    if (error.response?.status === 403) {
      ElMessage.error('无管理员权限，禁止访问后台功能');
      return Promise.reject(error);
    }
    ElMessage.error(error.response?.data?.message || '网络异常，请稍后重试');
    return Promise.reject(error);
  },
);

export default request;
