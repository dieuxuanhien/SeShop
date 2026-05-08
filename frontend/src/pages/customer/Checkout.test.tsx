import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter } from 'react-router-dom';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { Checkout } from './Checkout';
import { getMyCart } from '@/features/commerce/api/cartApi';
import { processCheckout, validateDiscount } from '@/features/commerce/api/checkoutApi';
import { useCartStore } from '@/features/cart/model/cartStore';

vi.mock('@/features/commerce/api/cartApi', () => ({
  getMyCart: vi.fn(),
}));

vi.mock('@/features/commerce/api/checkoutApi', () => ({
  processCheckout: vi.fn(),
  validateDiscount: vi.fn(),
}));

const mockedGetMyCart = vi.mocked(getMyCart);
const mockedProcessCheckout = vi.mocked(processCheckout);
const mockedValidateDiscount = vi.mocked(validateDiscount);

describe('Checkout', () => {
  beforeEach(() => {
    vi.clearAllMocks();
    useCartStore.getState().setItems([]);

    mockedGetMyCart.mockResolvedValue({
      id: 111,
      customerId: 42,
      status: 'ACTIVE',
      items: [
        {
          id: 1,
          variantId: 7001,
          skuCode: 'SKU-LINEN-001',
          name: 'Linen Shirt',
          qty: 2,
          unitPrice: 300000,
        },
      ],
    });
  });

  it('submits checkout with cart, shipping address, payment method, and valid discount', async () => {
    const user = userEvent.setup();
    mockedValidateDiscount.mockResolvedValue({ valid: true, discountAmount: 50000 });
    mockedProcessCheckout.mockResolvedValue({
      orderId: 9001,
      orderNumber: 'ORD-9001',
      paymentStatus: 'PENDING',
      shipmentStatus: 'PENDING',
    });

    render(
      <MemoryRouter>
        <Checkout />
      </MemoryRouter>,
    );

    expect(await screen.findByText('Linen Shirt')).toBeInTheDocument();

    await user.type(screen.getByPlaceholderText(/enter code/i), 'SAVE50');
    await user.click(screen.getByRole('button', { name: /apply/i }));

    await waitFor(() => {
      expect(mockedValidateDiscount).toHaveBeenCalledWith('SAVE50', 600000);
    });
    expect(screen.getByText('-50,000 VND')).toBeInTheDocument();

    await user.type(screen.getByLabelText(/full name/i), 'Linh Nguyen');
    await user.type(screen.getByLabelText(/phone number/i), '0909000000');
    await user.type(screen.getByLabelText(/address line 1/i), '12 Le Loi');
    await user.type(screen.getByLabelText(/ward/i), 'Ben Nghe');
    await user.type(screen.getByLabelText(/district/i), 'District 1');
    await user.click(screen.getByRole('button', { name: /continue to payment/i }));
    await user.click(screen.getByLabelText(/cash on delivery/i));
    await user.click(screen.getByRole('button', { name: /place order/i }));

    await waitFor(() => {
      expect(mockedProcessCheckout).toHaveBeenCalledWith({
        cartId: 111,
        shippingAddress: {
          fullName: 'Linh Nguyen',
          phoneNumber: '0909000000',
          line1: '12 Le Loi',
          ward: 'Ben Nghe',
          district: 'District 1',
          city: 'Ho Chi Minh City',
        },
        paymentMethod: 'COD',
        discountCode: 'SAVE50',
      });
    });

    expect(await screen.findByRole('heading', { name: /order confirmed/i })).toBeInTheDocument();
    expect(screen.getByText(/ORD-9001/)).toBeInTheDocument();
  });
});
