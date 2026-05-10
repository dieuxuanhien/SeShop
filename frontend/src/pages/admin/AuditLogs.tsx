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
  const [actor, setActor] = useState('');
  const [action, setAction] = useState('');
  const [status, setStatus] = useState('ALL');

  useEffect(() => {
    getAuditLogs()
      .then(setLogs)
      .catch(() => setLogs([]))
      .finally(() => setIsLoading(false));
  }, []);

  const filteredLogs = logs.filter((log) => {
    const actorMatches = actor ? log.actor.toLowerCase().includes(actor.toLowerCase()) : true;
    const actionMatches = action ? log.action.toLowerCase().includes(action.toLowerCase()) : true;
    const statusMatches = status === 'ALL' ? true : log.status === status;
    return actorMatches && actionMatches && statusMatches;
  });

  function handleExport() {
    const csv = [
      'id,action,actor,target,status,createdAt',
      ...filteredLogs.map((log) => [log.id, log.action, log.actor, log.target, log.status, log.createdAt].join(',')),
    ].join('\n');
    navigator.clipboard?.writeText(csv).catch(() => undefined);
  }

  return (
    <PageScaffold
      title="Audit & Compliance Logs"
      viewCode="ADMIN_004"
      purpose="Review sensitive actions with actor, target, and status filters."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="grid gap-4 lg:grid-cols-[repeat(4,minmax(0,1fr))_140px]">
            <Input label="Actor" placeholder="Search by user" value={actor} onChange={(event) => setActor(event.target.value)} />
            <Input label="Action" placeholder="action type" value={action} onChange={(event) => setAction(event.target.value)} />
            <Select
              label="Status"
              value={status}
              onChange={(event) => setStatus(event.target.value)}
              options={[
                { label: 'All', value: 'ALL' },
                { label: 'OK', value: 'OK' },
                { label: 'Flagged', value: 'FLAG' },
              ]}
            />
            <Input label="Date Range" placeholder="Last 24 hours" />
            <div className="flex items-end">
              <Button variant="secondary" className="w-full" onClick={() => undefined}>Apply</Button>
            </div>
          </div>
        </Card>

        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Audit Stream</h2>
              <p className="mt-1 text-xs text-ink/50">Immutable trail of sensitive actions.</p>
            </div>
            <Button variant="secondary" onClick={handleExport}>Export</Button>
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
                ) : filteredLogs.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-3 py-6 text-center text-sm text-ink/60">No audit logs match the current filters.</td>
                  </tr>
                ) : filteredLogs.map((log) => (
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
