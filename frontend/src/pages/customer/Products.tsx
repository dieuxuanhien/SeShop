import { useState, useMemo } from 'react';
import { NavLink, useSearchParams } from 'react-router-dom';
import { Search, SlidersHorizontal, Star, X } from 'lucide-react';
import { useProducts, useCategories } from '@/features/catalog/model/catalogHooks';
import type { ProductListParams } from '@/features/catalog/api/catalogApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Pagination } from '@/shared/ui/Pagination';
import { ProductCardSkeleton } from '@/shared/ui/Skeleton';
import { EmptyState } from '@/shared/ui/EmptyState';

const sortOptions = [
  { label: 'Newest', value: 'newest' },
  { label: 'Price: Low → High', value: 'price_asc' },
  { label: 'Price: High → Low', value: 'price_desc' },
  { label: 'Most Popular', value: 'popular' },
] as const;

export function Products() {
  const [searchParams, setSearchParams] = useSearchParams();
  const [filtersOpen, setFiltersOpen] = useState(false);
  const [searchInput, setSearchInput] = useState(searchParams.get('q') ?? '');

  const params: ProductListParams = useMemo(
    () => ({
      page: Number(searchParams.get('page')) || 1,
      size: 9,
      sort: (searchParams.get('sort') as ProductListParams['sort']) ?? 'newest',
      search: searchParams.get('q') ?? undefined,
      categoryId: searchParams.get('cat') ? Number(searchParams.get('cat')) : undefined,
    }),
    [searchParams],
  );

  const { data, isLoading } = useProducts(params);
  const { data: categories } = useCategories();

  function updateParam(key: string, value: string | null) {
    setSearchParams((prev) => {
      const next = new URLSearchParams(prev);
      if (value) {
        next.set(key, value);
      } else {
        next.delete(key);
      }
      if (key !== 'page') next.delete('page');
      return next;
    });
  }

  function handleSearch(e: React.FormEvent) {
    e.preventDefault();
    updateParam('q', searchInput || null);
  }

  const activeCategory = categories?.find((c) => c.id === params.categoryId);

  return (
    <div className="min-h-screen">
      {/* Hero Banner */}
      <section className="relative py-20 lg:py-28 overflow-hidden">
        <div className="absolute inset-0 bg-gradient-to-b from-primary/5 via-transparent to-transparent" />
        <div className="relative mx-auto max-w-7xl px-6 lg:px-12 text-center">
          <span className="text-primary text-xs uppercase tracking-[0.3em] font-semibold mb-4 block">
            The Collection
          </span>
          <h1 className="font-display text-4xl md:text-5xl lg:text-6xl text-highlight mb-6">
            {activeCategory ? activeCategory.name : 'All Pieces'}
          </h1>
          <p className="text-surface/60 max-w-xl mx-auto text-sm leading-relaxed">
            Each piece is carefully selected for its craftsmanship, heritage, and timeless appeal.
          </p>
        </div>
      </section>

      {/* Controls Bar */}
      <div className="sticky top-20 z-30 border-y border-primary/10 bg-ink/95 backdrop-blur">
        <div className="mx-auto max-w-7xl px-6 lg:px-12 flex items-center justify-between gap-4 h-14">
          {/* Left: Search */}
          <form onSubmit={handleSearch} className="flex items-center gap-2 flex-1 max-w-sm">
            <div className="relative flex-1">
              <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-surface/40" />
              <input
                type="text"
                placeholder="Search collection…"
                value={searchInput}
                onChange={(e) => setSearchInput(e.target.value)}
                className="w-full rounded-md border border-primary/20 bg-transparent py-2 pl-9 pr-3 text-sm text-surface placeholder:text-surface/30 outline-none focus:border-primary focus:ring-1 focus:ring-primary/30 transition"
              />
              {searchInput && (
                <button type="button" onClick={() => { setSearchInput(''); updateParam('q', null); }} className="absolute right-2 top-1/2 -translate-y-1/2 text-surface/40 hover:text-surface">
                  <X size={14} />
                </button>
              )}
            </div>
          </form>

          {/* Center: Result count */}
          <span className="hidden md:block text-xs text-surface/50 tracking-wide">
            {data ? `${data.totalElements} pieces` : '—'}
          </span>

          {/* Right: Sort + Filter toggle */}
          <div className="flex items-center gap-3">
            <select
              value={params.sort ?? 'newest'}
              onChange={(e) => updateParam('sort', e.target.value)}
              className="appearance-none rounded-md border border-primary/20 bg-transparent px-3 py-2 pr-8 text-xs text-surface outline-none focus:border-primary transition cursor-pointer"
            >
              {sortOptions.map((opt) => (
                <option key={opt.value} value={opt.value} className="bg-ink">{opt.label}</option>
              ))}
            </select>
            <button
              onClick={() => setFiltersOpen(!filtersOpen)}
              className={`flex items-center gap-2 rounded-md border px-3 py-2 text-xs transition ${filtersOpen ? 'border-primary text-primary' : 'border-primary/20 text-surface/70 hover:border-primary/40'}`}
            >
              <SlidersHorizontal size={14} />
              <span className="hidden sm:inline">Filters</span>
            </button>
          </div>
        </div>
      </div>

      {/* Active Filters */}
      {(params.search || params.categoryId) && (
        <div className="mx-auto max-w-7xl px-6 lg:px-12 py-3 flex items-center gap-2 flex-wrap">
          <span className="text-xs text-surface/40">Active:</span>
          {params.search && (
            <button onClick={() => { setSearchInput(''); updateParam('q', null); }} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              "{params.search}" <X size={12} />
            </button>
          )}
          {activeCategory && (
            <button onClick={() => updateParam('cat', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              {activeCategory.name} <X size={12} />
            </button>
          )}
        </div>
      )}

      {/* Main Content */}
      <div className="mx-auto max-w-7xl px-6 lg:px-12 py-8">
        <div className={`grid gap-8 ${filtersOpen ? 'lg:grid-cols-[220px_1fr]' : ''}`}>

          {/* Filter Sidebar */}
          {filtersOpen && (
            <aside className="space-y-8 animate-fade-in">
              {/* Categories */}
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-4">Category</h3>
                <div className="space-y-2">
                  <button
                    onClick={() => updateParam('cat', null)}
                    className={`block w-full text-left text-sm px-2 py-1.5 rounded transition ${!params.categoryId ? 'text-primary font-medium' : 'text-surface/70 hover:text-surface'}`}
                  >
                    All
                  </button>
                  {categories?.map((cat) => (
                    <button
                      key={cat.id}
                      onClick={() => updateParam('cat', String(cat.id))}
                      className={`flex w-full items-center justify-between text-sm px-2 py-1.5 rounded transition ${params.categoryId === cat.id ? 'text-primary font-medium' : 'text-surface/70 hover:text-surface'}`}
                    >
                      <span>{cat.name}</span>
                      <span className="text-xs text-surface/30">{cat.productCount}</span>
                    </button>
                  ))}
                </div>
              </div>

              {/* Brands */}
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-4">Brand</h3>
                <div className="space-y-2">
                  {['Maison Sé', 'Rebel Heritage', 'Atelier Noir'].map((brand) => (
                    <button
                      key={brand}
                      onClick={() => updateParam('brand', params.brand === brand ? null : brand)}
                      className={`block w-full text-left text-sm px-2 py-1.5 rounded transition ${searchParams.get('brand') === brand ? 'text-primary font-medium' : 'text-surface/70 hover:text-surface'}`}
                    >
                      {brand}
                    </button>
                  ))}
                </div>
              </div>
            </aside>
          )}

          {/* Product Grid */}
          <div>
            {isLoading ? (
              <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-10">
                {Array.from({ length: 6 }).map((_, i) => (
                  <ProductCardSkeleton key={i} />
                ))}
              </div>
            ) : data && data.items.length > 0 ? (
              <>
                <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-10">
                  {data.items.map((product, idx) => (
                    <ProductCard key={product.id} product={product} index={idx} />
                  ))}
                </div>
                <div className="mt-12">
                  <Pagination
                    currentPage={data.page}
                    totalPages={data.totalPages}
                    onPageChange={(p) => updateParam('page', String(p))}
                  />
                </div>
              </>
            ) : (
              <EmptyState
                title="No pieces found"
                description="Try adjusting your search or filters to discover more."
              />
            )}
          </div>
        </div>
      </div>
    </div>
  );
}

// ── Product Card ────────────────────────────────────────────
function ProductCard({ product, index }: { product: import('@/entities/product/types').Product; index: number }) {
  const minPrice = Math.min(...product.variants.map((v) => v.price));
  const compareAt = product.variants[0]?.compareAtPrice;
  const hasDiscount = compareAt && compareAt > minPrice;
  const rating = product.reviewSummary;

  return (
    <NavLink
      to={`/products/${product.id}`}
      className="group block"
      style={{ animationDelay: `${index * 80}ms` }}
    >
      {/* Image */}
      <div className="relative aspect-[3/4] overflow-hidden bg-surface/5 mb-4">
        <img
          src={product.thumbnailUrl}
          alt={product.name}
          className="h-full w-full object-cover transition-transform duration-700 group-hover:scale-105"
          loading="lazy"
        />
        {/* Hover overlay */}
        <div className="absolute inset-0 bg-ink/0 group-hover:bg-ink/20 transition-colors duration-300 flex items-end justify-center pb-6 opacity-0 group-hover:opacity-100">
          <span className="bg-surface text-ink text-xs font-semibold uppercase tracking-widest px-6 py-2.5 translate-y-4 group-hover:translate-y-0 transition-transform duration-300">
            View Details
          </span>
        </div>
        {/* Sale badge */}
        {hasDiscount && (
          <Badge variant="sale" className="absolute top-3 left-3">Sale</Badge>
        )}
      </div>

      {/* Info */}
      <div className="space-y-1.5">
        <p className="text-xs uppercase tracking-widest text-primary/70">{product.brand}</p>
        <h3 className="text-sm font-medium text-surface group-hover:text-highlight transition-colors leading-snug">
          {product.name}
        </h3>
        <div className="flex items-center gap-2">
          <span className="text-sm font-semibold text-highlight">{formatCurrency(minPrice)}</span>
          {hasDiscount && (
            <span className="text-xs text-surface/40 line-through">{formatCurrency(compareAt)}</span>
          )}
        </div>
        {rating && rating.reviewCount > 0 && (
          <div className="flex items-center gap-1.5 text-xs text-surface/50">
            <Star size={12} className="fill-primary text-primary" />
            <span>{rating.averageRating.toFixed(1)}</span>
            <span>({rating.reviewCount})</span>
          </div>
        )}
      </div>
    </NavLink>
  );
}
