import { PageScaffold } from '@/shared/ui/PageScaffold';

export function OrdersManagement() {
  return (
    <PageScaffold
      title="Orders Management"
      viewCode="STAFF_004"
      purpose="Pending online order queue with allocation, pick, pack, ship, and cancellation workflow."
      endpoints={['GET /staff/orders', 'POST /staff/orders/{orderId}/allocate', 'POST /staff/orders/{orderId}/pack', 'POST /staff/orders/{orderId}/ship']}
    />
  );
}
