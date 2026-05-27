import axios, { AxiosError, AxiosHeaders } from 'axios';
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
  const locationId = localStorage.getItem('seshop.activeLocationId');
  if (locationId) {
    config.headers['X-Location-Id'] = locationId;
  }
  if (typeof FormData !== 'undefined' && config.data instanceof FormData) {
    if (config.headers instanceof AxiosHeaders) {
      config.headers.delete('Content-Type');
    } else if (config.headers) {
      delete (config.headers as Record<string, unknown>)['Content-Type'];
    }
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
