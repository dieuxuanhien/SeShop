import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getInstagramStatus, type InstagramConnection as InstagramConnectionData } from '@/features/marketing/api/marketingApi';

export function InstagramConnection() {
  const [connection, setConnection] = useState<InstagramConnectionData | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getInstagramStatus()
      .then(setConnection)
      .catch(() => setConnection(null))
      .finally(() => setIsLoading(false));
  }, []);

  const status = connection?.status ?? 'DISCONNECTED';

  return (
    <PageScaffold
      title="Instagram Account Connection"
      viewCode="STAFF_010"
      purpose="OAuth connection status, token expiry, reconnect action, and integration health surface."
      endpoints={['POST /marketing/instagram/connect', 'POST /marketing/instagram/reconnect', 'GET /marketing/instagram/status']}
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Connection Status</h2>
              <p className="mt-1 text-xs text-ink/50">OAuth connection, token health, and sync cadence.</p>
            </div>
            <Badge variant={status === 'CONNECTED' ? 'success' : 'warning'}>{status}</Badge>
          </div>
          {isLoading ? (
            <p className="mt-4 text-sm text-ink/60">Loading Instagram status...</p>
          ) : (
          <div className="mt-4 grid gap-4 md:grid-cols-3">
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4">
              <p className="text-xs uppercase text-ink/50">Token Expiry</p>
              <p className="mt-2 text-sm font-semibold text-ink">{connection?.tokenExpiresAt ? new Date(connection.tokenExpiresAt).toLocaleString() : 'Not connected'}</p>
            </div>
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4">
              <p className="text-xs uppercase text-ink/50">Account ID</p>
              <p className="mt-2 text-sm font-semibold text-ink">{connection?.accountId ?? 'Not connected'}</p>
            </div>
            <div className="rounded-md border border-primary/15 bg-ink/5 p-4">
              <p className="text-xs uppercase text-ink/50">Account Name</p>
              <p className="mt-2 text-sm font-semibold text-ink">{connection?.accountName ?? 'Not connected'}</p>
            </div>
          </div>
          )}
          <div className="mt-4 flex flex-wrap gap-2">
            <Button variant="secondary">Verify Connection</Button>
            <Button variant="secondary">Reconnect</Button>
            <Button variant="danger">Disconnect</Button>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Account Details</h2>
            <div className="mt-4 grid gap-4">
              <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/70">
                <span>Account</span>
                <span className="font-semibold text-ink">{connection?.accountName ?? 'Not connected'}</span>
              </div>
              <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/5 p-4 text-sm text-ink/70">
                <span>Account ID</span>
                <span className="font-semibold text-ink">{connection?.accountId ?? 'Not connected'}</span>
              </div>
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Permissions</h2>
            <ul className="mt-4 grid gap-2 text-sm text-ink/70">
              <li className="rounded-md border border-primary/15 bg-ink/5 p-2">
                OAuth scopes are configured by the backend integration settings.
              </li>
            </ul>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
