import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { getRoles, type Role } from '@/features/admin/api/adminApi';

export function UserRoleManagement() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    getRoles()
      .then(setRoles)
      .catch(() => setRoles([]))
      .finally(() => setIsLoading(false));
  }, []);

  return (
    <PageScaffold
      title="User & Role Management"
      viewCode="ADMIN_002"
      purpose="Staff account, role, permission, multi-role assignment, and role audit management surface."
      endpoints={[
        'POST /admin/roles',
        'GET /admin/roles',
        'POST /admin/roles/{roleId}/permissions',
        'POST /admin/users/{userId}/roles',
        'DELETE /admin/users/{userId}/roles/{assignmentId}',
      ]}
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Role Directory</h2>
              <p className="mt-1 text-xs text-ink/50">Roles returned by the admin roles API.</p>
            </div>
            <div className="flex gap-2">
              <Button variant="secondary">Create Role</Button>
            </div>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-3 py-2">Role ID</th>
                  <th className="px-3 py-2">Name</th>
                  <th className="px-3 py-2">Description</th>
                  <th className="px-3 py-2">Status</th>
                  <th className="px-3 py-2 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">Loading roles...</td>
                  </tr>
                ) : roles.length === 0 ? (
                  <tr>
                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">No roles returned by the API.</td>
                  </tr>
                ) : roles.map((row) => (
                  <tr key={row.id} className="text-ink/80">
                    <td className="px-3 py-3 font-semibold text-ink">ROLE-{row.id}</td>
                    <td className="px-3 py-3">{row.name}</td>
                    <td className="px-3 py-3">{row.description ?? 'No description'}</td>
                    <td className="px-3 py-3">
                      <Badge variant={row.status === 'ACTIVE' ? 'success' : 'warning'}>{row.status}</Badge>
                    </td>
                    <td className="px-3 py-3 text-right">
                      <Button variant="secondary">Assign</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_320px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Roles & Permissions</h2>
              <Button variant="secondary">Create Role</Button>
            </div>
            <div className="mt-4 grid gap-4">
              {roles.map((role) => (
                <div key={role.name} className="rounded-md border border-primary/15 bg-ink/5 p-4">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-ink">{role.name}</p>
                    <Button variant="secondary">Edit</Button>
                  </div>
                  <p className="mt-2 text-xs text-ink/60">{role.description ?? 'No description returned.'}</p>
                </div>
              ))}
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Role Audit</h2>
            <ul className="mt-4 grid gap-3 text-sm text-ink/70">
              <li>Role activity is available from the audit logs API.</li>
            </ul>
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
