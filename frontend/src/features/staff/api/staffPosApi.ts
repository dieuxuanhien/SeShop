import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type PosItem = {
  variantId: number;
  skuCode: string;
  name: string;
  price: number;
  qty: number;
};

export type ProductVariant = {
  id?: number;
  variantId: number;
  skuCode: string;
  productName: string;
  price: number;
};

export type ShiftData = {
  shiftId: number;
  registerName: string;
  openedAt: string;
  transactionCount: number;
  cardPaymentsTotal: number;
  expectedCash: number;
};

export async function lookupProductBySku(skuCode: string): Promise<ProductVariant> {
  const response = await apiClient.get<ApiResponse<ProductVariant>>(`/staff/inventory/balances/sku/${skuCode}`);
  const variant = response.data.data;
  return { ...variant, variantId: variant.variantId ?? variant.id ?? 0 };
}

export async function getCurrentShift(): Promise<ShiftData> {
  const response = await apiClient.get<ApiResponse<ShiftData>>('/pos/shifts/current');
  return response.data.data;
}

export async function processPosSale(items: PosItem[], paymentMethod: 'CASH' | 'CARD', amountPaid: number): Promise<{ receiptId: number; changeDue: number }> {
  const response = await apiClient.post<ApiResponse<{ receiptId: number; changeDue: number }>>('/pos/receipts', {
    paymentMethod,
    amountPaid,
    items,
  });
  return response.data.data;
}

export async function closeShift(shiftId: number, expectedCash: number, actualCash: number): Promise<void> {
  await apiClient.post(`/pos/shifts/${shiftId}/close`, {
    endingCash: actualCash,
    expectedCash,
  });
}
