import { useEffect, useMemo, useState } from 'react';
import { ClipboardCheck, PackagePlus, ReceiptText } from 'lucide-react';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import {
  createGoodsReceipt,
  createPurchaseOrder,
  type GoodsReceiptResponse,
  type PurchaseOrderResponse,
} from '@/features/staff/api/staffProcurementApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';

export function PurchaseOrders() {
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [supplierId, setSupplierId] = useState(1);
  const [destinationLocationId, setDestinationLocationId] = useState(1);
  const [variantId, setVariantId] = useState(0);
  const [orderedQty, setOrderedQty] = useState(1);
  const [unitCost, setUnitCost] = useState(100000);
  const [purchaseOrder, setPurchaseOrder] = useState<PurchaseOrderResponse | null>(null);
  const [receipt, setReceipt] = useState<GoodsReceiptResponse | null>(null);
  const [receivedQty, setReceivedQty] = useState(1);
  const [damagedQty, setDamagedQty] = useState(0);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    getInventoryBalances(1, 100)
      .then((page) => {
        setBalances(page.items);
        const first = page.items[0];
        if (first) {
          setDestinationLocationId(first.locationId);
          setVariantId(first.variantId);
        }
      })
      .catch(() => setBalances([]));
  }, []);

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

  async function handleCreatePurchaseOrder(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createPurchaseOrder({
        supplierId,
        destinationLocationId,
        items: [{ variantId, orderedQty, unitCost }],
      });
      setPurchaseOrder(created);
      setReceivedQty(orderedQty);
      setMessage(`${created.poNumber} created.`);
    } catch {
      setMessage('Purchase order could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleCreateReceipt(event: React.FormEvent) {
    event.preventDefault();
    if (!purchaseOrder) return;
    setIsSaving(true);
    setMessage('');
    try {
      const created = await createGoodsReceipt({
        purchaseOrderId: purchaseOrder.id,
        receivedAt: new Date().toISOString(),
        items: [{ variantId, receivedQty, damagedQty }],
      });
      setReceipt(created);
      setMessage(`Goods receipt ${created.id} recorded.`);
    } catch {
      setMessage('Goods receipt could not be recorded.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="Purchase Orders"
      viewCode="STAFF_011"
      purpose="Create supplier orders and record inbound stock as goods arrive."
    >
      <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_380px]">
        <Card className="border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-start justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Inbound Order</h2>
              <p className="mt-1 text-xs text-ink/50">Choose an existing SKU and destination to prepare replenishment.</p>
            </div>
            {purchaseOrder ? <Badge variant="success">{purchaseOrder.status}</Badge> : <Badge variant="info">Draft</Badge>}
          </div>

          <form onSubmit={handleCreatePurchaseOrder} className="mt-5 grid gap-4">
            <div className="grid gap-4 md:grid-cols-2">
              <Input
                label="Supplier ID"
                type="number"
                min={1}
                value={supplierId}
                onChange={(event) => setSupplierId(Number(event.target.value))}
                required
              />
              <Select
                label="Destination"
                value={String(destinationLocationId)}
                onChange={(event) => setDestinationLocationId(Number(event.target.value))}
                options={locationOptions.length ? locationOptions : [{ label: 'Location 1', value: '1' }]}
              />
              <div className="md:col-span-2">
                <Select
                  label="Variant"
                  value={String(variantId)}
                  onChange={(event) => setVariantId(Number(event.target.value))}
                  options={variantOptions.length ? variantOptions : [{ label: 'Load inventory to select a variant', value: '0' }]}
                />
              </div>
              <Input
                label="Ordered Qty"
                type="number"
                min={1}
                value={orderedQty}
                onChange={(event) => setOrderedQty(Number(event.target.value))}
                required
              />
              <Input
                label="Unit Cost"
                type="number"
                min={1}
                value={unitCost}
                onChange={(event) => setUnitCost(Number(event.target.value))}
                required
              />
            </div>
            <Button type="submit" icon={<PackagePlus size={16} />} disabled={!variantId} isLoading={isSaving}>
              Create Purchase Order
            </Button>
          </form>

          <div className="mt-5 rounded-md border border-primary/15 bg-ink/[0.03] p-4">
            <h3 className="text-sm font-semibold text-ink">Order Preview</h3>
            <div className="mt-3 grid gap-2 text-sm text-ink/65">
              <div className="flex justify-between gap-3">
                <span>Quantity</span>
                <span className="font-semibold text-ink">{orderedQty.toLocaleString()}</span>
              </div>
              <div className="flex justify-between gap-3">
                <span>Estimated Cost</span>
                <span className="font-semibold text-ink">{formatCurrency(orderedQty * unitCost)}</span>
              </div>
            </div>
          </div>
        </Card>

        <div className="grid gap-5">
          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Latest Purchase Order</h2>
            {purchaseOrder ? (
              <div className="mt-4 grid gap-3 text-sm text-ink/70">
                <div className="flex justify-between">
                  <span>PO Number</span>
                  <span className="font-semibold text-ink">{purchaseOrder.poNumber}</span>
                </div>
                <div className="flex justify-between">
                  <span>Created</span>
                  <span className="font-semibold text-ink">{new Date(purchaseOrder.createdAt).toLocaleString()}</span>
                </div>
                <div className="flex justify-between">
                  <span>Destination</span>
                  <span className="font-semibold text-ink">LOC-{purchaseOrder.destinationLocationId}</span>
                </div>
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink/55">No purchase order created in this session.</p>
            )}
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Goods Receipt</h2>
            <form onSubmit={handleCreateReceipt} className="mt-4 grid gap-4">
              <Input label="Purchase Order ID" value={purchaseOrder?.id ?? ''} readOnly />
              <Input
                label="Received Qty"
                type="number"
                min={1}
                value={receivedQty}
                onChange={(event) => setReceivedQty(Number(event.target.value))}
              />
              <Input
                label="Damaged Qty"
                type="number"
                min={0}
                value={damagedQty}
                onChange={(event) => setDamagedQty(Number(event.target.value))}
              />
              <Button type="submit" variant="secondary" icon={<ClipboardCheck size={16} />} disabled={!purchaseOrder} isLoading={isSaving}>
                Record Receipt
              </Button>
            </form>
          </Card>

          <Card className="border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Receiving Result</h2>
            {receipt ? (
              <div className="mt-4 flex items-center justify-between rounded-md border border-primary/15 bg-ink/[0.03] p-3 text-sm text-ink/70">
                <span>Receipt {receipt.id}</span>
                <Badge variant="success">{receipt.status}</Badge>
              </div>
            ) : (
              <p className="mt-4 text-sm text-ink/55">Record a receipt when stock arrives.</p>
            )}
            {message ? (
              <p className="mt-4 inline-flex items-center gap-2 text-sm text-ink/65">
                <ReceiptText size={15} className="text-primary" />
                {message}
              </p>
            ) : null}
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
