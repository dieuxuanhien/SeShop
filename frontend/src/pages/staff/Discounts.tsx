import { useEffect, useMemo, useState } from 'react';
import { Percent, Plus, Search, Trash2 } from 'lucide-react';
import {
  createDiscount,
  deactivateDiscount,
  getDiscounts,
  updateDiscount,
  type Discount,
  type DiscountMutationRequest,
} from '@/features/marketing/api/discountApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';

const emptyForm: DiscountMutationRequest = {
  code: '',
  discountType: 'PERCENT',
  discountValue: 10,
  minSpend: 0,
  maxUses: 100,
  startAt: '',
  endAt: '',
  status: 'ACTIVE',
};

function toDatetimeLocal(value?: string) {
  if (!value) return '';
  return value.slice(0, 16);
}

function toApiDatetime(value?: string) {
  return value ? new Date(value).toISOString() : undefined;
}

export function Discounts() {
  const [discounts, setDiscounts] = useState<Discount[]>([]);
  const [form, setForm] = useState<DiscountMutationRequest>(emptyForm);
  const [editingId, setEditingId] = useState<number | null>(null);
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  const filteredDiscounts = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return discounts;
    return discounts.filter((discount) => discount.code.toLowerCase().includes(keyword));
  }, [discounts, search]);

  async function loadDiscounts() {
    setIsLoading(true);
    try {
      setDiscounts(await getDiscounts());
    } catch {
      setDiscounts([]);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    loadDiscounts();
  }, []);

  function handleEdit(discount: Discount) {
    setEditingId(discount.id);
    setForm({
      code: discount.code,
      discountType: discount.discountType,
      discountValue: Number(discount.discountValue),
      minSpend: Number(discount.minSpend ?? 0),
      maxUses: discount.maxUses ?? 100,
      startAt: toDatetimeLocal(discount.startAt),
      endAt: toDatetimeLocal(discount.endAt),
      status: discount.status,
    });
    setMessage('');
  }

  function handleNew() {
    setEditingId(null);
    setForm(emptyForm);
    setMessage('');
  }

  async function handleSubmit(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    const request: DiscountMutationRequest = {
      ...form,
      code: form.code.trim().toUpperCase(),
      discountValue: Number(form.discountValue),
      minSpend: Number(form.minSpend ?? 0),
      maxUses: Number(form.maxUses ?? 0),
      startAt: toApiDatetime(form.startAt),
      endAt: toApiDatetime(form.endAt),
    };

    try {
      const saved = editingId ? await updateDiscount(editingId, request) : await createDiscount(request);
      setEditingId(saved.id);
      setMessage(`${saved.code} saved.`);
      await loadDiscounts();
    } catch {
      setMessage('Discount could not be saved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeactivate(discountId: number) {
    setIsSaving(true);
    setMessage('');
    try {
      await deactivateDiscount(discountId);
      setMessage('Discount deactivated.');
      await loadDiscounts();
    } catch {
      setMessage('Discount could not be deactivated.');
    } finally {
      setIsSaving(false);
    }
  }

  const activeCount = discounts.filter((discount) => discount.status === 'ACTIVE').length;

  return (
    <PageScaffold
      title="Discount & Promotion Management"
      viewCode="STAFF_006"
      purpose="Build code-based promotions, control redemption limits, and monitor active campaigns."
    >
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <Card className="border-primary/20 bg-surface/95">
          <div className="flex flex-wrap items-center justify-between gap-3 border-b border-primary/15 p-5">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Promotion Library</h2>
              <p className="mt-1 text-xs text-ink/50">{activeCount} active of {discounts.length} total campaigns.</p>
            </div>
            <label className="relative w-full sm:w-72">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search by code"
                className="min-h-10 w-full rounded-md border border-primary/25 bg-surface pl-9 pr-3 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
              />
            </label>
          </div>

          <div className="grid gap-3 p-5">
            {isLoading ? (
              <p className="text-sm text-ink/60">Loading discounts...</p>
            ) : filteredDiscounts.length === 0 ? (
              <EmptyState title="No discounts found" description="Create a promotion code to make it available at checkout." />
            ) : (
              filteredDiscounts.map((discount) => (
                <div key={discount.id} className="grid gap-3 rounded-md border border-primary/15 bg-ink/[0.03] p-4 md:grid-cols-[1fr_auto]">
                  <div className="min-w-0">
                    <div className="flex flex-wrap items-center gap-2">
                      <p className="font-semibold text-ink">{discount.code}</p>
                      <Badge variant={discount.status === 'ACTIVE' ? 'success' : 'warning'}>{discount.status}</Badge>
                    </div>
                    <p className="mt-1 text-sm text-ink/60">
                      {discount.discountType === 'PERCENT'
                        ? `${Number(discount.discountValue).toLocaleString()}% off`
                        : `${formatCurrency(Number(discount.discountValue))} off`}
                      {discount.minSpend ? ` from ${formatCurrency(Number(discount.minSpend))}` : ''}
                    </p>
                    <p className="mt-1 text-xs text-ink/45">
                      Uses: {discount.maxUses ?? 'Unlimited'} | Ends: {discount.endAt ? new Date(discount.endAt).toLocaleString() : 'Open'}
                    </p>
                  </div>
                  <div className="flex flex-wrap items-center gap-2 md:justify-end">
                    <Button variant="secondary" size="sm" onClick={() => handleEdit(discount)}>
                      Edit
                    </Button>
                    <Button variant="danger" size="sm" icon={<Trash2 size={14} />} onClick={() => handleDeactivate(discount.id)} disabled={isSaving}>
                      Disable
                    </Button>
                  </div>
                </div>
              ))
            )}
          </div>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5">
          <div className="mb-4 flex items-center justify-between">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">
              {editingId ? 'Edit Promotion' : 'Create Promotion'}
            </h2>
            <Button type="button" variant="secondary" size="sm" onClick={handleNew}>
              New
            </Button>
          </div>

          <form onSubmit={handleSubmit} className="grid gap-4">
            <Input
              label="Code"
              value={form.code}
              onChange={(event) => setForm((current) => ({ ...current, code: event.target.value.toUpperCase() }))}
              required
            />
            <div className="grid gap-3 sm:grid-cols-2">
              <Select
                label="Type"
                value={form.discountType}
                onChange={(event) => setForm((current) => ({ ...current, discountType: event.target.value }))}
                options={[
                  { label: 'Percent', value: 'PERCENT' },
                  { label: 'Fixed amount', value: 'FIXED' },
                ]}
              />
              <Input
                label="Value"
                type="number"
                min={0}
                value={form.discountValue}
                onChange={(event) => setForm((current) => ({ ...current, discountValue: Number(event.target.value) }))}
                required
              />
              <Input
                label="Minimum Spend"
                type="number"
                min={0}
                value={form.minSpend ?? 0}
                onChange={(event) => setForm((current) => ({ ...current, minSpend: Number(event.target.value) }))}
              />
              <Input
                label="Max Uses"
                type="number"
                min={0}
                value={form.maxUses ?? 0}
                onChange={(event) => setForm((current) => ({ ...current, maxUses: Number(event.target.value) }))}
              />
            </div>
            <Input
              label="Starts"
              type="datetime-local"
              value={form.startAt}
              onChange={(event) => setForm((current) => ({ ...current, startAt: event.target.value }))}
            />
            <Input
              label="Ends"
              type="datetime-local"
              value={form.endAt}
              onChange={(event) => setForm((current) => ({ ...current, endAt: event.target.value }))}
            />
            <Select
              label="Status"
              value={form.status}
              onChange={(event) => setForm((current) => ({ ...current, status: event.target.value }))}
              options={[
                { label: 'Active', value: 'ACTIVE' },
                { label: 'Inactive', value: 'INACTIVE' },
              ]}
            />
            <Button type="submit" icon={editingId ? <Percent size={16} /> : <Plus size={16} />} isLoading={isSaving}>
              Save Promotion
            </Button>
          </form>
          {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
        </Card>
      </div>
    </PageScaffold>
  );
}
