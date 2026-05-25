import { useState } from 'react';
import { NavLink, useNavigate } from 'react-router-dom';
import { Star, ShoppingCart, Check, Loader2 } from 'lucide-react';
import type { Product } from '@/entities/product/types';
import { formatCurrency } from '@/shared/lib/formatters';
import { Badge } from '@/shared/ui/Badge';
import { useCartStore } from '@/features/cart/model/cartStore';
import { addCartItem } from '@/features/commerce/api/cartApi';
import { useAuth } from '@/features/auth';

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
  const { token } = useAuth();

  const [isAdding, setIsAdding] = useState(false);
  const [isAdded, setIsAdded] = useState(false);

  const handleQuickAdd = async (e: React.MouseEvent) => {
    e.preventDefault();
    e.stopPropagation();
    
    if (product.variants.length === 1) {
      const variant = product.variants[0];
      setIsAdding(true);
      
      try {
        if (token) {
          await addCartItem(variant.id, 1);
        }
        addItem({
          variantId: variant.id,
          skuCode: variant.skuCode,
          name: product.name,
          attributes: variant.attributes,
          imageUrl: product.thumbnailUrl,
          qty: 1,
          unitPrice: variant.price,
        });
        
        setIsAdded(true);
        setTimeout(() => setIsAdded(false), 2000);
      } catch (error) {
        console.error('Failed to add to cart:', error);
        alert('Failed to add item to cart.');
      } finally {
        setIsAdding(false);
      }
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
      <div className="relative aspect-[3/4] overflow-hidden bg-surface/5 mb-4 rounded-xl">
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
              disabled={isAdding}
              className="flex-1 flex items-center justify-center gap-1.5 text-center bg-primary text-ink text-xs font-semibold uppercase tracking-widest px-4 py-3 rounded-md shadow-xl hover:bg-highlight transition-all duration-300 disabled:opacity-80 disabled:cursor-wait"
            >
              {isAdding ? (
                <Loader2 size={14} className="animate-spin" />
              ) : isAdded ? (
                <>
                  <Check size={14} className="text-success" />
                  Added
                </>
              ) : (
                <>
                  <ShoppingCart size={14} />
                  {product.variants.length === 1 ? 'Quick Add' : 'Options'}
                </>
              )}
            </button>
            <span className="flex-1 text-center bg-surface/90 backdrop-blur-md text-ink text-xs font-semibold uppercase tracking-widest px-4 py-3 rounded-md shadow-xl hover:bg-surface transition-colors duration-300 flex items-center justify-center">
              Details
            </span>
          </div>
        </div>
        {/* Sale badge */}
        {hasDiscount && (
          <Badge variant="sale" className="absolute top-3 left-3 shadow-md">Sale</Badge>
        )}
      </div>

      {/* Info */}
      <div className="space-y-1.5 px-1">
        <p className="text-xs uppercase tracking-widest text-primary/70 font-semibold">{product.brand}</p>
        <h3 className="text-sm font-semibold text-surface group-hover:text-highlight transition-colors leading-snug">
          {product.name}
        </h3>
        <div className="flex items-center gap-2">
          <span className="text-sm font-bold text-highlight">{formatCurrency(minPrice)}</span>
          {hasDiscount && (
            <span className="text-xs text-surface/40 line-through font-medium">{formatCurrency(compareAt)}</span>
          )}
        </div>
        {rating && rating.reviewCount > 0 && (
          <div className="flex items-center gap-1.5 text-xs text-surface/50 font-medium">
            <Star size={12} className="fill-primary text-primary" />
            <span>{rating.averageRating.toFixed(1)}</span>
            <span>({rating.reviewCount})</span>
          </div>
        )}
      </div>
    </NavLink>
  );
}
