import { useEffect, useMemo, useState } from 'react';
import { PackageSearch, Search } from 'lucide-react';
import { getStaffProducts } from '@/features/catalog/api/catalogApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Card } from '@/shared/ui/Card';
import { EmptyState } from '@/shared/ui/EmptyState';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Spinner } from '@/shared/ui/Spinner';

type VariantStockRow = {
  productId: number;
  productName: string;
  brand?: string;
  variantId: number;
  skuCode: string;
  price: number;
  status: string;
  totalOnHand: number;
  totalReserved: number;
  totalAvailable: number;
  locationCount: number;
};

export function CatalogStockManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [search, setSearch] = useState('');
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    Promise.all([
      getStaffProducts({ page: 1, size: 200 }),
      getInventoryBalances(1, 500),
    ])
      .then(([productPage, balancePage]) => {
        setProducts(productPage.items);
        setBalances(balancePage.items);
      })
      .catch(() => {
        setProducts([]);
        setBalances([]);
      })
      .finally(() => setIsLoading(false));
  }, []);

  const stockRows = useMemo<VariantStockRow[]>(() => {
    const byVariant = new Map<number, InventoryBalance[]>();
    balances.forEach((balance) => {
      byVariant.set(balance.variantId, [...(byVariant.get(balance.variantId) ?? []), balance]);
    });

    return products.flatMap((product) =>
      product.variants.map((variant) => {
        const variantBalances = byVariant.get(variant.id) ?? [];
        return {
          productId: product.id,
          productName: product.name,
          brand: product.brand,
          variantId: variant.id,
          skuCode: variant.skuCode,
          price: Number(variant.price),
          status: variant.status,
          totalOnHand: variantBalances.reduce((sum, balance) => sum + Number(balance.onHandQty ?? 0), 0),
          totalReserved: variantBalances.reduce((sum, balance) => sum + Number(balance.reservedQty ?? 0), 0),
          totalAvailable: variantBalances.reduce((sum, balance) => sum + Number(balance.availableQty ?? 0), 0),
          locationCount: variantBalances.length,
        };
      }),
    );
  }, [balances, products]);

  const filteredRows = useMemo(() => {
    const keyword = search.trim().toLowerCase();
    if (!keyword) return stockRows;
    return stockRows.filter((row) =>
      [row.productName, row.brand, row.skuCode]
        .filter(Boolean)
        .join(' ')
        .toLowerCase()
        .includes(keyword),
    );
  }, [search, stockRows]);

  if (isLoading) {
    return (
      <PageScaffold title="Catalog & Stock" viewCode="ADMIN_CATALOG_STOCK" purpose="Manage product master data and all-location stock positions.">
        <div className="flex h-64 items-center justify-center">
          <Spinner size="lg" />
        </div>
      </PageScaffold>
    );
  }

  return (
    <PageScaffold title="Catalog & Stock" viewCode="ADMIN_CATALOG_STOCK" purpose="Manage product master data and all-location stock positions.">
      <div className="grid gap-5">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Global SKU Stock</h2>
              <p className="mt-1 text-xs text-ink/50">{filteredRows.length} variants across {products.length} products.</p>
            </div>
            <label className="relative w-full sm:w-80">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" />
              <input
                value={search}
                onChange={(event) => setSearch(event.target.value)}
                placeholder="Search products or SKUs"
                className="min-h-10 w-full rounded-md border border-primary/25 bg-surface pl-9 pr-3 text-sm text-ink outline-none focus:border-primary focus:ring-2 focus:ring-primary/20"
              />
            </label>
          </div>

          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="bg-ink/[0.03] text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-4 py-3">Product</th>
                  <th className="px-4 py-3">SKU</th>
                  <th className="px-4 py-3 text-right">Price</th>
                  <th className="px-4 py-3 text-right">On Hand</th>
                  <th className="px-4 py-3 text-right">Reserved</th>
                  <th className="px-4 py-3 text-right">Available</th>
                  <th className="px-4 py-3 text-right">Locations</th>
                  <th className="px-4 py-3">Status</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {filteredRows.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="px-4 py-10">
                      <EmptyState title="No SKU stock found" description="Adjust the search term or create catalog variants." />
                    </td>
                  </tr>
                ) : filteredRows.map((row) => (
                  <tr key={`${row.productId}-${row.variantId}`} className="text-ink/80">
                    <td className="px-4 py-3">
                      <p className="font-semibold text-ink">{row.productName}</p>
                      <p className="text-xs text-ink/50">{row.brand || 'Unbranded'}</p>
                    </td>
                    <td className="px-4 py-3 font-medium text-ink">{row.skuCode}</td>
                    <td className="px-4 py-3 text-right">{formatCurrency(row.price)}</td>
                    <td className="px-4 py-3 text-right">{row.totalOnHand}</td>
                    <td className="px-4 py-3 text-right text-warning">{row.totalReserved}</td>
                    <td className="px-4 py-3 text-right text-success">{row.totalAvailable}</td>
                    <td className="px-4 py-3 text-right">{row.locationCount}</td>
                    <td className="px-4 py-3">
                      <Badge variant={row.status === 'ACTIVE' ? 'success' : 'warning'}>{row.status}</Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
