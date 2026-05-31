import { useState, useEffect } from 'react';
import { CheckCircle2, RotateCcw, WalletCards } from 'lucide-react';
import { approveReturn, createRefund, createReturn, getReturnsByOrderId, type RefundResponse, type ReturnResponse } from '@/features/commerce/api/returnsApi';
import { processPosReturn, getReceiptHistory, getReceiptDetails, type PosReturnDisposition, type ReceiptDto } from '@/features/staff/api/staffPosApi';
import { getStaffOrders, getStaffOrderDetails, getOrderPayments, type StaffOrder, type StaffOrderDetails, type PaymentDto } from '@/features/staff/api/staffOrdersApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { SearchSelect } from '@/shared/ui/SearchSelect';

export function ReturnsManagement() {
  const [orders, setOrders] = useState<StaffOrder[]>([]);
  const [isLoadingOrders, setIsLoadingOrders] = useState(false);

  const [orderId, setOrderId] = useState<string>('');
  const [orderDetails, setOrderDetails] = useState<StaffOrderDetails | null>(null);
  const [orderItemId, setOrderItemId] = useState<string>('');
  const [qty, setQty] = useState<string>('1');
  const [reason, setReason] = useState('');
  const [returnResult, setReturnResult] = useState<ReturnResponse | null>(null);

  const [returns, setReturns] = useState<ReturnResponse[]>([]);
  const [payments, setPayments] = useState<PaymentDto[]>([]);

  const [approveId, setApproveId] = useState<string>('');
  const [refund, setRefund] = useState({ paymentId: '', returnRequestId: '', amount: '' });
  const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);

  const [receipts, setReceipts] = useState<ReceiptDto[]>([]);
  const [isLoadingReceipts, setIsLoadingReceipts] = useState(false);
  const [receiptDetails, setReceiptDetails] = useState<ReceiptDto | null>(null);

  const [posReturn, setPosReturn] = useState({
    originalReceiptId: '',
    variantId: '',
    qty: '1',
    disposition: 'RESTOCK' as PosReturnDisposition,
    refundAmount: '',
    reason: '',
  });
  const [posResult, setPosResult] = useState<{ id: number; refundAmount: number; processedAt: string } | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    async function loadInitialData() {
      setIsLoadingOrders(true);
      setIsLoadingReceipts(true);
      try {
        const [ordersRes, receiptsRes] = await Promise.all([
          getStaffOrders(1, 100),
          getReceiptHistory(0, 100)
        ]);
        setOrders(ordersRes?.items || []);
        setReceipts(receiptsRes?.items || []);
      } catch (err) {
        console.error('Failed to load initial data', err);
      } finally {
        setIsLoadingOrders(false);
        setIsLoadingReceipts(false);
      }
    }
    loadInitialData();
  }, []);

  const refreshOrderData = async (id: number) => {
    try {
      const [details, rets, pays] = await Promise.all([
        getStaffOrderDetails(id),
        getReturnsByOrderId(id),
        getOrderPayments(id)
      ]);
      setOrderDetails(details);
      setReturns(rets || []);
      setPayments(pays || []);
    } catch (err) {
      console.error('Failed to load order related data', err);
    }
  };

  useEffect(() => {
    if (!orderId) {
      setOrderDetails(null);
      setReturns([]);
      setPayments([]);
      setOrderItemId('');
      setApproveId('');
      setRefund({ paymentId: '', returnRequestId: '', amount: '' });
      return;
    }
    
    refreshOrderData(Number(orderId)).then(() => {
      // Auto select first item if available and not selected
      setOrderItemId(prev => prev || '');
    });
  }, [orderId]);

  // Set default orderItemId when details load
  useEffect(() => {
    if (orderDetails && (orderDetails.items || []).length > 0 && !orderItemId) {
      setOrderItemId(String(orderDetails.items[0].id));
    }
  }, [orderDetails, orderItemId]);

  useEffect(() => {
    if (!posReturn.originalReceiptId) {
      setReceiptDetails(null);
      return;
    }
    async function loadReceipt() {
      try {
        const details = await getReceiptDetails(Number(posReturn.originalReceiptId));
        setReceiptDetails(details);
        if ((details.items || []).length > 0) {
          const firstItem = details.items[0];
          setPosReturn(p => ({ ...p, variantId: String(firstItem.variantId), refundAmount: String(firstItem.unitPrice) }));
        }
      } catch (err) {
        console.error('Failed to load receipt details', err);
      }
    }
    loadReceipt();
  }, [posReturn.originalReceiptId]);

  async function handleCreateReturn(event: React.FormEvent) {
    event.preventDefault();
    if (!orderId || !orderItemId) return;
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createReturn({
        orderId: Number(orderId),
        reason,
        items: [{ orderItemId: Number(orderItemId), qty: Number(qty) }],
      });
      setReturnResult(created);
      await refreshOrderData(Number(orderId));
      setApproveId(String(created.returnId));
      setRefund((current) => ({ ...current, returnRequestId: String(created.returnId) }));
      setMessage(`Successfully created return request #${created.returnId}`);
      setReason('');
    } catch (err: any) {
      const errorMsg = err?.response?.data?.message || 'Return request could not be created.';
      setMessage(errorMsg);
    } finally {
      setIsSaving(false);
    }
  }

  async function handleApprove(event: React.FormEvent) {
    event.preventDefault();
    if (!approveId) return;
    setIsSaving(true);
    setMessage('');
    try {
      await approveReturn(Number(approveId));
      await refreshOrderData(Number(orderId));
      setApproveId('');
      setMessage(`Successfully approved return.`);
    } catch (err: any) {
      setMessage(err?.response?.data?.message || 'Approve Return failed.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleRefund(event: React.FormEvent) {
    event.preventDefault();
    if (!orderId || !refund.paymentId || !refund.returnRequestId) return;
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createRefund({
        orderId: Number(orderId),
        paymentId: Number(refund.paymentId),
        returnRequestId: Number(refund.returnRequestId),
        amount: Number(refund.amount),
      });
      setRefundResult(created);
      setMessage(`Successfully processed refund.`);
    } catch (err: any) {
      setMessage(err?.response?.data?.message || 'Refund could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handlePosReturn(event: React.FormEvent) {
    event.preventDefault();
    if (!posReturn.originalReceiptId || !posReturn.variantId) return;
    setIsSaving(true);
    setMessage('');
    try {
      const created = await processPosReturn({
        originalOrderId: receiptDetails?.id || 0,
        refundAmount: Number(posReturn.refundAmount),
        reason: posReturn.reason,
        items: [
          {
            variantId: Number(posReturn.variantId),
            qty: Number(posReturn.qty),
            disposition: posReturn.disposition,
          },
        ],
      });
      setPosResult(created);
      setMessage(`POS return ${created.id} processed.`);
    } catch {
      setMessage('POS return could not be processed.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Refunds & Returns"
      viewCode="STAFF_005"
      purpose="Process online return requests, issue refunds, and handle in-store return intake."
    >
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <Card className="border-primary/20 bg-surface/95 p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Online Return Intake</h2>
              <p className="mt-1 text-xs text-ink/50">Create a return request against an order item.</p>
            </div>
            <RotateCcw className="text-primary" size={22} />
          </div>
          <form onSubmit={handleCreateReturn} className="mt-5 grid gap-4">
            <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
              <SearchSelect
                label="Select Order"
                value={orderId}
                onChange={setOrderId}
                options={orders.map(o => ({ value: String(o.id), label: `Order ${o.orderNumber}` }))}
                placeholder="Search order"
                isLoading={isLoadingOrders}
                required
              />
              <Select
                label="Select Order Item"
                value={orderItemId}
                onChange={(e) => setOrderItemId(e.target.value)}
                options={(orderDetails?.items || []).map(i => ({ value: String(i.id), label: `${i.productName || 'Unknown Product'} (x${i.qty})` }))}
                disabled={!orderDetails || (orderDetails.items || []).length === 0}
                required
              />
              <Input label="Quantity" type="number" min={1} value={qty} onChange={(event) => setQty(event.target.value)} required />
            </div>
            <label className="grid gap-1 text-sm font-medium text-ink">
              <span>Reason</span>
              <textarea
                value={reason}
                onChange={(event) => setReason(event.target.value)}
                className="min-h-24 rounded-md border border-primary/30 bg-surface px-3 py-2 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
                required
              />
            </label>
            <div className="flex items-center gap-2 mt-2 border-t border-primary/10 pt-4">
              <input 
                type="checkbox" 
                id="isExchange" 
                className="rounded border-primary/30 text-primary focus:ring-primary"
                onChange={(e) => {
                  if (e.target.checked) {
                    setReason(prev => prev ? `[EXCHANGE] ${prev}` : '[EXCHANGE] ');
                  } else {
                    setReason(prev => prev.replace(/\[EXCHANGE\]\s*/g, ''));
                  }
                }}
                checked={reason.includes('[EXCHANGE]')}
              />
              <label htmlFor="isExchange" className="text-sm font-medium text-ink">
                Process as Exchange
              </label>
            </div>
            {reason.includes('[EXCHANGE]') && (
              <Input 
                label="Replacement Item SKU (Optional)" 
                placeholder="e.g. SKU-12345" 
                onChange={(e) => {
                  const val = e.target.value;
                  const prefixMatch = reason.match(/\[EXCHANGE.*?\]/);
                  const prefix = val ? `[EXCHANGE for ${val}]` : '[EXCHANGE]';
                  if (prefixMatch) {
                    setReason(reason.replace(prefixMatch[0], prefix));
                  } else {
                    setReason(`${prefix} ${reason}`);
                  }
                }}
              />
            )}
            {message && <p className={`mt-3 text-sm ${message.includes('could not') ? 'text-danger' : 'text-success'}`}>{message}</p>}
            <Button type="submit" isLoading={isSaving} className="mt-2" disabled={!orderId || !orderItemId}>
              Create Return
            </Button>
          </form>

          {returnResult ? (
            <div className="mt-5 rounded-md border border-primary/15 bg-ink/[0.03] p-4 text-sm text-ink/70">
              <div className="flex items-center justify-between">
                <span>Return {returnResult.returnId}</span>
                <Badge variant={returnResult.status === 'APPROVED' ? 'success' : 'warning'}>{returnResult.status}</Badge>
              </div>
              <p className="mt-2 text-xs text-ink/50">{returnResult.reason}</p>
            </div>
          ) : null}
        </Card>

        <div className="grid gap-5">
          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Approve Return</h2>
            <form onSubmit={handleApprove} className="mt-4 grid gap-4">
              <Select
                label="Select Pending Return"
                value={approveId}
                onChange={(e) => setApproveId(e.target.value)}
                options={returns.filter(r => r.status === 'PENDING').map(r => ({ value: String(r.returnId), label: `Return #${r.returnId} - ${r.reason}` }))}
                disabled={!orderId || returns.filter(r => r.status === 'PENDING').length === 0}
                required
              />
              {message && message.includes('Approve') && <p className="mt-2 text-sm text-danger">{message}</p>}
              <Button type="submit" variant="secondary" icon={<CheckCircle2 size={16} />} isLoading={isSaving} disabled={!approveId}>
                Approve
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Issue Refund</h2>
            <form onSubmit={handleRefund} className="mt-4 grid gap-4">
              <Input label="Order ID" value={orderId ? `Order ${orderDetails?.orderNumber || orderId}` : ''} disabled />
              
              <Select
                label="Select Payment"
                value={refund.paymentId}
                onChange={(e) => setRefund((current) => ({ ...current, paymentId: e.target.value }))}
                options={payments.filter(p => p.status === 'COMPLETED' || p.status === 'PAID').map(p => ({ value: String(p.id), label: `${p.provider} - ${formatCurrency(p.amount)}` }))}
                disabled={!orderId || payments.length === 0}
                required
              />

              <Select
                label="Select Approved Return"
                value={refund.returnRequestId}
                onChange={(e) => setRefund((current) => ({ ...current, returnRequestId: e.target.value }))}
                options={returns.filter(r => r.status === 'APPROVED').map(r => ({ value: String(r.returnId), label: `Return #${r.returnId}` }))}
                disabled={!orderId || returns.filter(r => r.status === 'APPROVED').length === 0}
                required
              />

              <Input label="Amount (VND)" type="number" min={1} value={refund.amount} onChange={(event) => setRefund((current) => ({ ...current, amount: event.target.value }))} disabled={!orderId} required />
              {message && message.includes('Refund') && <p className="mt-2 text-sm text-danger">{message}</p>}
              <Button type="submit" variant="secondary" icon={<WalletCards size={16} />} isLoading={isSaving} disabled={!orderId || !refund.paymentId || !refund.returnRequestId}>
                Create Refund
              </Button>
            </form>
            {refundResult ? (
              <p className="mt-4 text-sm text-ink/65">Refunded {formatCurrency(Number(refundResult.amount))} with status {refundResult.status}.</p>
            ) : null}
          </Card>
        </div>

        <Card className="border-primary/20 bg-surface/95 p-5 xl:col-span-2">
          <div className="grid gap-5 lg:grid-cols-[1fr_300px]">
            <form onSubmit={handlePosReturn} className="grid gap-4">
              <div>
                <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">POS Return</h2>
                <p className="mt-1 text-xs text-ink/50">Use this for counter returns that refund immediately.</p>
              </div>
              <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                <SearchSelect
                  label="Select Receipt"
                  value={posReturn.originalReceiptId}
                  onChange={(val: string) => setPosReturn((current) => ({ ...current, originalReceiptId: val }))}
                  options={receipts.map(r => ({ value: String(r.id), label: `${r.receiptNumber} - ${formatCurrency(r.amountPaid)}` }))}
                  placeholder="Search receipt"
                  isLoading={isLoadingReceipts}
                  required
                />
                
                <Select
                  label="Select Variant"
                  value={posReturn.variantId}
                  onChange={(e) => {
                    const newVariantId = e.target.value;
                    const item = receiptDetails?.items.find(i => String(i.variantId) === newVariantId);
                    setPosReturn((current) => ({ 
                      ...current, 
                      variantId: newVariantId,
                      refundAmount: item ? String(item.unitPrice * Number(current.qty)) : current.refundAmount
                    }));
                  }}
                  options={(receiptDetails?.items || []).map(i => ({ value: String(i.variantId), label: `${i.name || 'Unknown'} (${i.skuCode || 'No SKU'})` }))}
                  disabled={!receiptDetails || (receiptDetails.items || []).length === 0}
                  required
                />
                
                <div className="grid grid-cols-2 gap-4">
                  <Input 
                    label="Quantity" 
                    type="number" 
                    min={1} 
                    value={posReturn.qty} 
                    onChange={(e) => {
                      const newQty = e.target.value;
                      const item = receiptDetails?.items.find(i => String(i.variantId) === posReturn.variantId);
                      setPosReturn((current) => ({ 
                        ...current, 
                        qty: newQty,
                        refundAmount: item ? String(item.unitPrice * Number(newQty)) : current.refundAmount
                      }));
                    }} 
                    disabled={!posReturn.originalReceiptId}
                    required 
                  />
                  <Select
                    label="Disposition"
                    value={posReturn.disposition}
                    onChange={(event) => setPosReturn((current) => ({ ...current, disposition: event.target.value as any }))}
                    options={[
                      { label: 'Restock', value: 'RESTOCK' },
                      { label: 'Refurbish', value: 'REFURBISH' },
                      { label: 'Dispose', value: 'DISPOSE' },
                    ]}
                    disabled={!posReturn.originalReceiptId}
                    required
                  />
                </div>
                <Input label="Refund Amount (VND)" type="number" min={0.01} step="0.01" value={posReturn.refundAmount} onChange={(event) => setPosReturn((current) => ({ ...current, refundAmount: event.target.value }))} disabled={!posReturn.originalReceiptId} required />
                <Input label="Reason" value={posReturn.reason} onChange={(event) => setPosReturn((current) => ({ ...current, reason: event.target.value }))} disabled={!posReturn.originalReceiptId} required />
              </div>
              <Button type="submit" isLoading={isSaving} disabled={!posReturn.originalReceiptId || !posReturn.variantId}>
                Process POS Return
              </Button>
            </form>
            <div className="rounded-md border border-primary/15 bg-ink/[0.03] p-4">
              <h3 className="text-sm font-semibold text-ink">Latest Activity</h3>
              {posResult ? (
                <p className="mt-3 text-sm text-ink/65">POS return {posResult.id} for {formatCurrency(Number(posResult.refundAmount))}</p>
              ) : (
                <p className="mt-3 text-sm text-ink/55">No POS return processed yet.</p>
              )}
              {message ? <p className="mt-3 text-sm text-ink/65">{message}</p> : null}
            </div>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
