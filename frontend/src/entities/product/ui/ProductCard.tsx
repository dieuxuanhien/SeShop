import { NavLink, useNavigate } from 'react-router-dom';
import { Star, ShoppingCart } from 'lucide-react';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { useCartStore } from '@/features/cart/model/cartStore';

type ProductCardProps = {
  product: Product;
  index: number;
};

export function ProductCard({ product, index }: ProductCardProps) {
  const minPrice = Math.min(...product.variants.map((v) => v.price));
  const compareAt = product.variants[0]?.compareAtPrice;
  const hasDiscount = compareAt && compareAt > minPrice;
  const rating = product.reviewSummary;
  const addItem = useCartStore((s) => s.addItem);
  const navigate = useNavigate();

  const handleQuickAdd = (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    if (product.variants.length === 1) {
      const variant = product.variants[0];
      addItem({
        variantId: variant.id,
        skuCode: variant.skuCode,
        name: product.name,
        attributes: variant.attributes,
        imageUrl: product.thumbnailUrl,
        qty: 1,
        unitPrice: variant.price,
      });
    } else {
      navigate(`/products/${product.id}`);
    }
  };

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
        {!product.thumbnailUrl && (
          <div className="absolute inset-0 flex items-center justify-center bg-surface/10 text-xs uppercase tracking-widest text-surface/40">
            No image
          </div>
        )}
        {/* Hover overlay */}
        <div className="absolute inset-0 bg-gradient-to-t from-ink/80 via-ink/20 to-ink/0 transition-opacity duration-500 flex flex-col justify-end p-4 opacity-0 group-hover:opacity-100">
          <div className="translate-y-4 group-hover:translate-y-0 transition-transform duration-500 ease-out flex gap-2">
            <button
              onClick={handleQuickAdd}
              className="flex-1 flex items-center justify-center gap-1.5 text-center bg-primary text-ink text-xs font-semibold uppercase tracking-widest px-4 py-3 rounded-sm shadow-xl hover:bg-highlight transition-colors duration-300"
            >
              <ShoppingCart size={14} />
              {product.variants.length === 1 ? 'Quick Add' : 'Options'}
            </button>
            <span className="flex-1 text-center bg-surface/90 backdrop-blur-md text-ink text-xs font-semibold uppercase tracking-widest px-4 py-3 rounded-sm shadow-xl hover:bg-surface transition-colors duration-300">
              Details
            </span>
          </div>
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
