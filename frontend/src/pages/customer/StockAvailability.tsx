import { useParams, NavLink } from 'react-router-dom';
import { ChevronRight, MapPin } from 'lucide-react';
import { useProduct, useProductAvailability } from '@/features/catalog/model/catalogHooks';
import { Badge } from '@/shared/ui/Badge';
import { Skeleton } from '@/shared/ui/Skeleton';

export function StockAvailability() {
  const { productId } = useParams<{ productId: string }>();
  const id = Number(productId);
  const { data: product } = useProduct(id);
  const { data: availability, isLoading } = useProductAvailability(id);

  return (
    <div className="min-h-screen">
      {/* Breadcrumbs */}
      <nav className="mx-auto max-w-7xl px-6 lg:px-12 py-4 flex items-center gap-2 text-xs text-surface/50">
        <NavLink to="/" className="hover:text-primary transition-colors">Home</NavLink>
        <ChevronRight size={12} />
        <NavLink to="/products" className="hover:text-primary transition-colors">Collection</NavLink>
        <ChevronRight size={12} />
        {product && (
          <>
            <NavLink to={`/products/${product.id}`} className="hover:text-primary transition-colors">{product.name}</NavLink>
            <ChevronRight size={12} />
          </>
        )}
        <span className="text-surface/80">Store Availability</span>
      </nav>

      <div className="mx-auto max-w-3xl px-6 lg:px-12 py-8">
        {/* Header */}
        <div className="text-center mb-10">
          <MapPin className="mx-auto text-primary mb-4" size={32} strokeWidth={1.5} />
          <h1 className="font-display text-3xl text-highlight mb-2">Store Availability</h1>
          {product && (
            <p className="text-surface/60 text-sm">
              Check where <span className="text-surface/90 font-medium">{product.name}</span> is available
            </p>
          )}
        </div>

        {/* Availability Table */}
        {isLoading ? (
          <div className="space-y-3">
            {[1, 2, 3].map((i) => (
              <Skeleton key={i} className="h-16 w-full" />
            ))}
          </div>
        ) : availability && availability.length > 0 ? (
          <div className="space-y-3">
            {availability.map((loc) => {
              const variant =
                loc.availableQty === 0 ? 'danger' : loc.availableQty <= 3 ? 'warning' : 'success';
              const label =
                loc.availableQty === 0 ? 'Out of Stock' : loc.availableQty <= 3 ? 'Low Stock' : 'In Stock';

              return (
                <div
                  key={loc.locationId}
                  className="flex items-center justify-between rounded-lg border border-primary/10 bg-surface/[0.02] px-5 py-4 transition hover:border-primary/20"
                >
                  <div className="flex items-center gap-4">
                    <div className="flex h-10 w-10 items-center justify-center rounded-full bg-primary/10 text-primary">
                      <MapPin size={18} />
                    </div>
                    <div>
                      <p className="text-sm font-medium text-surface">{loc.locationName}</p>
                      <p className="text-xs text-surface/40">
                        {loc.availableQty} {loc.availableQty === 1 ? 'unit' : 'units'} available
                      </p>
                    </div>
                  </div>
                  <Badge variant={variant}>{label}</Badge>
                </div>
              );
            })}
          </div>
        ) : (
          <p className="text-center text-surface/50 py-12">No availability data found.</p>
        )}

        {/* Back Link */}
        {product && (
          <div className="mt-10 text-center">
            <NavLink
              to={`/products/${product.id}`}
              className="inline-flex items-center gap-2 text-sm text-primary hover:underline"
            >
              ← Back to {product.name}
            </NavLink>
          </div>
        )}
      </div>
    </div>
  );
}
