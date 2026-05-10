import { useEffect, useMemo, useState } from 'react';
import { ArrowRightLeft, CheckCircle2, PackageCheck } from 'lucide-react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Spinner } from '@/shared/ui/Spinner';
import {
  approveStockTransfer,
  createStockTransfer,
  getInventoryBalances,
  getStockTransfers,
  receiveStockTransfer,
  type InventoryBalance,
  type StockTransfer,
} from '@/features/staff/api/staffInventoryApi';

export function StockTransfer() {
  const [transfers, setTransfers] = useState<StockTransfer[]>([]);
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [sourceLocationId, setSourceLocationId] = useState(0);
  const [destinationLocationId, setDestinationLocationId] = useState(0);
  const [variantId, setVariantId] = useState(0);
  const [qty, setQty] = useState(1);
  const [reason, setReason] = useState('');
  const [message, setMessage] = useState('');

  const locationOptions = useMemo(() => {
    const locations = new Map<number, string>();
    balances.forEach((balance) => locations.set(balance.locationId, balance.locationName));
    return [...locations.entries()].map(([id, name]) => ({ label: name, value: String(id) }));
  }, [balances]);

  const variantOptions = useMemo(() => {
    const variants = new Map<number, string>();
    balances.forEach((balance) => variants.set(balance.variantId, `${balance.skuCode} - ${balance.productName}`));
    return [...variants.entries()].map(([id, label]) => ({ label, value: String(id) }));
  }, [balances]);

  async function fetchTransfers() {
    setIsLoading(true);
    try {
      const [transferPage, balancePage] = await Promise.all([
        getStockTransfers(),
        getInventoryBalances(1, 100),
      ]);
      setTransfers(transferPage.items);
      setBalances(balancePage.items);
      const first = balancePage.items[0];
      const second = balancePage.items.find((balance) => balance.locationId !== first?.locationId);
      if (first) {
        setSourceLocationId(first.locationId);
        setVariantId(first.variantId);
      }
      if (second) setDestinationLocationId(second.locationId);
    } catch {
      setTransfers([]);
      setBalances([]);
    } finally {
      setIsLoading(false);
    }
  }

  useEffect(() => {
    fetchTransfers();
  }, []);

  async function handleCreate(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createStockTransfer({
        sourceLocationId,
        destinationLocationId,
        reason,
        items: [{ variantId, qty }],
      });
      setMessage(`Transfer ${created.transferId} created.`);
      await fetchTransfers();
    } catch {
      setMessage('Transfer could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAdvance(transfer: StockTransfer) {
    setIsSaving(true);
    setMessage('');
    try {
      if (transfer.status === 'DRAFT') {
        await approveStockTransfer(transfer.id);
        setMessage(`Transfer ${transfer.id} approved.`);
      }
      if (transfer.status === 'IN_TRANSIT') {
        await receiveStockTransfer(transfer.id, {
          receivedItems: [{ variantId, receivedQty: qty, damagedQty: 0 }],
        });
        setMessage(`Transfer ${transfer.id} received.`);
      }
      await fetchTransfers();
    } catch {
      setMessage('Transfer action failed.');
    } finally {
      setIsSaving(false);
    }
  }

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="mb-8 flex items-end justify-between gap-3">
        <div>
          <h1 className="font-display text-2xl font-semibold text-surface">Stock Transfers</h1>
          <p className="mt-1 text-sm text-surface/60">Move inventory between stores and storage locations.</p>
        </div>
      </div>

      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_360px]">
        <Card className="border-primary/20 bg-surface/95">
          <div className="overflow-x-auto">
            <table className="min-w-full divide-y divide-primary/10">
              <thead className="bg-ink/[0.03]">
                <tr>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">ID</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">From</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">To</th>
                  <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Status</th>
                  <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Items</th>
                  <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10 bg-surface">
                {transfers.map((transfer) => (
                  <tr key={transfer.id}>
                    <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-ink">TRN-{transfer.id}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-ink/60">{transfer.sourceLocationName}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm text-ink/60">{transfer.destinationLocationName}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-sm">
                      <Badge variant={transfer.status === 'COMPLETED' ? 'success' : transfer.status === 'IN_TRANSIT' ? 'warning' : 'default'}>
                        {transfer.status}
                      </Badge>
                    </td>
                    <td className="whitespace-nowrap px-6 py-4 text-right text-sm text-ink/60">{transfer.itemCount}</td>
                    <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-medium">
                      {['DRAFT', 'IN_TRANSIT'].includes(transfer.status) ? (
                        <Button size="sm" variant={transfer.status === 'DRAFT' ? 'secondary' : 'primary'} onClick={() => handleAdvance(transfer)} isLoading={isSaving}>
                          {transfer.status === 'DRAFT' ? 'Approve' : 'Receive'}
                        </Button>
                      ) : null}
                    </td>
                  </tr>
                ))}
                {transfers.length === 0 && (
                  <tr>
                    <td colSpan={6} className="px-6 py-4 text-center text-sm text-ink/55">
                      No transfers found.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </Card>

        <Card className="border-primary/20 bg-surface/95 p-5">
          <div className="flex items-center justify-between gap-3">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">New Transfer</h2>
            <ArrowRightLeft size={18} className="text-primary" />
          </div>
          <form onSubmit={handleCreate} className="mt-4 grid gap-4">
            <Select label="Source" value={String(sourceLocationId)} onChange={(event) => setSourceLocationId(Number(event.target.value))} options={locationOptions.length ? locationOptions : [{ label: 'No locations loaded', value: '0' }]} />
            <Select label="Destination" value={String(destinationLocationId)} onChange={(event) => setDestinationLocationId(Number(event.target.value))} options={locationOptions.length ? locationOptions : [{ label: 'No locations loaded', value: '0' }]} />
            <Select label="Variant" value={String(variantId)} onChange={(event) => setVariantId(Number(event.target.value))} options={variantOptions.length ? variantOptions : [{ label: 'No variants loaded', value: '0' }]} />
            <Input label="Quantity" type="number" min={1} value={qty} onChange={(event) => setQty(Number(event.target.value))} />
            <Input label="Reason" value={reason} onChange={(event) => setReason(event.target.value)} />
            <Button type="submit" icon={<PackageCheck size={16} />} disabled={!sourceLocationId || !destinationLocationId || !variantId || sourceLocationId === destinationLocationId} isLoading={isSaving}>
              Create Transfer
            </Button>
          </form>
          {message ? (
            <p className="mt-4 inline-flex items-center gap-2 text-sm text-ink/65">
              <CheckCircle2 size={15} className="text-primary" />
              {message}
            </p>
          ) : null}
        </Card>
      </div>
    </div>
  );
}
