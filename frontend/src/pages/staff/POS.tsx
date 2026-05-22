import { useState, useRef, useEffect } from 'react';
import { CheckCircle2, X, Clock, Calendar, CreditCard, User, Store, Printer, Eye } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { processPosSale, lookupProductBySku, getReceiptHistory, type PosItem, type ProcessPosSaleResponse, type ReceiptDto } from '@/features/staff/api/staffPosApi';

export function POS() {
  const [items, setItems] = useState<PosItem[]>([]);
  const [skuInput, setSkuInput] = useState('');
  const [paymentMethod, setPaymentMethod] = useState<'CASH' | 'CARD'>('CASH');
  const [amountPaid, setAmountPaid] = useState<number | ''>('');
  const [isProcessing, setIsProcessing] = useState(false);
  const [receipt, setReceipt] = useState<ProcessPosSaleResponse | null>(null);
  const [skuError, setSkuError] = useState('');

  const [recentSales, setRecentSales] = useState<ReceiptDto[]>([]);
  const [recentSalesLoading, setRecentSalesLoading] = useState(false);
  const [printReceipt, setPrintReceipt] = useState<ReceiptDto | null>(null);

  const [view, setView] = useState<'pos' | 'history'>('pos');
  const [historyList, setHistoryList] = useState<ReceiptDto[]>([]);
  const [selectedHistoryReceipt, setSelectedHistoryReceipt] = useState<ReceiptDto | null>(null);
  const [historyLoading, setHistoryLoading] = useState(false);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyTotalPages, setHistoryTotalPages] = useState(0);

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

  const handleViewRecentReceipt = (receipt: ReceiptDto) => {
    setView('history');
    setSelectedHistoryReceipt(receipt);
    fetchHistory();
  };

  const handleReprintRecentReceipt = (receipt: ReceiptDto) => {
    setPrintReceipt(receipt);
    setTimeout(() => {
      window.print();
      setTimeout(() => setPrintReceipt(null), 500);
    }, 150);
  };

  const fetchHistory = async (page = 0) => {
    setHistoryLoading(true);
    try {
      const data = await getReceiptHistory(page, 10);
      setHistoryList(data.items || []);
      setHistoryPage(data.page);
      setHistoryTotalPages(data.totalPages);
      if (data.items && data.items.length > 0) {
        setSelectedHistoryReceipt(data.items[0]);
      } else {
        setSelectedHistoryReceipt(null);
      }
    } catch (e) {
      console.error(e);
    } finally {
      setHistoryLoading(false);
    }
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

  if (receipt) {
    return (
      <div className="flex min-h-[calc(100vh-7rem)] flex-col items-center justify-center p-4">
        {/* Printable Receipt Container (Hidden on screen, visible only during print) */}
        <div id="pos-receipt-print" className="hidden print:block text-black bg-white p-4 font-mono text-[11px] w-[80mm] mx-auto">
          <div className="text-center mb-4">
            <h2 className="text-base font-bold uppercase tracking-wider">SESHOP</h2>
            <p className="text-[10px]">{receipt.locationName}</p>
            <p className="text-[10px]">Date: {new Date(receipt.createdAt).toLocaleString()}</p>
            <p className="text-[10px]">Receipt: {receipt.receiptNumber}</p>
            <p className="text-[10px]">Cashier: {receipt.operatorName}</p>
          </div>
          
          <div className="border-t border-b border-dashed border-black py-2 my-2">
            <table className="w-full text-left text-[11px] border-collapse">
              <thead>
                <tr className="border-b border-dashed border-black">
                  <th className="pb-1">Item Details</th>
                  <th className="text-right pb-1">Price x Qty</th>
                  <th className="text-right pb-1">Total</th>
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
              <span>Total Amount:</span>
              <span className="font-bold">{receipt.totalAmount.toLocaleString()} VND</span>
            </div>
            <div className="flex justify-between">
              <span>Payment Method:</span>
              <span>{receipt.paymentMethod}</span>
            </div>
            <div className="flex justify-between">
              <span>Amount Paid:</span>
              <span>{receipt.amountPaid.toLocaleString()} VND</span>
            </div>
            <div className="flex justify-between border-t border-dashed border-black pt-1 mt-1 font-bold">
              <span>Change Due:</span>
              <span>{receipt.changeDue.toLocaleString()} VND</span>
            </div>
          </div>

          <div className="text-center mt-6 pt-4 border-t border-dashed border-black text-[9px]">
            <p className="font-bold">THANK YOU FOR YOUR PURCHASE!</p>
            <p>Please keep this receipt for refund or return.</p>
            <p className="mt-1">Powered by SeShop POS</p>
          </div>
        </div>

        {/* Beautiful Screen Preview (Visible on screen, hidden on print) */}
        <div className="w-full max-w-lg rounded-md border border-primary/20 bg-surface p-6 shadow-soft print:hidden">
          <style dangerouslySetInnerHTML={{__html: `
            @media print {
              body * {
                visibility: hidden !important;
              }
              #pos-receipt-print, #pos-receipt-print * {
                visibility: visible !important;
              }
              #pos-receipt-print {
                position: absolute !important;
                left: 0 !important;
                top: 0 !important;
                width: 80mm !important;
                margin: 0 auto !important;
                padding: 10px !important;
                display: block !important;
                background-color: white !important;
                color: black !important;
              }
            }
          `}} />
          <div className="text-center mb-6">
            <div className="mx-auto mb-3 flex size-12 items-center justify-center rounded-full bg-success/10 text-success">
              <CheckCircle2 size={28} />
            </div>
            <h2 className="text-xl font-bold text-ink">Sale Complete</h2>
            <p className="text-sm text-ink/55">Transaction processed successfully</p>
          </div>

          {/* Premium Digital Receipt Card */}
          <div className="mb-6 overflow-hidden rounded-md border border-primary/10 bg-ink/[0.02] p-5 text-ink/80 shadow-inner">
            <div className="flex justify-between border-b border-primary/10 pb-3 mb-4 text-sm font-semibold">
              <span className="text-primary font-bold">SESHOP RECEIPT</span>
              <span className="text-ink/60">{receipt.receiptNumber}</span>
            </div>

            <div className="grid grid-cols-2 gap-y-2 text-xs border-b border-primary/10 pb-4 mb-4">
              <div><span className="text-ink/40">Location:</span> <p className="font-medium text-ink">{receipt.locationName}</p></div>
              <div><span className="text-ink/40">Date:</span> <p className="font-medium text-ink">{new Date(receipt.createdAt).toLocaleString()}</p></div>
              <div><span className="text-ink/40">Cashier:</span> <p className="font-medium text-ink">{receipt.operatorName}</p></div>
              <div><span className="text-ink/40">Payment:</span> <p className="font-medium text-ink">{receipt.paymentMethod}</p></div>
            </div>

            {/* List of Purchased items */}
            <div className="space-y-3 max-h-48 overflow-y-auto mb-4 pr-1">
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
                <span>Total Amount:</span>
                <span className="font-semibold text-ink">{receipt.totalAmount.toLocaleString()} VND</span>
              </div>
              <div className="flex justify-between text-ink/60">
                <span>Amount Tendered:</span>
                <span className="text-ink">{receipt.amountPaid.toLocaleString()} VND</span>
              </div>
              <div className="flex justify-between border-t border-primary/10 pt-2 text-base font-bold text-ink">
                <span>Change Due:</span>
                <span className="text-success">{receipt.changeDue.toLocaleString()} VND</span>
              </div>
            </div>
          </div>

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
                fetchHistory();
              }}
              className={`px-3 py-1 text-xs font-semibold rounded-full border transition-all ${view === 'history' ? 'bg-primary text-ink border-primary' : 'bg-surface hover:bg-primary/10 border-primary/20 text-ink/75'}`}
            >
              Receipt History
            </button>
          </div>
        </div>
        <div className="text-sm text-ink/55">Register: REG-01 | Operator: Staff User</div>
      </div>

      {view === 'history' ? (
        <div className="flex flex-1 overflow-hidden bg-surfaceMuted/20 print:block">
          {/* History List Side */}
          <div className="flex flex-1 flex-col border-r border-primary/15 bg-surface print:hidden">
            <div className="border-b border-primary/15 p-4 flex items-center justify-between">
              <h2 className="text-lg font-bold flex items-center gap-2">
                <Clock size={20} className="text-primary" /> Past Receipts
              </h2>
              <div className="flex items-center gap-2">
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={historyPage === 0 || historyLoading}
                  onClick={() => fetchHistory(historyPage - 1)}
                >
                  Prev
                </Button>
                <span className="text-xs text-ink/60 font-semibold">
                  Page {historyPage + 1} of {Math.max(1, historyTotalPages)}
                </span>
                <Button
                  size="sm"
                  variant="secondary"
                  disabled={historyPage >= historyTotalPages - 1 || historyLoading}
                  onClick={() => fetchHistory(historyPage + 1)}
                >
                  Next
                </Button>
              </div>
            </div>

            <div className="flex-1 overflow-y-auto p-4 space-y-3">
              {historyLoading ? (
                <div className="flex h-full items-center justify-center text-ink/40">
                  <span className="animate-pulse">Loading history...</span>
                </div>
              ) : historyList.length === 0 ? (
                <div className="flex h-full items-center justify-center text-ink/40">
                  No receipt history found for this store.
                </div>
              ) : (
                historyList.map((hist) => (
                  <div
                    key={hist.id}
                    onClick={() => setSelectedHistoryReceipt(hist)}
                    className={`p-4 rounded-md border cursor-pointer transition-all duration-200 ${
                      selectedHistoryReceipt?.id === hist.id
                        ? 'border-primary bg-primary/5 shadow-sm'
                        : 'border-primary/10 bg-surface hover:border-primary/30 hover:bg-ink/[0.01]'
                    }`}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <span className="font-bold text-ink">{hist.receiptNumber}</span>
                      <span className="text-[10px] bg-primary/10 text-primary px-2 py-0.5 rounded-full font-semibold">
                        {hist.paymentMethod}
                      </span>
                    </div>
                    <div className="flex justify-between text-xs text-ink/65">
                      <span>{new Date(hist.issuedAt).toLocaleString()}</span>
                      <span className="font-bold text-ink">
                        {hist.totalAmount?.toLocaleString() || '0'} VND
                      </span>
                    </div>
                  </div>
                ))
              )}
            </div>
          </div>

          {/* Receipt Details and Action Side */}
          <div className="flex w-[480px] flex-col bg-surfaceMuted/30 overflow-y-auto print:w-full print:bg-white print:p-0">
            {selectedHistoryReceipt ? (
              <div className="p-6 space-y-6 print:p-0">
                {/* Hidden print container for selected history receipt */}
                <div id="pos-receipt-print" className="hidden print:block text-black bg-white p-4 font-mono text-[11px] w-[80mm] mx-auto">
                  <div className="text-center mb-4">
                    <h2 className="text-base font-bold uppercase tracking-wider">SESHOP</h2>
                    <p className="text-[10px]">{selectedHistoryReceipt.locationName}</p>
                    <p className="text-[10px]">Date: {new Date(selectedHistoryReceipt.issuedAt).toLocaleString()}</p>
                    <p className="text-[10px]">Receipt: {selectedHistoryReceipt.receiptNumber}</p>
                    <p className="text-[10px]">Cashier: {selectedHistoryReceipt.operatorName}</p>
                  </div>
                  
                  <div className="border-t border-b border-dashed border-black py-2 my-2">
                    <table className="w-full text-left text-[11px] border-collapse">
                      <thead>
                        <tr className="border-b border-dashed border-black">
                          <th className="pb-1">Item Details</th>
                          <th className="text-right pb-1">Price x Qty</th>
                          <th className="text-right pb-1">Total</th>
                        </tr>
                      </thead>
                      <tbody>
                        {selectedHistoryReceipt.items?.map((item) => (
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
                      <span>Total Amount:</span>
                      <span className="font-bold">{selectedHistoryReceipt.totalAmount.toLocaleString()} VND</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Payment Method:</span>
                      <span>{selectedHistoryReceipt.paymentMethod}</span>
                    </div>
                    <div className="flex justify-between">
                      <span>Amount Paid:</span>
                      <span>{selectedHistoryReceipt.amountPaid.toLocaleString()} VND</span>
                    </div>
                    <div className="flex justify-between border-t border-dashed border-black pt-1 mt-1 font-bold">
                      <span>Change Due:</span>
                      <span>0 VND</span>
                    </div>
                  </div>

                  <div className="text-center mt-6 pt-4 border-t border-dashed border-black text-[9px]">
                    <p className="font-bold">THANK YOU FOR YOUR PURCHASE!</p>
                    <p>Please keep this receipt for refund or return.</p>
                    <p className="mt-1">Powered by SeShop POS</p>
                  </div>
                </div>

                <div className="rounded-md border border-primary/20 bg-surface p-6 shadow-soft print:hidden">
                  <style dangerouslySetInnerHTML={{__html: `
                    @media print {
                      body * {
                        visibility: hidden !important;
                      }
                      #pos-receipt-print, #pos-receipt-print * {
                        visibility: visible !important;
                      }
                      #pos-receipt-print {
                        position: absolute !important;
                        left: 0 !important;
                        top: 0 !important;
                        width: 80mm !important;
                        margin: 0 auto !important;
                        padding: 10px !important;
                        display: block !important;
                        background-color: white !important;
                        color: black !important;
                      }
                    }
                  `}} />
                  
                  {/* Premium History Digital Receipt Card */}
                  <div className="overflow-hidden rounded-md border border-primary/10 bg-ink/[0.02] p-5 text-ink/80 shadow-inner mb-6">
                    <div className="flex justify-between border-b border-primary/10 pb-3 mb-4 text-sm font-semibold">
                      <span className="text-primary font-bold">SESHOP HISTORIC RECEIPT</span>
                      <span className="text-ink/60">{selectedHistoryReceipt.receiptNumber}</span>
                    </div>

                    <div className="grid grid-cols-2 gap-y-2 text-xs border-b border-primary/10 pb-4 mb-4">
                      <div><span className="text-ink/40">Location:</span> <p className="font-medium text-ink">{selectedHistoryReceipt.locationName}</p></div>
                      <div><span className="text-ink/40">Date:</span> <p className="font-medium text-ink">{new Date(selectedHistoryReceipt.issuedAt).toLocaleString()}</p></div>
                      <div><span className="text-ink/40">Cashier:</span> <p className="font-medium text-ink">{selectedHistoryReceipt.operatorName}</p></div>
                      <div><span className="text-ink/40">Payment:</span> <p className="font-medium text-ink">{selectedHistoryReceipt.paymentMethod}</p></div>
                    </div>

                    {/* List of Purchased items */}
                    <div className="space-y-3 max-h-60 overflow-y-auto mb-4 pr-1">
                      {selectedHistoryReceipt.items?.map((item) => (
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
                      <div className="flex justify-between border-b border-primary/5 pb-2 text-base font-bold text-ink">
                        <span>Total Paid:</span>
                        <span className="text-success">{selectedHistoryReceipt.totalAmount.toLocaleString()} VND</span>
                      </div>
                    </div>
                  </div>

                  <Button className="w-full h-12 text-sm" onClick={() => window.print()}>
                    Reprint Receipt
                  </Button>
                </div>
              </div>
            ) : (
              <div className="flex h-full items-center justify-center text-ink/40 p-6 text-center">
                Select a receipt from the history list to view details and print.
              </div>
            )}
          </div>
        </div>
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
      )}

      {printReceipt && (
        <div id="pos-receipt-print" className="hidden print:block text-black bg-white p-4 font-mono text-[11px] w-[80mm] mx-auto">
          <div className="text-center mb-4">
            <h2 className="text-base font-bold uppercase tracking-wider">SESHOP</h2>
            <p className="text-[10px]">{printReceipt.locationName}</p>
            <p className="text-[10px]">Date: {new Date(printReceipt.issuedAt).toLocaleString()}</p>
            <p className="text-[10px]">Receipt: {printReceipt.receiptNumber}</p>
            <p className="text-[10px]">Cashier: {printReceipt.operatorName}</p>
          </div>
          
          <div className="border-t border-b border-dashed border-black py-2 my-2">
            <table className="w-full text-left text-[11px] border-collapse">
              <thead>
                <tr className="border-b border-dashed border-black">
                  <th className="pb-1">Item Details</th>
                  <th className="text-right pb-1">Price x Qty</th>
                  <th className="text-right pb-1">Total</th>
                </tr>
              </thead>
              <tbody>
                {printReceipt.items?.map((item) => (
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
              <span>Total Amount:</span>
              <span className="font-bold">{printReceipt.totalAmount.toLocaleString()} VND</span>
            </div>
            <div className="flex justify-between">
              <span>Payment Method:</span>
              <span>{printReceipt.paymentMethod}</span>
            </div>
            <div className="flex justify-between">
              <span>Amount Paid:</span>
              <span>{printReceipt.amountPaid.toLocaleString()} VND</span>
            </div>
            <div className="flex justify-between border-t border-dashed border-black pt-1 mt-1 font-bold">
              <span>Change Due:</span>
              <span>0 VND</span>
            </div>
          </div>

          <div className="text-center mt-6 pt-4 border-t border-dashed border-black text-[9px]">
            <p className="font-bold">THANK YOU FOR YOUR PURCHASE!</p>
            <p>Please keep this receipt for refund or return.</p>
            <p className="mt-1">Powered by SeShop POS</p>
          </div>
        </div>
      )}
    </div>
  );
}
