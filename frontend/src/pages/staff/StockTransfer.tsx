import { PageScaffold } from '@/shared/ui/PageScaffold';

export function StockTransfer() {
  return (
    <PageScaffold
      title="Stock Transfer & Logistics"
      viewCode="STAFF_003"
      purpose="Transfer draft, approval, in-transit tracking, receiving, discrepancy, and lifecycle controls."
      endpoints={['POST /staff/inventory/transfers', 'POST /staff/inventory/transfers/{transferId}/approve', 'POST /staff/inventory/transfers/{transferId}/receive']}
    />
  );
}
