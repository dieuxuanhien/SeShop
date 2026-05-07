import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';

export type CustomerOrder = {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  shippingAddress?: string;
};

export async function getMyOrders(page = 1, size = 20): Promise<PageResponse<CustomerOrder>> {
  const response = await apiClient.get<ApiResponse<PageResponse<CustomerOrder>>>('/orders/me', {
    params: { page: page - 1, size },
  });
  const data = response.data.data;
  return { ...data, page: data.page + 1 };
}

export async function getOrder(orderId: number): Promise<CustomerOrder> {
  const response = await apiClient.get<ApiResponse<CustomerOrder>>(`/orders/${orderId}`);
  return response.data.data;
}

export async function refreshShipment(orderId: number): Promise<{ status: string; trackingNumbers: string[] }> {
  const response = await apiClient.post<ApiResponse<{ status: string; trackingNumbers: string[] }>>(`/orders/${orderId}/track-shipment`);
  return response.data.data;
}
