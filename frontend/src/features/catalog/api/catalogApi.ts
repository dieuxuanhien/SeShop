import type { Product, Category } from '@/entities/product/types';
import type { StockAvailability } from '@/entities/inventory/types';
import type { PageResponse } from '@/shared/types/api';
import { mockProducts, mockCategories, getMockAvailability } from '@/shared/lib/mockData';

// ── Request Types ───────────────────────────────────────────
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

// ── Mock Implementations ────────────────────────────────────
// These functions simulate API calls using mock data.
// Replace the body with real `apiClient.get(...)` calls when the backend is ready.

function delay(ms = 300) {
  return new Promise((resolve) => setTimeout(resolve, ms));
}

export async function getProducts(params: ProductListParams = {}): Promise<PageResponse<Product>> {
  await delay();
  let filtered = [...mockProducts];

  if (params.search) {
    const q = params.search.toLowerCase();
    filtered = filtered.filter(
      (p) => p.name.toLowerCase().includes(q) || p.brand?.toLowerCase().includes(q),
    );
  }

  if (params.categoryId) {
    filtered = filtered.filter((p) => p.categories.some((c) => c.id === params.categoryId));
  }

  if (params.brand) {
    filtered = filtered.filter((p) => p.brand === params.brand);
  }

  if (params.minPrice != null) {
    filtered = filtered.filter((p) => p.variants.some((v) => v.price >= params.minPrice!));
  }
  if (params.maxPrice != null) {
    filtered = filtered.filter((p) => p.variants.some((v) => v.price <= params.maxPrice!));
  }

  // Sort
  switch (params.sort) {
    case 'price_asc':
      filtered.sort((a, b) => (a.variants[0]?.price ?? 0) - (b.variants[0]?.price ?? 0));
      break;
    case 'price_desc':
      filtered.sort((a, b) => (b.variants[0]?.price ?? 0) - (a.variants[0]?.price ?? 0));
      break;
    case 'popular':
      filtered.sort((a, b) => (b.reviewSummary?.reviewCount ?? 0) - (a.reviewSummary?.reviewCount ?? 0));
      break;
    case 'newest':
    default:
      filtered.sort((a, b) => (b.createdAt ?? '').localeCompare(a.createdAt ?? ''));
      break;
  }

  const page = params.page ?? 1;
  const size = params.size ?? 9;
  const start = (page - 1) * size;
  const items = filtered.slice(start, start + size);

  return {
    items,
    page,
    size,
    totalElements: filtered.length,
    totalPages: Math.ceil(filtered.length / size),
  };
}

export async function getProductById(productId: number): Promise<Product | undefined> {
  await delay(200);
  return mockProducts.find((p) => p.id === productId);
}

export async function getProductAvailability(productId: number): Promise<StockAvailability[]> {
  await delay(250);
  return getMockAvailability(productId);
}

export async function getCategories(): Promise<Category[]> {
  await delay(150);
  return mockCategories;
}
