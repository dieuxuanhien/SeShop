import { PageScaffold } from '@/shared/ui/PageScaffold';

export function InventoryAdjustment() {
  return (
    <PageScaffold
      title="Inventory Adjustment & Movement"
      viewCode="STAFF_002"
      purpose="Stock adjustment, low stock, cycle count, purchase order receiving, and movement controls."
      endpoints={['GET /staff/inventory/balances', 'POST /staff/inventory/adjustments', 'POST /staff/cycle-counts', 'POST /staff/goods-receipts']}
    />
  );
}
