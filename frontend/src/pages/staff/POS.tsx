import { useState, useRef } from 'react';
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
      <div className="h-screen bg-gray-100 flex flex-col items-center justify-center p-4">
        <div className="bg-white w-full max-w-md p-8 rounded shadow text-center">
          <div className="w-16 h-16 bg-green-100 text-green-600 rounded-full flex items-center justify-center mx-auto mb-4">
            <svg className="w-8 h-8" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7"></path></svg>
          </div>
          <h2 className="text-2xl font-bold mb-2">Sale Complete</h2>
          <p className="text-gray-500 mb-6">Receipt #{receipt.id}</p>
          
          {paymentMethod === 'CASH' && (
            <div className="bg-gray-50 p-4 rounded mb-6 text-left">
              <div className="flex justify-between mb-2"><span>Total:</span> <span>{total.toLocaleString()} VND</span></div>
              <div className="flex justify-between mb-2"><span>Paid:</span> <span>{Number(amountPaid).toLocaleString()} VND</span></div>
              <div className="flex justify-between font-bold text-lg pt-2 border-t border-gray-200"><span>Change Due:</span> <span>{receipt.changeDue.toLocaleString()} VND</span></div>
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
    <div className="h-screen bg-gray-100 flex flex-col">
      <div className="bg-white border-b border-gray-200 p-4 flex justify-between items-center">
        <h1 className="text-xl font-bold">SeShop POS</h1>
        <div className="text-sm text-gray-500">Register: REG-01 | Operator: Staff User</div>
      </div>

      <div className="flex-1 flex overflow-hidden">
        {/* Left Side: Cart */}
        <div className="flex-1 flex flex-col bg-white border-r border-gray-200">
          <div className="p-4 border-b border-gray-200">
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
                  <p className="text-red-600 text-sm mt-1">{skuError}</p>
                )}
              </div>
              <Button type="submit" disabled={isProcessing}>
                {isProcessing ? 'Loading...' : 'Add'}
              </Button>
            </form>
          </div>

          <div className="flex-1 overflow-y-auto p-4">
            {items.length === 0 ? (
              <div className="h-full flex items-center justify-center text-gray-400">
                Cart is empty. Scan an item to begin.
              </div>
            ) : (
              <ul className="divide-y divide-gray-100">
                {items.map((item, idx) => (
                  <li key={idx} className="py-3 flex justify-between items-center">
                    <div>
                      <p className="font-medium">{item.name}</p>
                      <p className="text-sm text-gray-500">{item.skuCode}</p>
                    </div>
                    <div className="flex items-center gap-4">
                      <div className="text-right">
                        <p>{item.price.toLocaleString()} x {item.qty}</p>
                        <p className="font-bold">{(item.price * item.qty).toLocaleString()} VND</p>
                      </div>
                      <button 
                        onClick={() => setItems(items.filter((_, i) => i !== idx))}
                        className="text-red-500 hover:text-red-700 font-bold px-2"
                      >
                        &times;
                      </button>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>
        </div>

        {/* Right Side: Payment Checkout */}
        <div className="w-96 bg-gray-50 flex flex-col">
          <div className="flex-1 p-6">
            <div className="bg-white p-4 rounded shadow-sm mb-6">
              <div className="flex justify-between text-gray-500 mb-2"><span>Subtotal</span><span>{total.toLocaleString()}</span></div>
              <div className="flex justify-between text-gray-500 mb-2"><span>Tax</span><span>Included</span></div>
              <div className="flex justify-between text-2xl font-bold mt-4 pt-4 border-t border-gray-200">
                <span>Total</span>
                <span>{total.toLocaleString()} ₫</span>
              </div>
            </div>

            <div className="mb-6">
              <h3 className="font-medium mb-3">Payment Method</h3>
              <div className="grid grid-cols-2 gap-2">
                <button
                  className={`p-3 rounded border text-center font-medium ${paymentMethod === 'CASH' ? 'bg-brand-dark text-white border-brand-dark' : 'bg-white border-gray-200 hover:bg-gray-50'}`}
                  onClick={() => setPaymentMethod('CASH')}
                >
                  Cash
                </button>
                <button
                  className={`p-3 rounded border text-center font-medium ${paymentMethod === 'CARD' ? 'bg-brand-dark text-white border-brand-dark' : 'bg-white border-gray-200 hover:bg-gray-50'}`}
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

          <div className="p-4 bg-white border-t border-gray-200">
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
