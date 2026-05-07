import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';

export type StaffOrder = {
  id: number;
  orderNumber: string;
  status: string;
  totalAmount: number;
  shippingAddress: string;
};

export async function getStaffOrders(page = 1, size = 20): Promise<PageResponse<StaffOrder>> {
  const response = await apiClient.get<ApiResponse<PageResponse<StaffOrder>>>('/staff/orders', {
    params: { page: page - 1, size },
  });
  const data = response.data.data;
  return { ...data, page: data.page + 1 };
}

export async function allocateOrder(orderId: number): Promise<void> {
  await apiClient.post(`/staff/orders/${orderId}/allocate`);
}

export async function packOrder(orderId: number): Promise<void> {
  await apiClient.post(`/staff/orders/${orderId}/pack`);
}

export async function shipOrder(orderId: number, trackingNumber: string): Promise<void> {
  await apiClient.post(`/staff/orders/${orderId}/ship`, { carrier: 'GHN', trackingNumber });
}
