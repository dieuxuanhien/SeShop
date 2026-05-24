import { useEffect, useMemo, useState } from 'react';
import { ChevronLeft, ChevronRight, Search } from 'lucide-react';
import { getStaffProducts } from '@/features/catalog/api/catalogApi';
import { getInventoryBalances, type InventoryBalance } from '@/features/staff/api/staffInventoryApi';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
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

const catalogStockPageSize = 10;

export function CatalogStockManagement() {
  const [products, setProducts] = useState<Product[]>([]);
  const [balances, setBalances] = useState<InventoryBalance[]>([]);
  const [search, setSearch] = useState('');
  const [currentPage, setCurrentPage] = useState(1);
  const [pageInfo, setPageInfo] = useState({
    page: 1,
    size: catalogStockPageSize,
    totalElements: 0,
    totalPages: 1,
  });
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    let active = true;

    async function loadStockPage() {
      setIsLoading(true);
      try {
        const productPage = await getStaffProducts({
          page: currentPage,
          size: catalogStockPageSize,
          search,
        });
        const variants = productPage.items.flatMap((product) => product.variants);
        const balancePages = await Promise.all(
          variants.map((variant) =>
            getInventoryBalances({
              page: 1,
              size: 100,
              variantId: variant.id,
            }),
          ),
        );

        if (!active) return;
        setProducts(productPage.items);
        setBalances(balancePages.flatMap((page) => page.items));
        setPageInfo({
          page: productPage.page,
          size: productPage.size,
          totalElements: productPage.totalElements,
          totalPages: Math.max(productPage.totalPages, 1),
        });
      } catch {
        if (!active) return;
        setProducts([]);
        setBalances([]);
        setPageInfo({
          page: currentPage,
          size: catalogStockPageSize,
          totalElements: 0,
          totalPages: 1,
        });
      } finally {
        if (active) setIsLoading(false);
      }
    }

    loadStockPage();
    return () => {
      active = false;
    };
  }, [currentPage, search]);

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
              <p className="mt-1 text-xs text-ink/50">
                {stockRows.length} variants on page {pageInfo.page} of {pageInfo.totalPages}, {pageInfo.totalElements} products total.
              </p>
            </div>
            <label className="relative w-full sm:w-80">
              <Search size={15} className="absolute left-3 top-1/2 -translate-y-1/2 text-ink/40" />
              <input
                value={search}
                onChange={(event) => {
                  setSearch(event.target.value);
                  setCurrentPage(1);
                }}
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
                {stockRows.length === 0 ? (
                  <tr>
                    <td colSpan={8} className="px-4 py-10">
                      <EmptyState title="No SKU stock found" description="Adjust the search term or create catalog variants." />
                    </td>
                  </tr>
                ) : stockRows.map((row) => (
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
          <div className="mt-4 flex flex-wrap items-center justify-between gap-3 border-t border-primary/10 pt-4">
            <p className="text-xs text-ink/50">
              Showing page {pageInfo.page} of {pageInfo.totalPages}
            </p>
            <div className="flex items-center gap-2">
              <Button
                type="button"
                variant="secondary"
                size="sm"
                icon={<ChevronLeft size={15} />}
                disabled={currentPage <= 1}
                onClick={() => setCurrentPage((page) => Math.max(1, page - 1))}
              >
                Previous
              </Button>
              <Button
                type="button"
                variant="secondary"
                size="sm"
                icon={<ChevronRight size={15} />}
                disabled={currentPage >= pageInfo.totalPages}
                onClick={() => setCurrentPage((page) => Math.min(pageInfo.totalPages, page + 1))}
              >
                Next
              </Button>
            </div>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
