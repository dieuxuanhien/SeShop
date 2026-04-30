import { PageScaffold } from '@/shared/ui/PageScaffold';

export function ReturnsManagement() {
  return (
    <PageScaffold
      title="Refunds & Returns"
      viewCode="STAFF_005"
      purpose="Return intake, inspection, refund processing, exchange creation, and status tracking."
      endpoints={['POST /returns', 'POST /returns/{returnId}/approve', 'POST /refunds', 'POST /exchanges']}
    />
  );
}
