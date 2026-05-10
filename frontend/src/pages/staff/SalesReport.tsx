import { useEffect, useMemo, useState } from 'react';
import { BarChart3, CreditCard, PackageCheck, Wallet } from 'lucide-react';
import { getStaffOrders, type StaffOrder } from '@/features/staff/api/staffOrdersApi';
import { getCurrentShift, type ShiftData } from '@/features/staff/api/staffPosApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function SalesReport() {
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [shift, setShift] = useState<ShiftData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getStaffOrders(1, 100).then((page) => page.items).catch(() => []),
      getCurrentShift().catch(() => null),
    ])
      .then(([orderData, shiftData]) => {
        setOrders(orderData);
        setShift(shiftData);
      })
      .finally(() => setIsLoading(false));
  }, []);

  const metrics = useMemo(() => {
    const total = orders.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);
    const active = orders.filter((order) => !['SHIPPED', 'DELIVERED', 'CANCELLED'].includes(order.status)).length;
    const shipped = orders.filter((order) => ['SHIPPED', 'DELIVERED'].includes(order.status)).length;
    return { total, active, shipped };
  }, [orders]);

  const statCards = [
    { label: 'Online Revenue', value: formatCurrency(metrics.total), icon: BarChart3 },
    { label: 'Open Orders', value: metrics.active.toLocaleString(), icon: PackageCheck },
    { label: 'Card Payments', value: formatCurrency(Number(shift?.cardPaymentsTotal ?? 0)), icon: CreditCard },
    { label: 'Expected Cash', value: formatCurrency(Number(shift?.expectedCash ?? 0)), icon: Wallet },
  ];

  return (
    <PageScaffold
      title="Sales Report"
      viewCode="STAFF_012"
      purpose="Review online order value, current register totals, and fulfillment status."
    >
      <div className="grid gap-5">
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
                  <span className="font-semibold text-ink">{metrics.shipped}</span>
                </div>
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink/55">No active register shift was returned.</p>
            )}
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
