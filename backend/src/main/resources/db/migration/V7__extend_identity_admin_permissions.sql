INSERT INTO permissions (code, description)
VALUES
  ('role.update', 'Edit custom roles'),
  ('role.delete', 'Delete or deactivate custom roles'),
  ('staff.user.read', 'View staff and customer user accounts'),
  ('staff.user.create', 'Create staff or customer user accounts'),
  ('staff.user.update', 'Edit staff or customer user accounts'),
  ('staff.user.delete', 'Deactivate staff or customer user accounts')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
  'role.create',
  'role.update',
  'role.delete',
  'role.permission.assign',
  'staff.role.assign',
  'staff.user.read',
  'staff.user.create',
  'staff.user.update',
  'staff.user.delete'
)
WHERE r.name IN ('SUPER_ADMIN', 'STORE_MANAGER')
ON CONFLICT (role_id, permission_id) DO NOTHING;
