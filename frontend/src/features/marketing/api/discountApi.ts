import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type Discount = {
  id: number;
  code: string;
  discountType: string;
  discountValue: number;
  minSpend?: number;
  maxUses?: number;
  startAt?: string;
  endAt?: string;
  status: string;
};

export async function getDiscounts(): Promise<Discount[]> {
  const response = await apiClient.get<ApiResponse<Discount[]>>('/staff/discounts');
  return response.data.data;
}

export type DiscountMutationRequest = Omit<Discount, 'id'> & {
  id?: number;
};

export async function createDiscount(request: DiscountMutationRequest): Promise<Discount> {
  const response = await apiClient.post<ApiResponse<Discount>>('/staff/discounts', request);
  return response.data.data;
}

export async function updateDiscount(discountId: number, request: DiscountMutationRequest): Promise<Discount> {
  const response = await apiClient.put<ApiResponse<Discount>>(`/staff/discounts/${discountId}`, request);
  return response.data.data;
}

export async function deactivateDiscount(discountId: number): Promise<void> {
  await apiClient.delete(`/staff/discounts/${discountId}`);
}
