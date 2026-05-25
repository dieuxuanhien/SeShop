import { useCallback, useEffect, useMemo, useState } from 'react';
import { ChevronLeft, ChevronRight, PackageSearch, Activity, MapPin, ChevronDown } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { Select } from '@/shared/ui/Select';
import { Modal } from '@/shared/ui/Modal';
import { Spinner } from '@/shared/ui/Spinner';
import { getInventoryBalances, adjustInventory, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';

const inventoryPageSize = 20;

export function InventoryAdjustment() {
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [selectedBalance, setSelectedBalance] = useState<InventoryBalance | null>(null);
  
  const [locationFilter, setLocationFilter] = useState('ALL');
  const [skuFilter, setSkuFilter] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageInfo, setPageInfo] = useState({
    page: 1,
    size: inventoryPageSize,
    totalElements: 0,
    totalPages: 1,
  });

  // Form state
  const [deltaQty, setDeltaQty] = useState(0);
  const [reasonCode, setReasonCode] = useState('DAMAGE');
  const [notes, setNotes] = useState('');
  const [isSubmitting, setIsSubmitting] = useState(false);

  const fetchBalances = useCallback(async () => {
    setIsLoading(true);
    try {
      const res = await getInventoryBalances({
        page: currentPage,
        size: inventoryPageSize,
        skuCode: skuFilter.trim() || undefined,
        locationId: locationFilter === 'ALL' ? undefined : Number(locationFilter),
      });
      setBalances(res.items);
      setPageInfo({
        page: res.page,
        size: res.size,
        totalElements: res.totalElements,
        totalPages: Math.max(res.totalPages, 1),
      });
    } catch (e) {
      console.error(e);
      setBalances([]);
      setPageInfo((current) => ({ ...current, totalElements: 0, totalPages: 1 }));
    } finally {
      setIsLoading(false);
    }
  }, [currentPage, locationFilter, skuFilter]);

  useEffect(() => {
    fetchBalances();
  }, [fetchBalances]);

  const uniqueLocations = useMemo(() => {
    const byId = new Map<number, string>();
    balances.forEach((balance) => byId.set(balance.locationId, balance.locationName));
    return [...byId.entries()];
  }, [balances]);

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
      <div className="flex h-[60vh] items-center justify-center text-primary">
        <Spinner size="lg" />
      </div>
    );
  }

  return (
    <div className="mx-auto max-w-7xl animate-fade-in space-y-8">
      {/* Header Section */}
      <div className="flex items-end justify-between">
        <div>
          <div className="flex items-center gap-3 mb-2">
            <div className="rounded-xl bg-primary/10 p-2.5 text-primary shadow-sm shadow-primary/5">
              <Activity size={24} />
            </div>
            <h1 className="font-display text-3xl font-bold tracking-tight bg-gradient-to-r from-primary to-highlight bg-clip-text text-transparent">
              Inventory Adjustments
            </h1>
          </div>
          <p className="text-surface/60 max-w-2xl text-sm leading-relaxed">
            Monitor real-time stock balances, track reserved quantities, and execute manual cycle counts with full audit traceability across all locations.
          </p>
        </div>
      </div>

      {/* Filters Section */}
      <Card className="border border-primary/20 bg-surface/50 backdrop-blur-md p-5 shadow-lg shadow-black/5 rounded-2xl">
        <div className="flex flex-col sm:flex-row items-end gap-6">
          <div className="w-full sm:w-64">
            <label className="mb-2 flex items-center gap-2 text-sm font-semibold text-surface/80">
              <MapPin size={16} className="text-primary" />
              Location
            </label>
            <div className="relative">
              <select 
                className="w-full appearance-none rounded-xl border border-primary/20 bg-surface/80 px-4 py-2.5 text-sm text-surface shadow-inner transition-colors focus:border-primary focus:outline-none focus:ring-2 focus:ring-primary/20"
                value={locationFilter} 
                onChange={(e) => {
                  setLocationFilter(e.target.value);
                  setCurrentPage(1);
                }}
              >
                <option value="ALL">All Locations</option>
                {uniqueLocations.map(([id, name]) => (
                  <option key={id} value={id}>{name}</option>
                ))}
              </select>
              <div className="pointer-events-none absolute right-3 top-1/2 -translate-y-1/2 text-surface/50">
                <ChevronDown size={16} />
              </div>
            </div>
          </div>
          <div className="w-full sm:w-80">
            <label className="mb-2 flex items-center gap-2 text-sm font-semibold text-surface/80">
              <PackageSearch size={16} className="text-primary" />
              Search SKU
            </label>
            <Input 
              placeholder="e.g. TSHIRT-001..." 
              value={skuFilter} 
              onChange={(e) => {
                setSkuFilter(e.target.value);
                setCurrentPage(1);
              }}
              className="rounded-xl border-primary/20 bg-surface/80 shadow-inner"
            />
          </div>
        </div>
      </Card>

      {/* Main Table Section */}
      <Card className="overflow-hidden border border-primary/10 bg-surface shadow-2xl shadow-black/10 rounded-2xl">
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-primary/10">
            <thead className="bg-primary/[0.02]">
              <tr>
                <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-widest text-surface/50">Location</th>
                <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-widest text-surface/50">SKU</th>
                <th className="px-6 py-4 text-left text-xs font-bold uppercase tracking-widest text-surface/50">Product</th>
                <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-widest text-surface/50">On Hand</th>
                <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-widest text-surface/50">Reserved</th>
                <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-widest text-surface/50">Available</th>
                <th className="px-6 py-4 text-right text-xs font-bold uppercase tracking-widest text-surface/50">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-primary/5">
              {balances.map((balance) => (
                <tr key={balance.id} className="group transition-all duration-200 hover:bg-primary/[0.03]">
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-surface/70">
                    <div className="flex items-center gap-2">
                      <div className="h-1.5 w-1.5 rounded-full bg-primary/50"></div>
                      {balance.locationName}
                    </div>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4">
                    <span className="inline-flex items-center rounded-md bg-highlight/10 px-2 py-1 text-xs font-semibold text-highlight ring-1 ring-inset ring-highlight/20">
                      {balance.skuCode}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-sm font-medium text-surface">
                    {balance.productName}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right text-sm font-semibold text-surface">
                    {balance.onHandQty}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    {balance.reservedQty > 0 ? (
                      <span className="inline-flex items-center rounded-full bg-warning/10 px-2.5 py-0.5 text-xs font-bold text-warning border border-warning/20">
                        {balance.reservedQty}
                      </span>
                    ) : (
                      <span className="text-xs font-medium text-surface/30">0</span>
                    )}
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <span className="inline-flex items-center rounded-full bg-success/10 px-3 py-1 text-xs font-bold text-success border border-success/20 shadow-sm">
                      {balance.availableQty}
                    </span>
                  </td>
                  <td className="whitespace-nowrap px-6 py-4 text-right">
                    <Button 
                      variant="primary" 
                      size="sm" 
                      onClick={() => handleAdjustClick(balance)} 
                      className="opacity-0 translate-x-2 group-hover:opacity-100 group-hover:translate-x-0 transition-all duration-300 shadow-md hover:shadow-lg hover:-translate-y-0.5"
                    >
                      Adjust
                    </Button>
                  </td>
                </tr>
              ))}
              {balances.length === 0 && (
                <tr>
                  <td colSpan={7} className="px-6 py-16 text-center">
                    <div className="flex flex-col items-center justify-center text-surface/40">
                      <PackageSearch size={48} className="mb-4 opacity-20" />
                      <p className="text-lg font-medium">No balances found</p>
                      <p className="text-sm mt-1">Try adjusting your filters or search criteria.</p>
                    </div>
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
        
        {/* Pagination Footer */}
        <div className="flex flex-wrap items-center justify-between gap-4 border-t border-primary/10 bg-primary/[0.01] px-6 py-4">
          <span className="text-sm font-medium text-surface/60">
            Showing page <span className="text-surface font-bold">{pageInfo.page}</span> of <span className="text-surface">{pageInfo.totalPages}</span> 
            <span className="mx-2 text-primary/20">|</span> 
            Total <span className="text-surface font-bold">{pageInfo.totalElements}</span> items
          </span>
          <div className="flex gap-2">
            <Button
              type="button"
              variant="secondary"
              size="sm"
              icon={<ChevronLeft size={16} />}
              disabled={isLoading || pageInfo.page <= 1}
              onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
              className="rounded-full px-4"
            >
              Prev
            </Button>
            <Button
              type="button"
              variant="secondary"
              size="sm"
              icon={<ChevronRight size={16} />}
              disabled={isLoading || pageInfo.page >= pageInfo.totalPages}
              onClick={() => setCurrentPage((page) => Math.min(pageInfo.totalPages, page + 1))}
              className="rounded-full px-4 flex-row-reverse"
            >
              Next
            </Button>
          </div>
        </div>
      </Card>

      <Modal isOpen={isModalOpen} onClose={() => setIsModalOpen(false)} title="Execute Adjustment">
        {selectedBalance && (
          <form onSubmit={handleSubmitAdjustment} className="space-y-6 p-6">
            <div className="rounded-xl border border-primary/10 bg-primary/5 p-5 shadow-inner">
              <div className="flex items-start justify-between">
                <div>
                  <h3 className="text-lg font-bold text-surface">{selectedBalance.productName}</h3>
                  <div className="mt-2 flex flex-wrap gap-2">
                    <span className="inline-flex items-center rounded-md bg-highlight/10 px-2 py-1 text-xs font-semibold text-highlight">
                      {selectedBalance.skuCode}
                    </span>
                    <span className="inline-flex items-center rounded-md bg-surface/10 px-2 py-1 text-xs font-semibold text-surface/70">
                      {selectedBalance.locationName}
                    </span>
                  </div>
                </div>
                <div className="text-right">
                  <p className="text-xs font-medium uppercase tracking-wider text-surface/50">Current On Hand</p>
                  <p className="text-2xl font-black text-primary">{selectedBalance.onHandQty}</p>
                </div>
              </div>
            </div>
            
            <div className="grid grid-cols-2 gap-6">
              <div className="col-span-2 sm:col-span-1">
                <Input
                  label="Quantity Change"
                  type="number"
                  value={deltaQty}
                  onChange={(e) => setDeltaQty(parseInt(e.target.value) || 0)}
                  required
                  className="font-mono text-lg"
                  placeholder="e.g. -2 or 5"
                />
                <p className="mt-2 text-xs text-surface/50">Use negative values to deduct stock.</p>
              </div>
              
              <div className="col-span-2 sm:col-span-1">
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
              </div>
            </div>

            <div className="col-span-2">
              <Input
                label="Adjustment Notes"
                value={notes}
                onChange={(e) => setNotes(e.target.value)}
                placeholder="Optional explanation for this adjustment..."
              />
            </div>

            <div className="mt-8 flex justify-end gap-3 border-t border-primary/10 pt-6">
              <Button type="button" variant="secondary" onClick={() => setIsModalOpen(false)}>
                Cancel
              </Button>
              <Button type="submit" isLoading={isSubmitting} className="shadow-lg shadow-primary/20">
                Confirm Adjustment
              </Button>
            </div>
          </form>
        )}
      </Modal>
    </div>
  );
}
