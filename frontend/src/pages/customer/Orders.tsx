import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Orders() {
  return (
    <PageScaffold
      title="Customer Orders"
      viewCode="CUST_006"
      purpose="Customer order history, order detail, shipment state, and tracking timeline entry point."
      endpoints={['GET /orders/me', 'GET /orders/{orderId}', 'POST /orders/{orderId}/track-shipment']}
    />
  );
}
