import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

// ── Types ────────────────────────────────────────────────────

export type CountedItem = {
  variantId: number;
  countedQty: number;
  reasonCode?: string;
};

export type CreateCycleCountRequest = {
  locationId: number;
  reason?: string;
};

export type CycleCountItemsRequest = {
  items: CountedItem[];
};

export type CycleCountResponse = {
  cycleCountId: number;
};

// ── API Calls ────────────────────────────────────────────────

export async function createCycleCount(request: CreateCycleCountRequest): Promise<CycleCountResponse> {
  const response = await apiClient.post<ApiResponse<CycleCountResponse>>('/staff/cycle-counts', request);
  return response.data.data;
}

export async function submitCycleCountItems(cycleCountId: number, request: CycleCountItemsRequest): Promise<void> {
  await apiClient.post(`/staff/cycle-counts/${cycleCountId}/items`, request);
}

export async function approveCycleCount(cycleCountId: number): Promise<void> {
  await apiClient.post(`/staff/cycle-counts/${cycleCountId}/approve`);
}
