import { useEffect, useMemo, useState } from 'react';
import { CheckCircle2, ClipboardList, PackageSearch, Send } from 'lucide-react';
import { useTranslation } from 'react-i18next';
import { createCycleCount, submitCycleCountItems, approveCycleCount, type CountedItem } from '@/features/staff/api/staffCycleCountApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { Spinner } from '@/shared/ui/Spinner';

type CountRow = InventoryBalance & { countedQty: number; reasonCode: string };

export function CycleCount() {
  const { t } = useTranslation();
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');
  const [step, setStep] = useState<'create' | 'count' | 'review'>('create');
  const [cycleCountId, setCycleCountId] = useState<number | null>(null);
  const [locationId, setLocationId] = useState(0);
  const [reason, setReason] = useState('');
  const [countRows, setCountRows] = useState<CountRow[]>([]);

  useEffect(() => {
    getInventoryBalances(1, 500)
      .then((page) => { setBalances(page.items); if (page.items[0]) setLocationId(page.items[0].locationId); })
      .catch(() => setBalances([]))
      .finally(() => setIsLoading(false));
  }, []);

  const locationOptions = useMemo(() => {
    const m = new Map<number, string>();
    balances.forEach((b) => m.set(b.locationId, b.locationName));
    return [...m.entries()].map(([id, name]) => ({ label: name, value: String(id) }));
  }, [balances]);

  const locationBalances = useMemo(() => balances.filter((b) => b.locationId === locationId), [balances, locationId]);

  async function handleCreateBatch(e: React.FormEvent) {
    e.preventDefault(); setIsSaving(true); setMessage('');
    try {
      const res = await createCycleCount({ locationId, reason });
      setCycleCountId(res.cycleCountId);
      setCountRows(locationBalances.map((b) => ({ ...b, countedQty: b.onHandQty, reasonCode: '' })));
      setStep('count'); setMessage(`Cycle count #${res.cycleCountId} created.`);
    } catch { setMessage('Cycle count could not be created.'); }
    finally { setIsSaving(false); }
  }

  async function handleSubmitItems(e: React.FormEvent) {
    e.preventDefault(); if (!cycleCountId) return; setIsSaving(true); setMessage('');
    try {
      const items: CountedItem[] = countRows.map((r) => ({ variantId: r.variantId, countedQty: r.countedQty, reasonCode: r.reasonCode || undefined }));
      await submitCycleCountItems(cycleCountId, { items });
      setStep('review'); setMessage('Counted items submitted for review.');
    } catch { setMessage('Items could not be submitted.'); }
    finally { setIsSaving(false); }
  }

  async function handleApprove() {
    if (!cycleCountId) return; setIsSaving(true); setMessage('');
    try { await approveCycleCount(cycleCountId); setMessage(`Cycle count #${cycleCountId} approved.`); }
    catch { setMessage('Cycle count could not be approved.'); }
    finally { setIsSaving(false); }
  }

  function handleReset() { setStep('create'); setCycleCountId(null); setReason(''); setCountRows([]); setMessage(''); }

  function updateRow(variantId: number, field: 'countedQty' | 'reasonCode', value: number | string) {
    setCountRows((rows) => rows.map((r) => (r.variantId === variantId ? { ...r, [field]: value } : r)));
  }

  if (isLoading) return (
    <PageScaffold title={t('cycleCount.title', 'Cycle Count')} viewCode="STAFF_013" purpose="Run periodic stock counts and reconcile variances.">
      <div className="flex h-64 items-center justify-center"><Spinner size="lg" /></div>
    </PageScaffold>
  );

  const stepLabels = ['Create Batch', 'Capture Counts', 'Review & Approve'] as const;
  const stepKeys = ['create', 'count', 'review'] as const;
  const stepIdx = stepKeys.indexOf(step);

  return (
    <PageScaffold title={t('cycleCount.title', 'Cycle Count')} viewCode="STAFF_013" purpose="Run periodic stock counts and reconcile variances with approval workflow.">
      {/* Step Progress */}
      <div className="flex items-center gap-3 mb-2">
        {stepKeys.map((s, i) => (
          <div key={s} className="flex items-center gap-2">
            <span className={`flex size-7 items-center justify-center rounded-full text-xs font-bold ${step === s ? 'bg-primary text-ink' : i < stepIdx ? 'bg-success/20 text-success' : 'bg-ink/10 text-ink/40'}`}>{i + 1}</span>
            <span className={`text-xs font-medium ${step === s ? 'text-ink' : 'text-ink/40'}`}>{stepLabels[i]}</span>
            {i < 2 && <div className="w-12 border-t border-primary/20" />}
          </div>
        ))}
      </div>

      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
        <Card className="border-primary/20 bg-surface/95 p-5">
          {step === 'create' && (
            <>
              <div className="flex items-center justify-between gap-3 mb-5">
                <div><h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">New Cycle Count</h2><p className="mt-1 text-xs text-ink/50">Select a location and define the scope.</p></div>
                <ClipboardList size={22} className="text-primary" />
              </div>
              <form onSubmit={handleCreateBatch} className="grid gap-4">
                <Select label="Location" value={String(locationId)} onChange={(e) => setLocationId(Number(e.target.value))} options={locationOptions.length ? locationOptions : [{ label: 'No locations', value: '0' }]} />
                <Input label="Reason / Notes" value={reason} onChange={(e) => setReason(e.target.value)} placeholder="e.g. Monthly scheduled count" />
                <div className="rounded-md border border-primary/15 bg-ink/[0.03] p-3 text-xs text-ink/60"><p><strong>{locationBalances.length}</strong> SKU positions will be included.</p></div>
                <Button type="submit" icon={<ClipboardList size={16} />} disabled={!locationId} isLoading={isSaving}>Create Count Batch</Button>
              </form>
            </>
          )}

          {step === 'count' && (
            <form onSubmit={handleSubmitItems}>
              <div className="flex items-center justify-between gap-3 mb-5">
                <div><h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Capture Counted Quantities</h2><p className="mt-1 text-xs text-ink/50">Enter physical count for each SKU.</p></div>
                <PackageSearch size={22} className="text-primary" />
              </div>
              <div className="overflow-x-auto mb-4">
                <table className="min-w-full text-left text-sm">
                  <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50"><tr><th className="px-4 py-3">SKU</th><th className="px-4 py-3">Product</th><th className="px-4 py-3 text-right">System</th><th className="px-4 py-3 text-right">Counted</th><th className="px-4 py-3 text-right">Variance</th><th className="px-4 py-3">Reason</th></tr></thead>
                  <tbody className="divide-y divide-primary/10">
                    {countRows.map((row) => {
                      const v = row.countedQty - row.onHandQty;
                      return (<tr key={row.variantId} className="text-ink/80">
                        <td className="px-4 py-3 font-medium text-ink">{row.skuCode}</td>
                        <td className="px-4 py-3">{row.productName}</td>
                        <td className="px-4 py-3 text-right">{row.onHandQty}</td>
                        <td className="px-4 py-3 text-right"><input type="number" min={0} value={row.countedQty} onChange={(e) => updateRow(row.variantId, 'countedQty', parseInt(e.target.value) || 0)} className="w-20 rounded-md border border-primary/20 bg-surface px-2 py-1 text-right text-sm text-ink focus:border-primary focus:outline-none" /></td>
                        <td className={`px-4 py-3 text-right font-semibold ${v === 0 ? 'text-ink/40' : v > 0 ? 'text-success' : 'text-danger'}`}>{v > 0 ? '+' : ''}{v}</td>
                        <td className="px-4 py-3">{v !== 0 && <input type="text" value={row.reasonCode} onChange={(e) => updateRow(row.variantId, 'reasonCode', e.target.value)} placeholder="Reason..." className="w-full rounded-md border border-primary/20 bg-surface px-2 py-1 text-sm text-ink focus:border-primary focus:outline-none" />}</td>
                      </tr>);
                    })}
                  </tbody>
                </table>
              </div>
              <Button type="submit" icon={<Send size={16} />} isLoading={isSaving}>Submit Counts for Review</Button>
            </form>
          )}

          {step === 'review' && (
            <>
              <div className="flex items-center justify-between gap-3 mb-5">
                <div><h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Review & Approve</h2><p className="mt-1 text-xs text-ink/50">Review variances. Approve to post adjustments.</p></div>
                <CheckCircle2 size={22} className="text-success" />
              </div>
              <div className="overflow-x-auto mb-6">
                <table className="min-w-full text-left text-sm">
                  <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50"><tr><th className="px-4 py-3">SKU</th><th className="px-4 py-3">Product</th><th className="px-4 py-3 text-right">System</th><th className="px-4 py-3 text-right">Counted</th><th className="px-4 py-3 text-right">Variance</th><th className="px-4 py-3">Reason</th></tr></thead>
                  <tbody className="divide-y divide-primary/10">
                    {countRows.map((row) => {
                      const v = row.countedQty - row.onHandQty;
                      return (<tr key={row.variantId} className="text-ink/80"><td className="px-4 py-3 font-medium text-ink">{row.skuCode}</td><td className="px-4 py-3">{row.productName}</td><td className="px-4 py-3 text-right">{row.onHandQty}</td><td className="px-4 py-3 text-right font-semibold text-ink">{row.countedQty}</td><td className={`px-4 py-3 text-right font-semibold ${v === 0 ? 'text-ink/40' : v > 0 ? 'text-success' : 'text-danger'}`}>{v > 0 ? '+' : ''}{v}</td><td className="px-4 py-3 text-xs text-ink/60">{row.reasonCode || '—'}</td></tr>);
                    })}
                  </tbody>
                </table>
              </div>
              <div className="flex gap-3">
                <Button onClick={handleApprove} icon={<CheckCircle2 size={16} />} isLoading={isSaving}>Approve & Post Adjustments</Button>
                <Button variant="secondary" onClick={handleReset}>New Count</Button>
              </div>
            </>
          )}
        </Card>

        {/* Sidebar */}
        <Card className="border-primary/20 bg-surface/95 p-5">
          <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Count Summary</h2>
          <div className="mt-4 grid gap-3 text-sm text-ink/70">
            <div className="flex justify-between"><span>Batch ID</span><span className="font-semibold text-ink">{cycleCountId ? `CC-${cycleCountId}` : '—'}</span></div>
            <div className="flex justify-between"><span>Status</span><Badge variant={step === 'review' ? 'success' : step === 'count' ? 'warning' : 'default'}>{step === 'create' ? 'DRAFT' : step === 'count' ? 'COUNTING' : 'PENDING APPROVAL'}</Badge></div>
            <div className="flex justify-between"><span>Location</span><span className="font-semibold text-ink">{locationOptions.find((o) => o.value === String(locationId))?.label ?? '—'}</span></div>
            <div className="flex justify-between"><span>SKU Positions</span><span className="font-semibold text-ink">{countRows.length || locationBalances.length}</span></div>
            {countRows.length > 0 && (<>
              <div className="border-t border-primary/10 pt-3 flex justify-between"><span>Total Variances</span><span className="font-semibold text-ink">{countRows.filter((r) => r.countedQty !== r.onHandQty).length}</span></div>
              <div className="flex justify-between"><span>Net Qty Variance</span><span className="font-semibold text-ink">{countRows.reduce((s, r) => s + (r.countedQty - r.onHandQty), 0)}</span></div>
            </>)}
          </div>
          {message && <p className="mt-6 inline-flex items-center gap-2 text-sm text-ink/65"><CheckCircle2 size={15} className="text-primary" />{message}</p>}
        </Card>
      </div>
    </PageScaffold>
  );
}
