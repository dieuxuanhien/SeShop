import { Minus, Plus, ShoppingBag, Trash2 } from 'lucide-react';
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
          attributes: item.attributes,
          imageUrl: item.imageUrl,
          qty: item.qty,
          unitPrice: Number(item.unitPrice),
        })));
      })
      .catch(() => setError('Sign in to view your cart.'))
      .finally(() => setIsLoading(false));
  }, [setItems]);

  const handleRemove = async (itemId: number | undefined, variantId: number) => {
    if (!itemId) return;
    try {
      await removeCartItem(itemId);
      removeItem(variantId);
    } catch (err) {
      setError('Failed to remove item. Please try again.');
    }
  };

  const handleQtyChange = async (itemId: number | undefined, qty: number) => {
    if (!itemId || qty < 1) return;
    try {
      const cart = await updateCartItem(itemId, qty);
      setItems(cart.items.map((item) => ({
        id: item.id,
        variantId: item.variantId,
        skuCode: item.skuCode,
        name: item.name,
        attributes: item.attributes,
        imageUrl: item.imageUrl,
        qty: item.qty,
        unitPrice: Number(item.unitPrice),
      })));
    } catch (err) {
      setError('Failed to update quantity. Please try again.');
    }
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
          <div className="flex flex-col items-center justify-center rounded-card border border-dashed border-primary/40 bg-surface p-12 text-center">
            <div className="flex h-16 w-16 items-center justify-center rounded-full bg-primary/10 mb-6">
              <ShoppingBag className="h-8 w-8 text-primary" strokeWidth={1.5} />
            </div>
            <h2 className="font-display text-lg font-medium text-ink">Cart unavailable</h2>
            <p className="mt-2 text-sm text-ink/70 max-w-sm">{error}</p>
            <NavLink to="/auth/login" className="mt-4 inline-flex items-center gap-2 rounded-md bg-primary px-5 py-2.5 text-sm font-semibold text-ink transition hover:bg-primaryStrong">
              Sign in
            </NavLink>
          </div>
        ) : items.length === 0 ? (
          <EmptyState title="Cart is empty" description="Add a piece from the collection to begin checkout." />
        ) : (
          <div className="grid gap-4">
            {items.map((item) => (
              <div key={item.variantId} className="grid gap-3 rounded-md border border-primary/20 p-4 md:grid-cols-[minmax(0,1fr)_auto] items-center">
                <div className="flex gap-4 items-center">
                  {item.imageUrl ? (
                    <img src={item.imageUrl} alt={item.name} className="h-16 w-16 rounded-md object-cover border border-primary/20" />
                  ) : (
                    <div className="h-16 w-16 rounded-md bg-ink/10 flex items-center justify-center text-xs text-ink/40">No img</div>
                  )}
                  <div>
                    <p className="font-semibold text-ink">{item.name}</p>
                    <p className="mt-0.5 text-xs text-ink/60">
                      {item.skuCode} {item.attributes && Object.keys(item.attributes).length > 0 ? `| ${Object.entries(item.attributes).map(([k, v]) => `${k}: ${v}`).join(', ')}` : ''}
                    </p>
                    <p className="mt-1 text-sm font-medium text-ink">
                      {formatCurrency(item.unitPrice)}
                    </p>
                  </div>
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
