import { PageScaffold } from '@/shared/ui/PageScaffold';

export function StockAvailability() {
  return (
    <PageScaffold
      title="Stock Availability by Location"
      viewCode="CUST_003"
      purpose="Friendly location availability display using computed available quantity from inventory balances."
      endpoints={['GET /products/{productId}/availability']}
    />
  );
}
