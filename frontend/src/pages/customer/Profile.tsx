import { PageScaffold } from '@/shared/ui/PageScaffold';
import { useAuth } from '@/features/auth';

export function Profile() {
  const { user } = useAuth();

  return (
    <PageScaffold
      title="Customer Account & Profile"
      viewCode="CUST_008"
      purpose="Customer profile shell for personal data, addresses, notification preferences, and account security."
      endpoints={['GET /orders/me']}
    >
      <div className="grid gap-3 text-sm text-ink/80">
        <p className="font-semibold">{user?.username ?? 'Authenticated customer'}</p>
        <p>{user?.userType ?? 'No profile endpoint is currently documented.'}</p>
        <p>Order history is loaded from the customer orders API.</p>
      </div>
    </PageScaffold>
  );
}
