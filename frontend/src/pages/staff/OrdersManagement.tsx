import { useState, useEffect } from 'react';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Badge } from '@/shared/ui/Badge';
import { getStaffOrders, allocateOrder, packOrder, shipOrder, type StaffOrder } from '@/features/staff/api/staffOrdersApi';
import { Spinner } from '@/shared/ui/Spinner';

export function OrdersManagement() {
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);

  const fetchOrders = async () => {
    setIsLoading(true);
    try {
      const res = await getStaffOrders();
      setOrders(res.items);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, []);

  const handleAction = async (orderId: number, action: 'allocate' | 'pack' | 'ship') => {
    setActionLoading(orderId);
    try {
      if (action === 'allocate') await allocateOrder(orderId);
      if (action === 'pack') await packOrder(orderId);
      if (action === 'ship') await shipOrder(orderId, `TRACK-${orderId}`);
      await fetchOrders();
    } catch (e) {
      console.error(e);
    } finally {
      setActionLoading(null);
    }
  };

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h1 className="font-display text-2xl font-semibold text-surface">Orders Management</h1>
          <p className="mt-1 text-sm text-surface/60">Manage order fulfillment workflow.</p>
        </div>
      </div>

      <Card className="border-primary/20 bg-surface/95">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-primary/10">
            <thead className="bg-ink/[0.03]">
              <tr>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Order</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Customer</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Total</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Payment</th>
                <th scope="col" className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Status</th>
                <th scope="col" className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary/10 bg-surface">
              {orders.map((order) => (
                <tr key={order.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-ink">{order.orderNumber}</td>
                  <td className="max-w-xs truncate px-6 py-4 whitespace-nowrap text-sm text-ink/60">{order.shippingAddress}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-ink/60">{order.totalAmount.toLocaleString()} VND</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-ink/60">Gateway</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Badge variant={order.status === 'SHIPPED' || order.status === 'DELIVERED' ? 'success' : order.status === 'CANCELLED' ? 'danger' : 'warning'}>
                      {order.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {order.status === 'PENDING' && (
                      <Button size="sm" onClick={() => handleAction(order.id, 'allocate')} isLoading={actionLoading === order.id}>
                        Allocate
                      </Button>
                    )}
                    {order.status === 'ALLOCATED' && (
                      <Button size="sm" onClick={() => handleAction(order.id, 'pack')} isLoading={actionLoading === order.id}>
                        Pack
                      </Button>
                    )}
                    {order.status === 'PACKED' && (
                      <Button size="sm" onClick={() => handleAction(order.id, 'ship')} isLoading={actionLoading === order.id}>
                        Ship
                      </Button>
                    )}
                  </td>
                </tr>
              ))}
              {orders.length === 0 && (
                <tr>
                  <td colSpan={6} className="px-6 py-4 text-center text-sm text-ink/55">
                    No orders found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>
    </div>
  );
}
