import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';

export type InventoryBalance = {
  id: number;
  locationId: number;
  locationName: string;
  variantId: number;
  skuCode: string;
  productName: string;
  onHandQty: number;
  reservedQty: number;
  availableQty: number;
};

export type StockTransfer = {
  id: number;
  sourceLocationName: string;
  destinationLocationName: string;
  status: 'DRAFT' | 'IN_TRANSIT' | 'COMPLETED' | 'CANCELLED';
  itemCount: number;
  createdAt: string;
};

export type TransferItemRequest = {
  variantId: number;
  qty: number;
};

export type CreateTransferRequest = {
  sourceLocationId: number;
  destinationLocationId: number;
  reason?: string;
  items: TransferItemRequest[];
};

export type ReceiveTransferRequest = {
  receivedItems: Array<{
    variantId: number;
    receivedQty: number;
    damagedQty: number;
  }>;
};

export async function getInventoryBalances(page = 1, size = 20): Promise<PageResponse<InventoryBalance>> {
  const response = await apiClient.get<ApiResponse<PageResponse<InventoryBalance>>>('/staff/inventory/balances', {
    params: { page: page - 1, size },
  });
  const data = response.data.data;
  return { ...data, page: data.page + 1 };
}

export async function adjustInventory(variantId: number, locationId: number, deltaQty: number, reasonCode: string, notes: string): Promise<void> {
  await apiClient.post('/staff/inventory/adjustments', {
    variantId,
    locationId,
    deltaQty,
    reasonCode,
    notes,
  });
}

export async function getStockTransfers(page = 1, size = 20): Promise<PageResponse<StockTransfer>> {
  const response = await apiClient.get<ApiResponse<PageResponse<StockTransfer>>>('/staff/inventory/transfers', {
    params: { page: page - 1, size },
  });
  const data = response.data.data;
  return { ...data, page: data.page + 1 };
}

export async function createStockTransfer(request: CreateTransferRequest): Promise<{ transferId: number }> {
  const response = await apiClient.post<ApiResponse<{ transferId: number }>>('/staff/inventory/transfers', request);
  return response.data.data;
}

export async function approveStockTransfer(transferId: number): Promise<void> {
  await apiClient.post(`/staff/inventory/transfers/${transferId}/approve`);
}

export async function receiveStockTransfer(transferId: number, request: ReceiveTransferRequest): Promise<void> {
  await apiClient.post(`/staff/inventory/transfers/${transferId}/receive`, request);
}
