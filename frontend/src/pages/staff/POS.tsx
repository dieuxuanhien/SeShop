import { useState, useRef, useEffect } from 'react';
import { CheckCircle2, X, Clock, Calendar, CreditCard, User, Store, Printer, Eye } from 'lucide-react';
import { PrintableReceipt } from '@/features/staff/ui/PrintableReceipt';
import { PosHistory } from '@/features/staff/ui/PosHistory';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { useStaffLocation } from '@/shared/context/LocationContext';
import { processPosSale, lookupProductBySku, getReceiptHistory, type PosItem, type ProcessPosSaleResponse, type ReceiptDto } from '@/features/staff/api/staffPosApi';

export function POS() {
  const [items, setItems] = useState<PosItem[]>([]);
  const [skuInput, setSkuInput] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD'>('CASH');
  const [amountPaid, setAmountPaid] = useState<string>('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [receipt, setReceipt] = useState<ProcessPosSaleResponse | null>(null);
  const [skuError, setSkuError] = useState('');

  const [recentSales, setRecentSales] = useState<ReceiptDto[]>([]);
  const [recentSalesLoading, setRecentSalesLoading] = useState(false);
  const [printReceipt, setPrintReceipt] = useState<ReceiptDto | null>(null);

  const [view, setView] = useState<'pos' | 'history'>('pos');
  const { activeLocationId, locations } = useStaffLocation();
  const activeLocation = locations.find(l => l.id === activeLocationId);

  const fetchRecentSales = async () => {
    setRecentSalesLoading(true);
    try {
      const data = await getReceiptHistory(0, 5);
      setRecentSales(data.items || []);
    } catch (e) {
      console.error(e);
    } finally {
      setRecentSalesLoading(false);
    }
  };

  useEffect(() => {
    fetchRecentSales();
  }, []);

  // Barcode scanner support (GAP-20)
  useEffect(() => {
    let barcodeBuffer = '';
    let barcodeTimeout: ReturnType<typeof setTimeout>;

    const handleKeyDown = (e: KeyboardEvent) => {
      // Ignore if typing in an input other than our main barcode input
      if (
        e.target instanceof HTMLInputElement &&
        e.target !== barcodeInputRef.current &&
        e.target.type !== 'radio' &&
        e.target.type !== 'checkbox'
      ) {
        return;
      }

      if (e.key === 'Enter' && barcodeBuffer.length > 3) {
        e.preventDefault();
        setSkuInput(barcodeBuffer);
        // Simulate form submission
        setTimeout(() => {
          if (barcodeInputRef.current?.form) {
            barcodeInputRef.current.form.requestSubmit();
          }
        }, 10);
        barcodeBuffer = '';
        return;
      }

      if (e.key.length === 1) {
        barcodeBuffer += e.key;
        clearTimeout(barcodeTimeout);
        barcodeTimeout = setTimeout(() => {
          barcodeBuffer = '';
        }, 50); // Scanner types very fast, usually < 30ms between strokes
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => {
      window.removeEventListener('keydown', handleKeyDown);
      clearTimeout(barcodeTimeout);
    };
  }, []);

  const handleViewRecentReceipt = (receipt: ReceiptDto) => {
    setView('history');
  };

  const handleReprintRecentReceipt = (receipt: ReceiptDto) => {
    setPrintReceipt(receipt);
    setTimeout(() => {
      window.print();
      setTimeout(() => setPrintReceipt(null), 500);
    }, 150);
  };

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
      setReceipt(res);
      fetchRecentSales();
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
    fetchRecentSales();
    setTimeout(() => barcodeInputRef.current?.focus(), 100);
  };

  if (!activeLocationId) {
    return (
      <div className="flex min-h-[calc(100vh-7rem)] flex-col items-center justify-center p-4">
        <div className="flex flex-col items-center gap-4 text-ink/50">
          <Store size={48} className="opacity-20" />
          <p className="text-lg font-medium">Please select a location from the top navigation to use POS.</p>
        </div>
      </div>
    );
  }

  if (receipt) {
    return (
      <div className="flex min-h-[calc(100vh-7rem)] flex-col items-center justify-center p-4">
        <PrintableReceipt receipt={receipt} printOnly={true} />
        {/* Beautiful Screen Preview */}
        <div className="w-full max-w-lg rounded-md border border-primary/20 bg-surface p-6 shadow-soft print:hidden">
          <div className="text-center mb-6">
            <div className="mx-auto mb-3 flex size-12 items-center justify-center rounded-full bg-success/10 text-success">
              <CheckCircle2 size={28} />
            </div>
            <h2 className="text-xl font-bold text-ink">Sale Complete</h2>
            <p className="text-sm text-ink/55">Transaction processed successfully</p>
          </div>
          <PrintableReceipt receipt={receipt} className="mb-6" />
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
      <div className="flex items-center justify-between border-b border-primary/15 bg-surface p-4 print:hidden">
        <div className="flex items-center gap-4">
          <h1 className="text-xl font-bold">SeShop POS</h1>
          <div className="flex gap-2">
            <button
              onClick={() => setView('pos')}
              className={`px-3 py-1 text-xs font-semibold rounded-full border transition-all ${view === 'pos' ? 'bg-primary text-ink border-primary' : 'bg-surface hover:bg-primary/10 border-primary/20 text-ink/75'}`}
            >
              POS Register
            </button>
            <button
              onClick={() => {
                setView('history');
              }}
              className={`px-3 py-1 text-xs font-semibold rounded-full border transition-all ${view === 'history' ? 'bg-primary text-ink border-primary' : 'bg-surface hover:bg-primary/10 border-primary/20 text-ink/75'}`}
            >
              Receipt History
            </button>
          </div>
        </div>
        <div className="text-sm text-ink/55">Register: REG-01 | {activeLocation?.displayName}</div>
      </div>

      {view === 'history' ? (
        <PosHistory />
      ) : (
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
                <div className="flex h-full flex-col justify-between p-4">
                  <div className="flex flex-1 flex-col items-center justify-center text-ink/40 py-10">
                    <Store size={48} className="mb-2 text-primary/30" />
                    <p className="font-medium text-ink/60">Cart is empty</p>
                    <p className="text-xs text-ink/40">Scan an item or type a SKU above to begin</p>
                  </div>
                  
                  {/* Recent Transactions Section inside POS Cart view */}
                  <div className="border-t border-primary/10 pt-4">
                    <div className="mb-3 flex items-center justify-between">
                      <h3 className="text-xs font-semibold uppercase tracking-wider text-ink/55 flex items-center gap-1.5">
                        <Clock size={13} className="text-primary/70" /> Recent Sales (Today)
                      </h3>
                      <button 
                        onClick={fetchRecentSales} 
                        className="text-[10px] font-semibold text-primary hover:text-primaryStrong transition-colors"
                      >
                        Refresh List
                      </button>
                    </div>

                    {recentSalesLoading ? (
                      <div className="py-4 text-center text-xs text-ink/40">Loading past sales...</div>
                    ) : recentSales.length === 0 ? (
                      <div className="py-4 text-center text-xs text-ink/30 border border-dashed border-primary/10 rounded-md bg-ink/[0.01]">
                        No sales processed yet today.
                      </div>
                    ) : (
                      <div className="space-y-2 max-h-56 overflow-y-auto pr-1">
                        {recentSales.map((sale) => (
                          <div 
                            key={sale.id} 
                            className="flex items-center justify-between rounded-md border border-primary/10 bg-ink/[0.01] p-2.5 transition-all hover:bg-primary/5 hover:border-primary/25"
                          >
                            <div className="flex-1 min-w-0">
                              <div className="flex items-center gap-1.5">
                                <span className="font-mono text-xs font-bold text-ink truncate">
                                  {sale.receiptNumber}
                                </span>
                                <span className="bg-primary/10 text-primary text-[9px] px-1 py-0.5 rounded-full font-bold">
                                  {sale.paymentMethod}
                                </span>
                              </div>
                              <p className="text-[10px] text-ink/40 mt-0.5">
                                {new Date(sale.issuedAt).toLocaleTimeString()} · {sale.operatorName}
                              </p>
                            </div>
                            
                            <div className="flex items-center gap-3">
                              <span className="font-bold text-xs text-ink">
                                {sale.totalAmount.toLocaleString()} VND
                              </span>
                              <div className="flex gap-1.5">
                                <button
                                  onClick={() => handleViewRecentReceipt(sale)}
                                  className="p-1 rounded-md border border-primary/10 bg-surface hover:bg-primary/10 hover:border-primary/20 text-ink/65 hover:text-ink transition-all"
                                  title="View Details"
                                >
                                  <Eye size={13} />
                                </button>
                                <button
                                  onClick={() => handleReprintRecentReceipt(sale)}
                                  className="p-1 rounded-md border border-primary/10 bg-surface hover:bg-primary/10 hover:border-primary/20 text-ink/65 hover:text-ink transition-all"
                                  title="Instant Reprint"
                                >
                                  <Printer size={13} />
                                </button>
                              </div>
                            </div>
                          </div>
                        ))}
                      </div>
                    )}
                  </div>
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
                    onChange={(e) => setAmountPaid(e.target.value)}
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
      )}

      {printReceipt && (
        <PrintableReceipt receipt={printReceipt} printOnly={true} />
      )}
    </div>
  );
}
