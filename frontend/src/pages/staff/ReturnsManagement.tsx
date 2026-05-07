import { PageScaffold } from '@/shared/ui/PageScaffold';

export function ReturnsManagement() {
  return (
    <PageScaffold
      title="Refunds & Returns"
      viewCode="STAFF_005"
      purpose="Return intake, inspection, refund processing, exchange creation, and status tracking."
      endpoints={['POST /returns', 'POST /returns/{returnId}/approve', 'POST /refunds', 'POST /exchanges']}
    >
      <p className="text-sm text-ink/60">Return and refund mutation endpoints are available; no staff return queue endpoint is currently documented.</p>
    </PageScaffold>
  );
}
