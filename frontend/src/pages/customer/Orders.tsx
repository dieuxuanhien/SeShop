import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getMyOrders, type CustomerOrder } from '@/features/commerce/api/orderApi';
import { formatCurrency } from '@/shared/lib/formatters';

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
      purpose="Customer order history, order detail, shipment state, and tracking timeline entry point."
      endpoints={['GET /orders/me', 'GET /orders/{orderId}', 'POST /orders/{orderId}/track-shipment']}
    >
      {isLoading ? (
        <p className="text-sm text-ink/60">Loading orders...</p>
      ) : orders.length === 0 ? (
        <p className="text-sm text-ink/60">No orders returned by the API.</p>
      ) : (
        <div className="grid gap-3">
          {orders.map((order) => (
            <Link key={order.id} to={`/orders/${order.id}`} className="rounded-md border border-primary/15 bg-ink/5 p-3 text-sm text-ink/80 hover:border-primary/40">
              <div className="flex items-center justify-between">
                <span className="font-semibold">{order.orderNumber}</span>
                <span>{order.status}</span>
              </div>
              <p className="mt-1 text-xs text-ink/60">{formatCurrency(Number(order.totalAmount))}</p>
            </Link>
          ))}
        </div>
      )}
    </PageScaffold>
  );
}
