import { PageScaffold } from '@/shared/ui/PageScaffold';

export function PurchaseOrders() {
  return (
    <PageScaffold
      title="Purchase Orders"
      purpose="Supplier order creation, approval, partial receiving, and inbound stock control shell."
      endpoints={['POST /staff/purchase-orders', 'POST /staff/goods-receipts']}
    />
  );
}
