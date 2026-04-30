export type ProductStatus = 'DRAFT' | 'PUBLISHED' | 'ARCHIVED';
export type VariantStatus = 'ACTIVE' | 'INACTIVE';

export type ProductVariant = {
  id: number;
  skuCode: string;
  size?: string;
  color?: string;
  price: number;
  status: VariantStatus;
};

export type Product = {
  id: number;
  name: string;
  brand?: string;
  description?: string;
  status: ProductStatus;
  variants: ProductVariant[];
};
