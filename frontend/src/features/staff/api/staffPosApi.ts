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

export type PosReturnDisposition = 'RESTOCK' | 'REFURBISH' | 'DISPOSE';

export type PosReturnRequest = {
  originalOrderId: number;
  refundAmount: number;
  reason: string;
  items: Array<{
    variantId: number;
    qty: number;
    disposition: PosReturnDisposition;
  }>;
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

export type ReceiptItemResponse = {
  id: number;
  variantId: number;
  skuCode: string;
  name: string;
  qty: number;
  unitPrice: number;
  totalPrice: number;
};

export type ProcessPosSaleResponse = {
  receiptId: number;
  receiptNumber: string;
  changeDue: number;
  totalAmount: number;
  paymentMethod: string;
  amountPaid: number;
  createdAt: string;
  locationName: string;
  operatorName: string;
  items: ReceiptItemResponse[];
};

export async function processPosSale(items: PosItem[], paymentMethod: 'CASH' | 'CARD', amountPaid: number): Promise<ProcessPosSaleResponse> {
  const response = await apiClient.post<ApiResponse<ProcessPosSaleResponse>>('/pos/receipts', {
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

export async function processPosReturn(request: PosReturnRequest): Promise<{
  id: number;
  originalOrderId: number;
  originalReceiptId: number;
  refundAmount: number;
  reason: string;
  processedAt: string;
}> {
  const response = await apiClient.post<ApiResponse<{
    id: number;
    originalOrderId: number;
    originalReceiptId: number;
    refundAmount: number;
    reason: string;
    processedAt: string;
  }>>('/pos/returns', request);
  return response.data.data;
}

export type ReceiptItemDto = {
  id: number;
  variantId: number;
  skuCode: string;
  name: string;
  qty: number;
  unitPrice: number;
  totalPrice: number;
};

export type ReceiptDto = {
  id: number;
  transactionId?: number;
  receiptNumber: string;
  receiptContent: string;
  issuedAt: string;
  
  totalAmount: number;
  paymentMethod: string;
  amountPaid: number;
  locationName: string;
  operatorName: string;
  items: ReceiptItemDto[];
};

export type PageResponse<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

export async function getReceiptHistory(page = 0, size = 20): Promise<PageResponse<ReceiptDto>> {
  const response = await apiClient.get<ApiResponse<any>>('/pos/receipts', {
    params: { page, size }
  });
  const data = response.data.data;
  return {
    items: data.items || [],
    page: data.page ?? 0,
    size: data.size ?? 20,
    totalElements: data.totalElements ?? 0,
    totalPages: data.totalPages ?? 0,
  };
}

export async function getReceiptDetails(receiptId: number): Promise<ReceiptDto> {
  const response = await apiClient.get<ApiResponse<ReceiptDto>>(`/pos/receipts/${receiptId}`);
  return response.data.data;
}
