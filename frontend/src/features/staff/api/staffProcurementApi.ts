import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type PurchaseOrderItemRequest = {
  variantId: number;
  orderedQty: number;
  unitCost: number;
};

export type PurchaseOrderRequest = {
  supplierId: number;
  destinationLocationId: number;
  items: PurchaseOrderItemRequest[];
};

export type PurchaseOrderResponse = {
  id: number;
  poNumber: string;
  status: string;
  createdAt: string;
  supplierId: number;
  destinationLocationId: number;
};

export type GoodsReceiptRequest = {
  purchaseOrderId: number;
  receivedAt: string;
  items: Array<{
    variantId: number;
    receivedQty: number;
    damagedQty?: number;
  }>;
};

export type GoodsReceiptResponse = {
  id: number;
  purchaseOrderId: number;
  status: string;
  receivedAt: string;
};

export async function createPurchaseOrder(request: PurchaseOrderRequest): Promise<PurchaseOrderResponse> {
  const response = await apiClient.post<ApiResponse<PurchaseOrderResponse>>('/staff/purchase-orders', request);
  return response.data.data;
}

export async function createGoodsReceipt(request: GoodsReceiptRequest): Promise<GoodsReceiptResponse> {
  const response = await apiClient.post<ApiResponse<GoodsReceiptResponse>>('/staff/goods-receipts', request);
  return response.data.data;
}
