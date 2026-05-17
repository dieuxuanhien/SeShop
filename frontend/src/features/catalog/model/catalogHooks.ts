import { useQuery } from '@tanstack/react-query';
import {
  getProducts,
  getProductById,
  getProductAvailability,
  getCategories,
  type ProductListParams,
} from '../api/catalogApi';

export function useProducts(params: ProductListParams = {}) {
  return useQuery({
    queryKey: ['products', params],
    queryFn: () => getProducts(params),
  });
}

export function useProduct(productId: number) {
  return useQuery({
    queryKey: ['product', productId],
    queryFn: () => getProductById(productId),
    enabled: productId > 0,
  });
}

export function useProductAvailability(productId: number, variantId?: number, enabled = true) {
  return useQuery({
    queryKey: ['product-availability', productId, variantId ?? 'product'],
    queryFn: () => getProductAvailability(productId, variantId),
    enabled: enabled && productId > 0,
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => getCategories(),
    staleTime: 5 * 60 * 1000, // 5 min
  });
}
