import { useState } from 'react';
import { CheckCircle2, RotateCcw, WalletCards } from 'lucide-react';
import { approveReturn, createRefund, createReturn, type RefundResponse, type ReturnResponse } from '@/features/commerce/api/returnsApi';
import { processPosReturn } from '@/features/staff/api/staffPosApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function ReturnsManagement() {
  const [orderId, setOrderId] = useState(0);
  const [orderItemId, setOrderItemId] = useState(0);
  const [qty, setQty] = useState(1);
  const [reason, setReason] = useState('');
  const [returnResult, setReturnResult] = useState<ReturnResponse | null>(null);
  const [approveId, setApproveId] = useState(0);
  const [refund, setRefund] = useState({ orderId: 0, paymentId: 0, returnRequestId: 0, amount: 0 });
  const [refundResult, setRefundResult] = useState<RefundResponse | null>(null);
  const [posReturn, setPosReturn] = useState({ originalOrderId: 0, refundAmount: 0, reason: '' });
  const [posResult, setPosResult] = useState<{ id: number; refundAmount: number; processedAt: string } | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  async function handleCreateReturn(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createReturn({
        orderId,
        reason,
        items: [{ orderItemId, qty }],
      });
      setReturnResult(created);
      setApproveId(created.returnId);
      setRefund((current) => ({ ...current, orderId: created.orderId, returnRequestId: created.returnId }));
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
      const approved = await approveReturn(approveId);
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
      const created = await createRefund(refund);
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
      const created = await processPosReturn(posReturn.originalOrderId, posReturn.refundAmount, posReturn.reason);
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
              <Input label="Order ID" type="number" min={1} value={orderId || ''} onChange={(event) => setOrderId(Number(event.target.value))} required />
              <Input label="Order Item ID" type="number" min={1} value={orderItemId || ''} onChange={(event) => setOrderItemId(Number(event.target.value))} required />
              <Input label="Quantity" type="number" min={1} value={qty} onChange={(event) => setQty(Number(event.target.value))} required />
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
            <Button type="submit" isLoading={isSaving}>
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
              <Input label="Return ID" type="number" min={1} value={approveId || ''} onChange={(event) => setApproveId(Number(event.target.value))} required />
              <Button type="submit" variant="secondary" icon={<CheckCircle2 size={16} />} isLoading={isSaving}>
                Approve
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Issue Refund</h2>
            <form onSubmit={handleRefund} className="mt-4 grid gap-4">
              <Input label="Order ID" type="number" min={1} value={refund.orderId || ''} onChange={(event) => setRefund((current) => ({ ...current, orderId: Number(event.target.value) }))} required />
              <Input label="Payment ID" type="number" min={1} value={refund.paymentId || ''} onChange={(event) => setRefund((current) => ({ ...current, paymentId: Number(event.target.value) }))} required />
              <Input label="Return ID" type="number" min={1} value={refund.returnRequestId || ''} onChange={(event) => setRefund((current) => ({ ...current, returnRequestId: Number(event.target.value) }))} required />
              <Input label="Amount" type="number" min={1} value={refund.amount || ''} onChange={(event) => setRefund((current) => ({ ...current, amount: Number(event.target.value) }))} required />
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
                <Input label="Original Order ID" type="number" min={1} value={posReturn.originalOrderId || ''} onChange={(event) => setPosReturn((current) => ({ ...current, originalOrderId: Number(event.target.value) }))} required />
                <Input label="Refund Amount" type="number" min={0} value={posReturn.refundAmount || ''} onChange={(event) => setPosReturn((current) => ({ ...current, refundAmount: Number(event.target.value) }))} required />
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
