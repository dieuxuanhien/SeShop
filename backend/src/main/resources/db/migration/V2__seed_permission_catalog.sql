INSERT INTO permissions (code, description)
VALUES
  ('role.create', 'Create custom roles'),
  ('role.permission.assign', 'Assign permissions to roles'),
  ('staff.role.assign', 'Assign or revoke staff roles'),
  ('audit.read', 'View audit logs'),
  ('catalog.write', 'Create or update products and variants'),
  ('inventory.adjust', 'Adjust SKU inventory'),
  ('inventory.transfer', 'Create and process inventory transfers'),
  ('order.read', 'View staff-visible orders'),
  ('order.ship', 'Mark orders shipped'),
  ('refund.process', 'Process refunds and returns'),
  ('promo.manage', 'Manage discount codes'),
  ('social.compose', 'Compose Instagram drafts'),
  ('social.connect', 'Connect or reconnect Instagram account'),
  ('customer.read', 'View customer profiles'),
  ('customer.write', 'Manage customer accounts'),
  ('report.read', 'View sales and inventory reports')
ON CONFLICT (code) DO NOTHING;
