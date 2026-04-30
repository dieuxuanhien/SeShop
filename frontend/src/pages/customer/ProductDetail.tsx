import { PageScaffold } from '@/shared/ui/PageScaffold';

export function ProductDetail() {
  return (
    <PageScaffold
      title="Product Detail & Variant Selection"
      viewCode="CUST_002"
      purpose="Product media, SKU selection, pricing, reviews, recommendations, and stock availability by selected variant."
      endpoints={['GET /products/{productId}', 'GET /products/{productId}/availability']}
    />
  );
}
