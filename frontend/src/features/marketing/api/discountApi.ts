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
