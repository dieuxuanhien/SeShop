import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Reviews() {
  return (
    <PageScaffold
      title="Product Reviews & Ratings"
      viewCode="CUST_007"
      purpose="Verified-purchase review submission with optional image and product review list."
      endpoints={['POST /reviews', 'GET /reviews/product/{productId}']}
    />
  );
}
