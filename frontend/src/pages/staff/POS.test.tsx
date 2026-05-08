import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { beforeEach, describe, expect, it, vi } from 'vitest';
import { POS } from './POS';
import { lookupProductBySku, processPosSale } from '@/features/staff/api/staffPosApi';

vi.mock('@/features/staff/api/staffPosApi', () => ({
  closeShift: vi.fn(),
  getCurrentShift: vi.fn(),
  lookupProductBySku: vi.fn(),
  processPosSale: vi.fn(),
}));

const mockedLookupProductBySku = vi.mocked(lookupProductBySku);
const mockedProcessPosSale = vi.mocked(processPosSale);

describe('POS', () => {
  beforeEach(() => {
    vi.clearAllMocks();
  });

  it('looks up a scanned SKU and completes a cash sale', async () => {
    const user = userEvent.setup();
    mockedLookupProductBySku.mockResolvedValue({
      variantId: 7001,
      skuCode: 'SKU-LINEN-001',
      productName: 'Linen Shirt',
      price: 250000,
    });
    mockedProcessPosSale.mockResolvedValue({
      receiptId: 501,
      changeDue: 50000,
    });

    render(<POS />);

    await user.type(screen.getByPlaceholderText(/scan barcode/i), 'SKU-LINEN-001');
    await user.click(screen.getByRole('button', { name: /add/i }));

    expect(await screen.findByText('Linen Shirt')).toBeInTheDocument();
    expect(screen.getByText('SKU-LINEN-001')).toBeInTheDocument();
    expect(screen.getByText('250,000 x 1')).toBeInTheDocument();

    await user.type(screen.getByLabelText(/amount tendered/i), '300000');
    await user.click(screen.getByRole('button', { name: /complete sale/i }));

    await waitFor(() => {
      expect(mockedProcessPosSale).toHaveBeenCalledWith(
        [
          {
            variantId: 7001,
            skuCode: 'SKU-LINEN-001',
            name: 'Linen Shirt',
            price: 250000,
            qty: 1,
          },
        ],
        'CASH',
        300000,
      );
    });

    expect(await screen.findByRole('heading', { name: /sale complete/i })).toBeInTheDocument();
    expect(screen.getByText(/Receipt #501/)).toBeInTheDocument();
    expect(screen.getByText('50,000 VND')).toBeInTheDocument();
  });
});
