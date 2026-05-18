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
  permissionCodes: string[];
};

type RolesResponse = {
  items: Role[];
};

export type Permission = {
  id: number;
  code: string;
  description?: string;
};

type PermissionsResponse = {
  items: Permission[];
};

export type UserRoleAssignment = {
  assignmentId: number;
  roleId: number;
  roleName: string;
  assignedAt?: string;
};

export type StaffLocationAssignment = {
  assignmentId: number;
  locationId: number;
  locationName: string;
  assignedAt?: string;
};

export type AdminUser = {
  id: number;
  username: string;
  email: string;
  phoneNumber: string;
  userType: 'ADMIN' | 'STAFF' | 'CUSTOMER';
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
  roles: UserRoleAssignment[];
  assignedLocations: StaffLocationAssignment[];
  permissions: string[];
};

type UsersResponse = {
  items: AdminUser[];
};

export type RoleMutationRequest = {
  name: string;
  description?: string;
  status: 'ACTIVE' | 'INACTIVE';
};

export type UserMutationRequest = {
  username: string;
  email: string;
  phoneNumber: string;
  password?: string;
  userType: 'ADMIN' | 'STAFF' | 'CUSTOMER';
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED';
};

export async function getAuditLogs(): Promise<AuditLog[]> {
  const response = await apiClient.get<ApiResponse<AuditLog[]>>('/admin/audit-logs');
  return response.data.data;
}

export async function getRoles(): Promise<Role[]> {
  const response = await apiClient.get<ApiResponse<RolesResponse>>('/admin/roles');
  return response.data.data.items;
}

export async function getPermissions(): Promise<Permission[]> {
  const response = await apiClient.get<ApiResponse<PermissionsResponse>>('/admin/permissions');
  return response.data.data.items;
}

export async function createRole(name: string, description?: string): Promise<Role> {
  const response = await apiClient.post<ApiResponse<Role>>('/admin/roles', { name, description });
  return response.data.data;
}

export async function updateRole(roleId: number, request: RoleMutationRequest): Promise<Role> {
  const response = await apiClient.put<ApiResponse<Role>>(`/admin/roles/${roleId}`, request);
  return response.data.data;
}

export async function deleteRole(roleId: number): Promise<void> {
  await apiClient.delete(`/admin/roles/${roleId}`);
}

export async function assignPermissions(roleId: number, permissionCodes: string[]): Promise<void> {
  await apiClient.post(`/admin/roles/${roleId}/permissions`, { permissionCodes });
}

export async function assignRoleToUser(userId: number, roleId: number): Promise<void> {
  await apiClient.post(`/admin/users/${userId}/roles`, { roleId });
}

export async function revokeRoleFromUser(userId: number, assignmentId: number): Promise<void> {
  await apiClient.delete(`/admin/users/${userId}/roles/${assignmentId}`);
}

export async function assignLocationToUser(userId: number, locationId: number): Promise<void> {
  await apiClient.post(`/admin/users/${userId}/locations`, { locationId });
}

export async function revokeLocationFromUser(userId: number, assignmentId: number): Promise<void> {
  await apiClient.delete(`/admin/users/${userId}/locations/${assignmentId}`);
}

export async function getUsers(): Promise<AdminUser[]> {
  const response = await apiClient.get<ApiResponse<UsersResponse>>('/admin/users');
  return response.data.data.items;
}

export async function createUser(request: UserMutationRequest): Promise<AdminUser> {
  const response = await apiClient.post<ApiResponse<AdminUser>>('/admin/users', request);
  return response.data.data;
}

export async function updateUser(userId: number, request: UserMutationRequest): Promise<AdminUser> {
  const response = await apiClient.put<ApiResponse<AdminUser>>(`/admin/users/${userId}`, request);
  return response.data.data;
}

export async function deleteUser(userId: number): Promise<void> {
  await apiClient.delete(`/admin/users/${userId}`);
}

export type LocationSummary = {
  id: number;
  code?: string;
  name: string;
  type?: string;
  status?: string;
  skus: number;
};

export type AdminLocation = {
  id: number;
  code: string;
  displayName: string;
  locationType: 'STORE' | 'STORAGE';
  status: 'ACTIVE' | 'INACTIVE';
};

type LocationsResponse = {
  items: AdminLocation[];
};

export async function getLocations(): Promise<AdminLocation[]> {
  const response = await apiClient.get<ApiResponse<LocationsResponse>>('/admin/locations');
  return response.data.data.items;
}

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
      code: undefined,
      name: balance.locationName,
      type: undefined,
      status: undefined,
      skus: 1,
    });
  });
  return [...byLocation.values()];
}
