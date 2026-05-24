INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code = 'inventory.cycle_count'
WHERE r.name IN ('STORE_MANAGER', 'STAFF')
ON CONFLICT (role_id, permission_id) DO NOTHING;
