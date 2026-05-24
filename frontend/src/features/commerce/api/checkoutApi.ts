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
    latitude?: number;
    longitude?: number;
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
  clientSecret?: string;
};

export type ShippingFeeResponse = {
  fee: number;
};

export type AddressValidationResponse = {
  valid: boolean;
  message: string;
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

export async function estimateShippingFee(toAddress: string): Promise<ShippingFeeResponse> {
  const response = await apiClient.post<ApiResponse<ShippingFeeResponse>>('/shipping/estimate-fee', {
    toAddress,
  });
  return response.data.data;
}

export async function validateShippingAddress(ward: string, district: string, city: string): Promise<AddressValidationResponse> {
  const response = await apiClient.post<ApiResponse<AddressValidationResponse>>('/shipping/validate-address', {
    ward,
    district,
    city,
  });
  return response.data.data;
}

export async function estimateStripeFee(amount: number): Promise<{ fee: number }> {
  const response = await apiClient.post<ApiResponse<{ fee: number }>>('/payment/estimate-fee', {
    amount,
  });
  return response.data.data;
}

export type Province = {
  ProvinceID: number;
  ProvinceName: string;
};

export type District = {
  DistrictID: number;
  DistrictName: string;
};

export type Ward = {
  WardCode: string;
  WardName: string;
};

export async function getProvinces(): Promise<Province[]> {
  const response = await apiClient.get<ApiResponse<Province[]>>('/shipping/provinces');
  return response.data.data;
}

export async function getDistricts(provinceId: number): Promise<District[]> {
  const response = await apiClient.get<ApiResponse<District[]>>(`/shipping/districts?provinceId=${provinceId}`);
  return response.data.data;
}

export async function getWards(districtId: number): Promise<Ward[]> {
  const response = await apiClient.get<ApiResponse<Ward[]>>(`/shipping/wards?districtId=${districtId}`);
  return response.data.data;
}
