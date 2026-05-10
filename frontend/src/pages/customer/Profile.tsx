import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ShieldCheck, ShoppingBag, UserRound } from 'lucide-react';
import { useAuth } from '@/features/auth';
import { getMyOrders, type CustomerOrder } from '@/features/commerce/api/orderApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Profile() {
  const { user } = useAuth();
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [isSaved, setIsSaved] = useState(false);
  const [displayName, setDisplayName] = useState(user?.username ?? '');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');

  useEffect(() => {
    getMyOrders(1, 5)
      .then((page) => setOrders(page.items))
      .catch(() => setOrders([]));
  }, []);

  const totalSpend = orders.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);

  return (
    <PageScaffold
      title="Customer Account & Profile"
      viewCode="CUST_008"
      purpose="Manage account details and review recent purchasing activity."
    >
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_340px]">
        <Card className="border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div className="flex items-center gap-3">
              <div className="flex size-12 items-center justify-center rounded-full bg-primary/15 text-primary">
                <UserRound size={22} />
              </div>
              <div>
                <h2 className="text-lg font-semibold text-ink">{user?.username ?? 'Customer'}</h2>
                <p className="text-sm text-ink/55">{user?.userType ?? 'CUSTOMER'}</p>
              </div>
            </div>
            <Badge variant="success">Active</Badge>
          </div>

          <form
            className="mt-6 grid gap-4 md:grid-cols-2"
            onSubmit={(event) => {
              event.preventDefault();
              setIsSaved(true);
            }}
          >
            <Input label="Display Name" value={displayName} onChange={(event) => setDisplayName(event.target.value)} />
            <Input label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} />
            <Input label="Phone" value={phone} onChange={(event) => setPhone(event.target.value)} />
            <Input label="Default City" defaultValue="Ho Chi Minh City" />
            <div className="md:col-span-2 flex flex-wrap items-center justify-between gap-3 border-t border-primary/15 pt-4">
              <span className="text-sm text-ink/55">{isSaved ? 'Profile saved in this browser session.' : 'Keep contact details ready for checkout.'}</span>
              <Button type="submit">Save Profile</Button>
            </div>
          </form>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Account Summary</h2>
          <div className="mt-4 grid gap-3 text-sm text-ink/70">
            <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/[0.03] p-3">
              <span className="inline-flex items-center gap-2"><ShoppingBag size={15} className="text-primary" />Orders</span>
              <span className="font-semibold text-ink">{orders.length}</span>
            </div>
            <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/[0.03] p-3">
              <span className="inline-flex items-center gap-2"><Mail size={15} className="text-primary" />Contact</span>
              <span className="font-semibold text-ink">{email || 'Not set'}</span>
            </div>
            <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/[0.03] p-3">
              <span className="inline-flex items-center gap-2"><ShieldCheck size={15} className="text-primary" />Permissions</span>
              <span className="font-semibold text-ink">{user?.roles?.join(', ') || 'Customer'}</span>
            </div>
          </div>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5 lg:col-span-2">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Recent Orders</h2>
              <p className="mt-1 text-xs text-ink/50">Lifetime value in loaded orders: {formatCurrency(totalSpend)}</p>
            </div>
            <Link to="/orders">
              <Button variant="secondary">View Orders</Button>
            </Link>
          </div>
          <div className="mt-4 grid gap-3">
            {orders.length === 0 ? (
              <p className="text-sm text-ink/55">No recent orders yet.</p>
            ) : (
              orders.map((order) => (
                <Link key={order.id} to={`/orders/${order.id}`} className="flex flex-wrap items-center justify-between gap-3 rounded-md border border-primary/15 bg-ink/[0.03] p-3 text-sm text-ink/70">
                  <span className="font-semibold text-ink">{order.orderNumber}</span>
                  <span>{formatCurrency(Number(order.totalAmount))}</span>
                </Link>
              ))
            )}
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
