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

export function useProductAvailability(productId: number) {
  return useQuery({
    queryKey: ['product-availability', productId],
    queryFn: () => getProductAvailability(productId),
    enabled: productId > 0,
  });
}

export function useCategories() {
  return useQuery({
    queryKey: ['categories'],
    queryFn: () => getCategories(),
    staleTime: 5 * 60 * 1000, // 5 min
  });
}
