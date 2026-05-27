-- Enhance the development seed with richer RBAC, extra locations, and
-- internet-sourced accessory products for catalog/inventory workflows.
CREATE TEMP TABLE seed_managed_roles (role_name VARCHAR(80) PRIMARY KEY) ON COMMIT DROP;
INSERT INTO seed_managed_roles (role_name)
VALUES ('SUPER_ADMIN'),
  ('STORE_MANAGER'),
  ('STAFF'),
  ('CUSTOMER'),
  ('INVENTORY_MANAGER'),
  ('CASHIER'),
  ('FULFILLMENT_STAFF'),
  ('MARKETING_MANAGER'),
  ('CUSTOMER_SUPPORT'),
  ('FINANCE_MANAGER'),
  ('INVENTORY_AUDITOR'),
  ('LOCATION_SUPERVISOR');
INSERT INTO roles (name, description, status)
VALUES (
    'SUPER_ADMIN',
    'Full platform administrator with every permission enabled.',
    'ACTIVE'
  ),
  (
    'STORE_MANAGER',
    'Store operator for catalog, inventory, order, discount, and social workflows.',
    'ACTIVE'
  ),
  (
    'STAFF',
    'Daily store staff for POS, stock, and order operations.',
    'ACTIVE'
  ),
  (
    'CUSTOMER',
    'Registered shopper role for storefront and self-service flows.',
    'ACTIVE'
  ),
  (
    'INVENTORY_MANAGER',
    'Owns multi-location stock, transfers, receiving, adjustments, and cycle counts.',
    'ACTIVE'
  ),
  (
    'CASHIER',
    'Runs point-of-sale receipts and shift operations at assigned stores.',
    'ACTIVE'
  ),
  (
    'FULFILLMENT_STAFF',
    'Picks, packs, ships, and transfers customer orders across assigned locations.',
    'ACTIVE'
  ),
  (
    'MARKETING_MANAGER',
    'Manages promotions, catalog presentation, reviews, and social commerce drafts.',
    'ACTIVE'
  ),
  (
    'CUSTOMER_SUPPORT',
    'Handles customer profiles, order questions, reviews, returns, and refunds.',
    'ACTIVE'
  ),
  (
    'FINANCE_MANAGER',
    'Manages tax invoices, refund review, audit checks, and reporting.',
    'ACTIVE'
  ),
  (
    'INVENTORY_AUDITOR',
    'Runs stock counts and reads inventory reports without override privileges.',
    'ACTIVE'
  ),
  (
    'LOCATION_SUPERVISOR',
    'Assigns staff to locations and monitors inventory coverage across sites.',
    'ACTIVE'
  ) ON CONFLICT (name) DO
UPDATE
SET description = EXCLUDED.description,
  status = EXCLUDED.status,
  updated_at = NOW();
INSERT INTO users (
    username,
    email,
    phone_number,
    password_hash,
    user_type,
    status
  )
VALUES (
    'inventory.lead',
    'inventory.lead@seshop.local',
    '+84900000004',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ),
  (
    'cashier.d1',
    'cashier.d1@seshop.local',
    '+84900000005',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ),
  (
    'fulfillment.lead',
    'fulfillment.lead@seshop.local',
    '+84900000006',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ),
  (
    'marketing.lead',
    'marketing.lead@seshop.local',
    '+84900000007',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ),
  (
    'support.agent',
    'support.agent@seshop.local',
    '+84900000008',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ),
  (
    'finance.manager',
    'finance.manager@seshop.local',
    '+84900000009',
    '$2a$12$BQASlK4jwjeE9inrRByTj.o.s/4vQSBzNCXWIaTGjTQNfzGXGnfYa',
    'STAFF',
    'ACTIVE'
  ) ON CONFLICT (username) DO
UPDATE
SET email = EXCLUDED.email,
  phone_number = EXCLUDED.phone_number,
  password_hash = EXCLUDED.password_hash,
  user_type = EXCLUDED.user_type,
  status = EXCLUDED.status,
  updated_at = NOW();
CREATE TEMP TABLE seed_role_permissions (
  role_name VARCHAR(80) NOT NULL,
  permission_code VARCHAR(120) NOT NULL,
  PRIMARY KEY (role_name, permission_code)
) ON COMMIT DROP;
INSERT INTO seed_role_permissions (role_name, permission_code)
SELECT 'SUPER_ADMIN',
  p.code
FROM permissions p ON CONFLICT DO NOTHING;
INSERT INTO seed_role_permissions (role_name, permission_code)
VALUES ('STORE_MANAGER', 'role.create'),
  ('STORE_MANAGER', 'role.update'),
  ('STORE_MANAGER', 'role.delete'),
  ('STORE_MANAGER', 'role.permission.assign'),
  ('STORE_MANAGER', 'staff.role.assign'),
  ('STORE_MANAGER', 'staff.user.read'),
  ('STORE_MANAGER', 'staff.user.create'),
  ('STORE_MANAGER', 'staff.user.update'),
  ('STORE_MANAGER', 'staff.user.delete'),
  ('STORE_MANAGER', 'staff.location.assign'),
  ('STORE_MANAGER', 'location.scope.all'),
  ('STORE_MANAGER', 'audit.read'),
  ('STORE_MANAGER', 'catalog.write'),
  ('STORE_MANAGER', 'inventory.adjust'),
  ('STORE_MANAGER', 'inventory.adjust.override'),
  ('STORE_MANAGER', 'inventory.transfer'),
  ('STORE_MANAGER', 'inventory.cycle_count'),
  ('STORE_MANAGER', 'order.read'),
  ('STORE_MANAGER', 'order.ship'),
  ('STORE_MANAGER', 'refund.process'),
  ('STORE_MANAGER', 'promo.manage'),
  ('STORE_MANAGER', 'pos.sell'),
  ('STORE_MANAGER', 'pos.shift.manage'),
  ('STORE_MANAGER', 'invoice.manage'),
  ('STORE_MANAGER', 'social.compose'),
  ('STORE_MANAGER', 'social.connect'),
  ('STORE_MANAGER', 'customer.read'),
  ('STORE_MANAGER', 'customer.write'),
  ('STORE_MANAGER', 'report.read'),
  ('STORE_MANAGER', 'review.moderate'),
  ('STAFF', 'catalog.write'),
  ('STAFF', 'inventory.adjust'),
  ('STAFF', 'inventory.transfer'),
  ('STAFF', 'inventory.cycle_count'),
  ('STAFF', 'order.read'),
  ('STAFF', 'order.ship'),
  ('STAFF', 'refund.process'),
  ('STAFF', 'pos.sell'),
  ('STAFF', 'pos.shift.manage'),
  ('INVENTORY_MANAGER', 'inventory.adjust'),
  ('INVENTORY_MANAGER', 'inventory.adjust.override'),
  ('INVENTORY_MANAGER', 'inventory.transfer'),
  ('INVENTORY_MANAGER', 'inventory.cycle_count'),
  ('INVENTORY_MANAGER', 'report.read'),
  ('INVENTORY_MANAGER', 'location.scope.all'),
  ('CASHIER', 'pos.sell'),
  ('CASHIER', 'pos.shift.manage'),
  ('CASHIER', 'order.read'),
  ('FULFILLMENT_STAFF', 'inventory.transfer'),
  ('FULFILLMENT_STAFF', 'order.read'),
  ('FULFILLMENT_STAFF', 'order.ship'),
  ('MARKETING_MANAGER', 'catalog.write'),
  ('MARKETING_MANAGER', 'promo.manage'),
  ('MARKETING_MANAGER', 'social.compose'),
  ('MARKETING_MANAGER', 'social.connect'),
  ('MARKETING_MANAGER', 'review.moderate'),
  ('MARKETING_MANAGER', 'report.read'),
  ('CUSTOMER_SUPPORT', 'order.read'),
  ('CUSTOMER_SUPPORT', 'refund.process'),
  ('CUSTOMER_SUPPORT', 'customer.read'),
  ('CUSTOMER_SUPPORT', 'customer.write'),
  ('CUSTOMER_SUPPORT', 'review.moderate'),
  ('FINANCE_MANAGER', 'invoice.manage'),
  ('FINANCE_MANAGER', 'refund.process'),
  ('FINANCE_MANAGER', 'report.read'),
  ('FINANCE_MANAGER', 'audit.read'),
  ('INVENTORY_AUDITOR', 'inventory.cycle_count'),
  ('INVENTORY_AUDITOR', 'report.read'),
  ('LOCATION_SUPERVISOR', 'staff.location.assign'),
  ('LOCATION_SUPERVISOR', 'location.scope.all'),
  ('LOCATION_SUPERVISOR', 'inventory.adjust'),
  ('LOCATION_SUPERVISOR', 'inventory.transfer'),
  ('LOCATION_SUPERVISOR', 'report.read') ON CONFLICT DO NOTHING;
DELETE FROM role_permissions rp USING roles r
  JOIN seed_managed_roles managed ON managed.role_name = r.name
WHERE rp.role_id = r.id;
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id,
  p.id
FROM seed_role_permissions srp
  JOIN roles r ON r.name = srp.role_name
  JOIN permissions p ON p.code = srp.permission_code ON CONFLICT (role_id, permission_id) DO NOTHING;
INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id,
  r.id,
  assigner.id
FROM (
    VALUES (
        'inventory.lead',
        'INVENTORY_MANAGER',
        'super.admin'
      ),
      ('cashier.d1', 'CASHIER', 'super.admin'),
      (
        'fulfillment.lead',
        'FULFILLMENT_STAFF',
        'super.admin'
      ),
      (
        'marketing.lead',
        'MARKETING_MANAGER',
        'super.admin'
      ),
      (
        'support.agent',
        'CUSTOMER_SUPPORT',
        'super.admin'
      ),
      (
        'finance.manager',
        'FINANCE_MANAGER',
        'super.admin'
      )
  ) AS v(username, role_name, assigned_by_username)
  JOIN users u ON u.username = v.username
  JOIN roles r ON r.name = v.role_name
  JOIN users assigner ON assigner.username = v.assigned_by_username
WHERE NOT EXISTS (
    SELECT 1
    FROM user_roles ur
    WHERE ur.user_id = u.id
      AND ur.role_id = r.id
      AND ur.revoked_at IS NULL
  );
INSERT INTO locations (code, display_name, location_type, status)
VALUES (
    'ONLINE-HN',
    'Online Fulfillment - Hanoi',
    'STORAGE',
    'ACTIVE'
  ),
  (
    'STORE-D3',
    'District 3 Curated Accessories Studio',
    'STORE',
    'ACTIVE'
  ),
  (
    'STORE-THAO-DIEN',
    'Thao Dien Designer Vintage Store',
    'STORE',
    'ACTIVE'
  ),
  (
    'STORE-HN-OLD',
    'Hanoi Old Quarter Vintage Store',
    'STORE',
    'ACTIVE'
  ),
  (
    'STORAGE-BT',
    'Binh Thanh Returns and Repair Hub',
    'STORAGE',
    'ACTIVE'
  ),
  (
    'STORAGE-DN',
    'Da Nang Regional Stockroom',
    'STORAGE',
    'ACTIVE'
  ) ON CONFLICT (code) DO
UPDATE
SET display_name = EXCLUDED.display_name,
  location_type = EXCLUDED.location_type,
  status = EXCLUDED.status;
INSERT INTO staff_location_assignments (user_id, location_id, assigned_by)
SELECT staff.id,
  location.id,
  admin.id
FROM (
    VALUES ('staff.manager', 'ONLINE-HCM'),
      ('staff.manager', 'STORE-D1'),
      ('staff.manager', 'STORE-D3'),
      ('staff.manager', 'STORE-THAO-DIEN'),
      ('inventory.lead', 'ONLINE-HCM'),
      ('inventory.lead', 'ONLINE-HN'),
      ('inventory.lead', 'STORAGE-Q7'),
      ('inventory.lead', 'STORAGE-BT'),
      ('inventory.lead', 'STORAGE-DN'),
      ('cashier.d1', 'STORE-D1'),
      ('fulfillment.lead', 'ONLINE-HCM'),
      ('fulfillment.lead', 'ONLINE-HN'),
      ('fulfillment.lead', 'STORAGE-BT'),
      ('marketing.lead', 'STORE-D3'),
      ('support.agent', 'STORE-THAO-DIEN'),
      ('finance.manager', 'ONLINE-HCM')
  ) AS v(username, location_code)
  JOIN users staff ON staff.username = v.username
  JOIN locations location ON location.code = v.location_code
  JOIN users admin ON admin.username = 'super.admin'
WHERE NOT EXISTS (
    SELECT 1
    FROM staff_location_assignments existing
    WHERE existing.user_id = staff.id
      AND existing.location_id = location.id
      AND existing.revoked_at IS NULL
  );
CREATE TEMP TABLE seed_extra_accessories (
  source_sku VARCHAR(40) PRIMARY KEY,
  product_name VARCHAR(200) NOT NULL,
  brand VARCHAR(120),
  product_type VARCHAR(80) NOT NULL,
  size_label VARCHAR(30),
  color VARCHAR(30),
  price_vnd NUMERIC(12, 2) NOT NULL,
  condition_label VARCHAR(40),
  gender_label VARCHAR(20),
  fabric VARCHAR(80),
  era VARCHAR(40),
  image_url TEXT NOT NULL,
  category_name VARCHAR(120) NOT NULL
) ON COMMIT DROP;
INSERT INTO seed_extra_accessories (
    source_sku,
    product_name,
    brand,
    product_type,
    size_label,
    color,
    price_vnd,
    condition_label,
    gender_label,
    fabric,
    era,
    image_url,
    category_name
  )
VALUES (
    'ACC-BELT-001',
    'Vintage Black Leather Belt - One Size Black Leather',
    'Unbranded',
    'Belt',
    'One Size',
    'Black',
    490000,
    'Very Good',
    'Unisex',
    'Leather',
    '2000S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Leather_belt.jpg?width=1200',
    'Vintage Accessories'
  ),
  (
    'ACC-BAG-001',
    'Kate Spade Structured Leather Handbag - One Size Cream Leather',
    'Kate Spade',
    'Handbag',
    'One Size',
    'Cream',
    3200000,
    'Excellent',
    'Womens',
    'Leather',
    '2010S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Kate_Spade_handbag.jpg?width=1200',
    'Designer Accessories'
  ),
  (
    'ACC-SCARF-001',
    'Late 19th Century Silk Scarf - One Size Multicoloured Silk',
    'Unbranded',
    'Scarf',
    'One Size',
    'Multicoloured',
    1250000,
    'Good',
    'Womens',
    'Silk',
    'Pre-1950S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Scarf,_late_19th_century_(CH_18466163).jpg?width=1200',
    'Vintage Accessories'
  ),
  (
    'ACC-CAP-001',
    'Festival Baseball Cap - One Size Blue Cotton',
    'Unbranded',
    'Cap',
    'One Size',
    'Blue',
    380000,
    'Very Good',
    'Unisex',
    'Cotton',
    '2010S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Baseball_cap_(15930546668).jpg?width=1200',
    'Vintage Accessories'
  ),
  (
    'ACC-WATCH-001',
    'Fossil Minimal Dial Wristwatch - One Size Brown Leather',
    'Fossil',
    'Watch',
    'One Size',
    'Brown',
    2600000,
    'Very Good',
    'Unisex',
    'Leather/Steel',
    '2010S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Fossil_wristwatch_with_white_background.jpg?width=1200',
    'Jewelry & Watches'
  ),
  (
    'ACC-WALLET-001',
    'Vintage Men''s Brown Leather Wallet - One Size Brown Leather',
    'Unbranded',
    'Wallet',
    'One Size',
    'Brown',
    620000,
    'Good',
    'Mens',
    'Leather',
    '2000S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/A_men%27s_wallet.jpg?width=1200',
    'Vintage Accessories'
  ),
  (
    'ACC-TOTE-001',
    'QWSTION Black Flap Tote - One Size Black Canvas',
    'QWSTION',
    'Tote Bag',
    'One Size',
    'Black',
    1800000,
    'Excellent',
    'Unisex',
    'Canvas',
    '2010S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/QWSTION-FLAP-TOTE-MEDIUM-ALL-BLACK-FRONT.jpg?width=1200',
    'Bags & Totes'
  ),
  (
    'ACC-NECKLACE-001',
    'Vintage Sapphire Accent Necklace - One Size Gold Metal',
    'Unbranded',
    'Necklace',
    'One Size',
    'Gold',
    2100000,
    'Very Good',
    'Womens',
    'Metal/Stone',
    '1990S',
    'https://commons.wikimedia.org/wiki/Special:FilePath/Necklace_-_Meta_Overbeck_(27770427469).jpg?width=1200',
    'Jewelry & Watches'
  );
INSERT INTO categories (name, description)
SELECT DISTINCT category_name,
  'Internet-sourced accessory seed category for ' || category_name || '.'
FROM seed_extra_accessories ON CONFLICT (name) DO NOTHING;
INSERT INTO products (name, brand, description, status)
SELECT s.product_name,
  s.brand,
  'Seeded from internet-sourced accessory imagery for demo merchandising. Condition: ' || s.condition_label || '. Gender: ' || s.gender_label || '. Era: ' || s.era || '. Fabric: ' || s.fabric || '.',
  'PUBLISHED'
FROM seed_extra_accessories s
WHERE NOT EXISTS (
    SELECT 1
    FROM product_variants pv
    WHERE pv.sku_code = 'THR-' || s.source_sku
  );
INSERT INTO product_variants (product_id, sku_code, attributes, price, status)
SELECT p.id,
  'THR-' || s.source_sku,
  jsonb_strip_nulls(
    jsonb_build_object('size', s.size_label, 'color', s.color)
  ),
  s.price_vnd,
  'ACTIVE'
FROM seed_extra_accessories s
  JOIN LATERAL (
    SELECT id
    FROM products p
    WHERE p.name = s.product_name
      AND COALESCE(p.brand, '') = COALESCE(s.brand, '')
    ORDER BY p.id
    LIMIT 1
  ) p ON TRUE
WHERE NOT EXISTS (
    SELECT 1
    FROM product_variants pv
    WHERE pv.sku_code = 'THR-' || s.source_sku
  );
INSERT INTO product_images (
    product_id,
    variant_id,
    url,
    sort_order,
    is_instagram_ready
  )
SELECT pv.product_id,
  pv.id,
  s.image_url,
  0,
  TRUE
FROM seed_extra_accessories s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
WHERE NOT EXISTS (
    SELECT 1
    FROM product_images pi
    WHERE pi.variant_id = pv.id
      AND pi.url = s.image_url
  );
INSERT INTO product_categories (product_id, category_id)
SELECT DISTINCT pv.product_id,
  c.id
FROM seed_extra_accessories s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
  JOIN categories c ON c.name = s.category_name ON CONFLICT (product_id, category_id) DO NOTHING;
INSERT INTO product_categories (product_id, category_id)
SELECT DISTINCT pv.product_id,
  c.id
FROM seed_extra_accessories s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
  JOIN categories c ON c.name = 'New Arrivals' ON CONFLICT (product_id, category_id) DO NOTHING;
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  2 + (sv.rn % 3),
  CASE
    WHEN sv.rn % 4 = 0 THEN 1
    ELSE 0
  END
FROM seeded_variants sv
  JOIN locations l ON l.code = 'ONLINE-HCM' ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  1 + (sv.rn % 2),
  0
FROM seeded_variants sv
  JOIN locations l ON l.code = 'STORE-D1'
WHERE sv.rn <= 4 ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  1 + (sv.rn % 3),
  0
FROM seeded_variants sv
  JOIN locations l ON l.code = 'STORE-THAO-DIEN'
WHERE sv.rn BETWEEN 3 AND 8 ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  2,
  0
FROM seeded_variants sv
  JOIN locations l ON l.code = 'ONLINE-HN'
WHERE sv.rn >= 5 ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  3,
  0
FROM seeded_variants sv
  JOIN locations l ON l.code = 'STORAGE-BT' ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
WITH seeded_variants AS (
  SELECT pv.id AS variant_id,
    ROW_NUMBER() OVER (
      ORDER BY pv.sku_code
    ) AS rn
  FROM seed_extra_accessories s
    JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (
    variant_id,
    location_id,
    on_hand_qty,
    reserved_qty
  )
SELECT sv.variant_id,
  l.id,
  2,
  0
FROM seeded_variants sv
  JOIN locations l ON l.code = 'STORAGE-DN'
WHERE sv.rn % 2 = 0 ON CONFLICT (variant_id, location_id) DO
UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
  reserved_qty = EXCLUDED.reserved_qty,
  updated_at = NOW();
INSERT INTO audit_logs (
    actor_user_id,
    action,
    target_type,
    target_id,
    metadata_json,
    created_at
  )
SELECT u.id,
  'ROLE_PERMISSION_ASSIGNED',
  'SeedData',
  'V12_RBAC_MATRIX',
  '{"source":"V12__enhance_demo_seed_catalog","managedRoles":12}'::jsonb,
  TIMESTAMPTZ '2026-05-12 08:00:00+07'
FROM users u
WHERE u.username = 'super.admin'
UNION ALL
SELECT u.id,
  'PRODUCT_CREATED',
  'SeedData',
  'ACCESSORY_BATCH_2026_05',
  '{"source":"Wikimedia Commons","items":8}'::jsonb,
  TIMESTAMPTZ '2026-05-12 08:10:00+07'
FROM users u
WHERE u.username = 'marketing.lead'
UNION ALL
SELECT u.id,
  'INVENTORY_ADJUSTED',
  'SeedData',
  'MULTI_LOCATION_ACCESSORIES',
  '{"locations":["ONLINE-HCM","STORE-D1","STORE-THAO-DIEN","ONLINE-HN","STORAGE-BT","STORAGE-DN"]}'::jsonb,
  TIMESTAMPTZ '2026-05-12 08:20:00+07'
FROM users u
WHERE u.username = 'inventory.lead';
DROP TABLE seed_extra_accessories;
DROP TABLE seed_role_permissions;
DROP TABLE seed_managed_roles;