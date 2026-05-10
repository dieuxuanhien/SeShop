import { useState } from 'react';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
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
      setResponse({ answer: 'No recommendation was returned. Try a different styling request.' });
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <PageScaffold
      title="AI Recommendation Chat"
      viewCode="CUST_009"
      purpose="Ask for styling help and receive catalog-aware recommendations."
    >
      <Card className="border-primary/20 bg-surface/95 p-5">
        <form onSubmit={handleSubmit} className="grid gap-3 md:grid-cols-[minmax(0,1fr)_120px]">
          <Input value={message} onChange={(event) => setMessage(event.target.value)} placeholder="Ask for a recommendation" />
          <Button type="submit" isLoading={isLoading}>Send</Button>
        </form>
        {response && (
          <div className="mt-4 rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/80">
            <p>{response.answer ?? 'Recommendation returned.'}</p>
            {response.items?.length ? (
              <ul className="mt-3 grid gap-2">
                {response.items.map((item) => (
                  <li key={`${item.productId}-${item.variantId}`} className="rounded-md bg-surface p-3">
                    Product {item.productId}, variant {item.variantId}: {item.reason}
                  </li>
                ))}
              </ul>
            ) : null}
          </div>
        )}
      </Card>
    </PageScaffold>
  );
}
