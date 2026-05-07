import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type InstagramConnection = {
  accountId?: string;
  accountName?: string;
  status: string;
  tokenExpiresAt?: string;
};

export type InstagramDraft = {
  id: number;
  productId: number;
  caption?: string;
  hashtags?: string;
  status: string;
  createdAt?: string;
};

export async function getInstagramStatus(): Promise<InstagramConnection | null> {
  const response = await apiClient.get<ApiResponse<InstagramConnection | null>>('/marketing/instagram/status');
  return response.data.data;
}

export async function getInstagramDrafts(): Promise<InstagramDraft[]> {
  const response = await apiClient.get<ApiResponse<InstagramDraft[]>>('/marketing/drafts');
  return response.data.data;
}
