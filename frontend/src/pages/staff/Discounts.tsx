import { useEffect, useState } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Badge } from '@/shared/ui/Badge';
import { getDiscounts, type Discount } from '@/features/marketing/api/discountApi';

export function Discounts() {
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getDiscounts()
      .then(setDiscounts)
      .catch(() => setDiscounts([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="Discount & Promotion Management"
      viewCode="STAFF_006"
      purpose="Discount code CRUD, validity windows, usage limits, and redemption analytics shell."
      endpoints={['POST /staff/discounts', 'GET /staff/discounts', 'PUT /staff/discounts/{discountId}', 'DELETE /staff/discounts/{discountId}']}
    >
      <div className="p-1">
        {isLoading ? (
          <p className="text-sm text-ink/60">Loading discounts...</p>
        ) : discounts.length === 0 ? (
          <p className="text-sm text-ink/60">No discounts returned by the API.</p>
        ) : (
          <div className="grid gap-3">
            {discounts.map((discount) => (
              <div key={discount.id} className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/5 p-3">
                <div>
                  <p className="text-sm font-semibold text-ink">{discount.code}</p>
                  <p className="text-xs text-ink/60">{discount.discountType} · {Number(discount.discountValue).toLocaleString()}</p>
                </div>
                <Badge variant={discount.status === 'ACTIVE' ? 'success' : 'warning'}>{discount.status}</Badge>
              </div>
            ))}
          </div>
        )}
      </div>
    </PageScaffold>
  );
}
