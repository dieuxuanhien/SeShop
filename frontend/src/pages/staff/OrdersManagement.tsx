import { useState, useEffect } from 'react';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Badge } from '@/shared/ui/Badge';
import { getStaffOrders, allocateOrder, packOrder, shipOrder, type StaffOrder } from '@/features/staff/api/staffOrdersApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import { Spinner } from '@/shared/ui/Spinner';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';

export function OrdersManagement() {
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [actionLoading, setActionLoading] = useState<number | null>(null);
  const [statusFilter, setStatusFilter] = useState('ALL');
  const [paymentFilter, setPaymentFilter] = useState('ALL');
  const [locations, setLocations] = useState<InventoryBalance[]>([]);
  
  // Dialog states
  const [shipDialog, setShipDialog] = useState<number | null>(null);
  const [trackingInput, setTrackingInput] = useState('');
  
  const [allocateDialog, setAllocateDialog] = useState<number | null>(null);
  const [locationInput, setLocationInput] = useState('1');

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
    getInventoryBalances(1, 100).then(res => setLocations(res.items)).catch(console.error);
  }, []);

  const filteredOrders = orders.filter((order) => {
    const statusMatches = statusFilter === 'ALL' ? true : order.status === statusFilter;
    const paymentMatches = paymentFilter === 'ALL' ? true : order.paymentStatus === paymentFilter;
    return statusMatches && paymentMatches;
  });

  const handleAction = async (orderId: number, action: 'allocate' | 'pack' | 'ship') => {
    setActionLoading(orderId);
    try {
      if (action === 'allocate') {
        setAllocateDialog(orderId);
      }
      if (action === 'pack') {
        await packOrder(orderId);
        await fetchOrders();
      }
      if (action === 'ship') {
        setShipDialog(orderId);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setActionLoading(null);
    }
  };

  const confirmAllocate = async () => {
    if (!allocateDialog) return;
    setActionLoading(allocateDialog);
    try {
      await allocateOrder(allocateDialog);
      await fetchOrders();
    } catch (e) {
      console.error(e);
    } finally {
      setActionLoading(null);
      setAllocateDialog(null);
    }
  };

  const confirmShip = async () => {
    if (!shipDialog) return;
    setActionLoading(shipDialog);
    try {
      const order = orders.find((item) => item.id === shipDialog);
      await shipOrder(shipDialog, { ...getRecipient(order), trackingNumber: trackingInput || undefined });
      await fetchOrders();
    } catch (e) {
      console.error(e);
    } finally {
      setActionLoading(null);
      setShipDialog(null);
      setTrackingInput('');
    }
  };

  function getRecipient(order?: StaffOrder) {
    const [name, phone] = order?.shippingAddress?.split(',').map((part) => part.trim()) ?? [];
    return {
      recipientName: name || 'SeShop Dev Recipient',
      recipientPhone: phone || '0987654321',
    };
  }

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

      <Card className="border-primary/20 bg-surface/95 mb-6 p-4">
        <div className="flex gap-4">
          <div className="w-48">
            <label className="block text-sm font-medium text-ink/70 mb-1">Order Status</label>
            <select 
              className="w-full rounded-md border border-ink/20 bg-surface px-3 py-2 text-sm text-ink"
              value={statusFilter} 
              onChange={(e) => setStatusFilter(e.target.value)}
            >
              <option value="ALL">All Statuses</option>
              <option value="PENDING">Pending</option>
              <option value="ALLOCATED">Allocated</option>
              <option value="PACKED">Packed</option>
              <option value="SHIPPED">Shipped</option>
              <option value="DELIVERED">Delivered</option>
              <option value="CANCELLED">Cancelled</option>
            </select>
          </div>
          <div className="w-48">
            <label className="block text-sm font-medium text-ink/70 mb-1">Payment Status</label>
            <select 
              className="w-full rounded-md border border-ink/20 bg-surface px-3 py-2 text-sm text-ink"
              value={paymentFilter} 
              onChange={(e) => setPaymentFilter(e.target.value)}
            >
              <option value="ALL">All Payments</option>
              <option value="PENDING">Pending</option>
              <option value="PAID">Paid</option>
              <option value="FAILED">Failed</option>
              <option value="REFUNDED">Refunded</option>
            </select>
          </div>
        </div>
      </Card>

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
              {filteredOrders.map((order) => (
                <tr key={order.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-ink">
                    <span className="text-ink/50 mr-2">#{order.id}</span>
                    {order.orderNumber}
                  </td>
                  <td className="max-w-xs truncate px-6 py-4 whitespace-nowrap text-sm text-ink/60">{order.shippingAddress || 'Demo Customer'}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-ink/60">
                    {order.totalAmount.toLocaleString()} {order.currency ?? 'VND'}
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Badge variant={order.paymentStatus === 'PAID' ? 'success' : order.paymentStatus === 'FAILED' ? 'danger' : 'warning'}>
                      {order.paymentStatus ?? 'PENDING'}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm">
                    <Badge variant={order.status === 'SHIPPED' || order.status === 'DELIVERED' ? 'success' : order.status === 'CANCELLED' ? 'danger' : 'warning'}>
                      {order.status}
                    </Badge>
                  </td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    {(order.status === 'PAID' || order.status === 'CONFIRMED') && (
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
              {filteredOrders.length === 0 && (
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

      {/* Allocate Dialog */}
      {allocateDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/50 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-md border border-primary/20 bg-surface p-6 shadow-xl">
            <h2 className="text-xl font-bold text-ink mb-4">Allocate Order</h2>
            <p className="text-sm text-ink/70 mb-4">Are you sure you want to allocate inventory for this order? The system will automatically reserve stock from the nearest available fulfillment centers.</p>
            <div className="mt-6 flex justify-end gap-3">
              <Button variant="secondary" onClick={() => setAllocateDialog(null)}>Cancel</Button>
              <Button onClick={confirmAllocate} isLoading={actionLoading === allocateDialog}>Confirm Allocation</Button>
            </div>
          </div>
        </div>
      )}

      {/* Ship Dialog */}
      {shipDialog && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-ink/50 backdrop-blur-sm">
          <div className="w-full max-w-md rounded-md border border-primary/20 bg-surface p-6 shadow-xl">
            <h2 className="text-xl font-bold text-ink mb-4">Ship Order</h2>
            <p className="text-sm text-ink/70 mb-4">Enter the tracking number for the shipment.</p>
            <Input
              label="Tracking Number"
              placeholder="e.g. GHN123456789"
              value={trackingInput}
              onChange={(e) => setTrackingInput(e.target.value)}
            />
            <div className="mt-6 flex justify-end gap-3">
              <Button variant="secondary" onClick={() => setShipDialog(null)}>Cancel</Button>
              <Button onClick={confirmShip} isLoading={actionLoading === shipDialog}>Confirm Shipment</Button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
