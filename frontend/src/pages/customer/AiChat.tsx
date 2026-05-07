import { useState } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Button } from '@/shared/ui/Button';
import { Input } from '@/shared/ui/Input';
import { getAiRecommendations, type AiRecommendationResponse } from '@/features/marketing/api/assistantApi';

export function AiChat() {
  const [message, setMessage] = useState('');
  const [response, setResponse] = useState<AiRecommendationResponse | null>(null);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!message.trim()) return;
    setIsLoading(true);
    try {
      setResponse(await getAiRecommendations(message));
    } catch {
      setResponse({ answer: 'No recommendation returned by the API.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <PageScaffold
      title="AI Recommendation Chat"
      viewCode="CUST_009"
      purpose="Conversational product recommendations backed by current catalog and stock context."
      endpoints={['POST /assistant/recommendations']}
    >
      <form onSubmit={handleSubmit} className="grid gap-3">
        <Input value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Ask for a recommendation" />
        <Button type="submit" isLoading={isLoading}>Send</Button>
      </form>
      {response && (
        <div className="mt-4 rounded-md border border-primary/15 bg-ink/5 p-3 text-sm text-ink/80">
          <p>{response.answer ?? 'Recommendation returned.'}</p>
          {response.items?.length ? (
            <ul className="mt-3 grid gap-2">
              {response.items.map((item) => (
                <li key={`${item.productId}-${item.variantId}`}>Product {item.productId}, variant {item.variantId}: {item.reason}</li>
              ))}
            </ul>
          ) : null}
        </div>
      )}
    </PageScaffold>
  );
}
