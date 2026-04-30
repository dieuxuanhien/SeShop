import { PageScaffold } from '@/shared/ui/PageScaffold';

export function AiChat() {
  return (
    <PageScaffold
      title="AI Recommendation Chat"
      viewCode="CUST_009"
      purpose="Conversational product recommendations backed by current catalog and stock context."
      endpoints={['POST /assistant/recommendations']}
    />
  );
}
