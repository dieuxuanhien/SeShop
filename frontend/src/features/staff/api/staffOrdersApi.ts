import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';

export type StaffOrder = {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus: string;
  shipmentStatus: string;
  totalAmount: number;
  currency: string;
  shippingAddress: string;
  allocatedLocations?: string[];
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

export type ShipOrderPayload = {
  recipientName: string;
  recipientPhone: string;
  trackingNumber?: string;
};

export async function shipOrder(orderId: number, payload: ShipOrderPayload): Promise<void> {
  await apiClient.post(`/staff/orders/${orderId}/ship`, { carrier: 'GHN', ...payload });
}

export type OrderItemDto = {
  id: number;
  variantId: number;
  productName: string;
  qty: number;
  unitPrice: number;
  totalPrice: number;
};

export type StaffOrderDetails = StaffOrder & {
  items: OrderItemDto[];
};

export async function getStaffOrderDetails(orderId: number): Promise<StaffOrderDetails> {
  const response = await apiClient.get<ApiResponse<StaffOrderDetails>>(`/staff/orders/${orderId}`);
  return response.data.data;
}

export type PaymentDto = {
  id: number;
  amount: number;
  provider: string;
  status: string;
};

export async function getOrderPayments(orderId: number): Promise<PaymentDto[]> {
  const response = await apiClient.get<ApiResponse<PaymentDto[]>>(`/staff/orders/${orderId}/payments`);
  return response.data.data;
}
