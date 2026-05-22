import { NavLink } from 'react-router-dom';
import { Star } from 'lucide-react';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';

type ProductCardProps = {
  product: Product;
  index: number;
};

export function ProductCard({ product, index }: ProductCardProps) {
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
        {!product.thumbnailUrl && (
          <div className="absolute inset-0 flex items-center justify-center bg-surface/10 text-xs uppercase tracking-widest text-surface/40">
            No image
          </div>
        )}
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
