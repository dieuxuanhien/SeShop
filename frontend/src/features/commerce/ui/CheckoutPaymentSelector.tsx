type CheckoutPaymentSelectorProps = {
  paymentMethod: 'STRIPE' | 'COD';
  setPaymentMethod: (method: 'STRIPE' | 'COD') => void;
};

export function CheckoutPaymentSelector({
  paymentMethod,
  setPaymentMethod,
}: CheckoutPaymentSelectorProps) {
  return (
    <div className="space-y-4">
      <label className="flex cursor-pointer items-center space-x-3 rounded-md border border-primary/20 bg-surface p-4 text-ink hover:bg-surfaceMuted">
        <input
          type="radio"
          checked={paymentMethod === 'STRIPE'}
          onChange={() => setPaymentMethod('STRIPE')}
          className="h-4 w-4 accent-primary"
        />
        <span>Credit Card (Stripe)</span>
      </label>
      <label className="flex cursor-pointer items-center space-x-3 rounded-md border border-primary/20 bg-surface p-4 text-ink hover:bg-surfaceMuted">
        <input
          type="radio"
          checked={paymentMethod === 'COD'}
          onChange={() => setPaymentMethod('COD')}
          className="h-4 w-4 accent-primary"
        />
        <span>Cash on Delivery</span>
      </label>
    </div>
  );
}
