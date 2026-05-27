import { useEffect, useMemo, useState } from 'react';
import { BarChart3, CreditCard, PackageCheck, Wallet } from 'lucide-react';
import { getStaffOrders, type StaffOrder } from '@/features/staff/api/staffOrdersApi';
import { getCurrentShift, getReceiptHistory, type ShiftData, type ReceiptDto } from '@/features/staff/api/staffPosApi';
import { useStaffLocation } from '@/shared/context/LocationContext';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function SalesReport() {
  const { locations } = useStaffLocation();
  const [selectedLocationId, setSelectedLocationId] = useState<string>('ALL');

  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [receipts, setReceipts] = useState<ReceiptDto[]>([]);
  const [shift, setShift] = useState<ShiftData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(true);
    Promise.all([
      getStaffOrders(1, 100).then((page) => page.items).catch(() => []),
      getReceiptHistory(0, 100).then((page) => page.items).catch(() => []),
      getCurrentShift().catch(() => null),
    ])
      .then(([orderData, receiptData, shiftData]) => {
        setOrders(orderData);
        setReceipts(receiptData);
        setShift(shiftData);
      })
      .finally(() => setIsLoading(false));
  }, []);

  const selectedLocationName = useMemo(() => {
    if (selectedLocationId === 'ALL') return null;
    return locations.find(l => l.id.toString() === selectedLocationId)?.displayName;
  }, [selectedLocationId, locations]);

  const filteredReceipts = useMemo(() => {
    if (!selectedLocationName) return receipts;
    return receipts.filter(r => r.locationName === selectedLocationName);
  }, [receipts, selectedLocationName]);

  const metrics = useMemo(() => {
    const totalOrders = orders.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);
    const activeOrders = orders.filter((order) => !['SHIPPED', 'DELIVERED', 'CANCELLED'].includes(order.status)).length;
    const shippedOrders = orders.filter((order) => ['SHIPPED', 'DELIVERED'].includes(order.status)).length;
    
    const posTotal = filteredReceipts.reduce((sum, r) => sum + Number(r.totalAmount ?? 0), 0);
    const cardTotal = filteredReceipts.filter(r => r.paymentMethod === 'CARD').reduce((sum, r) => sum + Number(r.totalAmount ?? 0), 0);
    const cashTotal = filteredReceipts.filter(r => r.paymentMethod === 'CASH').reduce((sum, r) => sum + Number(r.totalAmount ?? 0), 0);

    return { totalOrders, activeOrders, shippedOrders, posTotal, cardTotal, cashTotal };
  }, [orders, filteredReceipts]);

  const statCards = [
    { label: 'Online Revenue', value: formatCurrency(metrics.totalOrders), icon: BarChart3 },
    { label: 'POS Revenue', value: formatCurrency(metrics.posTotal), icon: Wallet },
    { label: 'POS Card Total', value: formatCurrency(metrics.cardTotal), icon: CreditCard },
    { label: 'Open Online Orders', value: metrics.activeOrders.toLocaleString(), icon: PackageCheck },
  ];

  return (
    <PageScaffold
      title="Sales Report"
      viewCode="STAFF_012"
      purpose="Review online order value, current register totals, and fulfillment status."
    >
      <div className="grid gap-5">
        <div className="flex items-center justify-between border-b border-primary/10 pb-4">
          <h2 className="text-lg font-bold text-ink">Dashboard Overview</h2>
          <select 
            value={selectedLocationId}
            onChange={(e) => setSelectedLocationId(e.target.value)}
            className="rounded-md border border-primary/20 bg-surface px-3 py-1.5 text-sm font-medium shadow-sm focus:border-primary focus:outline-none"
          >
            <option value="ALL">All Locations (POS)</option>
            {locations.map(loc => (
              <option key={loc.id} value={loc.id.toString()}>{loc.displayName}</option>
            ))}
          </select>
        </div>

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {statCards.map((stat) => {
            const Icon = stat.icon;
            return (
              <Card key={stat.label} className="border-primary/20 bg-surface/95 p-5">
                <div className="flex items-center justify-between gap-3">
                  <p className="text-xs font-semibold uppercase tracking-wide text-ink/50">{stat.label}</p>
                  <Icon size={18} className="text-primary" />
                </div>
                <p className="mt-3 text-2xl font-semibold text-ink">{stat.value}</p>
              </Card>
            );
          })}
        </div>

        <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_320px]">
          <Card className="border-primary/20 bg-surface/95">
            <div className="border-b border-primary/15 p-5">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Order Revenue</h2>
              <p className="mt-1 text-xs text-ink/50">Latest orders returned by the staff order feed.</p>
            </div>
            <div className="overflow-x-auto">
              <table className="min-w-full text-left text-sm">
                <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50">
                  <tr>
                    <th className="px-5 py-3">Order</th>
                    <th className="px-5 py-3">Status</th>
                    <th className="px-5 py-3">Destination</th>
                    <th className="px-5 py-3 text-right">Total</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-primary/10">
                  {isLoading ? (
                    <tr>
                      <td colSpan={4} className="px-5 py-8 text-center text-ink/55">Loading sales data...</td>
                    </tr>
                  ) : orders.length === 0 ? (
                    <tr>
                      <td colSpan={4} className="px-5 py-8">
                        <EmptyState title="No orders found" description="Sales will appear here once orders are created." />
                      </td>
                    </tr>
                  ) : (
                    orders.map((order) => (
                      <tr key={order.id} className="text-ink/75">
                        <td className="px-5 py-4 font-semibold text-ink">{order.orderNumber}</td>
                        <td className="px-5 py-4">
                          <Badge variant={order.status === 'CANCELLED' ? 'danger' : ['SHIPPED', 'DELIVERED'].includes(order.status) ? 'success' : 'warning'}>
                            {order.status}
                          </Badge>
                        </td>
                        <td className="max-w-xs truncate px-5 py-4">{order.shippingAddress}</td>
                        <td className="px-5 py-4 text-right font-semibold text-ink">{formatCurrency(Number(order.totalAmount ?? 0))}</td>
                      </tr>
                    ))
                  )}
                </tbody>
              </table>
            </div>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Register Snapshot</h2>
            {shift ? (
              <div className="mt-4 grid gap-3 text-sm text-ink/70">
                <div className="flex justify-between">
                  <span>Register</span>
                  <span className="font-semibold text-ink">{shift.registerName}</span>
                </div>
                <div className="flex justify-between">
                  <span>Opened</span>
                  <span className="font-semibold text-ink">{new Date(shift.openedAt).toLocaleString()}</span>
                </div>
                <div className="flex justify-between">
                  <span>Transactions</span>
                  <span className="font-semibold text-ink">{shift.transactionCount}</span>
                </div>
                <div className="flex justify-between">
                  <span>Shipped Orders</span>
                  <span className="font-semibold text-ink">{metrics.shippedOrders}</span>
                </div>
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink/55">No active register shift was returned.</p>
            )}
          </Card>
        </div>

        {/* POS Receipts Table */}
        <Card className="border-primary/20 bg-surface/95 mt-4">
          <div className="border-b border-primary/15 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">POS Transactions</h2>
            <p className="mt-1 text-xs text-ink/50">Recent point of sale receipts for the selected location filter.</p>
          </div>
          <div className="overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-5 py-3">Receipt</th>
                  <th className="px-5 py-3">Location</th>
                  <th className="px-5 py-3">Payment</th>
                  <th className="px-5 py-3 text-right">Total</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-5 py-8 text-center text-ink/55">Loading POS data...</td>
                  </tr>
                ) : filteredReceipts.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-5 py-8">
                      <EmptyState title="No transactions found" description="POS sales will appear here." />
                    </td>
                  </tr>
                ) : (
                  filteredReceipts.map((receipt) => (
                    <tr key={receipt.id} className="text-ink/75">
                      <td className="px-5 py-4 font-semibold text-ink">{receipt.receiptNumber}</td>
                      <td className="px-5 py-4">{receipt.locationName}</td>
                      <td className="px-5 py-4">
                        <Badge variant={receipt.paymentMethod === 'CARD' ? 'info' : 'success'}>
                          {receipt.paymentMethod}
                        </Badge>
                      </td>
                      <td className="px-5 py-4 text-right font-semibold text-ink">{formatCurrency(Number(receipt.totalAmount ?? 0))}</td>
                    </tr>
                  ))
                )}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
