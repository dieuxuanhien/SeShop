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
  mediaOrder?: string[];
  status: string;
  createdAt?: string;
};

export type InstagramPublishResult = {
  draftId: number;
  status: string;
  instagramCreationId?: string;
  instagramMediaId?: string;
  instagramPermalink?: string;
  publishedAt?: string;
};

export async function getInstagramStatus(): Promise<InstagramConnection | null> {
  const response = await apiClient.get<ApiResponse<InstagramConnection | null>>('/marketing/instagram/status');
  return response.data.data;
}

export async function getInstagramDrafts(): Promise<InstagramDraft[]> {
  const response = await apiClient.get<ApiResponse<InstagramDraft[]>>('/marketing/drafts');
  return response.data.data;
}

export type InstagramDraftRequest = {
  productId: number;
  caption?: string;
  hashtags?: string;
  mediaOrder?: string[];
  status?: string;
};

export async function startInstagramConnection(): Promise<{ authorizationUrl: string }> {
  const response = await apiClient.post<ApiResponse<{ authorizationUrl: string }>>('/marketing/instagram/connect');
  return response.data.data;
}

export async function reconnectInstagram(): Promise<{ authorizationUrl: string }> {
  const response = await apiClient.post<ApiResponse<{ authorizationUrl: string }>>('/marketing/instagram/reconnect');
  return response.data.data;
}

export async function createInstagramDraft(request: InstagramDraftRequest): Promise<InstagramDraft> {
  const response = await apiClient.post<ApiResponse<InstagramDraft>>('/marketing/drafts', request);
  return response.data.data;
}

export async function updateInstagramDraft(draftId: number, request: InstagramDraftRequest): Promise<InstagramDraft> {
  const response = await apiClient.put<ApiResponse<InstagramDraft>>(`/marketing/drafts/${draftId}`, request);
  return response.data.data;
}

export async function submitInstagramDraftForReview(draftId: number): Promise<InstagramDraft> {
  const response = await apiClient.post<ApiResponse<InstagramDraft>>(`/marketing/drafts/${draftId}/submit-review`);
  return response.data.data;
}

export async function approveInstagramDraft(draftId: number): Promise<InstagramDraft> {
  const response = await apiClient.post<ApiResponse<InstagramDraft>>(`/marketing/drafts/${draftId}/approve`);
  return response.data.data;
}

export async function publishInstagramDraft(draftId: number): Promise<InstagramPublishResult> {
  const response = await apiClient.post<ApiResponse<InstagramPublishResult>>(`/marketing/drafts/${draftId}/publish`);
  return response.data.data;
}
