import { useEffect, useState } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getStaffOrders, type StaffOrder } from '@/features/staff/api/staffOrdersApi';
import { formatCurrency } from '@/shared/lib/formatters';

export function SalesReport() {
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getStaffOrders(1, 100)
      .then((page) => setOrders(page.items))
      .catch(() => setOrders([]))
      .finally(() => setIsLoading(false));
  }, []);

  const total = orders.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);

  return (
    <PageScaffold
      title="Sales Report"
      purpose="Daily sales summary shell for POS and online revenue, payment breakdown, and top products."
      endpoints={['GET /pos/shifts/{shiftId}', 'GET /staff/orders']}
    >
      {isLoading ? (
        <p className="text-sm text-ink/60">Loading sales data...</p>
      ) : (
        <div className="grid gap-3 text-sm text-ink/80">
          <p className="font-semibold">Orders returned: {orders.length}</p>
          <p>Total order value: {formatCurrency(total)}</p>
        </div>
      )}
    </PageScaffold>
  );
}
