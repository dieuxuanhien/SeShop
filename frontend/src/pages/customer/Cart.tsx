import { Minus, Plus, Trash2 } from 'lucide-react';
import { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { useCartStore } from '@/features/cart/model/cartStore';
import { getMyCart, removeCartItem, updateCartItem } from '@/features/commerce/api/cartApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
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

  const handleQtyChange = async (itemId: number | undefined, qty: number) => {
    if (!itemId || qty < 1) return;
    const cart = await updateCartItem(itemId, qty);
    setItems(cart.items.map((item) => ({
      id: item.id,
      variantId: item.variantId,
      skuCode: item.skuCode,
      name: item.name,
      qty: item.qty,
      unitPrice: Number(item.unitPrice),
    })));
  };

  return (
    <PageScaffold
      title="Shopping Cart"
      viewCode="CUST_004"
      purpose="Review selected pieces, adjust quantities, and continue to checkout."
    >
      <Card className="border-primary/20 bg-surface/95 p-5">
        {isLoading ? (
          <p className="text-sm text-ink/70">Loading cart...</p>
        ) : error ? (
          <EmptyState title="Cart unavailable" description={error} />
        ) : items.length === 0 ? (
          <EmptyState title="Cart is empty" description="Add a piece from the collection to begin checkout." />
        ) : (
          <div className="grid gap-4">
            {items.map((item) => (
              <div key={item.variantId} className="grid gap-3 rounded-md border border-primary/20 p-4 md:grid-cols-[minmax(0,1fr)_auto]">
                <div>
                  <p className="font-semibold text-ink">{item.name}</p>
                  <p className="mt-1 text-sm text-ink/60">
                    {item.skuCode} | {formatCurrency(item.unitPrice)}
                  </p>
                </div>
                <div className="flex flex-wrap items-center gap-3 md:justify-end">
                  <div className="inline-flex items-center rounded-md border border-primary/20">
                    <button className="px-3 py-2 text-ink/65" aria-label="Decrease quantity" onClick={() => handleQtyChange(item.id, item.qty - 1)}>
                      <Minus size={14} />
                    </button>
                    <span className="min-w-10 text-center text-sm font-semibold text-ink">{item.qty}</span>
                    <button className="px-3 py-2 text-ink/65" aria-label="Increase quantity" onClick={() => handleQtyChange(item.id, item.qty + 1)}>
                      <Plus size={14} />
                    </button>
                  </div>
                  <Button variant="secondary" icon={<Trash2 size={16} />} onClick={() => handleRemove(item.id, item.variantId)}>
                    Remove
                  </Button>
                </div>
              </div>
            ))}
            <div className="flex flex-wrap items-center justify-between gap-4 border-t border-primary/15 pt-4">
              <p className="text-lg font-semibold text-ink">Subtotal: {formatCurrency(subtotal)}</p>
              <NavLink to="/checkout">
                <Button>Checkout</Button>
              </NavLink>
            </div>
          </div>
        )}
      </Card>
    </PageScaffold>
  );
}
