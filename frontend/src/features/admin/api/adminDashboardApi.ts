import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type DashboardMetrics = {
  todayRevenue: number;
  revenueChange: number;
  activeOrdersCount: number;
  ordersChange: number;
  lowStockAlertsCount: number;
  alertsChange: number;
  staffOnlineCount: number;
  staffChange: string;
};

export type SystemStatus = {
  service: string;
  status: 'Healthy' | 'Degraded' | 'Down';
};

/**
 * Fetch dashboard KPIs: today's revenue, active orders, low stock alerts, staff online count.
 * These metrics are computed from audit logs, orders, and inventory data on the backend.
 */
export async function getDashboardMetrics(): Promise<DashboardMetrics> {
  const response = await apiClient.get<ApiResponse<DashboardMetrics>>('/admin/dashboard/metrics');
  return response.data.data;
}

/**
 * Fetch system service status for monitoring display.
 */
export async function getSystemStatus(): Promise<SystemStatus[]> {
  const response = await apiClient.get<ApiResponse<SystemStatus[]>>('/admin/system/status');
  return response.data.data;
}
