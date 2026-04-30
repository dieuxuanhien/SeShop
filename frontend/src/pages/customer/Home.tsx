import { ShoppingBag } from 'lucide-react';
import { Button } from '@/shared/ui/Button';
import { PageScaffold } from '@/shared/ui/PageScaffold';

export function Home() {
  return (
    <PageScaffold
      title="Homepage & Product Browsing"
      viewCode="CUST_001"
      purpose="Customer storefront shell for featured products, categories, search, filters, and stock-aware browsing."
      endpoints={['GET /products', 'GET /categories']}
    >
      <div className="grid gap-4 md:grid-cols-3">
        {['Featured products', 'Categories', 'Promotions'].map((label) => (
          <div key={label} className="rounded-md border border-slate-200 bg-surface p-4">
            <h2 className="font-semibold text-ink">{label}</h2>
            <p className="mt-2 text-sm text-slate-600">Ready for API-backed content in Phase 2.</p>
          </div>
        ))}
      </div>
      <Button className="mt-5" icon={<ShoppingBag size={16} />}>
        Browse products
      </Button>
    </PageScaffold>
  );
}
