import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getMyOrders, type CustomerOrder } from '@/features/commerce/api/orderApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';

export function Orders() {
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getMyOrders()
      .then((page) => setOrders(page.items))
      .catch(() => setOrders([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="Customer Orders"
      viewCode="CUST_006"
      purpose="Track past purchases, shipment status, and order totals."
    >
      <Card className="border-primary/20 bg-surface/95 p-5">
        {isLoading ? (
          <p className="text-sm text-ink/60">Loading orders...</p>
        ) : orders.length === 0 ? (
          <EmptyState title="No orders yet" description="Completed checkouts will appear here." />
        ) : (
          <div className="grid gap-3">
            {orders.map((order) => (
              <Link key={order.id} to={`/orders/${order.id}`} className="rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/80 transition hover:border-primary/40">
                <div className="flex flex-wrap items-center justify-between gap-3">
                  <span className="font-semibold text-ink">{order.orderNumber}</span>
                  <Badge variant={order.status === 'CANCELLED' ? 'danger' : ['SHIPPED', 'DELIVERED', 'COMPLETED'].includes(order.status) ? 'success' : 'warning'}>
                    {order.status}
                  </Badge>
                </div>
                <p className="mt-2 text-sm text-ink/60">{formatCurrency(Number(order.totalAmount))}</p>
                {order.shippingAddress ? <p className="mt-1 text-xs text-ink/45">{order.shippingAddress}</p> : null}
              </Link>
            ))}
          </div>
        )}
      </Card>
    </PageScaffold>
  );
}
