import { apiClient } from '@/shared/api/client';
import type { ApiResponse } from '@/shared/types/api';

export type CheckoutRequest = {
  cartId: number;
  shippingAddress: {
    fullName: string;
    phoneNumber: string;
    line1: string;
    ward: string;
    district: string;
    city: string;
  };
  paymentMethod: 'STRIPE' | 'COD';
  discountCode?: string;
};

export type CheckoutResponse = {
  orderId: number;
  orderNumber: string;
  paymentStatus: string;
  shipmentStatus: string;
  totalAmount?: number;
};

export async function validateDiscount(code: string, subtotal: number): Promise<{ valid: boolean; discountAmount: number }> {
  const response = await apiClient.post<ApiResponse<{ valid: boolean; discountAmount: number }>>('/discounts/validate', {
    code,
    orderSubtotal: subtotal,
  });
  return response.data.data;
}

export async function processCheckout(req: CheckoutRequest): Promise<CheckoutResponse> {
  const response = await apiClient.post<ApiResponse<CheckoutResponse>>('/checkout', req);
  return response.data.data;
}
