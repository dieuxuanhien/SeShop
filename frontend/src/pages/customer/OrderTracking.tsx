import { PageScaffold } from '@/shared/ui/PageScaffold';

export function OrderTracking() {
  return (
    <PageScaffold
      title="Order Tracking & Shipment Status"
      viewCode="CUST_006"
      purpose="Shipment timeline, carrier tracking metadata, delivery status, and customer notification surface."
      endpoints={['GET /orders/{orderId}', 'POST /orders/{orderId}/track-shipment']}
    />
  );
}
