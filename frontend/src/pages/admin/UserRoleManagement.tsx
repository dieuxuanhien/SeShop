import { PageScaffold } from '@/shared/ui/PageScaffold';

export function UserRoleManagement() {
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
    />
  );
}
