CREATE TABLE staff_location_assignments (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  location_id BIGINT NOT NULL REFERENCES locations(id),
  assigned_by BIGINT REFERENCES users(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ
);

CREATE UNIQUE INDEX ux_staff_location_assignments_active
  ON staff_location_assignments (user_id, location_id)
  WHERE revoked_at IS NULL;

CREATE INDEX idx_staff_location_assignments_user_active
  ON staff_location_assignments (user_id)
  WHERE revoked_at IS NULL;

CREATE INDEX idx_staff_location_assignments_location_active
  ON staff_location_assignments (location_id)
  WHERE revoked_at IS NULL;

INSERT INTO permissions (code, description)
VALUES
  ('staff.location.assign', 'Assign staff accounts to store or storage locations'),
  ('location.scope.all', 'Read and manage data across every store and storage location')
ON CONFLICT (code) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('staff.location.assign', 'location.scope.all')
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO staff_location_assignments (user_id, location_id, assigned_by)
SELECT staff.id, location.id, admin.id
FROM users staff
JOIN locations location ON location.code = 'STORE-D1'
JOIN users admin ON admin.username = 'super.admin'
WHERE staff.username = 'staff.manager'
  AND NOT EXISTS (
    SELECT 1
    FROM staff_location_assignments existing
    WHERE existing.user_id = staff.id
      AND existing.location_id = location.id
      AND existing.revoked_at IS NULL
  );
