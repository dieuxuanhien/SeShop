import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type ConversationTurn = {
  role: 'user' | 'model';
  content: string;
};

export type AiRecommendationResponse = {
  answer?: string;
  items?: Array<{
    productId: number;
    variantId: number;
    productName?: string;
    skuCode?: string;
    attributes?: Record<string, string>;
    price?: number;
    imageUrl?: string;
    description?: string;
    stockAvailable?: number;
    reason: string;
  }>;
};

export type AiRecommendationRequest = {
  message: string;
  context?: Record<string, string>;
  customerId?: number;
  conversationHistory?: ConversationTurn[];
};

export async function getAiRecommendations(request: AiRecommendationRequest): Promise<AiRecommendationResponse> {
  const response = await apiClient.post<ApiResponse<AiRecommendationResponse>>('/assistant/recommendations', request);
  return response.data.data;
}
