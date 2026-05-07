import { apiClient } from '@/shared/api/client';
import type { ApiResponse, PageResponse } from '@/shared/types/api';
import type { Product, Category } from '@/entities/product/types';
import type { StockAvailability } from '@/entities/inventory/types';

export type ProductListParams = {
  page?: number;
  size?: number;
  sort?: 'newest' | 'price_asc' | 'price_desc' | 'popular';
  search?: string;
  categoryId?: number;
  minPrice?: number;
  maxPrice?: number;
  brand?: string;
};

type BackendProduct = {
  id: number;
  name: string;
  brand?: string;
  description?: string;
  status: string;
  variants: Array<{
    id: number;
    skuCode: string;
    size?: string;
    color?: string;
    price: string | number;
    compareAtPrice?: string | number;
    status: string;
  }>;
};

type BackendPage<T> = {
  items: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
};

function toFrontendProduct(product: BackendProduct): Product {
  return {
    id: product.id,
    name: product.name,
    brand: product.brand,
    description: product.description,
    status: product.status === 'PUBLISHED' ? 'PUBLISHED' : product.status === 'ARCHIVED' ? 'ARCHIVED' : 'DRAFT',
    thumbnailUrl: undefined,
    images: [],
    variants: product.variants.map((variant) => ({
      id: variant.id,
      skuCode: variant.skuCode,
      size: variant.size,
      color: variant.color,
      colorHex: undefined,
      price: Number(variant.price),
      compareAtPrice: variant.compareAtPrice != null ? Number(variant.compareAtPrice) : undefined,
      status: variant.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
    })),
    categories: [],
  };
}

export async function getProducts(params: ProductListParams = {}): Promise<PageResponse<Product>> {
  const sort = params.sort === 'price_asc' ? 'price,asc' : params.sort === 'price_desc' ? 'price,desc' : 'createdAt,desc';
  const response = await apiClient.get<ApiResponse<BackendPage<BackendProduct>>>('/products', {
    params: {
      page: Math.max(0, (params.page ?? 1) - 1),
      size: params.size ?? 9,
      keyword: params.search || undefined,
      brand: params.brand || undefined,
      sort,
    },
  });

  const data = response.data.data;
  return {
    ...data,
    page: data.page + 1,
    items: data.items.map(toFrontendProduct),
  };
}

export async function getProductById(productId: number): Promise<Product | undefined> {
  const response = await apiClient.get<ApiResponse<BackendProduct>>(`/products/${productId}`);
  return toFrontendProduct(response.data.data);
}

export async function getProductAvailability(productId: number): Promise<StockAvailability[]> {
  const response = await apiClient.get<ApiResponse<StockAvailability[]>>(`/products/${productId}/availability`);
  return response.data.data;
}

export async function getCategories(): Promise<Category[]> {
  const response = await apiClient.get<ApiResponse<Category[]>>('/categories');
  return response.data.data;
}
