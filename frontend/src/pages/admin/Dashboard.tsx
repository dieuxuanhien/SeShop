import { PageScaffold } from '@/shared/ui/PageScaffold';

export function AdminDashboard() {
  return (
    <PageScaffold
      title="Dashboard Overview"
      viewCode="ADMIN_001"
      purpose="Super Admin overview for business metrics, inventory alerts, order activity, staff presence, and system health."
      endpoints={['GET /admin/audit-logs']}
    />
  );
}
