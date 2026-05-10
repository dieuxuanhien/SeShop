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
  id?: number;
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
  const data = response.data.data;
  return { ...data, shiftId: data.shiftId ?? data.id ?? 0 };
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
    actualCash,
    expectedCash,
  });
}

export async function openShift(locationId: number, startingCash: number): Promise<ShiftData> {
  const response = await apiClient.post<ApiResponse<ShiftData>>('/pos/shifts/open', {
    locationId,
    startingCash,
  });
  const data = response.data.data;
  return { ...data, shiftId: data.shiftId ?? data.id ?? 0 };
}

export async function processPosReturn(originalOrderId: number, refundAmount: number, reason: string): Promise<{
  id: number;
  originalOrderId: number;
  refundAmount: number;
  reason: string;
  processedAt: string;
}> {
  const response = await apiClient.post<ApiResponse<{
    id: number;
    originalOrderId: number;
    refundAmount: number;
    reason: string;
    processedAt: string;
  }>>('/pos/returns', {
    originalOrderId,
    refundAmount,
    reason,
  });
  return response.data.data;
}
