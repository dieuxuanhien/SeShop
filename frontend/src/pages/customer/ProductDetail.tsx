import { useEffect, useState } from 'react';
import { useParams, NavLink } from 'react-router-dom';
import { ChevronRight, Heart, MapPin, Minus, Plus, ShoppingBag, Star, Truck } from 'lucide-react';
import { useProduct } from '@/features/catalog/model/catalogHooks';
import { useCartStore } from '@/features/cart/model/cartStore';
import { addCartItem } from '@/features/commerce/api/cartApi';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Modal } from '@/shared/ui/Modal';
import { Skeleton } from '@/shared/ui/Skeleton';
import type { ProductVariant } from '@/entities/product/types';

type Tab = 'description' | 'shipping' | 'reviews';

export function ProductDetail() {
  const { productId } = useParams<{ productId: string }>();
  const { data: product, isLoading } = useProduct(Number(productId));
  const setCartItems = useCartStore((s) => s.setItems);

  const [selectedSize, setSelectedSize] = useState<string | null>(null);
  const [selectedColor, setSelectedColor] = useState<string | null>(null);
  const [qty, setQty] = useState(1);
  const [activeTab, setActiveTab] = useState<Tab>('description');
  const [lightboxUrl, setLightboxUrl] = useState<string | null>(null);
  const [activeImageIdx, setActiveImageIdx] = useState(0);
  const [addedFeedback, setAddedFeedback] = useState(false);
  const [cartError, setCartError] = useState('');
  const [isAdding, setIsAdding] = useState(false);

  const sizes = product ? [...new Set(product.variants.filter((v) => v.size).map((v) => v.size!))] : [];
  const colorOptions = product ? product.variants.reduce<Record<string, string>>((acc, v) => {
    if (v.color && v.colorHex && !acc[v.color]) acc[v.color] = v.colorHex;
    return acc;
  }, {}) : {};
  const colorKeys = Object.keys(colorOptions);

  useEffect(() => {
    if (!selectedSize && sizes.length > 0) setSelectedSize(sizes[0]);
    if (!selectedColor && colorKeys.length > 0) setSelectedColor(colorKeys[0]);
  }, [colorKeys, selectedColor, selectedSize, sizes]);

  if (isLoading) return <ProductDetailSkeleton />;
  if (!product) return <div className="py-20 text-center text-surface/50">Product not found.</div>;

  // Find matching variant
  const matchedVariant: ProductVariant | undefined = product.variants.find(
    (v) => (sizes.length === 0 || v.size === selectedSize) && (colorKeys.length === 0 || v.color === selectedColor),
  );
  const displayPrice = matchedVariant?.price ?? product.variants[0]?.price ?? 0;
  const compareAt = matchedVariant?.compareAtPrice ?? product.variants[0]?.compareAtPrice;
  const hasDiscount = compareAt && compareAt > displayPrice;

  async function handleAddToCart() {
    if (!matchedVariant) return;
    setIsAdding(true);
    setCartError('');
    try {
      const cart = await addCartItem(matchedVariant.id, qty);
      setCartItems(cart.items.map((item) => ({
        id: item.id,
        variantId: item.variantId,
        skuCode: item.skuCode,
        name: item.name,
        qty: item.qty,
        unitPrice: Number(item.unitPrice),
      })));
      setAddedFeedback(true);
      setTimeout(() => setAddedFeedback(false), 2000);
    } catch {
      setCartError('Sign in to add this item to your cart.');
    } finally {
      setIsAdding(false);
    }
  }

  const rating = product.reviewSummary;
  const heroImage = product.images[activeImageIdx]?.url ?? product.thumbnailUrl;

  return (
    <div className="min-h-screen">
      {/* Breadcrumbs */}
      <nav className="mx-auto max-w-7xl px-6 lg:px-12 py-4 flex items-center gap-2 text-xs text-surface/50">
        <NavLink to="/" className="hover:text-primary transition-colors">Home</NavLink>
        <ChevronRight size={12} />
        <NavLink to="/products" className="hover:text-primary transition-colors">Collection</NavLink>
        <ChevronRight size={12} />
        <span className="text-surface/80">{product.name}</span>
      </nav>

      {/* Main Grid */}
      <div className="mx-auto max-w-7xl px-6 lg:px-12 py-4 lg:py-8">
        <div className="grid lg:grid-cols-[1fr_420px] gap-10 lg:gap-16">
          {/* Left: Images */}
          <div className="space-y-4">
            {/* Hero Image */}
            <button
              onClick={() => setLightboxUrl(heroImage ?? null)}
              className="relative aspect-[3/4] w-full overflow-hidden bg-surface/5 cursor-zoom-in group"
            >
              {heroImage ? (
                <img
                  src={heroImage}
                  alt={product.name}
                  className="h-full w-full object-cover transition-transform duration-500 group-hover:scale-105"
                />
              ) : (
                <div className="flex h-full w-full items-center justify-center text-xs uppercase tracking-widest text-surface/40">
                  No image
                </div>
              )}
              {hasDiscount && (
                <Badge variant="sale" className="absolute top-4 left-4">Sale</Badge>
              )}
            </button>

            {/* Thumbnails */}
            {product.images.length > 1 && (
              <div className="flex gap-3 overflow-x-auto pb-2">
                {product.images.map((img, idx) => (
                  <button
                    key={img.id}
                    onClick={() => setActiveImageIdx(idx)}
                    className={`relative w-20 h-24 flex-shrink-0 overflow-hidden transition ${
                      idx === activeImageIdx
                        ? 'ring-2 ring-primary'
                        : 'opacity-60 hover:opacity-100'
                    }`}
                  >
                    <img src={img.url} alt={img.altText ?? ''} className="h-full w-full object-cover" />
                  </button>
                ))}
              </div>
            )}
          </div>

          {/* Right: Product Info */}
          <div className="lg:sticky lg:top-28 lg:self-start space-y-6">
            {/* Brand & Name */}
            <div>
              <p className="text-xs uppercase tracking-[0.3em] text-primary font-semibold mb-2">
                {product.brand}
              </p>
              <h1 className="font-display text-3xl lg:text-4xl text-highlight leading-tight">
                {product.name}
              </h1>
            </div>

            {/* Rating */}
            {rating && rating.reviewCount > 0 && (
              <div className="flex items-center gap-2">
                <div className="flex items-center gap-0.5">
                  {[1, 2, 3, 4, 5].map((star) => (
                    <Star
                      key={star}
                      size={14}
                      className={star <= Math.round(rating.averageRating) ? 'fill-primary text-primary' : 'text-surface/20'}
                    />
                  ))}
                </div>
                <span className="text-sm text-surface/60">
                  {rating.averageRating.toFixed(1)} ({rating.reviewCount} reviews)
                </span>
              </div>
            )}

            {/* Price */}
            <div className="flex items-baseline gap-3">
              <span className="text-2xl font-semibold text-highlight">{formatCurrency(displayPrice)}</span>
              {hasDiscount && (
                <span className="text-base text-surface/40 line-through">{formatCurrency(compareAt)}</span>
              )}
            </div>

            <div className="h-px bg-primary/10" />

            {/* Size Selector */}
            {sizes.length > 0 && (
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-3">
                  Size - <span className="text-surface/80">{selectedSize}</span>
                </h3>
                <div className="flex flex-wrap gap-2">
                  {sizes.map((size) => (
                    <button
                      key={size}
                      onClick={() => setSelectedSize(size)}
                      className={`min-w-[44px] rounded-md border px-4 py-2.5 text-sm font-medium transition ${
                        selectedSize === size
                          ? 'border-primary bg-primary/10 text-primary'
                          : 'border-primary/20 text-surface/70 hover:border-primary/40'
                      }`}
                    >
                      {size}
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Color Selector */}
            {colorKeys.length > 0 && (
              <div>
                <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-3">
                  Color - <span className="text-surface/80">{selectedColor}</span>
                </h3>
                <div className="flex flex-wrap gap-3">
                  {colorKeys.map((color) => (
                    <button
                      key={color}
                      onClick={() => setSelectedColor(color)}
                      className={`w-9 h-9 rounded-full border-2 transition ${
                        selectedColor === color ? 'border-primary scale-110' : 'border-transparent hover:scale-105'
                      }`}
                      title={color}
                    >
                      <span
                        className="block w-full h-full rounded-full"
                        style={{ backgroundColor: colorOptions[color] }}
                      />
                    </button>
                  ))}
                </div>
              </div>
            )}

            {/* Quantity */}
            <div>
              <h3 className="text-xs font-semibold uppercase tracking-widest text-surface/60 mb-3">Quantity</h3>
              <div className="inline-flex items-center rounded-md border border-primary/20">
                <button
                  onClick={() => setQty(Math.max(1, qty - 1))}
                  className="px-3 py-2.5 text-surface/70 hover:text-surface transition"
                  aria-label="Decrease quantity"
                >
                  <Minus size={14} />
                </button>
                <span className="min-w-[40px] text-center text-sm font-medium text-surface">{qty}</span>
                <button
                  onClick={() => setQty(qty + 1)}
                  className="px-3 py-2.5 text-surface/70 hover:text-surface transition"
                  aria-label="Increase quantity"
                >
                  <Plus size={14} />
                </button>
              </div>
            </div>

            {/* Actions */}
            <div className="flex gap-3">
              <Button
                onClick={handleAddToCart}
                disabled={!matchedVariant || isAdding}
                isLoading={isAdding}
                icon={<ShoppingBag size={16} />}
                className="flex-1"
              >
                {addedFeedback ? '✓ Added to Cart' : 'Add to Cart'}
              </Button>
              <button
                className="rounded-md border border-primary/20 px-4 text-surface/60 hover:text-primary hover:border-primary/40 transition"
                aria-label="Add to wishlist"
              >
                <Heart size={18} />
              </button>
            </div>
            {cartError && <p className="text-sm text-danger">{cartError}</p>}

            {/* Shipping & Availability Quick Info */}
            <div className="space-y-3 pt-2">
              <div className="flex items-center gap-3 text-sm text-surface/60">
                <Truck size={16} className="text-primary/70" />
                <span>Complimentary shipping on orders over 2.000.000 VND</span>
              </div>
              <NavLink
                to={`/products/${product.id}/availability`}
                className="flex items-center gap-3 text-sm text-primary hover:underline"
              >
                <MapPin size={16} />
                Check in-store availability
              </NavLink>
            </div>
          </div>
        </div>

        {/* Tabs */}
        <div className="mt-16 border-t border-primary/10 pt-8">
          <div className="flex gap-8 border-b border-primary/10 mb-8">
            {([
              ['description', 'Details'],
              ['shipping', 'Shipping & Care'],
              ['reviews', 'Reviews'],
            ] as const).map(([key, label]) => (
              <button
                key={key}
                onClick={() => setActiveTab(key)}
                className={`pb-3 text-sm uppercase tracking-widest transition border-b-2 -mb-px ${
                  activeTab === key
                    ? 'border-primary text-primary font-semibold'
                    : 'border-transparent text-surface/50 hover:text-surface/80'
                }`}
              >
                {label}
              </button>
            ))}
          </div>

          <div className="max-w-3xl animate-fade-in">
            {activeTab === 'description' && (
              <div className="prose prose-invert text-surface/80 text-sm leading-relaxed">
                <p>{product.description}</p>
                {product.categories.length > 0 && (
                  <div className="mt-6 flex items-center gap-2">
                    <span className="text-xs text-surface/40">Categories:</span>
                    {product.categories.map((cat) => (
                      <NavLink key={cat.id} to={`/products?cat=${cat.id}`} className="text-xs text-primary hover:underline">
                        {cat.name}
                      </NavLink>
                    ))}
                  </div>
                )}
              </div>
            )}
            {activeTab === 'shipping' && (
              <div className="space-y-4 text-sm text-surface/70">
                <p>Standard shipping: 3-5 business days</p>
                <p>Express shipping: 1-2 business days</p>
                <p>Free shipping on orders over 2.000.000 VND</p>
                <p>Returns accepted within 30 days of purchase</p>
                <p>Please refer to the care label attached to the garment for specific care instructions.</p>
              </div>
            )}
            {activeTab === 'reviews' && (
              <NavLink to={`/products/${product.id}/reviews`} className="text-sm text-primary hover:underline">
                Read and write reviews for this product.
              </NavLink>
            )}
          </div>
        </div>
      </div>

      {/* Image Lightbox */}
      <Modal open={!!lightboxUrl} onClose={() => setLightboxUrl(null)} className="max-w-4xl p-0">
        {lightboxUrl && (
          <img src={lightboxUrl} alt={product.name} className="w-full h-auto" />
        )}
      </Modal>
    </div>
  );
}

// ── Loading Skeleton ────────────────────────────────────────
function ProductDetailSkeleton() {
  return (
    <div className="mx-auto max-w-7xl px-6 lg:px-12 py-12">
      <div className="grid lg:grid-cols-[1fr_420px] gap-16">
        <Skeleton className="aspect-[3/4] w-full" />
        <div className="space-y-6">
          <Skeleton className="h-4 w-24" />
          <Skeleton className="h-10 w-3/4" />
          <Skeleton className="h-6 w-32" />
          <Skeleton className="h-px w-full" />
          <Skeleton className="h-12 w-48" />
          <Skeleton className="h-12 w-48" />
          <Skeleton className="h-14 w-full" />
        </div>
      </div>
    </div>
  );
}
