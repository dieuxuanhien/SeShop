import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { locationsFromBalances, type LocationSummary } from '@/features/admin/api/adminApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';

export function LocationsManagement() {
  const [locations, setLocations] = useState<LocationSummary[]>([]);
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  function loadLocations() {
    setIsLoading(true);
    getInventoryBalances(1, 100)
      .then((page) => {
        setBalances(page.items);
        setLocations(locationsFromBalances(page.items));
      })
      .catch(() => {
        setBalances([]);
        setLocations([]);
      })
      .finally(() => setIsLoading(false));
  }

  useEffect(() => {
    loadLocations();
  }, []);

  const totalsByLocation = locations.map((location) => {
    const locationBalances = balances.filter((balance) => balance.locationId === location.id);
    const available = locationBalances.reduce((sum, balance) => sum + Number(balance.availableQty ?? 0), 0);
    const reserved = locationBalances.reduce((sum, balance) => sum + Number(balance.reservedQty ?? 0), 0);
    return { ...location, available, reserved };
  });

  return (
    <PageScaffold
      title="Locations Management"
      viewCode="ADMIN_003"
      purpose="Review store and storage locations through current inventory balances."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Location Directory</h2>
              <p className="mt-1 text-xs text-ink/50">Monitor store and storage hubs with inventory snapshots.</p>
            </div>
            <Button variant="secondary" onClick={loadLocations}>Refresh</Button>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-3 py-2">ID</th>
                  <th className="px-3 py-2">Location</th>
                  <th className="px-3 py-2 text-right">SKU Count</th>
                  <th className="px-3 py-2 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={4} className="px-3 py-6 text-center text-sm text-ink/60">Loading locations...</td>
                  </tr>
                ) : totalsByLocation.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-3 py-6 text-center text-sm text-ink/60">No locations returned by inventory balances.</td>
                  </tr>
                ) : totalsByLocation.map((location) => (
                  <tr key={location.id} className="text-ink/80">
                    <td className="px-3 py-3 font-semibold text-ink">LOC-{location.id}</td>
                    <td className="px-3 py-3">{location.name}</td>
                    <td className="px-3 py-3 text-right">{location.skus} SKUs / {location.available} units</td>
                    <td className="px-3 py-3 text-right">
                      <Badge variant={location.available < 5 ? 'warning' : 'success'}>{location.available < 5 ? 'Review' : 'Active'}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-2">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Inventory Heatmap</h2>
            <div className="mt-4 grid gap-3">
              {totalsByLocation.map((location) => {
                const max = Math.max(...totalsByLocation.map((item) => item.available), 1);
                return (
                  <div key={location.id}>
                    <div className="flex justify-between text-xs text-ink/55">
                      <span>{location.name}</span>
                      <span>{location.available} available</span>
                    </div>
                    <div className="mt-1 h-2 rounded-full bg-ink/10">
                      <div className="h-full rounded-full bg-primary" style={{ width: `${Math.max(8, (location.available / max) * 100)}%` }} />
                    </div>
                  </div>
                );
              })}
            </div>
          </Card>
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Transfer Queue</h2>
            <ul className="mt-4 grid gap-2 text-sm text-ink/70">
              {totalsByLocation.slice(0, 4).map((location) => (
                <li key={location.id} className="flex justify-between rounded-md border border-primary/15 bg-ink/5 p-3">
                  <span>{location.name}</span>
                  <span>{location.reserved} reserved</span>
                </li>
              ))}
            </ul>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
