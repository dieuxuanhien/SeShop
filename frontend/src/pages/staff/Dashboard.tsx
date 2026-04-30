import { PageScaffold } from '@/shared/ui/PageScaffold';

export function StaffDashboard() {
  return (
    <PageScaffold
      title="Staff Dashboard"
      purpose="Daily operational shell for pending orders, low stock alerts, transfer queue, and POS activity."
      endpoints={['GET /staff/orders', 'GET /staff/inventory/balances']}
    />
  );
}
