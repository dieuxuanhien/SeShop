import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getOrder, refreshShipment, type CustomerOrder } from '@/features/commerce/api/orderApi';

export function OrderTracking() {
  const { orderId } = useParams<{ orderId: string }>();
  const id = Number(orderId);
  const [order, setOrder] = useState<CustomerOrder | null>(null);
  const [shipment, setShipment] = useState<{ status: string; trackingNumbers: string[] } | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    if (!id) return;
    Promise.all([
      getOrder(id),
      refreshShipment(id).catch(() => null),
    ])
      .then(([orderData, shipmentData]) => {
        setOrder(orderData);
        setShipment(shipmentData);
      })
      .catch(() => setOrder(null))
      .finally(() => setIsLoading(false));
  }, [id]);

  return (
    <PageScaffold
      title="Order Tracking & Shipment Status"
      viewCode="CUST_006"
      purpose="Shipment timeline, carrier tracking metadata, delivery status, and customer notification surface."
      endpoints={['GET /orders/{orderId}', 'POST /orders/{orderId}/track-shipment']}
    >
      {isLoading ? (
        <p className="text-sm text-ink/60">Loading order...</p>
      ) : !order ? (
        <p className="text-sm text-ink/60">Order was not returned by the API.</p>
      ) : (
        <div className="grid gap-3 text-sm text-ink/80">
          <p className="font-semibold">{order.orderNumber}</p>
          <p>Status: {order.status}</p>
          <p>Shipment: {shipment?.status ?? 'No shipment status returned'}</p>
          <p>Tracking: {shipment?.trackingNumbers?.join(', ') || 'No tracking number returned'}</p>
        </div>
      )}
    </PageScaffold>
  );
}
