import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type Review = {
  reviewId: number;
  productId: number;
  orderItemId: number;
  rating: number;
  comment: string;
  imageUrl?: string;
  createdAt: string;
};

export type ReviewRequest = {
  orderItemId: number;
  rating: number;
  comment: string;
  imageUrl?: string;
};

export async function getProductReviews(productId: number): Promise<Review[]> {
  const response = await apiClient.get<ApiResponse<Review[]>>(`/reviews/product/${productId}`);
  return response.data.data;
}

export async function createReview(request: ReviewRequest): Promise<Review> {
  const response = await apiClient.post<ApiResponse<Review>>('/reviews', request);
  return response.data.data;
}

export async function uploadReviewImage(file: File): Promise<string> {
  const formData = new FormData();
  formData.append('file', file);
  const response = await apiClient.post<ApiResponse<{ imageUrl: string }>>('/reviews/upload-image', formData, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return response.data.data.imageUrl;
}
