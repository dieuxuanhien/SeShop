import { useState, useRef, useEffect } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import {
  getAiRecommendations,
  type AiRecommendationResponse,
  type ConversationTurn,
} from '@/features/marketing/api/assistantApi';
import { addCartItem } from '@/features/commerce/api/cartApi';
import { useCartStore } from '@/features/cart/model/cartStore';
import { useAuthStore } from '@/features/auth/model/authStore';
import { formatCurrency } from '@/shared/lib/formatters';
import { ShoppingBag, Sparkles, Check, Send, Bot, MessageCircle } from 'lucide-react';

type RecommendedItem = NonNullable<AiRecommendationResponse['items']>[number];

type ChatMessage = {
  id: string;
  role: 'user' | 'assistant';
  content: string;
  items?: RecommendedItem[];
};

const SUGGESTION_CHIPS = [
  'Summer outfit ideas ☀️',
  'Business casual looks 👔',
  'Date night outfit 🌙',
  'Activewear picks 🏃',
  'Under ₫500,000 💰',
  "What's trending? 🔥",
];

function generateId(): string {
  if (typeof crypto !== 'undefined' && typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID();
  }
  return String(Date.now()) + '-' + String(Math.random()).slice(2, 10);
}

/* -------------------------------------------------------------------------- */
/*  Product Card                                                              */
/* -------------------------------------------------------------------------- */

function ProductCard({
  item,
  addingVariantId,
  addedVariantId,
  onAddToCart,
  onAskAbout,
}: {
  item: RecommendedItem;
  addingVariantId: number | null;
  addedVariantId: number | null;
  onAddToCart: (variantId: number) => void;
  onAskAbout: (item: RecommendedItem) => void;
}) {
  return (
    <Card
      className="flex flex-col h-full border-primary/20 bg-surface hover:border-primary/50 transition-all duration-300 shadow-sm hover:shadow-md overflow-hidden"
    >
      {/* Image */}
      <div className="relative pt-[100%] bg-ink/5 border-b border-primary/10">
        {item.imageUrl ? (
          <img
            src={item.imageUrl}
            alt={item.productName || 'Recommended Product'}
            className="absolute inset-0 w-full h-full object-cover"
          />
        ) : (
          <div className="absolute inset-0 flex items-center justify-center text-xs text-ink/40 bg-ink/5">
            No Image Available
          </div>
        )}
        {item.stockAvailable !== undefined && item.stockAvailable > 0 && (
          <span className="absolute top-3 right-3 bg-success/90 text-surface text-[10px] font-bold px-2.5 py-1 rounded-full shadow">
            {item.stockAvailable} Available
          </span>
        )}
      </div>

      {/* Details */}
      <div className="p-5 flex-1 flex flex-col justify-between space-y-4">
        <div className="space-y-2">
          <div className="flex justify-between items-start gap-2">
            <h4 className="font-semibold text-ink text-base line-clamp-1">
              {item.productName || `Product #${item.productId}`}
            </h4>
            <span className="font-bold text-primary text-sm whitespace-nowrap">
              {formatCurrency(item.price || 0)}
            </span>
          </div>

          <p className="text-xs text-ink/60 font-mono">
            {item.skuCode || `SKU-${item.variantId}`}
            {item.attributes && Object.keys(item.attributes).length > 0
              ? ` • ${Object.entries(item.attributes).map(([k, v]) => `${k}: ${v}`).join(', ')}`
              : ''}
          </p>

          {item.description && (
            <p className="text-xs text-ink/75 line-clamp-2 pt-1">{item.description}</p>
          )}

          {/* AI Reason */}
          <div className="mt-3 pt-3 border-t border-primary/10 bg-primary/5 p-3 rounded-md">
            <p className="text-xs text-ink/90 italic flex gap-2 items-start">
              <Sparkles className="text-primary shrink-0 mt-0.5" size={12} />
              <span>{item.reason}</span>
            </p>
          </div>
        </div>

        {/* Actions */}
        <div className="flex flex-col gap-2 pt-2">
          <Button
            className="w-full justify-center py-2.5 text-xs font-semibold shadow-sm hover:shadow"
            onClick={() => onAddToCart(item.variantId)}
            isLoading={addingVariantId === item.variantId}
            variant={addedVariantId === item.variantId ? 'secondary' : 'primary'}
            icon={
              addedVariantId === item.variantId
                ? <Check size={14} className="text-success" />
                : <ShoppingBag size={14} />
            }
          >
            {addedVariantId === item.variantId ? 'Added to Cart' : 'Add to Cart'}
          </Button>

          <Button
            className="w-full justify-center py-2 text-xs"
            variant="outline"
            size="sm"
            onClick={() => onAskAbout(item)}
            icon={<MessageCircle size={12} />}
          >
            Ask about this
          </Button>
        </div>
      </div>
    </Card>
  );
}

/* -------------------------------------------------------------------------- */
/*  Typing Indicator                                                          */
/* -------------------------------------------------------------------------- */

function TypingIndicator() {
  return (
    <div className="flex items-start gap-3 animate-fade-in">
      <div className="flex items-center justify-center size-8 rounded-full bg-primary/10 text-primary shrink-0">
        <Bot size={16} />
      </div>
      <div className="rounded-2xl rounded-tl-sm bg-surface border border-primary/15 px-5 py-3 shadow-sm">
        <div className="flex items-center gap-2">
          <div className="flex gap-1">
            <span className="size-2 rounded-full bg-primary/60 animate-bounce" style={{ animationDelay: '0ms' }} />
            <span className="size-2 rounded-full bg-primary/60 animate-bounce" style={{ animationDelay: '150ms' }} />
            <span className="size-2 rounded-full bg-primary/60 animate-bounce" style={{ animationDelay: '300ms' }} />
          </div>
          <span className="text-xs text-ink/50 ml-1">Thinking...</span>
        </div>
      </div>
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/*  Welcome State                                                             */
/* -------------------------------------------------------------------------- */

function WelcomeState({ onChipClick }: { onChipClick: (text: string) => void }) {
  return (
    <div className="flex-1 flex flex-col items-center justify-center py-16 px-4 animate-fade-in">
      <div className="mb-6 flex items-center justify-center size-20 rounded-full bg-primary/10">
        <Sparkles className="text-primary animate-pulse" size={36} />
      </div>

      <h2 className="font-display text-xl font-semibold text-ink mb-2 text-center">
        Welcome to SeShop AI Assistant
      </h2>
      <p className="text-sm text-ink/60 text-center max-w-md mb-10 leading-relaxed">
        I can help you find the perfect outfit. Ask me anything about fashion, styling, or browse
        our catalog.
      </p>

      <div className="flex flex-wrap justify-center gap-2.5 max-w-lg">
        {SUGGESTION_CHIPS.map((chip) => (
          <button
            key={chip}
            type="button"
            onClick={() => onChipClick(chip)}
            className="rounded-full border border-primary/20 bg-surface/80 px-4 py-2 text-sm text-ink/80 hover:bg-primary/10 hover:border-primary/40 hover:text-ink transition-all duration-200 shadow-sm"
          >
            {chip}
          </button>
        ))}
      </div>
    </div>
  );
}

/* -------------------------------------------------------------------------- */
/*  Main Component                                                            */
/* -------------------------------------------------------------------------- */

export function AiChat() {
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [input, setInput] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [addingVariantId, setAddingVariantId] = useState<number | null>(null);
  const [addedVariantId, setAddedVariantId] = useState<number | null>(null);
  const chatEndRef = useRef<HTMLDivElement>(null);
  const setCartItems = useCartStore((state) => state.setItems);
  const user = useAuthStore((state) => state.user);

  /* -- Auto-scroll -------------------------------------------------------- */
  useEffect(() => {
    chatEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, isLoading]);

  /* -- Build conversation history for API --------------------------------- */
  const buildConversationHistory = (): ConversationTurn[] => {
    return messages.map((msg) => ({
      role: msg.role === 'user' ? 'user' : 'model',
      content: msg.content,
    }));
  };

  /* -- Send message ------------------------------------------------------- */
  const handleSend = async (text?: string) => {
    const messageText = (text || input).trim();
    if (!messageText || isLoading) return;

    const userMessage: ChatMessage = {
      id: generateId(),
      role: 'user',
      content: messageText,
    };

    setMessages((prev) => [...prev, userMessage]);
    setInput('');
    setIsLoading(true);

    try {
      const history = buildConversationHistory();
      const res = await getAiRecommendations({
        message: messageText,
        conversationHistory: history,
        customerId: user?.id,
      });

      const aiMessage: ChatMessage = {
        id: generateId(),
        role: 'assistant',
        content: res.answer || 'Here are my recommendations for you.',
        items: res.items,
      };

      setMessages((prev) => [...prev, aiMessage]);
    } catch {
      const errorMessage: ChatMessage = {
        id: generateId(),
        role: 'assistant',
        content: 'Sorry, I encountered an issue processing your request. Please try again.',
      };
      setMessages((prev) => [...prev, errorMessage]);
    } finally {
      setIsLoading(false);
    }
  };

  /* -- Add to cart -------------------------------------------------------- */
  const handleAddToCart = async (variantId: number) => {
    setAddingVariantId(variantId);
    try {
      const cart = await addCartItem(variantId, 1);
      setCartItems(
        cart.items.map((item) => ({
          id: item.id,
          variantId: item.variantId,
          skuCode: item.skuCode,
          name: item.name,
          attributes: item.attributes,
          imageUrl: item.imageUrl,
          qty: item.qty,
          unitPrice: Number(item.unitPrice),
        })),
      );
      setAddedVariantId(variantId);
      setTimeout(() => setAddedVariantId(null), 3000);
    } catch (err) {
      console.error('Failed to add recommended item to cart', err);
    } finally {
      setAddingVariantId(null);
    }
  };

  /* -- Follow-up about a product ----------------------------------------- */
  const handleAskAboutProduct = (item: RecommendedItem) => {
    const text = `Tell me more about the ${item.productName || 'product'}. What occasions is it good for and how should I style it?`;
    handleSend(text);
  };

  /* -- Form submit -------------------------------------------------------- */
  const handleFormSubmit = (event: React.FormEvent) => {
    event.preventDefault();
    handleSend();
  };

  /* -- Render ------------------------------------------------------------- */
  const hasMessages = messages.length > 0;

  return (
    <PageScaffold
      title="AI Styling Assistant"
      viewCode="CUST_009"
      purpose="Ask our intelligent AI assistant for personalized outfit curations, styling advice, and catalog recommendations."
    >
      <div className="max-w-4xl mx-auto flex flex-col" style={{ minHeight: 'calc(100vh - 200px)' }}>
        {/* Chat area */}
        <div className="flex-1 flex flex-col">
          {!hasMessages ? (
            <WelcomeState onChipClick={(chip) => handleSend(chip)} />
          ) : (
            <div className="space-y-6 py-4">
              {messages.map((msg) => (
                <div key={msg.id} className="animate-fade-in">
                  {msg.role === 'user' ? (
                    /* ---- User bubble ---- */
                    <div className="flex justify-end">
                      <div className="max-w-[75%] flex items-end gap-2">
                        <div className="rounded-2xl rounded-br-sm bg-primary px-5 py-3 text-sm text-ink shadow-md">
                          {msg.content}
                        </div>
                      </div>
                    </div>
                  ) : (
                    /* ---- AI bubble + product cards ---- */
                    <div className="flex items-start gap-3">
                      <div className="flex items-center justify-center size-8 rounded-full bg-primary/10 text-primary shrink-0 mt-0.5">
                        <Bot size={16} />
                      </div>
                      <div className="flex-1 space-y-4 max-w-[85%]">
                        {/* Text response */}
                        <div className="rounded-2xl rounded-tl-sm bg-surface border border-primary/15 px-5 py-3 shadow-sm">
                          <p className="text-sm text-ink leading-relaxed whitespace-pre-wrap">
                            {msg.content}
                          </p>
                        </div>

                        {/* Product cards grid */}
                        {msg.items && msg.items.length > 0 && (
                          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-3">
                            {msg.items.map((item) => (
                              <ProductCard
                                key={`${msg.id}-${item.productId}-${item.variantId}`}
                                item={item}
                                addingVariantId={addingVariantId}
                                addedVariantId={addedVariantId}
                                onAddToCart={handleAddToCart}
                                onAskAbout={handleAskAboutProduct}
                              />
                            ))}
                          </div>
                        )}
                      </div>
                    </div>
                  )}
                </div>
              ))}

              {/* Typing indicator */}
              {isLoading && <TypingIndicator />}

              {/* Scroll anchor */}
              <div ref={chatEndRef} />
            </div>
          )}
        </div>

        {/* Sticky input bar */}
        <div className="sticky bottom-0 pt-3 pb-4 bg-gradient-to-t from-ink/[0.03] to-transparent">
          <Card className="p-2 border-primary/20 shadow-md">
            <form onSubmit={handleFormSubmit} className="flex items-center gap-2">
              <div className="flex-1">
                <Input
                  value={input}
                  onChange={(event) => setInput(event.target.value)}
                  placeholder="Ask about outfits, styling, or browse the catalog..."
                  className="w-full border-none shadow-none focus:ring-0"
                />
              </div>
              <Button
                type="submit"
                isLoading={isLoading}
                icon={<Send size={16} />}
                size="md"
                className="shrink-0 rounded-xl"
              >
                Send
              </Button>
            </form>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
