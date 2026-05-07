import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type CartItem = {
  id: number;
  variantId: number;
  skuCode: string;
  name: string;
  qty: number;
  unitPrice: number;
};

export type MyCart = {
  id: number;
  customerId: number;
  status: string;
  items: CartItem[];
};

export async function getMyCart(): Promise<MyCart> {
  const response = await apiClient.get<ApiResponse<MyCart>>('/carts/me');
  return response.data.data;
}

export async function addCartItem(variantId: number, qty: number): Promise<MyCart> {
  const response = await apiClient.post<ApiResponse<MyCart>>('/carts/me/items', { variantId, qty });
  return response.data.data;
}

export async function updateCartItem(itemId: number, qty: number): Promise<MyCart> {
  const response = await apiClient.patch<ApiResponse<MyCart>>(`/carts/me/items/${itemId}`, { qty });
  return response.data.data;
}

export async function removeCartItem(itemId: number): Promise<void> {
  await apiClient.delete(`/carts/me/items/${itemId}`);
}
