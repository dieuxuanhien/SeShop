import { useState, useMemo } from 'react';
import { NavLink, useSearchParams } from 'react-router-dom';
import { Search, SlidersHorizontal, Star, X } from 'lucide-react';
import { useProducts, useCategories, useBrands } from '@/features/catalog/model/catalogHooks';
import type { ProductListParams } from '@/features/catalog/api/catalogApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Pagination } from '@/shared/ui/Pagination';
import { ProductCardSkeleton } from '@/shared/ui/Skeleton';
import { EmptyState } from '@/shared/ui/EmptyState';
import { ProductCard } from '@/entities/product/ui/ProductCard';

const sortOptions = [
  { label: 'Newest', value: 'newest' },
  { label: 'Price: Low to High', value: 'price_asc' },
  { label: 'Price: High to Low', value: 'price_desc' },
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
      brand: searchParams.get('brand') ?? undefined,
      minPrice: searchParams.get('minPrice') ? Number(searchParams.get('minPrice')) : undefined,
      maxPrice: searchParams.get('maxPrice') ? Number(searchParams.get('maxPrice')) : undefined,
      productSize: searchParams.get('size') ?? undefined,
      color: searchParams.get('color') ?? undefined,
    }),
    [searchParams],
  );

  const { data, isLoading } = useProducts(params);
  const { data: categories } = useCategories();
  const { data: brands = [] } = useBrands();

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
                placeholder="Search collection..."
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
            {data ? `${data.totalElements} pieces` : 'Loading'}
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
      {(params.search || params.categoryId || params.brand || params.minPrice || params.maxPrice || params.productSize || params.color) && (
        <div className="mx-auto max-w-7xl px-6 lg:px-12 py-3 flex items-center gap-2 flex-wrap">
          <span className="text-xs text-surface/40">Active:</span>
          {params.search && (
            <button onClick={() => { setSearchInput(''); updateParam('q', null); }} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              {params.search} <X size={12} />
            </button>
          )}
          {activeCategory && (
            <button onClick={() => updateParam('cat', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              {activeCategory.name} <X size={12} />
            </button>
          )}
          {params.brand && (
            <button onClick={() => updateParam('brand', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              {params.brand} <X size={12} />
            </button>
          )}
          {params.minPrice && (
            <button onClick={() => updateParam('minPrice', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              Min {params.minPrice?.toLocaleString()}₫ <X size={12} />
            </button>
          )}
          {params.maxPrice && (
            <button onClick={() => updateParam('maxPrice', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              Max {params.maxPrice?.toLocaleString()}₫ <X size={12} />
            </button>
          )}
          {params.productSize && (
            <button onClick={() => updateParam('size', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              Size: {params.productSize} <X size={12} />
            </button>
          )}
          {params.color && (
            <button onClick={() => updateParam('color', null)} className="inline-flex items-center gap-1.5 rounded-full border border-primary/20 px-3 py-1 text-xs text-surface/80 hover:border-danger/40 hover:text-danger transition">
              Color: {params.color} <X size={12} />
            </button>
          )}
          <button
            onClick={() => {
              setSearchInput('');
              setSearchParams(new URLSearchParams());
            }}
            className="text-xs text-danger/70 hover:text-danger ml-2 transition underline underline-offset-2"
          >
            Clear All
          </button>
        </div>
      )}

      {/* Main Content */}
      <div className="mx-auto max-w-7xl px-6 lg:px-12 py-8">
        <div className={`grid gap-8 items-start transition-[grid-template-columns] duration-500 ease-in-out ${filtersOpen ? 'lg:grid-cols-[220px_1fr]' : 'lg:grid-cols-1'}`}>

          {/* Filter Sidebar */}
          {filtersOpen && (
            <aside className="space-y-8 sticky top-36 animate-in fade-in zoom-in-95 slide-in-from-top-4 duration-500 ease-out">
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
                  {brands.map((brand) => (
                    <button
                      key={brand}
                      onClick={() => updateParam('brand', params.brand === brand ? null : brand)}
                      className={`block w-full text-left text-sm px-2 py-1.5 rounded transition ${searchParams.get('brand') === brand ? 'text-primary font-medium' : 'text-surface/70 hover:text-surface'}`}
                    >
                      {brand}
                    </button>
                  ))}
                  {brands.length === 0 && (
                    <p className="text-sm text-surface/50 px-2">No brands available</p>
                  )}
                </div>
              </div>
              {/* Price Range */}
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-4">Price Range</h3>
                <div className="flex items-center gap-2">
                  <div className="relative flex-1">
                    <span className="absolute left-2 top-1/2 -translate-y-1/2 text-surface/40 text-xs">₫</span>
                    <input
                      type="number"
                      placeholder="Min"
                      defaultValue={params.minPrice || ''}
                      onBlur={(e) => updateParam('minPrice', e.target.value ? String(Math.max(0, Number(e.target.value))) : null)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.currentTarget.blur();
                        }
                      }}
                      className="w-full rounded border border-primary/20 bg-transparent py-1.5 pl-6 pr-2 text-sm text-surface outline-none focus:border-primary transition"
                    />
                  </div>
                  <span className="text-surface/40">-</span>
                  <div className="relative flex-1">
                    <span className="absolute left-2 top-1/2 -translate-y-1/2 text-surface/40 text-xs">₫</span>
                    <input
                      type="number"
                      placeholder="Max"
                      defaultValue={params.maxPrice || ''}
                      onBlur={(e) => updateParam('maxPrice', e.target.value ? String(Math.max(0, Number(e.target.value))) : null)}
                      onKeyDown={(e) => {
                        if (e.key === 'Enter') {
                          e.currentTarget.blur();
                        }
                      }}
                      className="w-full rounded border border-primary/20 bg-transparent py-1.5 pl-6 pr-2 text-sm text-surface outline-none focus:border-primary transition"
                    />
                  </div>
                </div>
              </div>

              {/* Size */}
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-4">Size</h3>
                <div className="flex flex-wrap gap-2">
                  {['S', 'M', 'L', 'XL', 'OS'].map((size) => (
                    <button
                      key={size}
                      onClick={() => updateParam('size', params.productSize === size ? null : size)}
                      className={`min-w-[2.5rem] rounded border px-2 py-1 text-xs transition ${params.productSize === size ? 'border-primary bg-primary/10 text-primary font-medium' : 'border-primary/20 text-surface/70 hover:border-primary/40'}`}
                    >
                      {size}
                    </button>
                  ))}
                </div>
              </div>

              {/* Color */}
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-4">Color</h3>
                <div className="flex flex-wrap gap-2">
                  {['Black', 'White', 'Beige', 'Navy', 'Grey'].map((color) => (
                    <button
                      key={color}
                      onClick={() => updateParam('color', params.color === color ? null : color)}
                      className={`rounded-full border px-3 py-1 text-xs transition ${params.color === color ? 'border-primary bg-primary/10 text-primary font-medium' : 'border-primary/20 text-surface/70 hover:border-primary/40'}`}
                    >
                      {color}
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

