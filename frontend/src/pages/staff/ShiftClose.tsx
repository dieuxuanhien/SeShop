import { PageScaffold } from '@/shared/ui/PageScaffold';

export function ShiftClose() {
  return (
    <PageScaffold
      title="POS Shift Close & Cash Reconciliation"
      viewCode="STAFF_008"
      purpose="Shift summary, expected cash, actual cash, variance handling, and finance document actions."
      endpoints={['POST /pos/shifts/{shiftId}/close', 'GET /pos/shifts/{shiftId}', 'POST /invoices/tax', 'POST /invoices/{invoiceId}/adjustments']}
    />
  );
}
