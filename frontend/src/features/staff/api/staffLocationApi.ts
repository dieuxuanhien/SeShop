import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type StaffLocationDto = {
  id: number;
  code: string;
  displayName: string;
  locationType: string;
  status: string;
};

export async function getStaffLocations(): Promise<StaffLocationDto[]> {
  const response = await apiClient.get<ApiResponse<StaffLocationDto[]>>('/staff/inventory/locations');
  return response.data.data;
}
