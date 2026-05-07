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
