import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type AiRecommendationResponse = {
  answer?: string;
  items?: Array<{
    productId: number;
    variantId: number;
    reason: string;
  }>;
};

export async function getAiRecommendations(message: string): Promise<AiRecommendationResponse> {
  const response = await apiClient.post<ApiResponse<AiRecommendationResponse>>('/assistant/recommendations', {
    message,
    context: {},
  });
  return response.data.data;
}
