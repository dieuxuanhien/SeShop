import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { getInstagramStatus, reconnectInstagram, type InstagramConnection } from '@/features/marketing/api/marketingApi';

export function Settings() {
  const [profileSaved, setProfileSaved] = useState(false);
  const [paymentStatus, setPaymentStatus] = useState<'Not Checked' | 'Ready'>('Not Checked');
  const [instagram, setInstagram] = useState<InstagramConnection | null>(null);
  const [message, setMessage] = useState('');

  useEffect(() => {
    getInstagramStatus()
      .then(setInstagram)
      .catch(() => setInstagram(null));
  }, []);

  async function handleReconnect() {
    setMessage('');
    try {
      const { authorizationUrl } = await reconnectInstagram();
      window.location.assign(authorizationUrl);
    } catch {
      setMessage('Instagram reconnect could not be started.');
    }
  }

  return (
    <PageScaffold
      title="System Configuration"
      viewCode="ADMIN_005"
      purpose="Keep business metadata, checkout defaults, and integration health in one place."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Business Profile</h2>
              <p className="mt-1 text-xs text-ink/50">Global brand identity and contact metadata.</p>
            </div>
            <Badge variant="sale">Core</Badge>
          </div>
          <div className="mt-4 grid gap-4 md:grid-cols-2">
            <Input label="Business Name" />
            <Input label="Support Email" />
            <Input label="Support Phone" />
            <Select
              label="Default Currency"
              options={[
                { label: 'VND', value: 'VND' },
                { label: 'USD', value: 'USD' },
              ]}
            />
          </div>
          <div className="mt-4 flex items-center justify-between gap-3">
            <span className="text-sm text-ink/55">{profileSaved ? 'Profile saved locally for this session.' : 'Review the business profile before saving.'}</span>
            <Button variant="secondary" onClick={() => setProfileSaved(true)}>Save Profile</Button>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-2">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Payments</h2>
            <div className="mt-4 grid gap-4">
              <Input label="Stripe API Key" />
              <Select
                label="Default Payment Method"
                options={[
                  { label: 'Stripe', value: 'STRIPE' },
                  { label: 'Cash on Delivery', value: 'COD' },
                ]}
              />
              <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/5 p-3 text-sm text-ink/70">
                <span>Webhook Status</span>
                <Badge variant={paymentStatus === 'Ready' ? 'success' : 'info'}>{paymentStatus}</Badge>
              </div>
              <div className="flex justify-end">
                <Button variant="secondary" onClick={() => setPaymentStatus('Ready')}>Test Connection</Button>
              </div>
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Instagram Integration</h2>
            <div className="mt-4 grid gap-4">
              <Input label="Instagram App ID" />
              <Input label="Webhook URL" />
              <div className="flex items-center justify-between rounded-md border border-primary/15 bg-ink/5 p-3 text-sm text-ink/70">
                <span>Connection Status</span>
                <Badge variant={instagram?.status === 'CONNECTED' ? 'success' : 'warning'}>
                  {instagram?.status ?? 'DISCONNECTED'}
                </Badge>
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="secondary" onClick={handleReconnect}>Reconnect</Button>
                <Button variant="secondary" onClick={() => getInstagramStatus().then(setInstagram).catch(() => setInstagram(null))}>Verify</Button>
              </div>
            </div>
          </Card>
        </div>
        {message ? <p className="text-sm text-surface/70">{message}</p> : null}
      </div>
    </PageScaffold>
  );
}
