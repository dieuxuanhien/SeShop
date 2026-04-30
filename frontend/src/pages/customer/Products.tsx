import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Products() {
  return (
    <PageScaffold
      title="Product Listing"
      viewCode="CUST_001"
      purpose="Published product grid with keyword, category, price, size, color, brand, pagination, and sorting controls."
      endpoints={['GET /products']}
    />
  );
}
