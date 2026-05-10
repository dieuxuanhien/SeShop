import { useState, useEffect } from 'react';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Modal } from '@/shared/ui/Modal';
import { Spinner } from '@/shared/ui/Spinner';
import { getInventoryBalances, adjustInventory, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';

export function InventoryAdjustment() {
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBalance, setSelectedBalance] = useState<InventoryBalance | null>(null);

  // Form state
  const [deltaQty, setDeltaQty] = useState(0);
  const [reasonCode, setReasonCode] = useState('DAMAGE');
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchBalances = async () => {
    setIsLoading(true);
    try {
      const res = await getInventoryBalances();
      setBalances(res.items);
    } catch (e) {
      console.error(e);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchBalances();
  }, []);

  const handleAdjustClick = (balance: InventoryBalance) => {
    setSelectedBalance(balance);
    setDeltaQty(0);
    setReasonCode('DAMAGE');
    setNotes('');
    setIsModalOpen(true);
  };

  const handleSubmitAdjustment = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!selectedBalance) return;
    setIsSubmitting(true);
    try {
      await adjustInventory(selectedBalance.variantId, selectedBalance.locationId, deltaQty, reasonCode, notes);
      setIsModalOpen(false);
      await fetchBalances();
    } catch (e) {
      console.error(e);
    } finally {
      setIsSubmitting(false);
    }
  };

  if (isLoading) {
    return (
      <div className="flex h-64 items-center justify-center">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl">
      <div className="flex justify-between items-end mb-8">
        <div>
          <h1 className="font-display text-2xl font-semibold text-surface">Inventory Adjustments</h1>
          <p className="mt-1 text-sm text-surface/60">View and adjust stock levels.</p>
        </div>
      </div>

      <Card className="border-primary/20 bg-surface/95">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-primary/10">
            <thead className="bg-ink/[0.03]">
              <tr>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Location</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">SKU</th>
                <th className="px-6 py-3 text-left text-xs font-medium uppercase tracking-wider text-ink/50">Product</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">On Hand</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Reserved</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Available</th>
                <th className="px-6 py-3 text-right text-xs font-medium uppercase tracking-wider text-ink/50">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary/10 bg-surface">
              {balances.map((balance) => (
                <tr key={balance.id}>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-ink/60">{balance.locationName}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-ink">{balance.skuCode}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-ink/60">{balance.productName}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-right font-medium">{balance.onHandQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-warning">{balance.reservedQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-sm text-right text-success">{balance.availableQty}</td>
                  <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <Button variant="secondary" size="sm" onClick={() => handleAdjustClick(balance)}>
                      Adjust
                    </Button>
                  </td>
                </tr>
              ))}
              {balances.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-4 text-center text-sm text-ink/55">
                    No balances found.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </Card>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Adjust Inventory">
        {selectedBalance && (
          <form onSubmit={handleSubmitAdjustment} className="space-y-4 p-6">
            <div className="mb-4 rounded-md bg-ink/[0.03] p-4">
              <p className="text-sm font-medium">{selectedBalance.productName}</p>
              <p className="text-xs text-ink/55">SKU: {selectedBalance.skuCode} | Location: {selectedBalance.locationName}</p>
              <p className="text-sm mt-2">Current On Hand: <strong>{selectedBalance.onHandQty}</strong></p>
            </div>
            
            <Input
              label="Quantity Change (+/-)"
              type="number"
              value={deltaQty}
              onChange={(e) => setDeltaQty(parseInt(e.target.value) || 0)}
              required
            />
            
            <Select
              label="Reason Code"
              value={reasonCode}
              onChange={(e) => setReasonCode(e.target.value)}
              options={[
                { value: 'DAMAGE', label: 'Damage/Spoilage' },
                { value: 'THEFT', label: 'Theft/Loss' },
                { value: 'COUNT', label: 'Cycle Count Variance' },
                { value: 'RETURN', label: 'Return to Stock' },
              ]}
              required
            />

            <Input
              label="Notes"
              value={notes}
              onChange={(e) => setNotes(e.target.value)}
            />

            <div className="mt-6 flex justify-end gap-3">
              <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)}>Cancel</Button>
              <Button type="submit" isLoading={isSubmitting}>Confirm Adjustment</Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
