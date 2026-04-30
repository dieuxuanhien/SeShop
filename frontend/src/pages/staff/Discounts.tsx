import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Discounts() {
  return (
    <PageScaffold
      title="Discount & Promotion Management"
      viewCode="STAFF_006"
      purpose="Discount code CRUD, validity windows, usage limits, and redemption analytics shell."
      endpoints={['POST /staff/discounts', 'GET /staff/discounts', 'PUT /staff/discounts/{discountId}', 'DELETE /staff/discounts/{discountId}']}
    />
  );
}
