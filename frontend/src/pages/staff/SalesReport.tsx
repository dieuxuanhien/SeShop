import { PageScaffold } from '@/shared/ui/PageScaffold';

export function SalesReport() {
  return (
    <PageScaffold
      title="Sales Report"
      purpose="Daily sales summary shell for POS and online revenue, payment breakdown, and top products."
      endpoints={['GET /pos/shifts/{shiftId}', 'GET /staff/orders']}
    />
  );
}
