import { useTranslation } from 'react-i18next';

export type ReceiptData = {
  receiptNumber: string;
  locationName: string;
  issuedAt?: string;
  createdAt?: string;
  operatorName: string;
  paymentMethod: string;
  totalAmount: number;
  amountPaid: number;
  changeDue?: number;
  items?: Array<{
    id: number;
    name: string;
    skuCode: string;
    unitPrice: number;
    qty: number;
    totalPrice: number;
  }>;
};

type PrintableReceiptProps = {
  /** The receipt data to render — works with both ProcessPosSaleResponse and ReceiptDto */
  receipt: ReceiptData;
  /** When true, renders the 80mm thermal-printer layout (hidden on screen). */
  printOnly?: boolean;
  /** Additional class names for the container. */
  className?: string;
};

/**
 * Unified receipt template used across the POS module.
 *
 * - `printOnly={true}` → Hidden on screen, visible only when `window.print()` is called.
 * - `printOnly={false}` → Visible on screen, hidden when printing.
 */
export function PrintableReceipt({ receipt, printOnly = false, className = '' }: PrintableReceiptProps) {
  const { t } = useTranslation();
  const date = receipt.issuedAt ?? receipt.createdAt ?? '';
  const changeDue = receipt.changeDue ?? 0;

  if (printOnly) {
    return (
      <div
        id="pos-receipt-print"
        className={`hidden print:block text-black bg-white p-4 font-mono text-[11px] w-[80mm] mx-auto ${className}`}
      >
        <div className="text-center mb-4">
          <h2 className="text-base font-bold uppercase tracking-wider">SESHOP</h2>
          <p className="text-[10px]">{receipt.locationName}</p>
          <p className="text-[10px]">{t('pos.date', 'Date')}: {new Date(date).toLocaleString()}</p>
          <p className="text-[10px]">{t('pos.receipt', 'Receipt')}: {receipt.receiptNumber}</p>
          <p className="text-[10px]">{t('pos.cashier', 'Cashier')}: {receipt.operatorName}</p>
        </div>

        <div className="border-t border-b border-dashed border-black py-2 my-2">
          <table className="w-full text-left text-[11px] border-collapse">
            <thead>
              <tr className="border-b border-dashed border-black">
                <th className="pb-1">{t('pos.itemDetails', 'Item Details')}</th>
                <th className="text-right pb-1">{t('pos.priceQty', 'Price x Qty')}</th>
                <th className="text-right pb-1">{t('pos.total', 'Total')}</th>
              </tr>
            </thead>
            <tbody>
              {receipt.items?.map((item) => (
                <tr key={item.id} className="align-top border-b border-black/5 last:border-0">
                  <td className="py-1">
                    <div className="font-bold">{item.name}</div>
                    <div className="text-[9px] text-black/75">
                      ID: {item.id} | SKU: {item.skuCode}
                    </div>
                  </td>
                  <td className="text-right py-1">
                    {item.unitPrice.toLocaleString()} x{item.qty}
                  </td>
                  <td className="text-right py-1 font-bold">
                    {item.totalPrice.toLocaleString()}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        <div className="text-right space-y-1 text-[11px] font-mono">
          <div className="flex justify-between">
            <span>{t('pos.totalAmount', 'Total Amount')}:</span>
            <span className="font-bold">{receipt.totalAmount.toLocaleString()} VND</span>
          </div>
          <div className="flex justify-between">
            <span>{t('pos.paymentMethod', 'Payment Method')}:</span>
            <span>{receipt.paymentMethod}</span>
          </div>
          <div className="flex justify-between">
            <span>{t('pos.amountPaid', 'Amount Paid')}:</span>
            <span>{receipt.amountPaid.toLocaleString()} VND</span>
          </div>
          <div className="flex justify-between border-t border-dashed border-black pt-1 mt-1 font-bold">
            <span>{t('pos.changeDue', 'Change Due')}:</span>
            <span>{changeDue.toLocaleString()} VND</span>
          </div>
        </div>

        <div className="text-center mt-6 pt-4 border-t border-dashed border-black text-[9px]">
          <p className="font-bold">{t('pos.thankYou', 'THANK YOU FOR YOUR PURCHASE!')}</p>
          <p>{t('pos.keepReceipt', 'Please keep this receipt for refund or return.')}</p>
          <p className="mt-1">Powered by SeShop POS</p>
        </div>
      </div>
    );
  }

  // Screen-visible receipt card
  return (
    <div className={`overflow-hidden rounded-md border border-primary/10 bg-ink/[0.02] p-5 text-ink/80 shadow-inner ${className}`}>
      <div className="flex justify-between border-b border-primary/10 pb-3 mb-4 text-sm font-semibold">
        <span className="text-primary font-bold">SESHOP RECEIPT</span>
        <span className="text-ink/60">{receipt.receiptNumber}</span>
      </div>

      <div className="grid grid-cols-2 gap-y-2 text-xs border-b border-primary/10 pb-4 mb-4">
        <div><span className="text-ink/40">{t('pos.location', 'Location')}:</span> <p className="font-medium text-ink">{receipt.locationName}</p></div>
        <div><span className="text-ink/40">{t('pos.date', 'Date')}:</span> <p className="font-medium text-ink">{new Date(date).toLocaleString()}</p></div>
        <div><span className="text-ink/40">{t('pos.cashier', 'Cashier')}:</span> <p className="font-medium text-ink">{receipt.operatorName}</p></div>
        <div><span className="text-ink/40">{t('pos.payment', 'Payment')}:</span> <p className="font-medium text-ink">{receipt.paymentMethod}</p></div>
      </div>

      {/* List of Purchased items */}
      <div className="space-y-3 max-h-60 overflow-y-auto mb-4 pr-1">
        {receipt.items?.map((item) => (
          <div key={item.id} className="flex justify-between items-start text-xs border-b border-primary/5 pb-2 last:border-0 last:pb-0">
            <div>
              <p className="font-bold text-ink">{item.name}</p>
              <p className="text-[10px] text-ink/45">Item ID: {item.id} | SKU: {item.skuCode}</p>
            </div>
            <div className="text-right">
              <p className="text-ink/60">{item.unitPrice.toLocaleString()} x {item.qty}</p>
              <p className="font-bold text-ink">{item.totalPrice.toLocaleString()} VND</p>
            </div>
          </div>
        ))}
      </div>

      {/* Receipt Summary */}
      <div className="border-t border-primary/10 pt-3 space-y-2 text-sm font-medium">
        <div className="flex justify-between text-ink/60">
          <span>{t('pos.totalAmount', 'Total Amount')}:</span>
          <span className="font-semibold text-ink">{receipt.totalAmount.toLocaleString()} VND</span>
        </div>
        {receipt.amountPaid > 0 && (
          <div className="flex justify-between text-ink/60">
            <span>{t('pos.amountTendered', 'Amount Tendered')}:</span>
            <span className="text-ink">{receipt.amountPaid.toLocaleString()} VND</span>
          </div>
        )}
        {changeDue > 0 && (
          <div className="flex justify-between border-t border-primary/10 pt-2 text-base font-bold text-ink">
            <span>{t('pos.changeDue', 'Change Due')}:</span>
            <span className="text-success">{changeDue.toLocaleString()} VND</span>
          </div>
        )}
      </div>
    </div>
  );
}
