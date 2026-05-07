import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { validateDiscount, processCheckout, type CheckoutRequest, type CheckoutResponse } from '@/features/commerce/api/checkoutApi';
import { getMyCart } from '@/features/commerce/api/cartApi';
import { useCartStore } from '@/features/cart/model/cartStore';

export function Checkout() {
  const navigate = useNavigate();
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [error, setError] = useState('');
  const cartItems = useCartStore((state) => state.items);
  const setCartItems = useCartStore((state) => state.setItems);
  const clearCart = useCartStore((state) => state.clear);
  const [cartId, setCartId] = useState<number | null>(null);

  const subtotal = useMemo(() => cartItems.reduce((sum, item) => sum + item.unitPrice * item.qty, 0), [cartItems]);
  const shippingFee = 0; // Free shipping
  const [discountCode, setDiscountCode] = useState('');
  const [discountAmount, setDiscountAmount] = useState(0);

  // Form state
  const [address, setAddress] = useState({
    fullName: '',
    phoneNumber: '',
    line1: '',
    ward: '',
    district: '',
    city: 'Ho Chi Minh City',
  });
  const [paymentMethod, setPaymentMethod] = useState<'STRIPE' | 'COD'>('STRIPE');
  const [orderResponse, setOrderResponse] = useState<CheckoutResponse | null>(null);

  const total = subtotal + shippingFee - discountAmount;

  useEffect(() => {
    getMyCart()
      .then((cart) => {
        setCartId(cart.id);
        setCartItems(cart.items.map((item) => ({
          id: item.id,
          variantId: item.variantId,
          skuCode: item.skuCode,
          name: item.name,
          qty: item.qty,
          unitPrice: Number(item.unitPrice),
        })));
      })
      .catch(() => setCartId(null));
  }, [setCartItems]);

  const handleApplyDiscount = async () => {
    if (!discountCode) return;
    setIsLoading(true);
    setError('');
    try {
      const res = await validateDiscount(discountCode, subtotal);
      if (res.valid) {
        setDiscountAmount(res.discountAmount);
      } else {
        setError('Invalid discount code.');
        setDiscountAmount(0);
      }
    } catch (err) {
      setError('Failed to validate discount.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleCheckout = async () => {
    setIsLoading(true);
    setError('');
    try {
      const req: CheckoutRequest = {
        cartId: cartId ?? 0,
        shippingAddress: address,
        paymentMethod,
        discountCode: discountAmount > 0 ? discountCode : undefined,
      };
      const res = await processCheckout(req);
      setOrderResponse(res);
      clearCart();
      setStep(3); // success
    } catch (err) {
      setError('Checkout failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  if (step === 3 && orderResponse) {
    return (
      <div className="max-w-2xl mx-auto py-16 px-4 sm:px-6 lg:px-8 text-center">
        <h1 className="text-3xl font-serif text-brand-dark mb-4">Order Confirmed</h1>
        <p className="text-gray-600 mb-8">
          Thank you for your purchase. Your order number is <strong>{orderResponse.orderNumber}</strong>.
        </p>
        <div className="bg-gray-50 p-6 rounded-lg mb-8 inline-block text-left">
          <p><strong>Payment Status:</strong> {orderResponse.paymentStatus}</p>
          <p><strong>Total:</strong> {total.toLocaleString()} VND</p>
        </div>
        <div>
          <Button onClick={() => navigate('/orders')}>View My Orders</Button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-7xl mx-auto py-12 px-4 sm:px-6 lg:px-8">
      <h1 className="text-3xl font-serif text-brand-dark mb-8">Checkout</h1>
      
      <div className="flex flex-col lg:flex-row gap-12">
        <div className="flex-1 space-y-8">
          {error && (
            <div className="p-4 bg-red-50 text-red-600 border border-red-200 rounded">
              {error}
            </div>
          )}

          {/* Step 1: Shipping */}
          <section className={`transition-opacity ${step === 2 ? 'opacity-50 pointer-events-none' : ''}`}>
            <h2 className="text-xl font-medium mb-4">1. Shipping Address</h2>
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <Input
                label="Full Name"
                value={address.fullName}
                onChange={(e) => setAddress({ ...address, fullName: e.target.value })}
                required
              />
              <Input
                label="Phone Number"
                value={address.phoneNumber}
                onChange={(e) => setAddress({ ...address, phoneNumber: e.target.value })}
                required
              />
              <div className="md:col-span-2">
                <Input
                  label="Address Line 1"
                  value={address.line1}
                  onChange={(e) => setAddress({ ...address, line1: e.target.value })}
                  required
                />
              </div>
              <Input
                label="Ward"
                value={address.ward}
                onChange={(e) => setAddress({ ...address, ward: e.target.value })}
                required
              />
              <Input
                label="District"
                value={address.district}
                onChange={(e) => setAddress({ ...address, district: e.target.value })}
                required
              />
            </div>
            {step === 1 && (
              <div className="mt-6 flex justify-end">
                <Button 
                  onClick={() => setStep(2)}
                  disabled={!address.fullName || !address.phoneNumber || !address.line1 || !address.district || !address.ward}
                >
                  Continue to Payment
                </Button>
              </div>
            )}
          </section>

          {/* Step 2: Payment */}
          {step === 2 && (
            <section className="animate-fade-in">
              <h2 className="text-xl font-medium mb-4">2. Payment Method</h2>
              <div className="space-y-4">
                <label className="flex items-center space-x-3 p-4 border rounded cursor-pointer hover:bg-gray-50">
                  <input
                    type="radio"
                    checked={paymentMethod === 'STRIPE'}
                    onChange={() => setPaymentMethod('STRIPE')}
                    className="h-4 w-4 text-brand-dark border-gray-300 focus:ring-brand-dark"
                  />
                  <span>Credit Card (Stripe)</span>
                </label>
                <label className="flex items-center space-x-3 p-4 border rounded cursor-pointer hover:bg-gray-50">
                  <input
                    type="radio"
                    checked={paymentMethod === 'COD'}
                    onChange={() => setPaymentMethod('COD')}
                    className="h-4 w-4 text-brand-dark border-gray-300 focus:ring-brand-dark"
                  />
                  <span>Cash on Delivery</span>
                </label>
              </div>

              <div className="mt-6 flex justify-between items-center">
                <button type="button" onClick={() => setStep(1)} className="text-sm text-gray-500 hover:text-brand-dark underline">
                  Back to Shipping
                </button>
                <Button onClick={handleCheckout} isLoading={isLoading}>
                  Place Order
                </Button>
              </div>
            </section>
          )}
        </div>

        {/* Order Summary Sidebar */}
        <div className="w-full lg:w-96">
          <div className="bg-gray-50 p-6 rounded-lg sticky top-8">
            <h2 className="text-lg font-medium mb-4">Order Summary</h2>
            
            <div className="space-y-4 mb-6">
              {cartItems.length > 0 ? cartItems.map((item) => (
                <div key={item.variantId} className="flex gap-4">
                  <div className="w-16 h-20 rounded bg-gray-200" />
                  <div>
                    <h3 className="text-sm font-medium">{item.name}</h3>
                    <p className="text-xs text-gray-500">Qty: {item.qty}</p>
                    <p className="text-sm mt-1">{item.unitPrice.toLocaleString()} VND</p>
                  </div>
                </div>
              )) : (
                <p className="text-sm text-gray-500">Your cart is empty.</p>
              )}
            </div>

            <div className="border-t border-gray-200 pt-4 space-y-2 text-sm">
              <div className="flex justify-between">
                <span>Subtotal</span>
                <span>{subtotal.toLocaleString()} VND</span>
              </div>
              <div className="flex justify-between">
                <span>Shipping</span>
                <span>Free</span>
              </div>
              {discountAmount > 0 && (
                <div className="flex justify-between text-green-600">
                  <span>Discount</span>
                  <span>-{discountAmount.toLocaleString()} VND</span>
                </div>
              )}
              <div className="flex justify-between font-medium text-lg pt-2 border-t border-gray-200">
                <span>Total</span>
                <span>{total.toLocaleString()} VND</span>
              </div>
            </div>

            {/* Discount Code Form */}
            <div className="mt-6 pt-6 border-t border-gray-200">
              <label className="block text-sm font-medium text-gray-700 mb-2">Discount Code</label>
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
        </div>
      </div>
    </div>
  );
}
