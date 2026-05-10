import { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getOrder, refreshShipment, type CustomerOrder } from '@/features/commerce/api/orderApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';

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
      purpose="Follow shipment progress and refresh carrier tracking details."
    >
      <Card className="border-primary/20 bg-surface/95 p-5">
        {isLoading ? (
          <p className="text-sm text-ink/60">Loading order...</p>
        ) : !order ? (
          <p className="text-sm text-ink/60">Order was not found.</p>
        ) : (
          <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_280px]">
            <div>
              <div className="flex flex-wrap items-center justify-between gap-3">
                <div>
                  <p className="text-xs uppercase tracking-wide text-ink/50">Order</p>
                  <h2 className="mt-1 text-xl font-semibold text-ink">{order.orderNumber}</h2>
                </div>
                <Badge variant={order.status === 'CANCELLED' ? 'danger' : ['SHIPPED', 'DELIVERED', 'COMPLETED'].includes(order.status) ? 'success' : 'warning'}>
                  {order.status}
                </Badge>
              </div>
              <div className="mt-5 grid gap-3 rounded-md border border-primary/15 bg-ink/[0.03] p-4 text-sm text-ink/70">
                <div className="flex justify-between gap-3">
                  <span>Total</span>
                  <span className="font-semibold text-ink">{formatCurrency(Number(order.totalAmount))}</span>
                </div>
                <div className="flex justify-between gap-3">
                  <span>Shipment</span>
                  <span className="font-semibold text-ink">{shipment?.status ?? 'Pending'}</span>
                </div>
                <div className="flex justify-between gap-3">
                  <span>Tracking</span>
                  <span className="font-semibold text-ink">{shipment?.trackingNumbers?.join(', ') || 'Not assigned'}</span>
                </div>
              </div>
            </div>
            <div className="rounded-md border border-primary/15 bg-ink/[0.03] p-4">
              <h3 className="text-sm font-semibold text-ink">Delivery Timeline</h3>
              <ol className="mt-4 grid gap-3 text-sm text-ink/65">
                {['Placed', 'Allocated', 'Packed', 'Shipped', 'Delivered'].map((step) => (
                  <li key={step} className="flex items-center gap-2">
                    <span className="size-2 rounded-full bg-primary" />
                    {step}
                  </li>
                ))}
              </ol>
              <Button className="mt-5 w-full" variant="secondary" onClick={() => refreshShipment(id).then(setShipment).catch(() => undefined)}>
                Refresh Tracking
              </Button>
            </div>
          </div>
        )}
      </Card>
    </PageScaffold>
  );
}
