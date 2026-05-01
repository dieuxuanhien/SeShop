export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type VariantStatus = 'ACTIVE' | 'INACTIVE';

export type ProductImage = {
  id: number;
  url: string;
  altText?: string;
  sortOrder: number;
};

export type ProductVariant = {
  id: number;
  skuCode: string;
  size?: string;
  color?: string;
  colorHex?: string;
  price: number;
  compareAtPrice?: number;
  status: VariantStatus;
};

export type ProductReviewSummary = {
  averageRating: number;
  reviewCount: number;
};

export type Category = {
  id: number;
  name: string;
  slug: string;
  parentId?: number;
  productCount?: number;
};

export type Product = {
  id: number;
  name: string;
  brand?: string;
  description?: string;
  status: ProductStatus;
  thumbnailUrl?: string;
  images: ProductImage[];
  variants: ProductVariant[];
  categories: Category[];
  reviewSummary?: ProductReviewSummary;
  createdAt?: string;
};
