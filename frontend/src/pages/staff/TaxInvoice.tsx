import { useState } from 'react';
import { FileText, FilePlus2, ReceiptText, CheckCircle2 } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { createTaxInvoice, createInvoiceAdjustment, type TaxInvoiceResponse, type InvoiceAdjustmentResponse } from '@/features/staff/api/staffInvoiceApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function TaxInvoice() {
  const { t } = useTranslation();
  const [orderId, setOrderId] = useState<string>('');
  const [invoice, setInvoice] = useState<TaxInvoiceResponse | null>(null);
  const [invoiceIdInput, setInvoiceIdInput] = useState<string>('');
  const [adjustReason, setAdjustReason] = useState('');
  const [adjustAmount, setAdjustAmount] = useState<string>('');
  const [adjustment, setAdjustment] = useState<InvoiceAdjustmentResponse | null>(null);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  async function handleCreateInvoice(e: React.FormEvent) {
    e.preventDefault(); if (!orderId) return;
    setIsSaving(true); setMessage('');
    try {
      const res = await createTaxInvoice(Number(orderId));
      setInvoice(res);
      setInvoiceIdInput(String(res.id));
      setMessage(`Invoice ${res.invoiceNumber} issued successfully.`);
    } catch { setMessage('Invoice could not be generated.'); }
    finally { setIsSaving(false); }
  }

  async function handleCreateAdjustment(e: React.FormEvent) {
    e.preventDefault(); if (!invoiceIdInput || !adjustReason) return;
    setIsSaving(true); setMessage('');
    try {
      const res = await createInvoiceAdjustment(Number(invoiceIdInput), adjustReason, Number(adjustAmount));
      setAdjustment(res);
      setMessage(`Adjustment note #${res.id} created.`);
    } catch { setMessage('Adjustment note could not be created.'); }
    finally { setIsSaving(false); }
  }

  function handleReset() {
    setOrderId(''); setInvoice(null); setInvoiceIdInput(''); setAdjustReason(''); setAdjustAmount('');
    setAdjustment(null); setMessage('');
  }

  return (
    <PageScaffold
      title={t('invoice.title', 'Tax Invoices')}
      viewCode="STAFF_014"
      purpose={t('invoice.purpose', 'Generate compliant tax invoices and create adjustment notes for corrections.')}
    >
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        {/* Main Panel */}
        <div className="grid gap-5">
          {/* Generate Invoice */}
          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3 mb-5">
              <div>
                <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Generate Tax Invoice</h2>
                <p className="mt-1 text-xs text-ink/50">Create a tax-compliant invoice from a completed order.</p>
              </div>
              <FileText size={22} className="text-primary" />
            </div>
            <form onSubmit={handleCreateInvoice} className="grid gap-4">
              <Input
                label="Order ID (Numeric)"
                type="number"
                min={1}
                value={orderId}
                onChange={(e) => setOrderId(e.target.value)}
                placeholder="Enter the order ID"
                required
              />
              <Button type="submit" icon={<ReceiptText size={16} />} disabled={!orderId} isLoading={isSaving}>
                Generate Invoice
              </Button>
            </form>
          </Card>

          {/* Adjustment Note */}
          <Card className="border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3 mb-5">
              <div>
                <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Adjustment Note</h2>
                <p className="mt-1 text-xs text-ink/50">Issue a correction note linked to an existing invoice.</p>
              </div>
              <FilePlus2 size={22} className="text-primary" />
            </div>
            <form onSubmit={handleCreateAdjustment} className="grid gap-4">
              <Input
                label="Invoice ID (Numeric)"
                type="number"
                min={1}
                value={invoiceIdInput}
                onChange={(e) => setInvoiceIdInput(e.target.value)}
                placeholder="Enter the invoice ID"
                required
              />
              <Input
                label="Reason"
                value={adjustReason}
                onChange={(e) => setAdjustReason(e.target.value)}
                placeholder="e.g. Price correction, returned item"
                required
                disabled={!invoiceIdInput}
              />
              <Input
                label="Delta Amount (VND)"
                type="number"
                value={adjustAmount}
                onChange={(e) => setAdjustAmount(e.target.value)}
                placeholder="Positive or negative adjustment"
                required
                disabled={!invoiceIdInput}
              />
              <Button type="submit" variant="secondary" icon={<FilePlus2 size={16} />} disabled={!invoiceIdInput || !adjustReason} isLoading={isSaving}>
                Create Adjustment Note
              </Button>
            </form>
          </Card>
        </div>

        {/* Sidebar */}
        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Latest Invoice</h2>
          {invoice ? (
            <div className="mt-4 grid gap-3 text-sm text-ink/70">
              <div className="flex justify-between">
                <span>Invoice No</span>
                <span className="font-semibold text-ink">{invoice.invoiceNumber}</span>
              </div>
              <div className="flex justify-between">
                <span>Order</span>
                <span className="font-semibold text-ink">#{invoice.orderId}</span>
              </div>
              <div className="flex justify-between">
                <span>Total</span>
                <span className="font-semibold text-ink">{formatCurrency(invoice.totalAmount)}</span>
              </div>
              <div className="flex justify-between">
                <span>Status</span>
                <Badge variant="success">{invoice.status}</Badge>
              </div>
              <div className="flex justify-between">
                <span>Issued</span>
                <span className="font-semibold text-ink">{new Date(invoice.createdAt).toLocaleString()}</span>
              </div>
            </div>
          ) : (
            <p className="mt-4 text-sm text-ink/55">No invoice generated yet.</p>
          )}

          {adjustment && (
            <div className="mt-6 border-t border-primary/10 pt-4">
              <h3 className="text-sm font-semibold text-ink">Latest Adjustment</h3>
              <div className="mt-3 grid gap-2 text-sm text-ink/70">
                <div className="flex justify-between">
                  <span>Note ID</span>
                  <span className="font-semibold text-ink">#{adjustment.id}</span>
                </div>
                <div className="flex justify-between">
                  <span>Amount</span>
                  <span className="font-semibold text-ink">{formatCurrency(adjustment.deltaAmount)}</span>
                </div>
                <div className="flex justify-between">
                  <span>Reason</span>
                  <span className="font-semibold text-ink">{adjustment.reason}</span>
                </div>
                <div className="flex justify-between">
                  <span>Status</span>
                  <Badge variant="success">{adjustment.status}</Badge>
                </div>
              </div>
            </div>
          )}

          {message && (
            <p className="mt-6 inline-flex items-center gap-2 text-sm text-ink/65">
              <CheckCircle2 size={15} className="text-primary" />
              {message}
            </p>
          )}

          {(invoice || adjustment) && (
            <Button variant="secondary" className="mt-6 w-full" onClick={handleReset}>
              Reset
            </Button>
          )}
        </Card>
      </div>
    </PageScaffold>
  );
}
