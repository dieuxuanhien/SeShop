import { useState } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { getAiRecommendations, type AiRecommendationResponse } from '@/features/marketing/api/assistantApi';
import { addCartItem } from '@/features/commerce/api/cartApi';
import { useCartStore } from '@/features/cart/model/cartStore';
import { formatCurrency } from '@/shared/lib/formatters';
import { ShoppingBag, Sparkles, Check } from 'lucide-react';

export function AiChat() {
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState<AiRecommendationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);
  const [addingVariantId, setAddingVariantId] = useState<number | null>(null);
  const [addedVariantId, setAddedVariantId] = useState<number | null>(null);
  const setCartItems = useCartStore((state) => state.setItems);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!message.trim()) return;
    setIsLoading(true);
    setResponse(null);
    setAddedVariantId(null);
    try {
      const res = await getAiRecommendations(message);
      setResponse(res);
    } catch {
      setResponse({ answer: 'No recommendation was returned. Try a different styling request.' });
    } finally {
      setIsLoading(false);
    }
  };

  const handleAddToCart = async (variantId: number) => {
    setAddingVariantId(variantId);
    try {
      const cart = await addCartItem(variantId, 1);
      setCartItems(cart.items.map((item) => ({
        id: item.id,
        variantId: item.variantId,
        skuCode: item.skuCode,
        name: item.name,
        color: item.color,
        size: item.size,
        imageUrl: item.imageUrl,
        qty: item.qty,
        unitPrice: Number(item.unitPrice),
      })));
      setAddedVariantId(variantId);
      setTimeout(() => setAddedVariantId(null), 3000);
    } catch (err) {
      console.error('Failed to add recommended item to cart', err);
    } finally {
      setAddingVariantId(null);
    }
  };

  return (
    <PageScaffold
      title="AI Styling Assistant"
      viewCode="CUST_009"
      purpose="Ask our intelligent AI assistant for personalized outfit curations, styling advice, and catalog recommendations."
    >
      <div className="max-w-4xl mx-auto space-y-6">
        <Card className="border-primary/20 bg-surface/95 p-6 shadow-md">
          <div className="flex items-center gap-3 mb-4 text-highlight font-display text-xl">
            <Sparkles className="text-primary animate-pulse" size={24} />
            <h2>What are you looking for today?</h2>
          </div>
          <form onSubmit={handleSubmit} className="flex flex-col sm:flex-row gap-3">
            <div className="flex-1">
              <Input
                value={message}
                onChange={(event) => setMessage(event.target.value)}
                placeholder="e.g., I need an elegant silk dress for a summer evening wedding party"
                className="w-full"
              />
            </div>
            <Button type="submit" isLoading={isLoading} icon={<Sparkles size={16} />}>
              Ask AI Assistant
            </Button>
          </form>
        </Card>

        {isLoading && (
          <div className="flex flex-col items-center justify-center py-12 space-y-4 animate-fade-in">
            <Sparkles className="text-primary animate-spin" size={32} />
            <p className="text-sm text-ink/70 animate-pulse">Analyzing catalog and curating perfect matches for you...</p>
          </div>
        )}

        {response && (
          <div className="space-y-6 animate-fade-in">
            <Card className="border-primary/30 bg-surface/90 p-6 border-l-4 border-l-primary shadow-sm">
              <h3 className="text-xs font-semibold uppercase tracking-wider text-primary mb-2 flex items-center gap-2">
                <Sparkles size={14} /> AI Assistant Response
              </h3>
              <p className="text-ink text-base leading-relaxed">{response.answer ?? 'Here are the curated recommendations tailored to your style request.'}</p>
            </Card>

            {response.items && response.items.length > 0 ? (
              <div className="space-y-4">
                <h3 className="text-sm font-semibold uppercase tracking-wider text-ink/70 px-1">Curated Pieces For You</h3>
                <div className="grid gap-6 md:grid-cols-2 lg:grid-cols-3">
                  {response.items.map((item) => (
                    <Card key={`${item.productId}-${item.variantId}`} className="flex flex-col h-full border-primary/20 bg-surface hover:border-primary/50 transition-all duration-300 shadow-sm hover:shadow-md overflow-hidden">
                      <div className="relative pt-[100%] bg-ink/5 border-b border-primary/10">
                        {item.imageUrl ? (
                          <img src={item.imageUrl} alt={item.productName || 'Recommended Product'} className="absolute inset-0 w-full h-full object-cover" />
                        ) : (
                          <div className="absolute inset-0 flex items-center justify-center text-xs text-ink/40 bg-ink/5">No Image Available</div>
                        )}
                        {item.stockAvailable !== undefined && item.stockAvailable > 0 && (
                          <span className="absolute top-3 right-3 bg-success/90 text-surface text-[10px] font-bold px-2.5 py-1 rounded-full shadow">
                            {item.stockAvailable} Available
                          </span>
                        )}
                      </div>

                      <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
                        <div className="space-y-2">
                          <div className="flex justify-between items-start gap-2">
                            <h4 className="font-semibold text-ink text-base line-clamp-1">{item.productName || `Product #${item.productId}`}</h4>
                            <span className="font-bold text-primary text-sm whitespace-nowrap">{formatCurrency(item.price || 0)}</span>
                          </div>
                          
                          <p className="text-xs text-ink/60 font-mono">
                            {item.skuCode || `SKU-${item.variantId}`} {item.color ? `• ${item.color}` : ''} {item.size ? `• ${item.size}` : ''}
                          </p>

                          {item.description && (
                            <p className="text-xs text-ink/75 line-clamp-2 pt-1">{item.description}</p>
                          )}

                          <div className="mt-3 pt-3 border-t border-primary/10 bg-primary/5 p-3 rounded-md">
                            <p className="text-xs text-ink/90 italic flex gap-2 items-start">
                              <Sparkles className="text-primary shrink-0 mt-0.5" size={12} />
                              <span>{item.reason}</span>
                            </p>
                          </div>
                        </div>

                        <div className="pt-2">
                          <Button
                            className="w-full justify-center py-2.5 text-xs font-semibold shadow-sm hover:shadow"
                            onClick={() => handleAddToCart(item.variantId)}
                            isLoading={addingVariantId === item.variantId}
                            variant={addedVariantId === item.variantId ? 'secondary' : 'primary'}
                            icon={addedVariantId === item.variantId ? <Check size={14} className="text-success" /> : <ShoppingBag size={14} />}
                          >
                            {addedVariantId === item.variantId ? 'Added to Cart' : 'Add to Cart'}
                          </Button>
                        </div>
                      </div>
                    </Card>
                  ))}
                </div>
              </div>
            ) : null}
          </div>
        )}
      </div>
    </PageScaffold>
  );
}
