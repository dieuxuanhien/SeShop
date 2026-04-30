import { Trash2 } from 'lucide-react';
import { useCartStore } from '@/features/cart/model/cartStore';
import { formatCurrency } from '@/shared/lib/formatters';
import { Button } from '@/shared/ui/Button';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Cart() {
  const { items, removeItem } = useCartStore();
  const subtotal = items.reduce((sum, item) => sum + item.qty * item.unitPrice, 0);

  return (
    <PageScaffold
      title="Shopping Cart"
      viewCode="CUST_004"
      purpose="Cart review, quantity updates, discount validation, stock checks, and checkout entry point."
      endpoints={['GET /carts/me', 'POST /carts/me/items', 'PATCH /carts/me/items/{itemId}', 'DELETE /carts/me/items/{itemId}']}
    >
      {items.length === 0 ? (
        <EmptyState title="Cart is empty" description="Cart state is wired and ready for catalog integration." />
      ) : (
        <div className="grid gap-3">
          {items.map((item) => (
            <div key={item.variantId} className="flex items-center justify-between rounded-md border border-slate-200 p-3">
              <div>
                <p className="font-medium">{item.name}</p>
                <p className="text-sm text-slate-600">
                  {item.skuCode} · {item.qty} x {formatCurrency(item.unitPrice)}
                </p>
              </div>
              <Button variant="secondary" icon={<Trash2 size={16} />} onClick={() => removeItem(item.variantId)}>
                Remove
              </Button>
            </div>
          ))}
          <p className="text-right font-semibold">Subtotal: {formatCurrency(subtotal)}</p>
        </div>
      )}
    </PageScaffold>
  );
}
