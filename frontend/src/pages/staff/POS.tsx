import { PageScaffold } from '@/shared/ui/PageScaffold';

export function POS() {
  return (
    <PageScaffold
      title="POS Transaction"
      viewCode="STAFF_007"
      purpose="Touch-optimized POS shell with active shift guard, product search, basket, cash/card payment, and receipt flow."
      endpoints={['POST /pos/shifts/open', 'POST /pos/receipts', 'GET /pos/receipts/{receiptId}']}
    />
  );
}
