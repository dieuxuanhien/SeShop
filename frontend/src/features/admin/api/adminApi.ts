import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';
import type { InventoryBalance } from '@/features/staff/api/staffInventoryApi';

export type AuditLog = {
  id: number;
  action: string;
  actor: string;
  target: string;
  status: string;
  createdAt: string;
};

export type Role = {
  id: number;
  name: string;
  description?: string;
  status: string;
};

type RolesResponse = {
  items: Role[];
};

export async function getAuditLogs(): Promise<AuditLog[]> {
  const response = await apiClient.get<ApiResponse<AuditLog[]>>('/admin/audit-logs');
  return response.data.data;
}

export async function getRoles(): Promise<Role[]> {
  const response = await apiClient.get<ApiResponse<RolesResponse>>('/admin/roles');
  return response.data.data.items;
}

export type LocationSummary = {
  id: number;
  name: string;
  skus: number;
};

export function locationsFromBalances(balances: InventoryBalance[]): LocationSummary[] {
  const byLocation = new Map<number, LocationSummary>();
  balances.forEach((balance) => {
    const existing = byLocation.get(balance.locationId);
    if (existing) {
      existing.skus += 1;
      return;
    }
    byLocation.set(balance.locationId, {
      id: balance.locationId,
      name: balance.locationName,
      skus: 1,
    });
  });
  return [...byLocation.values()];
}
