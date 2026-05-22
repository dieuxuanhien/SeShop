import { useEffect, useMemo, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import {
  validateDiscount,
  processCheckout,
  estimateShippingFee,
  validateShippingAddress,
  estimateStripeFee,
  getProvinces,
  getDistricts,
  getWards,
  type Province,
  type District,
  type Ward,
  type CheckoutRequest,
  type CheckoutResponse,
} from '@/features/commerce/api/checkoutApi';
import { getMyCart } from '@/features/commerce/api/cartApi';
import { useCartStore } from '@/features/cart/model/cartStore';
import { loadStripe } from '@stripe/stripe-js/pure';
import { Elements } from '@stripe/react-stripe-js';
import { env } from '@/shared/config/env';
import { StripePaymentForm } from '@/features/commerce/ui/StripePaymentForm';
import { CheckoutOrderSummary } from '@/features/commerce/ui/CheckoutOrderSummary';
import { CheckoutAddressForm } from '@/features/commerce/ui/CheckoutAddressForm';
import { CheckoutPaymentSelector } from '@/features/commerce/ui/CheckoutPaymentSelector';

export function Checkout() {
  const navigate = useNavigate();
  const stripePromise = useMemo(() => loadStripe(env.stripePublishableKey), []);
  const [step, setStep] = useState<1 | 2 | 3>(1);
  const [isLoading, setIsLoading] = useState(false);
  const [isValidatingAddress, setIsValidatingAddress] = useState(false);
  const [error, setError] = useState('');
  const [addressValidationMsg, setAddressValidationMsg] = useState('');

  const cartItems = useCartStore((state) => state.items);
  const setCartItems = useCartStore((state) => state.setItems);
  const clearCart = useCartStore((state) => state.clear);
  const [cartId, setCartId] = useState<number | null>(null);

  const subtotal = useMemo(() => cartItems.reduce((sum, item) => sum + item.unitPrice * item.qty, 0), [cartItems]);
  const [shippingFee, setShippingFee] = useState(0);
  const [stripeFee, setStripeFee] = useState(0);

  const [discountCode, setDiscountCode] = useState('');
  const [discountAmount, setDiscountAmount] = useState(0);

  // Address Combo Box State
  const [provinces, setProvinces] = useState<Province[]>([]);
  const [districts, setDistricts] = useState<District[]>([]);
  const [wards, setWards] = useState<Ward[]>([]);

  const [selectedProvinceId, setSelectedProvinceId] = useState<number | null>(null);
  const [selectedDistrictId, setSelectedDistrictId] = useState<number | null>(null);
  const [selectedWardCode, setSelectedWardCode] = useState<string>('');

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
  const [showStripeForm, setShowStripeForm] = useState(false);

  const total = subtotal + shippingFee + stripeFee - discountAmount;

  useEffect(() => {
    getMyCart()
      .then((cart) => {
        setCartId(cart.id);
        setCartItems(cart.items.map((item) => ({
          id: item.id,
          variantId: item.variantId,
          skuCode: item.skuCode,
          name: item.name,
          color: item.color,
          size: item.size,
          imageUrl: item.imageUrl,
          qty: item.qty,
          unitPrice: Number(item.unitPrice),
        })));
      })
      .catch(() => setCartId(null));
  }, [setCartItems]);

  // Load Provinces on mount
  useEffect(() => {
    getProvinces().then(setProvinces).catch(() => {});
  }, []);

  // Load Districts when Province changes
  useEffect(() => {
    if (selectedProvinceId) {
      getDistricts(selectedProvinceId).then(setDistricts).catch(() => {});
      const p = provinces.find((x) => x.ProvinceID === selectedProvinceId);
      if (p) setAddress((prev) => ({ ...prev, city: p.ProvinceName }));
    } else {
      setDistricts([]);
    }
    setSelectedDistrictId(null);
    setSelectedWardCode('');
    setWards([]);
  }, [selectedProvinceId, provinces]);

  // Load Wards when District changes
  useEffect(() => {
    if (selectedDistrictId) {
      getWards(selectedDistrictId).then(setWards).catch(() => {});
      const d = districts.find((x) => x.DistrictID === selectedDistrictId);
      if (d) setAddress((prev) => ({ ...prev, district: d.DistrictName }));
    } else {
      setWards([]);
    }
    setSelectedWardCode('');
  }, [selectedDistrictId, districts]);

  const handleWardChange = (code: string) => {
    setSelectedWardCode(code);
    const w = wards.find((x) => x.WardCode === code);
    if (w) setAddress((prev) => ({ ...prev, ward: w.WardName }));
  };

  // Estimate Stripe processing fee when applicable
  useEffect(() => {
    const currentBaseTotal = subtotal + shippingFee - discountAmount;
    if (paymentMethod === 'STRIPE' && currentBaseTotal > 0) {
      estimateStripeFee(currentBaseTotal)
        .then((res) => setStripeFee(res.fee))
        .catch(() => setStripeFee(0));
    } else {
      setStripeFee(0);
    }
  }, [subtotal, shippingFee, discountAmount, paymentMethod]);

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

  const handleValidateAndContinue = async () => {
    setIsValidatingAddress(true);
    setError('');
    setAddressValidationMsg('');
    try {
      const res = await validateShippingAddress(address.ward, address.district, address.city);
      if (res.valid) {
        // Fetch GHN shipping fee estimate
        try {
          const feeRes = await estimateShippingFee(`${address.line1}, ${address.ward}, ${address.district}, ${address.city}`);
          setShippingFee(feeRes.fee);
        } catch (e) {
          console.error('Failed to estimate shipping fee', e);
        }
        setStep(2);
      } else {
        setError(res.message || 'Invalid shipping address. Please check ward, district, and city.');
      }
    } catch (err: any) {
      setError(err.message || 'Failed to validate address with GHN.');
    } finally {
      setIsValidatingAddress(false);
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
      setOrderResponse({ ...res, totalAmount: total });

      if (res.clientSecret && paymentMethod === 'STRIPE') {
        setShowStripeForm(true);
      } else {
        clearCart();
        setStep(3); // success
      }
    } catch (err: any) {
      setError(err.message || 'Checkout failed. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  const handleStripeSuccess = () => {
    clearCart();
    setStep(3);
  };

  if (step === 3 && orderResponse) {
    return (
      <div className="mx-auto max-w-2xl px-4 py-16 text-center sm:px-6 lg:px-8">
        <h1 className="font-display mb-4 text-3xl text-highlight">Order Confirmed</h1>
        <p className="mb-8 text-surface/70">
          Thank you for your purchase. Your order number is <strong>{orderResponse.orderNumber}</strong>.
        </p>
        <div className="mb-8 inline-block rounded-md border border-primary/20 bg-surface p-6 text-left text-ink">
          <p><strong>Payment Status:</strong> {orderResponse.paymentStatus}</p>
          <p><strong>Total:</strong> {orderResponse.totalAmount?.toLocaleString() ?? 0} VND</p>
        </div>
        <div>
          <Button onClick={() => navigate('/orders')}>View My Orders</Button>
        </div>
      </div>
    );
  }

  return (
    <Elements stripe={stripePromise}>
      <div className="mx-auto max-w-7xl px-4 py-12 sm:px-6 lg:px-8">
        <h1 className="font-display mb-8 text-3xl text-highlight">Checkout</h1>

        <div className="flex flex-col lg:flex-row gap-12">
          <div className="flex-1 space-y-8">
            {error && (
              <div className="rounded-md border border-danger/30 bg-danger/10 p-4 text-danger">
                {error}
              </div>
            )}

            {/* Step 1: Shipping */}
            <section className={`transition-opacity ${step === 2 ? 'opacity-50 pointer-events-none' : ''}`}>
              <h2 className="mb-4 text-xl font-medium text-surface">1. Shipping Address</h2>
              <CheckoutAddressForm
                address={address}
                setAddress={setAddress}
                provinces={provinces}
                districts={districts}
                wards={wards}
                selectedProvinceId={selectedProvinceId}
                setSelectedProvinceId={setSelectedProvinceId}
                selectedDistrictId={selectedDistrictId}
                setSelectedDistrictId={setSelectedDistrictId}
                selectedWardCode={selectedWardCode}
                handleWardChange={handleWardChange}
              />
              {step === 1 && (
                <div className="mt-6 flex justify-end">
                  <Button
                    onClick={handleValidateAndContinue}
                    isLoading={isValidatingAddress}
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
                <h2 className="mb-4 text-xl font-medium text-surface">2. Payment Method</h2>
                <CheckoutPaymentSelector
                  paymentMethod={paymentMethod}
                  setPaymentMethod={setPaymentMethod}
                />

                {showStripeForm && orderResponse?.clientSecret ? (
                  <div className="mt-8 rounded-md border border-primary/20 bg-surface/50 p-6 animate-slide-up">
                    <h3 className="mb-4 text-lg font-medium text-surface">Complete Payment</h3>
                    <StripePaymentForm
                      clientSecret={orderResponse.clientSecret}
                      onSuccess={handleStripeSuccess}
                      onError={(msg) => setError(msg)}
                    />
                    <button
                      type="button"
                      onClick={() => setShowStripeForm(false)}
                      className="mt-4 text-xs text-surface/40 hover:text-surface"
                    >
                      Cancel and change payment method
                    </button>
                  </div>
                ) : (
                  <div className="mt-6 flex justify-between items-center">
                    <button type="button" onClick={() => setStep(1)} className="text-sm text-surface/60 underline hover:text-primary">
                      Back to Shipping
                    </button>
                    <Button onClick={handleCheckout} isLoading={isLoading}>
                      Place Order
                    </Button>
                  </div>
                )}
              </section>
            )}
          </div>

          {/* Order Summary Sidebar */}
          <div className="w-full lg:w-96">
            <CheckoutOrderSummary
              cartItems={cartItems}
              subtotal={subtotal}
              shippingFee={shippingFee}
              stripeFee={stripeFee}
              discountAmount={discountAmount}
              total={total}
              step={step}
              paymentMethod={paymentMethod}
              discountCode={discountCode}
              setDiscountCode={setDiscountCode}
              handleApplyDiscount={handleApplyDiscount}
              isLoading={isLoading}
            />
          </div>
        </div>
      </div>
    </Elements>
  );
}
