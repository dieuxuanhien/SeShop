import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type ReturnItemRequest = {
  orderItemId: number;
  qty: number;
};

export type ReturnRequest = {
  orderId: number;
  reason: string;
  items: ReturnItemRequest[];
};

export type ReturnResponse = {
  returnId: number;
  orderId: number;
  reason: string;
  status: string;
  createdAt: string;
};

export type RefundRequest = {
  orderId: number;
  paymentId: number;
  returnRequestId: number;
  amount: number;
};

export type RefundResponse = {
  refundId: number;
  orderId: number;
  amount: number;
  status: string;
  createdAt: string;
};

export async function createReturn(request: ReturnRequest): Promise<ReturnResponse> {
  const response = await apiClient.post<ApiResponse<ReturnResponse>>('/returns', request);
  return response.data.data;
}

export async function approveReturn(returnId: number): Promise<ReturnResponse> {
  const response = await apiClient.post<ApiResponse<ReturnResponse>>(`/returns/${returnId}/approve`);
  return response.data.data;
}

export async function createRefund(request: RefundRequest): Promise<RefundResponse> {
  const response = await apiClient.post<ApiResponse<RefundResponse>>('/refunds', request);
  return response.data.data;
}

export async function getRefund(refundId: number): Promise<RefundResponse> {
  const response = await apiClient.get<ApiResponse<RefundResponse>>(`/refunds/${refundId}`);
  return response.data.data;
}
