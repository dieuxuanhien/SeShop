import { PageScaffold } from '@/shared/ui/PageScaffold';

export function InstagramConnection() {
  return (
    <PageScaffold
      title="Instagram Account Connection"
      viewCode="STAFF_010"
      purpose="OAuth connection status, token expiry, reconnect action, and integration health surface."
      endpoints={['POST /marketing/instagram/connect', 'POST /marketing/instagram/reconnect', 'GET /marketing/instagram/status']}
    />
  );
}
