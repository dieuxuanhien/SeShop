import { PageScaffold } from '@/shared/ui/PageScaffold';

export function AuditLogs() {
  return (
    <PageScaffold
      title="Audit & Compliance Logs"
      viewCode="ADMIN_004"
      purpose="Read-only audit table with filters for actor, action, target, and time range."
      endpoints={['GET /admin/audit-logs']}
    />
  );
}
