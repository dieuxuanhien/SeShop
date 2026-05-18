import { useEffect, useMemo, useState } from 'react';
import { Badge } from '@/shared/ui/Badge';
import { Button } from '@/shared/ui/Button';
import { Card } from '@/shared/ui/Card';
import { Input } from '@/shared/ui/Input';
import { PageScaffold } from '@/shared/ui/PageScaffold';
import { Select } from '@/shared/ui/Select';
import { useAuth } from '@/features/auth';
import {
  assignPermissions,
  assignRoleToUser,
  createRole,
  createUser,
  deleteRole,
  deleteUser,
  getPermissions,
  getRoles,
  getUsers,
  revokeRoleFromUser,
  updateRole,
  updateUser,
  type AdminUser,
  type Permission,
  type Role,
  type UserMutationRequest,
} from '@/features/admin/api/adminApi';
import { hasAnyPermission, hasPermission } from '@/shared/lib/permissions';
import { IDENTITY_ADMIN_PERMISSIONS } from '@/shared/lib/access';

type RoleForm = {
  name: string;
  description: string;
  status: 'ACTIVE' | 'INACTIVE';
};

type UserForm = UserMutationRequest & {
  password: string;
};

const emptyRoleForm: RoleForm = {
  name: '',
  description: '',
  status: 'INACTIVE',
};

const emptyUserForm: UserForm = {
  username: '',
  email: '',
  phoneNumber: '',
  password: '',
  userType: 'STAFF',
  status: 'ACTIVE',
};

const userTypeOptions = [
  { label: 'Admin', value: 'ADMIN' },
  { label: 'Staff', value: 'STAFF' },
  { label: 'Customer', value: 'CUSTOMER' },
];

const userStatusOptions = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Inactive', value: 'INACTIVE' },
  { label: 'Locked', value: 'LOCKED' },
];

const roleStatusOptions = [
  { label: 'Active', value: 'ACTIVE' },
  { label: 'Inactive', value: 'INACTIVE' },
];

export function UserRoleManagement() {
  const { user } = useAuth();
  const [roles, setRoles] = useState<Role[]>([]);
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [permissions, setPermissions] = useState<Permission[]>([]);
  const [isLoading, setIsLoading] = useState(true);
  const [isSaving, setIsSaving] = useState(false);
  const [message, setMessage] = useState('');

  const [selectedRoleId, setSelectedRoleId] = useState<number | null>(null);
  const [roleForm, setRoleForm] = useState<RoleForm>(emptyRoleForm);
  const [selectedPermissionCodes, setSelectedPermissionCodes] = useState<string[]>([]);
  const [permissionSearch, setPermissionSearch] = useState('');

  const [selectedUserId, setSelectedUserId] = useState<number | null>(null);
  const [userForm, setUserForm] = useState<UserForm>(emptyUserForm);
  const [assignUserId, setAssignUserId] = useState<number | null>(null);
  const [assignRoleId, setAssignRoleId] = useState<number | null>(null);

  const canReadIdentity = hasAnyPermission(user, IDENTITY_ADMIN_PERMISSIONS);
  const canCreateRole = hasPermission(user, 'role.create');
  const canUpdateRole = hasPermission(user, 'role.update');
  const canDeleteRole = hasPermission(user, 'role.delete');
  const canAssignPermissions = hasPermission(user, 'role.permission.assign');
  const canAssignRoles = hasPermission(user, 'staff.role.assign');
  const canCreateUser = hasPermission(user, 'staff.user.create');
  const canUpdateUser = hasPermission(user, 'staff.user.update');
  const canDeleteUser = hasPermission(user, 'staff.user.delete');
  const canReadUsers = hasAnyPermission(user, ['staff.user.read', 'staff.user.create', 'staff.user.update', 'staff.user.delete', 'staff.role.assign']);

  const selectedRole = roles.find((role) => role.id === selectedRoleId);
  const selectedUser = users.find((row) => row.id === selectedUserId);

  const roleOptions = useMemo(
    () => [
      { label: 'Select role', value: '' },
      ...roles.map((role) => ({ label: `${role.name} (${role.status})`, value: String(role.id) })),
    ],
    [roles],
  );

  const userOptions = useMemo(
    () => [
      { label: 'Select user', value: '' },
      ...users.map((row) => ({ label: `${row.username} - ${row.email}`, value: String(row.id) })),
    ],
    [users],
  );

  const filteredPermissions = useMemo(() => {
    const search = permissionSearch.trim().toLowerCase();
    if (!search) {
      return permissions;
    }
    return permissions.filter((permission) =>
      `${permission.code} ${permission.description ?? ''}`.toLowerCase().includes(search),
    );
  }, [permissionSearch, permissions]);

  async function loadAccessData() {
    setIsLoading(true);
    const [roleResult, permissionResult, userResult] = await Promise.allSettled([
      canReadIdentity ? getRoles() : Promise.resolve([]),
      canAssignPermissions || canCreateRole || canUpdateRole || canDeleteRole || canAssignRoles ? getPermissions() : Promise.resolve([]),
      canReadUsers ? getUsers() : Promise.resolve([]),
    ]);

    const loadedRoles = roleResult.status === 'fulfilled' ? roleResult.value : [];
    const loadedPermissions = permissionResult.status === 'fulfilled' ? permissionResult.value : [];
    const loadedUsers = userResult.status === 'fulfilled' ? userResult.value : [];

    setRoles(loadedRoles);
    setPermissions(loadedPermissions);
    setUsers(loadedUsers);

    if (!selectedRoleId && loadedRoles.length > 0) {
      selectRole(loadedRoles[0]);
    }
    if (!selectedUserId && loadedUsers.length > 0) {
      selectUser(loadedUsers[0]);
    }
    setIsLoading(false);
  }

  useEffect(() => {
    loadAccessData().catch(() => {
      setRoles([]);
      setPermissions([]);
      setUsers([]);
      setIsLoading(false);
    });
    // The permission booleans are derived from the persisted auth user and should reload the screen when the signer changes.
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.id]);

  function selectRole(role: Role) {
    setSelectedRoleId(role.id);
    setAssignRoleId(role.id);
    setRoleForm({
      name: role.name,
      description: role.description ?? '',
      status: role.status === 'ACTIVE' ? 'ACTIVE' : 'INACTIVE',
    });
    setSelectedPermissionCodes(role.permissionCodes ?? []);
    setMessage('');
  }

  function startNewRole() {
    setSelectedRoleId(null);
    setRoleForm(emptyRoleForm);
    setSelectedPermissionCodes([]);
    setMessage('');
  }

  function selectUser(row: AdminUser) {
    setSelectedUserId(row.id);
    setAssignUserId(row.id);
    setUserForm({
      username: row.username,
      email: row.email,
      phoneNumber: row.phoneNumber,
      password: '',
      userType: row.userType,
      status: row.status,
    });
    setMessage('');
  }

  function startNewUser() {
    setSelectedUserId(null);
    setUserForm(emptyUserForm);
    setMessage('');
  }

  function togglePermission(code: string) {
    setSelectedPermissionCodes((current) =>
      current.includes(code)
        ? current.filter((item) => item !== code)
        : [...current, code].sort(),
    );
  }

  async function handleSaveRole(event: React.FormEvent) {
    event.preventDefault();
    const canSave = selectedRoleId ? canUpdateRole : canCreateRole;
    if (!canSave) return;

    setIsSaving(true);
    setMessage('');
    try {
      const saved = selectedRoleId
        ? await updateRole(selectedRoleId, roleForm)
        : await createRole(roleForm.name.trim(), roleForm.description.trim() || undefined);
      setSelectedRoleId(saved.id);
      setAssignRoleId(saved.id);
      setMessage(`${saved.name} saved.`);
      await loadAccessData();
    } catch {
      setMessage('Role could not be saved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteRole() {
    if (!selectedRoleId || !selectedRole || !canDeleteRole) return;
    if (!window.confirm(`Delete ${selectedRole.name}? Active assignments will be revoked.`)) return;

    setIsSaving(true);
    setMessage('');
    try {
      await deleteRole(selectedRoleId);
      startNewRole();
      setMessage(`${selectedRole.name} deleted.`);
      await loadAccessData();
    } catch {
      setMessage('Role could not be deleted.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAssignPermissions(event: React.FormEvent) {
    event.preventDefault();
    if (!selectedRoleId || !canAssignPermissions) return;

    setIsSaving(true);
    setMessage('');
    try {
      await assignPermissions(selectedRoleId, selectedPermissionCodes);
      setMessage('Permissions assigned.');
      await loadAccessData();
    } catch {
      setMessage('Permissions could not be assigned.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleSaveUser(event: React.FormEvent) {
    event.preventDefault();
    const canSave = selectedUserId ? canUpdateUser : canCreateUser;
    if (!canSave) return;
    if (!selectedUserId && !userForm.password.trim()) {
      setMessage('Password is required for new users.');
      return;
    }

    setIsSaving(true);
    setMessage('');
    const payload: UserMutationRequest = {
      username: userForm.username.trim(),
      email: userForm.email.trim(),
      phoneNumber: userForm.phoneNumber.trim(),
      userType: userForm.userType,
      status: userForm.status,
      ...(userForm.password.trim() ? { password: userForm.password } : {}),
    };

    try {
      const saved = selectedUserId
        ? await updateUser(selectedUserId, payload)
        : await createUser(payload);
      setSelectedUserId(saved.id);
      setAssignUserId(saved.id);
      setMessage(`${saved.username} saved.`);
      await loadAccessData();
    } catch {
      setMessage('User could not be saved.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleDeleteUser() {
    if (!selectedUserId || !selectedUser || !canDeleteUser) return;
    if (!window.confirm(`Delete ${selectedUser.username}? Active role assignments will be revoked.`)) return;

    setIsSaving(true);
    setMessage('');
    try {
      await deleteUser(selectedUserId);
      startNewUser();
      setMessage(`${selectedUser.username} deleted.`);
      await loadAccessData();
    } catch {
      setMessage('User could not be deleted.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleAssignRole(event: React.FormEvent) {
    event.preventDefault();
    if (!assignUserId || !assignRoleId || !canAssignRoles) return;

    setIsSaving(true);
    setMessage('');
    try {
      await assignRoleToUser(assignUserId, assignRoleId);
      const assignedRole = roles.find((role) => role.id === assignRoleId);
      const assignedUser = users.find((row) => row.id === assignUserId);
      setMessage(`${assignedRole?.name ?? 'Role'} assigned to ${assignedUser?.username ?? 'user'}.`);
      await loadAccessData();
    } catch {
      setMessage('Role could not be assigned.');
    } finally {
      setIsSaving(false);
    }
  }

  async function handleRevokeRole(row: AdminUser, assignmentId: number) {
    if (!canAssignRoles) return;

    setIsSaving(true);
    setMessage('');
    try {
      await revokeRoleFromUser(row.id, assignmentId);
      setMessage(`Role revoked from ${row.username}.`);
      await loadAccessData();
    } catch {
      setMessage('Role could not be revoked.');
    } finally {
      setIsSaving(false);
    }
  }

  return (
    <PageScaffold
      title="User & Role Management"
      viewCode="ADMIN_002"
      purpose="Manage users, roles, permissions, and assignments by backend permission tags."
    >
      <div className="grid gap-6">
        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Roles</h2>
              <p className="mt-1 text-xs text-ink/50">{roles.length} roles loaded from backend.</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="secondary" onClick={startNewRole} disabled={!canCreateRole}>New Role</Button>
              <Button variant="secondary" onClick={() => loadAccessData()} disabled={!canReadIdentity}>Refresh</Button>
            </div>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-3 py-2">Role</th>
                  <th className="px-3 py-2">Description</th>
                  <th className="px-3 py-2">Permissions</th>
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
                    <td colSpan={5} className="px-3 py-6 text-center text-sm text-ink/60">No roles available for this permission set.</td>
                  </tr>
                ) : roles.map((role) => (
                  <tr key={role.id} className="text-ink/80">
                    <td className="px-3 py-3 font-semibold text-ink">{role.name}</td>
                    <td className="px-3 py-3">{role.description ?? 'No description'}</td>
                    <td className="px-3 py-3">{role.permissionCodes?.length ?? 0}</td>
                    <td className="px-3 py-3">
                      <Badge variant={role.status === 'ACTIVE' ? 'success' : 'warning'}>{role.status}</Badge>
                    </td>
                    <td className="px-3 py-3 text-right">
                      <Button variant="secondary" onClick={() => selectRole(role)}>Select</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_380px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Permissions</h2>
              <Badge variant="info">{selectedPermissionCodes.length} selected</Badge>
            </div>
            <form onSubmit={handleAssignPermissions} className="mt-4 grid gap-4">
              <Select
                label="Role"
                value={selectedRoleId ? String(selectedRoleId) : ''}
                onChange={(event) => {
                  const nextRole = roles.find((role) => role.id === Number(event.target.value));
                  if (nextRole) selectRole(nextRole);
                }}
                options={roleOptions}
              />
              <Input label="Search Permissions" value={permissionSearch} onChange={(event) => setPermissionSearch(event.target.value)} />
              <div className="grid max-h-72 gap-2 overflow-y-auto rounded-md border border-primary/15 bg-ink/5 p-3">
                {filteredPermissions.length === 0 ? (
                  <p className="text-sm text-ink/55">No permissions returned.</p>
                ) : filteredPermissions.map((permission) => (
                  <label key={permission.code} className="flex gap-3 rounded-md border border-primary/10 bg-surface p-3 text-sm text-ink">
                    <input
                      type="checkbox"
                      checked={selectedPermissionCodes.includes(permission.code)}
                      onChange={() => togglePermission(permission.code)}
                      disabled={!canAssignPermissions}
                      className="mt-1 h-4 w-4 accent-primary"
                    />
                    <span>
                      <span className="block font-semibold">{permission.code}</span>
                      <span className="text-xs text-ink/55">{permission.description ?? 'No description'}</span>
                    </span>
                  </label>
                ))}
              </div>
              <div className="flex flex-wrap gap-2">
                <Button type="submit" variant="secondary" disabled={!selectedRoleId || !canAssignPermissions} isLoading={isSaving}>Save Permissions</Button>
                <Button type="button" variant="secondary" onClick={() => setSelectedPermissionCodes([])} disabled={!canAssignPermissions}>Clear</Button>
              </div>
            </form>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">{selectedRoleId ? 'Edit Role' : 'Create Role'}</h2>
            <form onSubmit={handleSaveRole} className="mt-4 grid gap-3">
              <Input label="Role Name" value={roleForm.name} onChange={(event) => setRoleForm((current) => ({ ...current, name: event.target.value }))} required />
              <Input label="Description" value={roleForm.description} onChange={(event) => setRoleForm((current) => ({ ...current, description: event.target.value }))} />
              <Select
                label="Status"
                value={roleForm.status}
                onChange={(event) => setRoleForm((current) => ({ ...current, status: event.target.value as RoleForm['status'] }))}
                options={roleStatusOptions}
              />
              <div className="flex flex-wrap gap-2">
                <Button type="submit" isLoading={isSaving} disabled={selectedRoleId ? !canUpdateRole : !canCreateRole}>Save Role</Button>
                <Button type="button" variant="secondary" onClick={handleDeleteRole} disabled={!selectedRoleId || !canDeleteRole || isSaving}>Delete</Button>
              </div>
            </form>
          </Card>
        </div>

        <Card className="border border-primary/20 bg-surface/95 p-5">
          <div className="flex flex-wrap items-center justify-between gap-3">
            <div>
              <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Users</h2>
              <p className="mt-1 text-xs text-ink/50">{users.length} users loaded from backend.</p>
            </div>
            <div className="flex flex-wrap gap-2">
              <Button variant="secondary" onClick={startNewUser} disabled={!canCreateUser}>New User</Button>
              <Button variant="secondary" onClick={() => loadAccessData()} disabled={!canReadUsers}>Refresh</Button>
            </div>
          </div>
          <div className="mt-4 overflow-x-auto">
            <table className="min-w-full text-left text-sm">
              <thead className="text-xs uppercase text-ink/50">
                <tr>
                  <th className="px-3 py-2">User</th>
                  <th className="px-3 py-2">Contact</th>
                  <th className="px-3 py-2">Type</th>
                  <th className="px-3 py-2">Roles</th>
                  <th className="px-3 py-2">Status</th>
                  <th className="px-3 py-2 text-right">Action</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-primary/10">
                {isLoading ? (
                  <tr>
                    <td colSpan={6} className="px-3 py-6 text-center text-sm text-ink/60">Loading users...</td>
                  </tr>
                ) : users.length === 0 ? (
                  <tr>
                    <td colSpan={6} className="px-3 py-6 text-center text-sm text-ink/60">No users available for this permission set.</td>
                  </tr>
                ) : users.map((row) => (
                  <tr key={row.id} className="text-ink/80">
                    <td className="px-3 py-3">
                      <p className="font-semibold text-ink">{row.username}</p>
                      <p className="text-xs text-ink/50">{row.permissions.length} permission tags</p>
                    </td>
                    <td className="px-3 py-3">
                      <p>{row.email}</p>
                      <p className="text-xs text-ink/50">{row.phoneNumber}</p>
                    </td>
                    <td className="px-3 py-3">{row.userType}</td>
                    <td className="px-3 py-3">
                      <div className="flex flex-wrap gap-1">
                        {row.roles.length === 0 ? (
                          <span className="text-xs text-ink/45">No roles</span>
                        ) : row.roles.map((assignment) => (
                          <button
                            key={assignment.assignmentId}
                            type="button"
                            onClick={() => handleRevokeRole(row, assignment.assignmentId)}
                            disabled={!canAssignRoles || isSaving}
                            className="rounded-md border border-primary/20 px-2 py-1 text-xs text-primary disabled:opacity-50"
                          >
                            {assignment.roleName}
                          </button>
                        ))}
                      </div>
                    </td>
                    <td className="px-3 py-3">
                      <Badge variant={row.status === 'ACTIVE' ? 'success' : row.status === 'LOCKED' ? 'danger' : 'warning'}>{row.status}</Badge>
                    </td>
                    <td className="px-3 py-3 text-right">
                      <Button variant="secondary" onClick={() => selectUser(row)}>Select</Button>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </Card>

        <div className="grid gap-4 lg:grid-cols-[minmax(0,1fr)_380px]">
          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">Role Assignment</h2>
            <form onSubmit={handleAssignRole} className="mt-4 grid gap-4">
              <Select
                label="User"
                value={assignUserId ? String(assignUserId) : ''}
                onChange={(event) => setAssignUserId(event.target.value ? Number(event.target.value) : null)}
                options={userOptions}
              />
              <Select
                label="Role"
                value={assignRoleId ? String(assignRoleId) : ''}
                onChange={(event) => setAssignRoleId(event.target.value ? Number(event.target.value) : null)}
                options={roleOptions}
              />
              <Button type="submit" disabled={!assignUserId || !assignRoleId || !canAssignRoles} isLoading={isSaving}>Assign Role</Button>
            </form>
          </Card>

          <Card className="border border-primary/20 bg-surface/95 p-5">
            <h2 className="text-sm font-semibold uppercase tracking-wide text-ink/70">{selectedUserId ? 'Edit User' : 'Create User'}</h2>
            <form onSubmit={handleSaveUser} className="mt-4 grid gap-3">
              <Input label="Username" value={userForm.username} onChange={(event) => setUserForm((current) => ({ ...current, username: event.target.value }))} required />
              <Input label="Email" type="email" value={userForm.email} onChange={(event) => setUserForm((current) => ({ ...current, email: event.target.value }))} required />
              <Input label="Phone" value={userForm.phoneNumber} onChange={(event) => setUserForm((current) => ({ ...current, phoneNumber: event.target.value }))} required />
              <Input
                label={selectedUserId ? 'New Password' : 'Password'}
                type="password"
                value={userForm.password}
                onChange={(event) => setUserForm((current) => ({ ...current, password: event.target.value }))}
                required={!selectedUserId}
              />
              <Select
                label="Type"
                value={userForm.userType}
                onChange={(event) => setUserForm((current) => ({ ...current, userType: event.target.value as UserForm['userType'] }))}
                options={userTypeOptions}
              />
              <Select
                label="Status"
                value={userForm.status}
                onChange={(event) => setUserForm((current) => ({ ...current, status: event.target.value as UserForm['status'] }))}
                options={userStatusOptions}
              />
              <div className="flex flex-wrap gap-2">
                <Button type="submit" isLoading={isSaving} disabled={selectedUserId ? !canUpdateUser : !canCreateUser}>Save User</Button>
                <Button type="button" variant="secondary" onClick={handleDeleteUser} disabled={!selectedUserId || !canDeleteUser || isSaving}>Delete</Button>
              </div>
            </form>
          </Card>
        </div>

        {message ? <p className="text-sm text-surface/75">{message}</p> : null}
      </div>
    </PageScaffold>
  );
}
