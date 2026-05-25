import { useState } from 'react';
import { CheckCircle2, RotateCcw, WalletCards } from 'lucide-react';
import { approveReturn, createRefund, createReturn, type RefundResponse, type ReturnResponse } from '@/features/commerce/api/returnsApi';
import { processPosReturn, type PosReturnDisposition } from '@/features/staff/api/staffPosApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';

export function ReturnsManagement() {
  const [orderId, setOrderId] = useState<string>('');
  const [orderItemId, setOrderItemId] = useState<string>('');
  const [qty, setQty] = useState<string>('1');
  const [reason, setReason] = useState('');
  const [returnResult, setReturnResult] = useState<ReturnResponse | null>(null);
  const [approveId, setApproveId] = useState<string>('');
  const [refund, setRefund] = useState({ orderId: '', paymentId: '', returnRequestId: '', amount: '' });
  const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);
  const [posReturn, setPosReturn] = useState({
    originalOrderId: '',
    variantId: '',
    qty: '1',
    disposition: 'RESTOCK' as PosReturnDisposition,
    refundAmount: '',
    reason: '',
  });
  const [posResult, setPosResult] = useState<{ id: number; refundAmount: number; processedAt: string } | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  async function handleCreateReturn(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createReturn({
        orderId: Number(orderId),
        reason,
        items: [{ orderItemId: Number(orderItemId), qty: Number(qty) }],
      });
      setReturnResult(created);
      setApproveId(String(created.returnId));
      setRefund((current) => ({ ...current, orderId: String(created.orderId), returnRequestId: String(created.returnId) }));
      setMessage(`Return ${created.returnId} created.`);
    } catch {
      setMessage('Return request could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleApprove(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const approved = await approveReturn(Number(approveId));
      setReturnResult(approved);
      setMessage(`Return ${approved.returnId} approved.`);
    } catch {
      setMessage('Return could not be approved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleRefund(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createRefund({
        orderId: Number(refund.orderId),
        paymentId: Number(refund.paymentId),
        returnRequestId: Number(refund.returnRequestId),
        amount: Number(refund.amount)
      });
      setRefundResult(created);
      setMessage(`Refund ${created.refundId} created.`);
    } catch {
      setMessage('Refund could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handlePosReturn(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await processPosReturn({
        originalOrderId: Number(posReturn.originalOrderId),
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
            <div className="grid gap-4 md:grid-cols-3">
              <Input label="Order ID (Numeric)" type="number" min={1} value={orderId} onChange={(event) => setOrderId(event.target.value)} required />
              <Input label="Order Item ID (Numeric)" type="number" min={1} value={orderItemId} onChange={(event) => setOrderItemId(event.target.value)} required />
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
            <Button type="submit" isLoading={isSaving} className="mt-2">
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
              <Input label="Return ID (Numeric)" type="number" min={1} value={approveId} onChange={(event) => setApproveId(event.target.value)} required />
              <Button type="submit" variant="secondary" icon={<CheckCircle2 size={16} />} isLoading={isSaving}>
                Approve
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Issue Refund</h2>
            <form onSubmit={handleRefund} className="mt-4 grid gap-4">
              <Input label="Order ID (Numeric)" type="number" min={1} value={refund.orderId} onChange={(event) => setRefund((current) => ({ ...current, orderId: event.target.value }))} required />
              <Input label="Payment ID (Numeric)" type="number" min={1} value={refund.paymentId} onChange={(event) => setRefund((current) => ({ ...current, paymentId: event.target.value }))} required />
              <Input label="Return ID (Numeric)" type="number" min={1} value={refund.returnRequestId} onChange={(event) => setRefund((current) => ({ ...current, returnRequestId: event.target.value }))} required />
              <Input label="Amount (VND)" type="number" min={1} value={refund.amount} onChange={(event) => setRefund((current) => ({ ...current, amount: event.target.value }))} required />
              <Button type="submit" variant="secondary" icon={<WalletCards size={16} />} isLoading={isSaving}>
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
              <div className="grid gap-4 md:grid-cols-3">
                <Input label="Original Receipt ID (Numeric)" type="number" min={1} value={posReturn.originalOrderId} onChange={(event) => setPosReturn((current) => ({ ...current, originalOrderId: event.target.value }))} required />
                <Input label="Variant ID (Numeric)" type="number" min={1} value={posReturn.variantId} onChange={(event) => setPosReturn((current) => ({ ...current, variantId: event.target.value }))} required />
                <Input label="Quantity" type="number" min={1} value={posReturn.qty} onChange={(event) => setPosReturn((current) => ({ ...current, qty: event.target.value }))} required />
                <Select
                  label="Disposition"
                  value={posReturn.disposition}
                  onChange={(event) => setPosReturn((current) => ({ ...current, disposition: event.target.value as PosReturnDisposition }))}
                  options={[
                    { label: 'Restock', value: 'RESTOCK' },
                    { label: 'Refurbish', value: 'REFURBISH' },
                    { label: 'Dispose', value: 'DISPOSE' },
                  ]}
                />
                <Input label="Refund Amount (VND)" type="number" min={0.01} step="0.01" value={posReturn.refundAmount} onChange={(event) => setPosReturn((current) => ({ ...current, refundAmount: event.target.value }))} required />
                <Input label="Reason" value={posReturn.reason} onChange={(event) => setPosReturn((current) => ({ ...current, reason: event.target.value }))} required />
              </div>
              <Button type="submit" isLoading={isSaving}>
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
