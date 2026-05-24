import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, ShieldCheck, ShoppingBag, UserRound, KeyRound } from 'lucide-react';
import { useAuth } from '@/features/auth';
import { getMe, updateProfile, updatePassword } from '@/features/auth/api/authApi';
import { getMyOrders, type CustomerOrder } from '@/features/commerce/api/orderApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Profile() {
  const { user, setAuth } = useAuth();
  const [orders, setOrders] = useState<CustomerOrder[]>([]);
  const [isSaved, setIsSaved] = useState(false);
  const [displayName, setDisplayName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  
  const [currentPassword, setCurrentPassword] = useState('');
  const [newPassword, setNewPassword] = useState('');
  const [pwdMsg, setPwdMsg] = useState('');

  useEffect(() => {
    getMe().then((data) => {
      setDisplayName(data.username);
      setEmail(data.email || '');
      setPhone(data.phoneNumber || '');
      setAuth(localStorage.getItem('seshop.accessToken')!, data);
    }).catch(console.error);

    getMyOrders(1, 5)
      .then((page) => setOrders(page.items))
      .catch(() => setOrders([]));
  }, []);

  const handleProfileSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      const data = await updateProfile({ username: displayName, email, phoneNumber: phone });
      setAuth(localStorage.getItem('seshop.accessToken')!, data);
      setIsSaved(true);
      setTimeout(() => setIsSaved(false), 3000);
    } catch (e) {
      console.error('Failed to update profile', e);
    }
  };

  const handlePasswordSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    try {
      await updatePassword({ currentPassword, newPassword });
      setPwdMsg('Password updated successfully');
      setCurrentPassword('');
      setNewPassword('');
      setTimeout(() => setPwdMsg(''), 3000);
    } catch (e) {
      setPwdMsg('Failed to update password');
      setTimeout(() => setPwdMsg(''), 3000);
    }
  };

  const totalSpend = orders.reduce((sum, order) => sum + Number(order.totalAmount ?? 0), 0);

  return (
    <PageScaffold
      title="Customer Account & Profile"
      viewCode="CUST_008"
      purpose="Manage account details, security and review recent purchasing activity."
    >
      <div className="grid gap-5 lg:grid-cols-[minmax(0,1fr)_340px]">
        <div className="flex flex-col gap-5">
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
              onSubmit={handleProfileSubmit}
            >
              <Input label="Display Name" value={displayName} onChange={(event) => setDisplayName(event.target.value)} required />
              <Input label="Email" type="email" value={email} onChange={(event) => setEmail(event.target.value)} required />
              <Input label="Phone" value={phone} onChange={(event) => setPhone(event.target.value)} required />
              <Input label="Default City" defaultValue="Ho Chi Minh City" />
              <div className="md:col-span-2 flex flex-wrap items-center justify-between gap-3 border-t border-primary/15 pt-4">
                <span className="text-sm text-ink/55">{isSaved ? 'Profile saved successfully.' : 'Keep contact details ready for checkout.'}</span>
                <Button type="submit">Save Profile</Button>
              </div>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
             <div className="flex items-center gap-3 mb-4">
                <div className="flex size-10 items-center justify-center rounded-full bg-primary/15 text-primary">
                  <KeyRound size={18} />
                </div>
                <div>
                  <h2 className="text-md font-semibold text-ink">Account Security</h2>
                </div>
              </div>
              <form
                className="grid gap-4 md:grid-cols-2"
                onSubmit={handlePasswordSubmit}
              >
                <Input label="Current Password" type="password" value={currentPassword} onChange={(e) => setCurrentPassword(e.target.value)} required />
                <Input label="New Password" type="password" value={newPassword} onChange={(e) => setNewPassword(e.target.value)} required minLength={8} />
                <div className="md:col-span-2 flex flex-wrap items-center justify-between gap-3 border-t border-primary/15 pt-4">
                  <span className={`text-sm ${pwdMsg.includes('Failed') ? 'text-danger' : 'text-success'}`}>{pwdMsg}</span>
                  <Button type="submit" variant="secondary">Change Password</Button>
                </div>
              </form>
          </Card>
        </div>

        <div className="flex flex-col gap-5">
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

          <Card className="border-primary/20 bg-surface/95 p-5">
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
      </div>
    </PageScaffold>
  );
}
