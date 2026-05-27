import { Input } from '@/shared/ui/Input';
import { Button } from '@/shared/ui/Button';
import type { CartLine } from '@/features/cart/model/cartStore';

type CheckoutOrderSummaryProps = {
  cartItems: CartLine[];
  subtotal: number;
  shippingFee: number;
  stripeFee: number;
  discountAmount: number;
  total: number;
  step: number;
  paymentMethod: string;
  discountCode: string;
  setDiscountCode: (val: string) => void;
  handleApplyDiscount: () => void;
  isLoading: boolean;
};

export function CheckoutOrderSummary({
  cartItems,
  subtotal,
  shippingFee,
  stripeFee,
  discountAmount,
  total,
  step,
  paymentMethod,
  discountCode,
  setDiscountCode,
  handleApplyDiscount,
  isLoading
}: CheckoutOrderSummaryProps) {
  return (
    <div className="sticky top-24 rounded-md border border-primary/20 bg-surface p-6 text-ink">
      <h2 className="mb-4 text-lg font-medium">Order Summary</h2>

      <div className="space-y-4 mb-6">
        {cartItems.length > 0 ? cartItems.map((item) => (
          <div key={item.variantId} className="flex gap-4 items-center">
            {item.imageUrl ? (
              <img src={item.imageUrl} alt={item.name} className="h-16 w-16 rounded-md object-cover border border-primary/20" />
            ) : (
              <div className="h-16 w-16 rounded-md bg-ink/10 flex items-center justify-center text-xs text-ink/40">No img</div>
            )}
            <div>
              <h3 className="text-sm font-medium">{item.name}</h3>
              <p className="text-xs text-ink/55">
                {item.skuCode} {item.attributes && Object.keys(item.attributes).length > 0 ? `| ${Object.entries(item.attributes).map(([k, v]) => `${k}: ${v}`).join(', ')}` : ''} | Qty: {item.qty}
              </p>
              <p className="text-sm mt-1 font-semibold">{item.unitPrice.toLocaleString()} VND</p>
            </div>
          </div>
        )) : (
          <p className="text-sm text-ink/55">Your cart is empty.</p>
        )}
      </div>

      <div className="space-y-2 border-t border-primary/15 pt-4 text-sm">
        <div className="flex justify-between">
          <span>Subtotal</span>
          <span>{subtotal.toLocaleString()} VND</span>
        </div>
        <div className="flex justify-between">
          <span>Shipping (GHN)</span>
          <span>
            {step === 1 ? 'Calculated at next step' : (
              <span className="text-ink/80">{shippingFee.toLocaleString()} VND (Paid on delivery)</span>
            )}
          </span>
        </div>
        {paymentMethod === 'STRIPE' && (
          <div className="flex justify-between text-ink/80">
            <span>Stripe Processing Fee</span>
            <span>{stripeFee > 0 ? `${stripeFee.toLocaleString()} VND` : '0 VND'}</span>
          </div>
        )}
        {discountAmount > 0 && (
          <div className="flex justify-between text-success">
            <span>Discount</span>
            <span>-{discountAmount.toLocaleString()} VND</span>
          </div>
        )}
        <div className="flex justify-between border-t border-primary/15 pt-2 text-lg font-medium">
          <span>Total</span>
          <span>{total.toLocaleString()} VND</span>
        </div>
      </div>

      {/* Discount Code Form */}
      <div className="mt-6 border-t border-primary/15 pt-6">
        <div className="flex gap-2">
          <Input
            label="Discount Code"
            placeholder="Enter code (e.g. SUMMER10)"
            value={discountCode}
            onChange={(e) => setDiscountCode(e.target.value)}
          />
          <Button variant="secondary" onClick={handleApplyDiscount} isLoading={isLoading}>
            Apply
          </Button>
        </div>
      </div>
    </div>
  );
}
