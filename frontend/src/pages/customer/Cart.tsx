import { Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { useCartStore } from '@/features/cart/model/cartStore';
import { getMyCart, removeCartItem } from '@/features/commerce/api/cartApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Button } from '@/shared/ui/Button';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Cart() {
  const { items, setItems, removeItem } = useCartStore();
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState('');
  const subtotal = items.reduce((sum, item) => sum + item.qty * item.unitPrice, 0);

  useEffect(() => {
    getMyCart()
      .then((cart) => {
        setItems(cart.items.map((item) => ({
          id: item.id,
          variantId: item.variantId,
          skuCode: item.skuCode,
          name: item.name,
          qty: item.qty,
          unitPrice: Number(item.unitPrice),
        })));
      })
      .catch(() => setError('Sign in to view your cart.'))
      .finally(() => setIsLoading(false));
  }, [setItems]);

  const handleRemove = async (itemId: number | undefined, variantId: number) => {
    if (!itemId) return;
    await removeCartItem(itemId);
    removeItem(variantId);
  };

  return (
    <PageScaffold
      title="Shopping Cart"
      viewCode="CUST_004"
      purpose="Cart review, quantity updates, discount validation, stock checks, and checkout entry point."
      endpoints={['GET /carts/me', 'POST /carts/me/items', 'PATCH /carts/me/items/{itemId}', 'DELETE /carts/me/items/{itemId}']}
    >
      {isLoading ? (
        <p className="text-sm text-ink/70">Loading cart...</p>
      ) : error ? (
        <EmptyState title="Cart unavailable" description={error} />
      ) : items.length === 0 ? (
        <EmptyState title="Cart is empty" description="No cart items returned by the API." />
      ) : (
        <div className="grid gap-3">
          {items.map((item) => (
            <div key={item.variantId} className="flex items-center justify-between rounded-md border border-primary/20 p-3">
              <div>
                <p className="font-medium">{item.name}</p>
                <p className="text-sm text-ink/70">
                  {item.skuCode} · {item.qty} x {formatCurrency(item.unitPrice)}
                </p>
              </div>
              <Button variant="secondary" icon={<Trash2 size={16} />} onClick={() => handleRemove(item.id, item.variantId)}>
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
