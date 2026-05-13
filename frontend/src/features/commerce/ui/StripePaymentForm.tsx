import { useState } from 'react';
import { useStripe, useElements, CardElement } from '@stripe/react-stripe-js';
import { Button } from '@/shared/ui/Button';

interface StripePaymentFormProps {
  clientSecret: string;
  onSuccess: () => void;
  onError: (message: string) => void;
}

export function StripePaymentForm({ clientSecret, onSuccess, onError }: StripePaymentFormProps) {
  const stripe = useStripe();
  const elements = useElements();
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();

    if (!stripe || !elements) {
      return;
    }

    setIsLoading(true);

    const cardElement = elements.getElement(CardElement);

    if (!cardElement) {
      setIsLoading(false);
      return;
    }

    try {
      const { error, paymentIntent } = await stripe.confirmCardPayment(clientSecret, {
        payment_method: {
          card: cardElement,
        },
      });

      if (error) {
        onError(error.message ?? 'Payment failed');
      } else if (paymentIntent && paymentIntent.status === 'succeeded') {
        onSuccess();
      } else {
        onError('Payment status: ' + (paymentIntent?.status ?? 'unknown'));
      }
    } catch (err) {
      onError('An unexpected error occurred during payment.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <form onSubmit={handleSubmit} className="space-y-6">
      <div className="rounded-md border border-primary/20 bg-white p-4">
        <CardElement
          options={{
            style: {
              base: {
                fontSize: '16px',
                color: '#32325d',
                fontFamily: '"Helvetica Neue", Helvetica, sans-serif',
                fontSmoothing: 'antialiased',
                '::placeholder': {
                  color: '#aab7c4',
                },
              },
              invalid: {
                color: '#fa755a',
                iconColor: '#fa755a',
              },
            },
          }}
        />
      </div>
      <Button type="submit" disabled={!stripe || isLoading} isLoading={isLoading} className="w-full">
        Confirm Payment
      </Button>
    </form>
  );
}
