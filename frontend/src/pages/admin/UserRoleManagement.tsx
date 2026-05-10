import { useEffect, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { assignPermissions, assignRoleToUser, createRole, getRoles, type Role } from '@/features/admin/api/adminApi';

export function UserRoleManagement() {
  const [roles, setRoles] = useState<Role[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [roleName, setRoleName] = useState('');
  const [roleDescription, setRoleDescription] = useState('');
  const [selectedRoleId, setSelectedRoleId] = useState(0);
  const [permissionCodes, setPermissionCodes] = useState('');
  const [userId, setUserId] = useState(0);
  const [assignRoleId, setAssignRoleId] = useState(0);
  const [message, setMessage] = useState('');
  const [isSaving, setIsSaving] = useState(false);

  function loadRoles() {
    setIsLoading(true);
    getRoles()
      .then(setRoles)
      .catch(() => setRoles([]))
      .finally(() => setIsLoading(false));
  }

  useEffect(() => {
    loadRoles();
  }, []);

  async function handleCreateRole(event: React.FormEvent) {
    event.preventDefault();
    setIsSaving(true);
    setMessage('');
    try {
      const role = await createRole(roleName.trim(), roleDescription.trim() || undefined);
      setSelectedRoleId(role.id);
      setAssignRoleId(role.id);
      setRoleName('');
      setRoleDescription('');
      setMessage(`${role.name} created.`);
      loadRoles();
    } catch {
      setMessage('Role could not be created.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAssignPermissions(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedRoleId) return;
    setIsSaving(true);
    setMessage('');
    try {
      await assignPermissions(
        selectedRoleId,
        permissionCodes.split(',').map((code) => code.trim()).filter(Boolean),
      );
      setPermissionCodes('');
      setMessage('Permissions assigned.');
    } catch {
      setMessage('Permissions could not be assigned.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAssignRole(event: React.FormEvent) {
    event.preventDefault();
    if (!userId || !assignRoleId) return;
    setIsSaving(true);
    setMessage('');
    try {
      await assignRoleToUser(userId, assignRoleId);
      setMessage(`Role ${assignRoleId} assigned to user ${userId}.`);
    } catch {
      setMessage('Role could not be assigned.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="User & Role Management"
      viewCode="ADMIN_002"
      purpose="Create roles, assign permissions, and grant staff access."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Role Directory</h2>
              <p className="mt-1 text-xs text-ink/50">Roles returned by the admin roles API.</p>
            </div>
            <Button variant="secondary" onClick={() => loadRoles()}>Refresh</Button>
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
                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">No roles found.</td>
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
                      <Button
                        variant="secondary"
                        onClick={() => {
                          setSelectedRoleId(row.id);
                          setAssignRoleId(row.id);
                        }}
                      >
                        Select
                      </Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_360px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Roles & Permissions</h2>
            </div>
            <div className="mt-4 grid gap-4">
              {roles.map((role) => (
                <div key={role.name} className="rounded-md border border-primary/15 bg-ink/5 p-4">
                  <div className="flex items-center justify-between">
                    <p className="text-sm font-semibold text-ink">{role.name}</p>
                    <Button variant="secondary" onClick={() => setSelectedRoleId(role.id)}>Choose</Button>
                  </div>
                  <p className="mt-2 text-xs text-ink/60">{role.description ?? 'No description returned.'}</p>
                </div>
              ))}
            </div>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Access Actions</h2>
            <form onSubmit={handleCreateRole} className="mt-4 grid gap-3">
              <Input label="Role Name" value={roleName} onChange={(event) => setRoleName(event.target.value)} required />
              <Input label="Description" value={roleDescription} onChange={(event) => setRoleDescription(event.target.value)} />
              <Button type="submit" isLoading={isSaving}>Create Role</Button>
            </form>

            <form onSubmit={handleAssignPermissions} className="mt-6 grid gap-3 border-t border-primary/10 pt-5">
              <Input
                label="Role ID"
                type="number"
                min={1}
                value={selectedRoleId || ''}
                onChange={(event) => setSelectedRoleId(Number(event.target.value))}
                required
              />
              <Input
                label="Permission Codes"
                value={permissionCodes}
                onChange={(event) => setPermissionCodes(event.target.value)}
                placeholder="PRODUCT_CREATE, ORDER_PACK"
                required
              />
              <Button type="submit" variant="secondary" isLoading={isSaving}>Assign Permissions</Button>
            </form>

            <form onSubmit={handleAssignRole} className="mt-6 grid gap-3 border-t border-primary/10 pt-5">
              <Input label="User ID" type="number" min={1} value={userId || ''} onChange={(event) => setUserId(Number(event.target.value))} required />
              <Input label="Role ID" type="number" min={1} value={assignRoleId || ''} onChange={(event) => setAssignRoleId(Number(event.target.value))} required />
              <Button type="submit" variant="secondary" isLoading={isSaving}>Assign Role</Button>
            </form>

            {message ? <p className="mt-4 text-sm text-ink/65">{message}</p> : null}
          </Card>
        </div>
      </div>
    </PageScaffold>
  );
}
