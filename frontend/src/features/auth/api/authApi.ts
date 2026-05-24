import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';
import type { AuthUser } from '@/entities/user/types';

export type LoginPayload = {
  usernameOrEmail: string;
  password: string;
};

export type LoginResponse = {
  accessToken: string;
  user: AuthUser;
};

export type RegisterPayload = {
  username: string;
  email: string;
  phoneNumber: string;
  password: string;
};

export type RegisterResponse = {
  userId: number;
  status: string;
  userType: string;
};

export async function login(payload: LoginPayload) {
  const response = await apiClient.post<ApiResponse<LoginResponse>>('/auth/login', payload);
  return response.data.data;
}

export async function register(payload: RegisterPayload) {
  const response = await apiClient.post<ApiResponse<RegisterResponse>>('/auth/register', payload);
  return response.data.data;
}

export async function getMe() {
  const response = await apiClient.get<ApiResponse<AuthUser>>('/auth/me');
  return response.data.data;
}

export async function updateProfile(payload: { username: string; email: string; phoneNumber: string }) {
  const response = await apiClient.put<ApiResponse<AuthUser>>('/auth/me', payload);
  return response.data.data;
}

export async function updatePassword(payload: { currentPassword: string; newPassword: string }) {
  const response = await apiClient.put<ApiResponse<void>>('/auth/me/password', payload);
  return response.data.data;
}
