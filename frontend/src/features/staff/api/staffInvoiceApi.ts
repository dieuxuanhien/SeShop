import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

// ── Types ────────────────────────────────────────────────────

export type TaxInvoiceResponse = {
  id: number;
  invoiceNumber: string;
  orderId: number;
  totalAmount: number;
  status: string;
  createdAt: string;
};

export type InvoiceAdjustmentResponse = {
  id: number;
  invoiceId: number;
  reason: string;
  deltaAmount: number;
  status: string;
  createdAt: string;
};

// ── API Calls ────────────────────────────────────────────────

export async function createTaxInvoice(orderId: number): Promise<TaxInvoiceResponse> {
  const response = await apiClient.post<ApiResponse<TaxInvoiceResponse>>('/invoices/tax', { orderId });
  return response.data.data;
}

export async function createInvoiceAdjustment(
  invoiceId: number,
  reason: string,
  deltaAmount: number,
): Promise<InvoiceAdjustmentResponse> {
  const response = await apiClient.post<ApiResponse<InvoiceAdjustmentResponse>>(
    `/invoices/${invoiceId}/adjustments`,
    { reason, deltaAmount },
  );
  return response.data.data;
}
