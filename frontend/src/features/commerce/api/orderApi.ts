import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';

export type OrderItem = {
  id: number;
  variantId: number;
  productName: string;
  skuCode?: string;
  attributes?: Record<string, string>;
  imageUrl?: string;
  qty: number;
  unitPrice: number;
  totalPrice: number;
};

export type CustomerOrder = {
  id: number;
  orderNumber: string;
  status: string;
  paymentStatus?: string;
  shipmentStatus?: string;
  totalAmount: number;
  currency?: string;
  shippingAddress?: string;
  trackingNumber?: string;
  items?: OrderItem[];
};

export type ShipmentTrackingEvent = {
  code: string;
  label: string;
  state: 'COMPLETED' | 'PENDING';
  occurredAt?: string;
};

export type ShipmentTracking = {
  status: string;
  trackingNumbers: string[];
  events: ShipmentTrackingEvent[];
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

export async function refreshShipment(orderId: number): Promise<ShipmentTracking> {
  const response = await apiClient.post<ApiResponse<ShipmentTracking>>(`/orders/${orderId}/track-shipment`);
  return response.data.data;
}
