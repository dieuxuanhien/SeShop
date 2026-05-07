import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';

export function Settings() {
  return (
    <PageScaffold
      title="System Configuration"
      viewCode="ADMIN_005"
      purpose="Global settings shell for payment, shipping, email, Instagram integration, and business metadata."
      endpoints={['GET /marketing/instagram/status']}
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
          <div className="mt-4 flex justify-end">
            <Button variant="secondary">Save Profile</Button>
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
                <Badge variant="info">Not Checked</Badge>
              </div>
              <div className="flex justify-end">
                <Button variant="secondary">Test Connection</Button>
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
                <Badge variant="info">Use Status API</Badge>
              </div>
              <div className="flex justify-end gap-2">
                <Button variant="secondary">Reconnect</Button>
                <Button variant="secondary">Verify</Button>
              </div>
            </div>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
