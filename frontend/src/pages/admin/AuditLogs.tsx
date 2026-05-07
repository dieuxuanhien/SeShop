import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { getAuditLogs, type AuditLog } from '@/features/admin/api/adminApi';

export function AuditLogs() {
  const [logs, setLogs] = useState<AuditLog[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getAuditLogs()
      .then(setLogs)
      .catch(() => setLogs([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="Audit & Compliance Logs"
      viewCode="ADMIN_004"
      purpose="Read-only audit table with filters for actor, action, target, and time range."
      endpoints={['GET /admin/audit-logs']}
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="grid gap-4 lg:grid-cols-[repeat(4,minmax(0,1fr))_140px]">
            <Input label="Actor" placeholder="Search by user" />
            <Input label="Action" placeholder="action type" />
            <Select
              label="Status"
              options={[
                { label: 'All', value: 'ALL' },
                { label: 'OK', value: 'OK' },
                { label: 'Flagged', value: 'FLAG' },
              ]}
            />
            <Input label="Date Range" placeholder="Last 24 hours" />
            <div className="flex items-end">
              <Button variant="secondary" className="w-full">Apply</Button>
            </div>
          </div>
        </Card>

        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Audit Stream</h2>
              <p className="mt-1 text-xs text-ink/50">Immutable trail of sensitive actions.</p>
            </div>
            <Button variant="secondary">Export</Button>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-3 py-2">Log ID</th>
                  <th className="px-3 py-2">Action</th>
                  <th className="px-3 py-2">Actor</th>
                  <th className="px-3 py-2">Target</th>
                  <th className="px-3 py-2">Status</th>
                  <th className="px-3 py-2 text-right">Timestamp</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="px-3 py-6 text-center text-sm text-ink/60">Loading audit logs...</td>
                  </tr>
                ) : logs.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-3 py-6 text-center text-sm text-ink/60">No audit logs returned by the API.</td>
                  </tr>
                ) : logs.map((log) => (
                  <tr key={log.id} className="text-ink/80">
                    <td className="px-3 py-3 font-semibold text-ink">AUD-{log.id}</td>
                    <td className="px-3 py-3">{log.action}</td>
                    <td className="px-3 py-3">{log.actor}</td>
                    <td className="px-3 py-3">{log.target}</td>
                    <td className="px-3 py-3">
                      <Badge variant={log.status === 'OK' ? 'success' : 'warning'}>{log.status}</Badge>
                    </td>
                    <td className="px-3 py-3 text-right text-xs text-ink/60">{new Date(log.createdAt).toLocaleString()}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>
      </div>
    </PageScaffold>
  );
}
