import { useState, useRef } from 'react';
import { CheckCircle2, X } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { processPosSale, lookupProductBySku, type PosItem } from '@/features/staff/api/staffPosApi';

export function POS() {
  const [items, setItems] = useState<PosItem[]>([]);
  const [skuInput, setSkuInput] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD'>('CASH');
  const [amountPaid, setAmountPaid] = useState<number | ''>('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [receipt, setReceipt] = useState<{ id: number; changeDue: number } | null>(null);
  const [skuError, setSkuError] = useState('');

  const barcodeInputRef = useRef<HTMLInputElement>(null);

  const total = items.reduce((sum, item) => sum + item.price * item.qty, 0);

  const handleAddSku = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!skuInput) return;

    setSkuError('');
    setIsProcessing(true);
    try {
      // Real API lookup: retrieve product variant by SKU code
      const variant = await lookupProductBySku(skuInput);
      
      setItems((prev) => {
        const existing = prev.find(i => i.skuCode === skuInput);
        if (existing) {
          return prev.map(i => i.skuCode === skuInput ? { ...i, qty: i.qty + 1 } : i);
        }
        return [...prev, {
          variantId: variant.variantId,
          skuCode: variant.skuCode,
          name: variant.productName,
          price: variant.price,
          qty: 1
        }];
      });
      setSkuInput('');
      barcodeInputRef.current?.focus();
    } catch (error) {
      setSkuError(`Product not found: ${skuInput}`);
      setSkuInput('');
    } finally {
      setIsProcessing(false);
    }
  };

  const handleCheckout = async () => {
    if (items.length === 0) return;
    setIsProcessing(true);
    try {
      const paid = paymentMethod === 'CASH' ? Number(amountPaid) : total;
      const res = await processPosSale(items, paymentMethod, paid);
      setReceipt({ id: res.receiptId, changeDue: res.changeDue });
    } catch (e) {
      console.error(e);
    } finally {
      setIsProcessing(false);
    }
  };

  const handleNewSale = () => {
    setItems([]);
    setReceipt(null);
    setAmountPaid('');
    setPaymentMethod('CASH');
    setSkuError('');
    setTimeout(() => barcodeInputRef.current?.focus(), 100);
  };

  if (receipt) {
    return (
      <div className="flex min-h-[calc(100vh-7rem)] flex-col items-center justify-center p-4">
        <div className="w-full max-w-md rounded-md border border-primary/20 bg-surface p-8 text-center shadow-soft">
          <div className="mx-auto mb-4 flex size-16 items-center justify-center rounded-full bg-success/10 text-success">
            <CheckCircle2 size={34} />
          </div>
          <h2 className="mb-2 text-2xl font-bold text-ink">Sale Complete</h2>
          <p className="mb-6 text-ink/55">Receipt #{receipt.id}</p>
          
          {paymentMethod === 'CASH' && (
            <div className="mb-6 rounded-md bg-ink/[0.03] p-4 text-left text-ink/70">
              <div className="flex justify-between mb-2"><span>Total:</span> <span>{total.toLocaleString()} VND</span></div>
              <div className="flex justify-between mb-2"><span>Paid:</span> <span>{Number(amountPaid).toLocaleString()} VND</span></div>
              <div className="flex justify-between border-t border-primary/15 pt-2 text-lg font-bold text-ink"><span>Change Due:</span> <span>{receipt.changeDue.toLocaleString()} VND</span></div>
            </div>
          )}

          <div className="flex gap-4">
            <Button className="flex-1" variant="secondary" onClick={() => window.print()}>Print Receipt</Button>
            <Button className="flex-1" onClick={handleNewSale}>New Sale</Button>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="flex min-h-[calc(100vh-7rem)] flex-col overflow-hidden rounded-md border border-primary/20 bg-surface text-ink shadow-soft">
      <div className="flex items-center justify-between border-b border-primary/15 bg-surface p-4">
        <h1 className="text-xl font-bold">SeShop POS</h1>
        <div className="text-sm text-ink/55">Register: REG-01 | Operator: Staff User</div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Left Side: Cart */}
        <div className="flex flex-1 flex-col border-r border-primary/15 bg-surface">
          <div className="border-b border-primary/15 p-4">
            <form onSubmit={handleAddSku} className="flex gap-2">
              <div className="flex-1">
                <Input
                  ref={barcodeInputRef}
                  placeholder="Scan barcode or enter SKU"
                  value={skuInput}
                  onChange={(e) => setSkuInput(e.target.value)}
                  autoFocus
                  disabled={isProcessing}
                />
                {skuError && (
                  <p className="mt-1 text-sm text-danger">{skuError}</p>
                )}
              </div>
              <Button type="submit" disabled={isProcessing}>
                {isProcessing ? 'Loading...' : 'Add'}
              </Button>
            </form>
          </div>

          <div className="flex-1 overflow-y-auto p-4">
            {items.length === 0 ? (
              <div className="flex h-full items-center justify-center text-ink/40">
                Cart is empty. Scan an item to begin.
              </div>
            ) : (
              <ul className="divide-y divide-primary/10">
                {items.map((item, idx) => (
                  <li key={idx} className="py-3 flex justify-between items-center">
                    <div>
                      <p className="font-medium">{item.name}</p>
                      <p className="text-sm text-ink/55">{item.skuCode}</p>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p>{item.price.toLocaleString()} x {item.qty}</p>
                        <p className="font-bold">{(item.price * item.qty).toLocaleString()} VND</p>
                      </div>
                      <button 
                        onClick={() => setItems(items.filter((_, i) => i !== idx))}
                        className="px-2 text-danger hover:text-danger/80"
                        aria-label={`Remove ${item.name}`}
                      >
                        <X size={18} />
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Right Side: Payment Checkout */}
        <div className="flex w-96 flex-col bg-surfaceMuted/40">
          <div className="flex-1 p-6">
            <div className="mb-6 rounded-md border border-primary/15 bg-surface p-4 shadow-sm">
              <div className="mb-2 flex justify-between text-ink/55"><span>Subtotal</span><span>{total.toLocaleString()}</span></div>
              <div className="mb-2 flex justify-between text-ink/55"><span>Tax</span><span>Included</span></div>
              <div className="mt-4 flex justify-between border-t border-primary/15 pt-4 text-2xl font-bold">
                <span>Total</span>
                <span>{total.toLocaleString()} VND</span>
              </div>
            </div>

            <div className="mb-6">
              <h3 className="font-medium mb-3">Payment Method</h3>
              <div className="grid grid-cols-2 gap-2">
                <button
                  className={`rounded-md border p-3 text-center font-medium ${paymentMethod === 'CASH' ? 'border-primary bg-primary text-ink' : 'border-primary/20 bg-surface hover:bg-primary/10'}`}
                  onClick={() => setPaymentMethod('CASH')}
                >
                  Cash
                </button>
                <button
                  className={`rounded-md border p-3 text-center font-medium ${paymentMethod === 'CARD' ? 'border-primary bg-primary text-ink' : 'border-primary/20 bg-surface hover:bg-primary/10'}`}
                  onClick={() => setPaymentMethod('CARD')}
                >
                  Card
                </button>
              </div>
            </div>

            {paymentMethod === 'CASH' && (
              <div className="mb-6 animate-fade-in">
                <Input
                  label="Amount Tendered"
                  type="number"
                  value={amountPaid}
                  onChange={(e) => setAmountPaid(Number(e.target.value))}
                  placeholder="Enter amount given"
                />
              </div>
            )}
          </div>

          <div className="border-t border-primary/15 bg-surface p-4">
            <Button 
              className="w-full h-14 text-lg" 
              onClick={handleCheckout}
              disabled={items.length === 0 || (paymentMethod === 'CASH' && Number(amountPaid) < total)}
              isLoading={isProcessing}
            >
              Complete Sale
            </Button>
          </div>
        </div>
      </div>
    </div>
  );
}
