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
      title="My Orders"
      viewCode="CUST_006"
      purpose="Track past purchases, shipment status, payment status, and review detailed product items."
    >
      <Card className="border-primary/20 bg-surface/95 p-6">
        {isLoading ? (
          <p className="text-sm text-ink/60">Loading orders...</p>
        ) : orders.length === 0 ? (
          <EmptyState title="No orders yet" description="Completed checkouts will appear here." />
        ) : (
          <div className="grid gap-6">
            {orders.map((order) => (
              <div key={order.id} className="rounded-md border border-primary/20 bg-surface p-6 shadow-sm transition hover:border-primary/40">
                <div className="flex flex-wrap items-center justify-between gap-4 border-b border-primary/15 pb-4 mb-4">
                  <div>
                    <div className="flex items-center gap-3">
                      <Link to={`/orders/${order.id}`} className="text-lg font-semibold text-ink hover:text-primary underline">
                        Order #{order.orderNumber}
                      </Link>
                      <Badge variant={order.status === 'CANCELLED' ? 'danger' : ['SHIPPED', 'DELIVERED', 'COMPLETED'].includes(order.status) ? 'success' : 'warning'}>
                        {order.status}
                      </Badge>
                    </div>
                    <p className="mt-1 text-sm font-medium text-ink/80">
                      Total: {formatCurrency(Number(order.totalAmount))} {order.currency ? `(${order.currency.toUpperCase()})` : ''}
                    </p>
                  </div>
                  <div className="flex flex-wrap gap-2 text-xs">
                    {order.paymentStatus && (
                      <span className="rounded bg-ink/5 px-2.5 py-1 text-ink/75 border border-primary/10">
                        Payment: <strong className="text-ink">{order.paymentStatus}</strong>
                      </span>
                    )}
                    {order.shipmentStatus && (
                      <span className="rounded bg-ink/5 px-2.5 py-1 text-ink/75 border border-primary/10">
                        Shipping: <strong className="text-ink">{order.shipmentStatus}</strong>
                      </span>
                    )}
                  </div>
                </div>

                {order.shippingAddress && (
                  <div className="mb-4 text-xs text-ink/70 bg-ink/5 p-3 rounded border border-primary/10">
                    <div className="mb-1">
                      <span className="font-semibold text-ink">Shipping Address:</span> {order.shippingAddress}
                    </div>
                    {order.trackingNumber && (
                      <div>
                        <span className="font-semibold text-ink">Tracking:</span>{' '}
                        <a 
                          href={`https://tracking.ghn.dev/?order_code=${order.trackingNumber}`} 
                          target="_blank" 
                          rel="noreferrer" 
                          className="text-primary hover:underline font-medium"
                        >
                          {order.trackingNumber}
                        </a>
                      </div>
                    )}
                  </div>
                )}

                {/* Product Items Details */}
                <div className="space-y-4">
                  <h4 className="text-xs font-semibold uppercase tracking-wider text-ink/60">Order Items</h4>
                  {order.items && order.items.length > 0 ? (
                    <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
                      {order.items.map((item) => (
                        <div key={item.id} className="flex gap-4 p-3 rounded border border-primary/15 bg-surface/50 items-center">
                          {item.imageUrl ? (
                            <img src={item.imageUrl} alt={item.productName} className="h-16 w-16 rounded object-cover border border-primary/20" />
                          ) : (
                            <div className="h-16 w-16 rounded bg-ink/10 flex items-center justify-center text-xs text-ink/40">No img</div>
                          )}
                          <div className="flex-1 min-w-0">
                            <h5 className="text-sm font-semibold text-ink truncate">{item.productName}</h5>
                            <p className="mt-0.5 text-xs text-ink/60 truncate">
                              {item.skuCode} {item.attributes && Object.keys(item.attributes).length > 0 ? `| ${Object.entries(item.attributes).map(([k, v]) => `${k}: ${v}`).join(', ')}` : ''}
                            </p>
                            <div className="mt-1 flex items-center justify-between text-xs font-medium text-ink/80">
                              <span>{formatCurrency(item.unitPrice)} × {item.qty}</span>
                              <span className="font-semibold text-ink">{formatCurrency(item.totalPrice)}</span>
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="text-xs text-ink/50">No item details available.</p>
                  )}
                </div>

                <div className="mt-6 flex justify-end">
                  <Link to={`/orders/${order.id}`}>
                    <span className="text-xs font-semibold text-primary hover:underline">View Full Order Details →</span>
                  </Link>
                </div>
              </div>
            ))}
          </div>
        )}
      </Card>
    </PageScaffold>
  );
}
