import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { ArrowRight, Clock } from 'lucide-react';
import { Card } from '@/shared/ui/Card';
import { getInventoryBalances, getStockTransfers } from '@/features/staff/api/staffInventoryApi';
import { getStaffOrders } from '@/features/staff/api/staffOrdersApi';
import { getReceiptHistory, type ReceiptDto } from '@/features/staff/api/staffPosApi';

export function StaffDashboard() {
  const [stats, setStats] = useState([
    { label: 'Pending Orders', value: '0', link: '/staff/orders' },
    { label: 'Low Stock Alerts', value: '0', link: '/staff/inventory' },
    { label: 'Active Transfers', value: '0', link: '/staff/transfers' },
    { label: 'Today\'s Sales', value: '0 VND', link: '/staff/sales-report' },
  ]);
  const [recentReceipts, setRecentReceipts] = useState<ReceiptDto[]>([]);

  useEffect(() => {
    Promise.all([
      getStaffOrders(1, 100),
      getInventoryBalances(1, 100),
      getStockTransfers(1, 100),
      getReceiptHistory(0, 5).catch(() => ({ items: [] })),
    ])
      .then(([orders, balances, transfers, historyData]) => {
        const pendingOrders = orders.items.filter((order) => !['SHIPPED', 'DELIVERED', 'CANCELLED'].includes(order.status)).length;
        const lowStockAlerts = balances.items.filter((balance) => balance.availableQty < 5).length;
        const activeTransfers = transfers.items.filter((transfer) => !['COMPLETED', 'CANCELLED'].includes(transfer.status)).length;
        const todaySales = orders.items.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);
        setStats([
          { label: 'Pending Orders', value: pendingOrders.toString(), link: '/staff/orders' },
          { label: 'Low Stock Alerts', value: lowStockAlerts.toString(), link: '/staff/inventory' },
          { label: 'Active Transfers', value: activeTransfers.toString(), link: '/staff/transfers' },
          { label: 'Today\'s Sales', value: `${todaySales.toLocaleString()} VND`, link: '/staff/sales-report' },
        ]);
        setRecentReceipts(historyData.items || []);
      })
      .catch(() => {
        setStats((current) => current);
      });
  }, []);

  return (
    <div className="mx-auto max-w-7xl">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h1 className="font-display text-2xl font-semibold text-surface">Staff Dashboard</h1>
          <p className="mt-1 text-sm text-surface/60">Overview of today's operational metrics.</p>
        </div>
        <Link 
          to="/staff/pos" 
          className="inline-flex min-h-10 items-center rounded-md bg-primary px-4 py-2 text-sm font-medium text-ink transition hover:bg-primaryStrong"
        >
          Open POS
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <div className="px-4 py-5 sm:p-6">
              <dt className="truncate text-sm font-medium text-ink/55">{stat.label}</dt>
              <dd className="mt-1 text-3xl font-semibold text-ink">{stat.value}</dd>
              <div className="mt-4">
                <Link to={stat.link} className="inline-flex items-center gap-1 text-sm font-medium text-primaryStrong hover:text-ink">
                  View details <ArrowRight size={14} />
                </Link>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        {/* Quick Actions */}
        <Card>
          <div className="border-b border-primary/15 px-4 py-5 sm:px-6">
            <h3 className="text-lg font-medium leading-6 text-ink">Quick Actions</h3>
          </div>
          <div className="px-4 py-5 sm:p-6">
            <div className="grid grid-cols-2 gap-4">
              <Link to="/staff/orders" className="rounded-md border border-primary/20 p-4 text-center transition-colors hover:bg-primary/10">
                <span className="block text-sm font-medium text-ink">Fulfill Orders</span>
              </Link>
              <Link to="/staff/inventory" className="rounded-md border border-primary/20 p-4 text-center transition-colors hover:bg-primary/10">
                <span className="block text-sm font-medium text-ink">Adjust Inventory</span>
              </Link>
              <Link to="/staff/transfers" className="rounded-md border border-primary/20 p-4 text-center transition-colors hover:bg-primary/10">
                <span className="block text-sm font-medium text-ink">Stock Transfers</span>
              </Link>
              <Link to="/staff/returns" className="rounded-md border border-primary/20 p-4 text-center transition-colors hover:bg-primary/10">
                <span className="block text-sm font-medium text-ink">Process Returns</span>
              </Link>
            </div>
          </div>
        </Card>

        {/* Recent POS Receipts */}
        <Card>
          <div className="border-b border-primary/15 px-4 py-5 sm:px-6 flex justify-between items-center">
            <h3 className="text-lg font-medium leading-6 text-ink">Recent POS Receipts</h3>
            <Link to="/staff/pos" className="text-xs text-primaryStrong hover:text-ink font-semibold">
              Open POS
            </Link>
          </div>
          <div className="px-4 py-5 sm:p-6">
            {recentReceipts.length === 0 ? (
              <div className="text-sm text-ink/40 text-center py-6">
                No recent POS receipts found.
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="min-w-full divide-y divide-primary/10 text-left text-xs">
                  <thead>
                    <tr>
                      <th className="pb-2 font-semibold text-ink/55">Receipt No</th>
                      <th className="pb-2 font-semibold text-ink/55">Method</th>
                      <th className="pb-2 text-right font-semibold text-ink/55">Total</th>
                    </tr>
                  </thead>
                  <tbody className="divide-y divide-primary/5">
                    {recentReceipts.map((receipt) => (
                      <tr key={receipt.id}>
                        <td className="py-2 font-medium text-ink">
                          <Link to="/staff/pos" className="hover:underline text-primary">
                            {receipt.receiptNumber}
                          </Link>
                        </td>
                        <td className="py-2">
                          <span className="bg-primary/10 text-primary px-1.5 py-0.5 rounded-full text-[10px] font-semibold">
                            {receipt.paymentMethod}
                          </span>
                        </td>
                        <td className="py-2 text-right font-bold text-ink">
                          {receipt.totalAmount.toLocaleString()}
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>
        </Card>

        {/* Recent Activity */}
        <Card>
          <div className="border-b border-primary/15 px-4 py-5 sm:px-6">
            <h3 className="text-lg font-medium leading-6 text-ink">Recent Activity</h3>
          </div>
          <div className="px-4 py-5 sm:p-6">
            <ul className="-my-5 divide-y divide-primary/10">
              <li className="py-4">
                <div className="flex items-center space-x-4">
                  <div className="flex-1 min-w-0">
                    <p className="truncate text-sm font-medium text-ink">Activity stream</p>
                    <p className="truncate text-sm text-ink/55">Open audit logs for the latest operational events.</p>
                  </div>
                </div>
              </li>
            </ul>
          </div>
        </Card>
      </div>
    </div>
  );
}
