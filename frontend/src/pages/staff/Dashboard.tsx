import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Card } from '@/shared/ui/Card';
import { getInventoryBalances, getStockTransfers } from '@/features/staff/api/staffInventoryApi';
import { getStaffOrders } from '@/features/staff/api/staffOrdersApi';

export function StaffDashboard() {
  const [stats, setStats] = useState([
    { label: 'Pending Orders', value: '0', link: '/staff/orders' },
    { label: 'Low Stock Alerts', value: '0', link: '/staff/inventory' },
    { label: 'Active Transfers', value: '0', link: '/staff/transfers' },
    { label: 'Today\'s Sales', value: '0 VND', link: '/staff/sales-report' },
  ]);

  useEffect(() => {
    Promise.all([
      getStaffOrders(1, 100),
      getInventoryBalances(1, 100),
      getStockTransfers(1, 100),
    ])
      .then(([orders, balances, transfers]) => {
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
      })
      .catch(() => {
        setStats((current) => current);
      });
  }, []);

  return (
    <div className="max-w-7xl mx-auto py-8 px-4 sm:px-6 lg:px-8">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h1 className="text-2xl font-semibold text-gray-900">Staff Dashboard</h1>
          <p className="mt-1 text-sm text-gray-500">Overview of today's operational metrics.</p>
        </div>
        <Link 
          to="/staff/pos" 
          className="inline-flex items-center px-4 py-2 border border-transparent text-sm font-medium rounded-md shadow-sm text-white bg-brand-dark hover:bg-black focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-brand-dark"
        >
          Open POS
        </Link>
      </div>

      <div className="grid grid-cols-1 gap-5 sm:grid-cols-2 lg:grid-cols-4 mb-8">
        {stats.map((stat) => (
          <Card key={stat.label}>
            <div className="px-4 py-5 sm:p-6">
              <dt className="text-sm font-medium text-gray-500 truncate">{stat.label}</dt>
              <dd className="mt-1 text-3xl font-semibold text-gray-900">{stat.value}</dd>
              <div className="mt-4">
                <Link to={stat.link} className="text-sm font-medium text-brand-dark hover:text-black">
                  View details &rarr;
                </Link>
              </div>
            </div>
          </Card>
        ))}
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-8">
        {/* Quick Actions */}
        <Card>
          <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
            <h3 className="text-lg leading-6 font-medium text-gray-900">Quick Actions</h3>
          </div>
          <div className="px-4 py-5 sm:p-6">
            <div className="grid grid-cols-2 gap-4">
              <Link to="/staff/orders" className="p-4 border border-gray-200 rounded-lg text-center hover:bg-gray-50 transition-colors">
                <span className="block text-sm font-medium text-gray-900">Fulfill Orders</span>
              </Link>
              <Link to="/staff/inventory" className="p-4 border border-gray-200 rounded-lg text-center hover:bg-gray-50 transition-colors">
                <span className="block text-sm font-medium text-gray-900">Adjust Inventory</span>
              </Link>
              <Link to="/staff/transfers" className="p-4 border border-gray-200 rounded-lg text-center hover:bg-gray-50 transition-colors">
                <span className="block text-sm font-medium text-gray-900">Stock Transfers</span>
              </Link>
              <Link to="/staff/returns" className="p-4 border border-gray-200 rounded-lg text-center hover:bg-gray-50 transition-colors">
                <span className="block text-sm font-medium text-gray-900">Process Returns</span>
              </Link>
            </div>
          </div>
        </Card>

        {/* Recent Activity */}
        <Card>
          <div className="px-4 py-5 sm:px-6 border-b border-gray-200">
            <h3 className="text-lg leading-6 font-medium text-gray-900">Recent Activity</h3>
          </div>
          <div className="px-4 py-5 sm:p-6">
            <ul className="divide-y divide-gray-200 -my-5">
              <li className="py-4">
                <div className="flex items-center space-x-4">
                  <div className="flex-1 min-w-0">
                    <p className="text-sm font-medium text-gray-900 truncate">Activity stream</p>
                    <p className="text-sm text-gray-500 truncate">Open audit logs for the latest operational events.</p>
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
