import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { locationsFromBalances, type LocationSummary } from '@/features/admin/api/adminApi';
import { getInventoryBalances } from '@/features/staff/api/staffInventoryApi';

export function LocationsManagement() {
  const [locations, setLocations] = useState<LocationSummary[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getInventoryBalances(1, 100)
      .then((page) => setLocations(locationsFromBalances(page.items)))
      .catch(() => setLocations([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="Locations Management"
      viewCode="ADMIN_003"
      purpose="Store and storage location configuration shell for inventory operations and location status."
      endpoints={['GET /staff/inventory/balances']}
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Location Directory</h2>
              <p className="mt-1 text-xs text-ink/50">Monitor store and storage hubs with inventory snapshots.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary">Add Location</Button>
              <Button variant="secondary">Assign Staff</Button>
            </div>
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
                ) : locations.length === 0 ? (
                  <tr>
                    <td colSpan={4} className="px-3 py-6 text-center text-sm text-ink/60">No locations returned by inventory balances.</td>
                  </tr>
                ) : locations.map((location) => (
                  <tr key={location.id} className="text-ink/80">
                    <td className="px-3 py-3 font-semibold text-ink">LOC-{location.id}</td>
                    <td className="px-3 py-3">{location.name}</td>
                    <td className="px-3 py-3 text-right">{location.skus}</td>
                    <td className="px-3 py-3 text-right">
                      <Button variant="secondary">View</Button>
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
            <div className="mt-4 h-40 rounded-md border border-dashed border-primary/30 bg-ink/5" />
          </Card>
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Transfer Queue</h2>
            <ul className="mt-4 grid gap-2 text-sm text-ink/70">
              <li>Transfer data is available from the stock transfer view.</li>
            </ul>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
