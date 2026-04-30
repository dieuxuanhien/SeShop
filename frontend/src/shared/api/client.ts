import axios, { AxiosError } from 'axios';
import { env } from '@/shared/config/env';
import type { ApiError } from '@/shared/types/api';

export const apiClient = axios.create({
  baseURL: env.apiBaseUrl,
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('seshop.accessToken');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  (error: AxiosError<ApiError>) => {
    const apiError = error.response?.data;
    if (apiError) {
      return Promise.reject(apiError);
    }
    return Promise.reject({
      code: 'GEN_NETWORK',
      message: 'Cannot reach SeShop API',
    } satisfies ApiError);
  },
);
