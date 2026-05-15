-- Seed a complete development database with accounts, operations, and a
-- Thrifted.com public catalog snapshot captured from /products.json on 2026-05-08.

CREATE TEMP TABLE seed_thrifted_products (
  source_sku VARCHAR(40) PRIMARY KEY,
  product_name VARCHAR(200) NOT NULL,
  brand VARCHAR(120),
  product_type VARCHAR(80) NOT NULL,
  size_label VARCHAR(30),
  color VARCHAR(30),
  price_vnd NUMERIC(12,2) NOT NULL,
  condition_label VARCHAR(40),
  gender_label VARCHAR(20),
  fabric VARCHAR(80),
  era VARCHAR(40),
  image_url TEXT NOT NULL,
  category_name VARCHAR(120) NOT NULL
);

INSERT INTO seed_thrifted_products (
  source_sku, product_name, brand, product_type, size_label, color, price_vnd,
  condition_label, gender_label, fabric, era, image_url, category_name
) VALUES
  ('93595-A', 'Classic Weekend ''94 Hanes Graphic T-Shirt - Large Yellow Cotton Blend', 'Hanes', 'T-Shirt', 'Large', 'Yellow', 913000, 'Good', 'Mens', 'Cotton Blend', '1990S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/CB7D3359-477F-4A08-9B80-2AA355A84F2A.webp?v=1778238048', 'Graphic T-Shirts'),
  ('93594-A', 'Carhartt T-Shirt - Small Orange Cotton', 'Carhartt', 'T-Shirt', 'Small', 'Orange', 803000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/AAE549A9-C63A-4E13-A753-DBF67DC3FC6C.webp?v=1778238045', 'Workwear Basics'),
  ('93593-A', '1994 Grateful Dead Summer Tour Liquid Blue Band T-Shirt - XL White Cotton', 'Liquid Blue', 'T-Shirt', 'XL', 'White', 5477000, 'Good', 'Mens', 'Cotton', '1990S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/3B1DE44A-D6C5-41E4-A80B-51D707F89C9D.webp?v=1778238042', 'Band Tees'),
  ('93592-A', 'Screen Stars T-Shirt - XL Grey Cotton Blend', 'Screen Stars', 'T-Shirt', 'XL', 'Grey', 803000, 'Good', 'Mens', 'Cotton Blend', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/D4AFD018-D49D-4EBD-8079-4FE5D876C00D.webp?v=1778238039', 'Graphic T-Shirts'),
  ('93591-A', 'Polo By Ralph Lauren Spellout Long Sleeve T-Shirt - 2XL White Cotton', 'Polo by Ralph Lauren', 'Long Sleeve T-Shirt', '2XL', 'White', 1534000, 'Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/547C60A1-C688-4953-8F18-F2F2F3655FC4.webp?v=1778238036', 'Long Sleeve T-Shirts'),
  ('93585-A', 'Stussy Tie-Dye T-Shirt - Medium Multicoloured Cotton', 'Stussy', 'T-Shirt', 'Medium', 'Multicoloured', 1643000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/7092657B-02FB-4EF5-B91D-42202496CBF3.webp?v=1778238034', 'Streetwear'),
  ('93584-A', 'Maui Unbranded Graphic T-Shirt - Large Yellow Cotton', 'Unbranded', 'T-Shirt', 'Large', 'Yellow', 730000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/322A3335-A1AC-48B3-8DF7-C2A560D8D77A.webp?v=1778238031', 'Graphic T-Shirts'),
  ('93583-A', 'Tommy Hilfiger T-Shirt - XL Beige Cotton', 'Tommy Hilfiger', 'T-Shirt', 'XL', 'Beige', 621000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/92C89449-C870-409E-8937-7B056CB6C9BA.webp?v=1778238027', 'Designer Vintage'),
  ('93582-A', 'Carhartt T-Shirt - XL Green Cotton', 'Carhartt', 'T-Shirt', 'XL', 'Green', 803000, 'Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/B58D557B-5154-462E-B4C0-093A25D16DB3.webp?v=1778238024', 'Workwear Basics'),
  ('93581-A', 'Sean John Spellout T-Shirt - XL Yellow Cotton', 'Sean John', 'T-Shirt', 'XL', 'Yellow', 913000, 'Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/F0C79240-7F26-414E-9085-D75F698A176B.webp?v=1778238021', 'Streetwear'),
  ('93579-A', 'True Religion Graphic T-Shirt - Large Black Cotton', 'True Religion', 'T-Shirt', 'Large', 'Black', 913000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/C0BA0109-B9AB-4904-A3FE-4566F4583DD5.webp?v=1778238015', 'Designer Vintage'),
  ('93578-A', 'Packers Nfl Football Shirt - Large Green Polyester', 'NFL', 'Football Shirt', 'Large', 'Green', 913000, 'Very Good', 'Mens', 'Polyester', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/C5D81507-EA05-49AD-91DB-F1BB9696EBB9.webp?v=1778238012', 'Sports Jerseys'),
  ('93576-A', 'Logo Athletics T-Shirt - Large Green Cotton', 'Logo Athletics', 'T-Shirt', 'Large', 'Green', 803000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/59D10223-1006-4E2A-B9D4-BEBF89D79FEF.webp?v=1778238008', 'Sportswear'),
  ('93570-A', 'Dickies T-Shirt - Large Black Cotton', 'Dickies', 'T-Shirt', 'Large', 'Black', 548000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/0BC427C0-EDB7-4CA2-AC0B-07D31B008C44.webp?v=1778237998', 'Workwear Basics'),
  ('93569-A', 'George Thorogood And The Destroyers Unbranded Band T-Shirt - Large Black Cotton', 'Unbranded', 'T-Shirt', 'Large', 'Black', 1278000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/E95F9632-A584-46F8-BD5E-1672087D0746.webp?v=1778237995', 'Band Tees'),
  ('93563-A', 'Polo By Ralph Lauren Polo Shirt - XL Green Cotton', 'Polo by Ralph Lauren', 'Polo Shirt', 'XL', 'Green', 1278000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/16509324-C6CD-40F4-8634-200E848A5A54.webp?v=1778237978', 'Polo Shirts'),
  ('93562-A', 'Skull Mona Lisa Social Collision Graphic T-Shirt - Medium Black Polyester', 'Social Collision', 'T-Shirt', 'Medium', 'Black', 1278000, 'Very Good', 'Mens', 'Polyester', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/81A9B3C6-546D-43BD-BDE5-6EB10E4329C2.webp?v=1778237976', 'Graphic T-Shirts'),
  ('93558-A', 'Golden State Warriors Hardwood Classics Nba Graphic Long Sleeve T-Shirt - Small Black Cotton', 'Hardwood Classics Nba', 'Long Sleeve T-Shirt', 'Small', 'Black', 1570000, 'Very Good', 'Mens', 'Cotton', '2000S', 'https://cdn.shopify.com/s/files/1/0556/1841/files/F7D8C41D-2502-4799-A4F8-AD2F27369F8C.webp?v=1778237968', 'Sportswear'),
  ('LVR-001', 'Louis Vuitton Monogram Pochette Accessoires', 'Louis Vuitton', 'Bag', 'One Size', 'Brown', 25000000, 'Excellent', 'Womens', 'Canvas', '2010S', 'https://images.vestiairecollective.com/cdn-cgi/image/width=1000,quality=80,format=auto,f=intrinsic/produit/35824589-1_1.jpg', 'Designer Accessories'),
  ('GUCCI-001', 'Gucci Double G Buckle Leather Belt', 'Gucci', 'Belt', '90cm', 'Black', 8500000, 'Pristine', 'Unisex', 'Leather', '2020S', 'https://media.gucci.com/style/DarkGray_Center_0_0_2400x2400/1473432313/414516_AP00T_1000_001_100_0000_Light-Leather-belt-with-Double-G-buckle.jpg', 'Designer Accessories'),
  ('NIKE-SB-001', 'Nike SB Dunk Low Pro ''Chicago''', 'Nike', 'Sneakers', 'US 10', 'Red/White/Black', 12500000, 'Deadstock', 'Mens', 'Leather', '2020S', 'https://images.stockx.com/images/Nike-SB-Dunk-Low-J-Pack-Chicago-Product.jpg?fit=fill&bg=FFFFFF&w=700&h=500&auto=format,compress&q=90&dpr=2&trim=color&updated_at=1606322238', 'Sneakers');

INSERT INTO roles (name, description, status)
VALUES
  ('SUPER_ADMIN', 'Full platform administrator with every permission enabled.', 'ACTIVE'),
  ('STORE_MANAGER', 'Store operator for catalog, inventory, order, discount, and social workflows.', 'ACTIVE'),
  ('STAFF', 'Daily store staff for POS, stock, and order operations.', 'ACTIVE'),
  ('CUSTOMER', 'Registered shopper role for storefront and self-service flows.', 'ACTIVE')
ON CONFLICT (name) DO UPDATE
SET description = EXCLUDED.description,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO users (username, email, phone_number, password_hash, user_type, status)
VALUES
  ('super.admin', 'super.admin@seshop.local', '+84900000001', '$2a$10$4JcGmhnLbnYpZ3ieV.IlvuFrgvSjkmXy0160MZvofelnSXP12aGCq', 'ADMIN', 'ACTIVE'),
  ('staff.manager', 'staff.manager@seshop.local', '+84900000002', '$2a$10$FF27zESs/E9SWwgL.ocbEuLN29sbbUbHXmis1W3NhRszDF//2dHLG', 'STAFF', 'ACTIVE'),
  ('demo.customer', 'customer@seshop.local', '+84900000003', '$2a$10$khhyCAMvKx3pBUjBaZxA2e/AUPqkmtdogJWLVmuTufrpZexukO5py', 'CUSTOMER', 'ACTIVE')
ON CONFLICT (username) DO UPDATE
SET email = EXCLUDED.email,
    phone_number = EXCLUDED.phone_number,
    password_hash = EXCLUDED.password_hash,
    user_type = EXCLUDED.user_type,
    status = EXCLUDED.status,
    updated_at = NOW();

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
CROSS JOIN permissions p
WHERE r.name = 'SUPER_ADMIN'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN (
  'catalog.write',
  'inventory.adjust',
  'inventory.transfer',
  'order.read',
  'order.ship',
  'refund.process',
  'promo.manage',
  'pos.sell',
  'pos.shift.manage',
  'invoice.manage',
  'social.compose',
  'social.connect',
  'customer.read',
  'report.read'
)
WHERE r.name = 'STORE_MANAGER'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
JOIN permissions p ON p.code IN ('inventory.adjust', 'inventory.transfer', 'order.read')
WHERE r.name = 'STAFF'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO user_roles (user_id, role_id, assigned_by)
SELECT u.id, r.id, assigner.id
FROM (VALUES
  ('super.admin', 'SUPER_ADMIN', 'super.admin'),
  ('staff.manager', 'STORE_MANAGER', 'super.admin'),
  ('demo.customer', 'CUSTOMER', 'super.admin')
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

INSERT INTO categories (name, description)
SELECT DISTINCT category_name, 'Scraped Thrifted.com seed category for ' || category_name || '.'
FROM seed_thrifted_products
ON CONFLICT (name) DO NOTHING;

INSERT INTO categories (name, description)
VALUES
  ('New Arrivals', 'Recently scraped seed products from Thrifted.com.'),
  ('Vintage Essentials', 'Core second-hand clothing pieces for storefront merchandising.'),
  ('Designer Accessories', 'Luxury items and accessories.'),
  ('Sneakers', 'Collectible and vintage footwear.')
ON CONFLICT (name) DO NOTHING;

INSERT INTO products (name, brand, description, status)
SELECT
  s.product_name,
  s.brand,
  'Seeded from Thrifted.com public catalog snapshot on 2026-05-08. Condition: '
    || s.condition_label || '. Gender: ' || s.gender_label || '. Era: ' || s.era
    || '. Fabric: ' || s.fabric || '.',
  'PUBLISHED'
FROM seed_thrifted_products s
WHERE NOT EXISTS (
  SELECT 1
  FROM product_variants pv
  WHERE pv.sku_code = 'THR-' || s.source_sku
);

INSERT INTO product_variants (product_id, sku_code, size, color, price, status)
SELECT p.id, 'THR-' || s.source_sku, s.size_label, s.color, s.price_vnd, 'ACTIVE'
FROM seed_thrifted_products s
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

INSERT INTO product_images (product_id, variant_id, url, sort_order, is_instagram_ready)
SELECT pv.product_id, pv.id, s.image_url, 0, TRUE
FROM seed_thrifted_products s
JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
WHERE NOT EXISTS (
  SELECT 1
  FROM product_images pi
  WHERE pi.variant_id = pv.id
    AND pi.url = s.image_url
);

INSERT INTO product_categories (product_id, category_id)
SELECT DISTINCT pv.product_id, c.id
FROM seed_thrifted_products s
JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
JOIN categories c ON c.name = s.category_name
ON CONFLICT (product_id, category_id) DO NOTHING;

INSERT INTO product_categories (product_id, category_id)
SELECT DISTINCT pv.product_id, c.id
FROM seed_thrifted_products s
JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
JOIN categories c ON c.name = 'New Arrivals'
ON CONFLICT (product_id, category_id) DO NOTHING;

INSERT INTO locations (code, display_name, location_type, status)
VALUES
  ('ONLINE-HCM', 'Online Fulfillment - Ho Chi Minh', 'STORAGE', 'ACTIVE'),
  ('STORE-D1', 'District 1 Vintage Store', 'STORE', 'ACTIVE'),
  ('STORAGE-Q7', 'District 7 Back Stock', 'STORAGE', 'ACTIVE')
ON CONFLICT (code) DO UPDATE
SET display_name = EXCLUDED.display_name,
    location_type = EXCLUDED.location_type,
    status = EXCLUDED.status;

WITH seeded_variants AS (
  SELECT pv.id AS variant_id, ROW_NUMBER() OVER (ORDER BY pv.sku_code) AS rn
  FROM seed_thrifted_products s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (variant_id, location_id, on_hand_qty, reserved_qty)
SELECT sv.variant_id, l.id, 2 + (sv.rn % 4), CASE WHEN sv.rn % 5 = 0 THEN 1 ELSE 0 END
FROM seeded_variants sv
JOIN locations l ON l.code = 'ONLINE-HCM'
ON CONFLICT (variant_id, location_id) DO UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
    reserved_qty = EXCLUDED.reserved_qty,
    updated_at = NOW();

WITH seeded_variants AS (
  SELECT pv.id AS variant_id, ROW_NUMBER() OVER (ORDER BY pv.sku_code) AS rn
  FROM seed_thrifted_products s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (variant_id, location_id, on_hand_qty, reserved_qty)
SELECT sv.variant_id, l.id, 1 + (sv.rn % 2), 0
FROM seeded_variants sv
JOIN locations l ON l.code = 'STORE-D1'
WHERE sv.rn <= 12
ON CONFLICT (variant_id, location_id) DO UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
    reserved_qty = EXCLUDED.reserved_qty,
    updated_at = NOW();

WITH seeded_variants AS (
  SELECT pv.id AS variant_id, ROW_NUMBER() OVER (ORDER BY pv.sku_code) AS rn
  FROM seed_thrifted_products s
  JOIN product_variants pv ON pv.sku_code = 'THR-' || s.source_sku
)
INSERT INTO inventory_balances (variant_id, location_id, on_hand_qty, reserved_qty)
SELECT sv.variant_id, l.id, 3, 0
FROM seeded_variants sv
JOIN locations l ON l.code = 'STORAGE-Q7'
WHERE sv.rn <= 6
ON CONFLICT (variant_id, location_id) DO UPDATE
SET on_hand_qty = EXCLUDED.on_hand_qty,
    reserved_qty = EXCLUDED.reserved_qty,
    updated_at = NOW();

INSERT INTO suppliers (name, tax_code, phone, email, address, status)
SELECT v.name, v.tax_code, v.phone, v.email, v.address, v.status
FROM (VALUES
  ('Thrifted Vintage Wholesale', 'THRIFTED-UK-001', '+442000000001', 'theteam@thrifted.com', 'Seed supplier based on Thrifted.com public catalog data.', 'ACTIVE'),
  ('Circular Supply Co.', 'CIRCULAR-VN-001', '+84900000010', 'ops@circular.example', 'Ho Chi Minh City circular fashion partner.', 'ACTIVE')
) AS v(name, tax_code, phone, email, address, status)
WHERE NOT EXISTS (
  SELECT 1
  FROM suppliers s
  WHERE s.name = v.name
);

INSERT INTO purchase_orders (po_number, supplier_id, destination_location_id, status, created_by, approved_by, created_at, approved_at)
SELECT 'PO-THR-2026-0001', s.id, l.id, 'RECEIVED', u.id, u.id, TIMESTAMPTZ '2026-05-06 09:00:00+07', TIMESTAMPTZ '2026-05-06 10:00:00+07'
FROM suppliers s
JOIN locations l ON l.code = 'ONLINE-HCM'
JOIN users u ON u.username = 'staff.manager'
WHERE s.name = 'Thrifted Vintage Wholesale'
ON CONFLICT (po_number) DO NOTHING;

INSERT INTO purchase_orders (po_number, supplier_id, destination_location_id, status, created_by, created_at)
SELECT 'PO-THR-2026-0002', s.id, l.id, 'OPEN', u.id, TIMESTAMPTZ '2026-05-08 08:30:00+07'
FROM suppliers s
JOIN locations l ON l.code = 'STORE-D1'
JOIN users u ON u.username = 'staff.manager'
WHERE s.name = 'Circular Supply Co.'
ON CONFLICT (po_number) DO NOTHING;

INSERT INTO purchase_order_items (purchase_order_id, variant_id, ordered_qty, unit_cost)
SELECT po.id, pv.id, v.ordered_qty, ROUND(pv.price * 0.55, 2)
FROM (VALUES
  ('PO-THR-2026-0001', 'THR-93595-A', 4),
  ('PO-THR-2026-0001', 'THR-93594-A', 5),
  ('PO-THR-2026-0001', 'THR-93593-A', 2),
  ('PO-THR-2026-0001', 'THR-93585-A', 3),
  ('PO-THR-2026-0002', 'THR-93591-A', 2),
  ('PO-THR-2026-0002', 'THR-93563-A', 2)
) AS v(po_number, sku_code, ordered_qty)
JOIN purchase_orders po ON po.po_number = v.po_number
JOIN product_variants pv ON pv.sku_code = v.sku_code
ON CONFLICT (purchase_order_id, variant_id) DO NOTHING;

INSERT INTO goods_receipts (purchase_order_id, received_by, received_at, status)
SELECT po.id, u.id, TIMESTAMPTZ '2026-05-07 14:00:00+07', 'RECEIVED'
FROM purchase_orders po
JOIN users u ON u.username = 'staff.manager'
WHERE po.po_number = 'PO-THR-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM goods_receipts gr
    WHERE gr.purchase_order_id = po.id
      AND gr.status = 'RECEIVED'
  );

INSERT INTO goods_receipt_items (goods_receipt_id, variant_id, received_qty, damaged_qty)
SELECT gr.id, poi.variant_id, poi.ordered_qty, CASE WHEN pv.sku_code = 'THR-93593-A' THEN 1 ELSE 0 END
FROM purchase_orders po
JOIN LATERAL (
  SELECT id
  FROM goods_receipts
  WHERE purchase_order_id = po.id
  ORDER BY id
  LIMIT 1
) gr ON TRUE
JOIN purchase_order_items poi ON poi.purchase_order_id = po.id
JOIN product_variants pv ON pv.id = poi.variant_id
WHERE po.po_number = 'PO-THR-2026-0001'
ON CONFLICT (goods_receipt_id, variant_id) DO NOTHING;

INSERT INTO inventory_transfers (source_location_id, destination_location_id, status, created_by, created_at, completed_at)
SELECT src.id, dst.id, 'COMPLETED', u.id, TIMESTAMPTZ '2026-05-07 16:00:00+07', TIMESTAMPTZ '2026-05-07 18:00:00+07'
FROM locations src
JOIN locations dst ON dst.code = 'STORE-D1'
JOIN users u ON u.username = 'staff.manager'
WHERE src.code = 'ONLINE-HCM'
  AND NOT EXISTS (
    SELECT 1
    FROM inventory_transfers it
    WHERE it.source_location_id = src.id
      AND it.destination_location_id = dst.id
      AND it.created_at = TIMESTAMPTZ '2026-05-07 16:00:00+07'
  );

INSERT INTO inventory_transfer_items (transfer_id, variant_id, qty, received_qty, damaged_qty)
SELECT transfer.id, pv.id, v.qty, v.qty, 0
FROM (VALUES
  ('THR-93595-A', 1),
  ('THR-93594-A', 2),
  ('THR-93585-A', 1)
) AS v(sku_code, qty)
JOIN product_variants pv ON pv.sku_code = v.sku_code
JOIN LATERAL (
  SELECT it.id
  FROM inventory_transfers it
  JOIN locations src ON src.id = it.source_location_id
  JOIN locations dst ON dst.id = it.destination_location_id
  WHERE src.code = 'ONLINE-HCM'
    AND dst.code = 'STORE-D1'
    AND it.created_at = TIMESTAMPTZ '2026-05-07 16:00:00+07'
  ORDER BY it.id
  LIMIT 1
) transfer ON TRUE
ON CONFLICT (transfer_id, variant_id) DO NOTHING;

INSERT INTO cycle_counts (location_id, status, started_by, approved_by, started_at, approved_at)
SELECT l.id, 'APPROVED', staff.id, admin.id, TIMESTAMPTZ '2026-05-08 07:30:00+07', TIMESTAMPTZ '2026-05-08 08:15:00+07'
FROM locations l
JOIN users staff ON staff.username = 'staff.manager'
JOIN users admin ON admin.username = 'super.admin'
WHERE l.code = 'STORE-D1'
  AND NOT EXISTS (
    SELECT 1
    FROM cycle_counts cc
    WHERE cc.location_id = l.id
      AND cc.started_at = TIMESTAMPTZ '2026-05-08 07:30:00+07'
  );

INSERT INTO cycle_count_items (cycle_count_id, variant_id, system_qty, counted_qty, reason_code)
SELECT cc.id, pv.id, v.system_qty, v.counted_qty, v.reason_code
FROM (VALUES
  ('THR-93595-A', 2, 2, NULL),
  ('THR-93594-A', 3, 3, NULL),
  ('THR-93585-A', 1, 0, 'DISPLAY_SAMPLE')
) AS v(sku_code, system_qty, counted_qty, reason_code)
JOIN product_variants pv ON pv.sku_code = v.sku_code
JOIN LATERAL (
  SELECT cc.id
  FROM cycle_counts cc
  JOIN locations l ON l.id = cc.location_id
  WHERE l.code = 'STORE-D1'
    AND cc.started_at = TIMESTAMPTZ '2026-05-08 07:30:00+07'
  ORDER BY cc.id
  LIMIT 1
) cc ON TRUE
ON CONFLICT (cycle_count_id, variant_id) DO NOTHING;

INSERT INTO carts (customer_user_id, status, created_at, updated_at)
SELECT u.id, 'ACTIVE', TIMESTAMPTZ '2026-05-08 09:00:00+07', TIMESTAMPTZ '2026-05-08 09:15:00+07'
FROM users u
WHERE u.username = 'demo.customer'
  AND NOT EXISTS (
    SELECT 1
    FROM carts c
    WHERE c.customer_user_id = u.id
      AND c.status = 'ACTIVE'
  );

INSERT INTO cart_items (cart_id, variant_id, qty, unit_price)
SELECT cart.id, pv.id, v.qty, pv.price
FROM (VALUES
  ('THR-93595-A', 1),
  ('THR-93585-A', 1)
) AS v(sku_code, qty)
JOIN product_variants pv ON pv.sku_code = v.sku_code
JOIN LATERAL (
  SELECT c.id
  FROM carts c
  JOIN users u ON u.id = c.customer_user_id
  WHERE u.username = 'demo.customer'
    AND c.status = 'ACTIVE'
  ORDER BY c.id
  LIMIT 1
) cart ON TRUE
ON CONFLICT (cart_id, variant_id) DO NOTHING;

INSERT INTO orders (order_number, customer_user_id, status, payment_status, shipment_status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-DEMO-2026-0001', u.id, 'DELIVERED', 'COMPLETED', 'DELIVERED', 1461000, 'VND', TIMESTAMPTZ '2026-05-04 11:20:00+07', TIMESTAMPTZ '2026-05-06 18:00:00+07'
FROM users u
WHERE u.username = 'demo.customer'
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO orders (order_number, customer_user_id, status, payment_status, shipment_status, total_amount, currency, created_at, updated_at)
SELECT 'ORD-DEMO-2026-0002', u.id, 'CONFIRMED', 'PENDING', 'PENDING', 2337000, 'VND', TIMESTAMPTZ '2026-05-08 10:40:00+07', TIMESTAMPTZ '2026-05-08 10:45:00+07'
FROM users u
WHERE u.username = 'demo.customer'
UNION ALL
SELECT 'ORD-DEMO-2026-0003', u.id, 'SHIPPED', 'COMPLETED', 'SHIPPED', 25000000, 'VND', TIMESTAMPTZ '2026-05-09 14:00:00+07', TIMESTAMPTZ '2026-05-09 16:00:00+07'
FROM users u
WHERE u.username = 'demo.customer'
UNION ALL
SELECT 'ORD-DEMO-2026-0004', u.id, 'CANCELLED', 'REFUNDED', 'CANCELLED', 8500000, 'VND', TIMESTAMPTZ '2026-05-10 10:00:00+07', TIMESTAMPTZ '2026-05-10 10:30:00+07'
FROM users u
WHERE u.username = 'demo.customer'
UNION ALL
SELECT 'ORD-DEMO-2026-0005', u.id, 'PENDING', 'FAILED', 'PENDING', 12500000, 'VND', TIMESTAMPTZ '2026-05-11 09:00:00+07', TIMESTAMPTZ '2026-05-11 09:05:00+07'
FROM users u
WHERE u.username = 'demo.customer'
ON CONFLICT (order_number) DO NOTHING;

INSERT INTO order_items (order_id, variant_id, qty, unit_price, discount_amount)
SELECT o.id, pv.id, v.qty, pv.price, v.discount_amount
FROM (VALUES
  ('ORD-DEMO-2026-0001', 'THR-93579-A', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0001', 'THR-93570-A', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0002', 'THR-93591-A', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0002', 'THR-93582-A', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0003', 'THR-LVR-001', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0004', 'THR-GUCCI-001', 1, 0::NUMERIC),
  ('ORD-DEMO-2026-0005', 'THR-NIKE-SB-001', 1, 0::NUMERIC)
) AS v(order_number, sku_code, qty, discount_amount)
JOIN orders o ON o.order_number = v.order_number
JOIN product_variants pv ON pv.sku_code = v.sku_code
WHERE NOT EXISTS (
  SELECT 1
  FROM order_items oi
  WHERE oi.order_id = o.id
    AND oi.variant_id = pv.id
);

INSERT INTO order_allocations (order_item_id, location_id, allocated_qty, status)
SELECT oi.id, l.id, oi.qty, v.status
FROM (VALUES
  ('ORD-DEMO-2026-0001', 'THR-93579-A', 'PICKED'),
  ('ORD-DEMO-2026-0001', 'THR-93570-A', 'PICKED'),
  ('ORD-DEMO-2026-0002', 'THR-93591-A', 'ALLOCATED'),
  ('ORD-DEMO-2026-0002', 'THR-93582-A', 'ALLOCATED'),
  ('ORD-DEMO-2026-0003', 'THR-LVR-001', 'SHIPPED')
) AS v(order_number, sku_code, status)
JOIN orders o ON o.order_number = v.order_number
JOIN product_variants pv ON pv.sku_code = v.sku_code
JOIN order_items oi ON oi.order_id = o.id AND oi.variant_id = pv.id
JOIN locations l ON l.code = 'ONLINE-HCM'
WHERE NOT EXISTS (
  SELECT 1
  FROM order_allocations oa
  WHERE oa.order_item_id = oi.id
);

INSERT INTO shipments (order_id, carrier, tracking_number, status, shipped_at, delivered_at)
SELECT o.id, 'GHN', 'GHN-DEMO-0001', 'DELIVERED', TIMESTAMPTZ '2026-05-05 09:00:00+07', TIMESTAMPTZ '2026-05-06 18:00:00+07'
FROM orders o
WHERE o.order_number = 'ORD-DEMO-2026-0001'
ON CONFLICT (carrier, tracking_number) DO NOTHING;

INSERT INTO shipments (order_id, carrier, tracking_number, status, shipped_at)
SELECT o.id, 'GHN', 'GHN-DEMO-0002', 'PENDING', NULL
FROM orders o
WHERE o.order_number = 'ORD-DEMO-2026-0002'
UNION ALL
SELECT o.id, 'DHL', 'DHL-DEMO-0003', 'SHIPPED', TIMESTAMPTZ '2026-05-09 17:00:00+07'
FROM orders o
WHERE o.order_number = 'ORD-DEMO-2026-0003'
ON CONFLICT (carrier, tracking_number) DO NOTHING;

INSERT INTO payments (order_id, provider, method, status, amount, created_at, updated_at)
SELECT o.id, 'STRIPE', 'CARD', 'COMPLETED', o.total_amount, TIMESTAMPTZ '2026-05-04 11:21:00+07', TIMESTAMPTZ '2026-05-04 11:22:00+07'
FROM orders o
WHERE o.order_number = 'ORD-DEMO-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM payments p
    WHERE p.order_id = o.id
      AND p.provider = 'STRIPE'
      AND p.status = 'COMPLETED'
  );

INSERT INTO payments (order_id, provider, method, status, amount, created_at, updated_at)
SELECT o.id, v.provider, v.method, v.status, o.total_amount, v.created_at, v.updated_at
FROM (
  VALUES 
    ('ORD-DEMO-2026-0002', 'COD', 'CASH', 'PENDING', TIMESTAMPTZ '2026-05-08 10:41:00+07', TIMESTAMPTZ '2026-05-08 10:41:00+07'),
    ('ORD-DEMO-2026-0003', 'STRIPE', 'CARD', 'COMPLETED', TIMESTAMPTZ '2026-05-09 14:01:00+07', TIMESTAMPTZ '2026-05-09 14:02:00+07'),
    ('ORD-DEMO-2026-0004', 'STRIPE', 'CARD', 'REFUNDED', TIMESTAMPTZ '2026-05-10 10:01:00+07', TIMESTAMPTZ '2026-05-10 10:35:00+07'),
    ('ORD-DEMO-2026-0005', 'STRIPE', 'CARD', 'FAILED', TIMESTAMPTZ '2026-05-11 09:01:00+07', TIMESTAMPTZ '2026-05-11 09:02:00+07')
) AS v(order_number, provider, method, status, created_at, updated_at)
JOIN orders o ON o.order_number = v.order_number
WHERE NOT EXISTS (
  SELECT 1 FROM payments p 
  WHERE p.order_id = o.id 
    AND p.provider = v.provider 
    AND p.status = v.status
);

INSERT INTO discount_codes (code, discount_type, discount_value, min_spend, max_uses, start_at, end_at, status)
VALUES
  ('VINTAGE10', 'PERCENT', 10, 500000, 200, TIMESTAMPTZ '2026-05-01 00:00:00+07', TIMESTAMPTZ '2026-06-01 00:00:00+07', 'ACTIVE'),
  ('THRIFTED50K', 'FIXED', 50000, 800000, 100, TIMESTAMPTZ '2026-05-01 00:00:00+07', TIMESTAMPTZ '2026-06-01 00:00:00+07', 'ACTIVE'),
  ('WELCOME20', 'PERCENT', 20, 0, 1000, TIMESTAMPTZ '2026-01-01 00:00:00+07', TIMESTAMPTZ '2027-01-01 00:00:00+07', 'ACTIVE')
ON CONFLICT (code) DO UPDATE
SET discount_type = EXCLUDED.discount_type,
    discount_value = EXCLUDED.discount_value,
    min_spend = EXCLUDED.min_spend,
    max_uses = EXCLUDED.max_uses,
    start_at = EXCLUDED.start_at,
    end_at = EXCLUDED.end_at,
    status = EXCLUDED.status;

INSERT INTO discount_redemptions (discount_code_id, order_id, customer_user_id, redeemed_at)
SELECT dc.id, o.id, u.id, TIMESTAMPTZ '2026-05-04 11:20:30+07'
FROM discount_codes dc
JOIN orders o ON o.order_number = 'ORD-DEMO-2026-0001'
JOIN users u ON u.username = 'demo.customer'
WHERE dc.code = 'VINTAGE10'
ON CONFLICT (discount_code_id, order_id) DO NOTHING;

INSERT INTO return_requests (order_id, customer_user_id, reason, status, requested_at, approved_by, approved_at)
SELECT o.id, customer.id, 'Size did not fit', 'APPROVED', TIMESTAMPTZ '2026-05-07 09:30:00+07', admin.id, TIMESTAMPTZ '2026-05-07 10:00:00+07'
FROM orders o
JOIN users customer ON customer.username = 'demo.customer'
JOIN users admin ON admin.username = 'super.admin'
WHERE o.order_number = 'ORD-DEMO-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM return_requests rr
    WHERE rr.order_id = o.id
      AND rr.customer_user_id = customer.id
      AND rr.reason = 'Size did not fit'
  );

INSERT INTO return_items (return_request_id, order_item_id, qty, disposition, inspected_by, inspected_at)
SELECT rr.id, oi.id, 1, 'RESTOCK', staff.id, TIMESTAMPTZ '2026-05-07 12:00:00+07'
FROM return_requests rr
JOIN orders o ON o.id = rr.order_id
JOIN product_variants pv ON pv.sku_code = 'THR-93570-A'
JOIN order_items oi ON oi.order_id = o.id AND oi.variant_id = pv.id
JOIN users staff ON staff.username = 'staff.manager'
WHERE o.order_number = 'ORD-DEMO-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM return_items ri
    WHERE ri.return_request_id = rr.id
      AND ri.order_item_id = oi.id
  );

INSERT INTO refunds (order_id, payment_id, return_request_id, amount, status, created_at, updated_at)
SELECT o.id, payment.id, rr.id, 548000, 'COMPLETED', TIMESTAMPTZ '2026-05-07 13:00:00+07', TIMESTAMPTZ '2026-05-07 13:15:00+07'
FROM return_requests rr
JOIN orders o ON o.id = rr.order_id
JOIN LATERAL (
  SELECT p.id
  FROM payments p
  WHERE p.order_id = o.id
    AND p.status = 'COMPLETED'
  ORDER BY p.id
  LIMIT 1
) payment ON TRUE
WHERE o.order_number = 'ORD-DEMO-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM refunds rf
    WHERE rf.return_request_id = rr.id
  );

INSERT INTO exchanges (return_item_id, replacement_variant_id, replacement_order_id, status, created_at)
SELECT ri.id, replacement.id, replacement_order.id, 'SHIPPED', TIMESTAMPTZ '2026-05-08 11:00:00+07'
FROM return_items ri
JOIN return_requests rr ON rr.id = ri.return_request_id
JOIN orders original_order ON original_order.id = rr.order_id
JOIN product_variants replacement ON replacement.sku_code = 'THR-93582-A'
JOIN orders replacement_order ON replacement_order.order_number = 'ORD-DEMO-2026-0002'
WHERE original_order.order_number = 'ORD-DEMO-2026-0001'
  AND NOT EXISTS (
    SELECT 1
    FROM exchanges e
    WHERE e.return_item_id = ri.id
  );

INSERT INTO pos_shifts (location_id, cashier_user_id, opened_at, closed_at, status)
SELECT l.id, u.id, TIMESTAMPTZ '2026-05-07 09:00:00+07', TIMESTAMPTZ '2026-05-07 21:00:00+07', 'CLOSED'
FROM locations l
JOIN users u ON u.username = 'staff.manager'
WHERE l.code = 'STORE-D1'
  AND NOT EXISTS (
    SELECT 1
    FROM pos_shifts ps
    WHERE ps.location_id = l.id
      AND ps.cashier_user_id = u.id
      AND ps.opened_at = TIMESTAMPTZ '2026-05-07 09:00:00+07'
  );

INSERT INTO pos_shifts (location_id, cashier_user_id, opened_at, status)
SELECT l.id, u.id, TIMESTAMPTZ '2026-05-08 09:00:00+07', 'OPEN'
FROM locations l
JOIN users u ON u.username = 'staff.manager'
WHERE l.code = 'STORE-D1'
  AND NOT EXISTS (
    SELECT 1
    FROM pos_shifts ps
    WHERE ps.location_id = l.id
      AND ps.cashier_user_id = u.id
      AND ps.opened_at = TIMESTAMPTZ '2026-05-08 09:00:00+07'
  );

INSERT INTO pos_receipts (shift_id, customer_user_id, total_amount, payment_method, created_at)
SELECT shift.id, customer.id, 803000, 'CASH', TIMESTAMPTZ '2026-05-07 15:30:00+07'
FROM users customer
JOIN LATERAL (
  SELECT ps.id
  FROM pos_shifts ps
  JOIN locations l ON l.id = ps.location_id
  JOIN users cashier ON cashier.id = ps.cashier_user_id
  WHERE l.code = 'STORE-D1'
    AND cashier.username = 'staff.manager'
    AND ps.opened_at = TIMESTAMPTZ '2026-05-07 09:00:00+07'
  ORDER BY ps.id
  LIMIT 1
) shift ON TRUE
WHERE customer.username = 'demo.customer'
  AND NOT EXISTS (
    SELECT 1
    FROM pos_receipts pr
    WHERE pr.shift_id = shift.id
      AND pr.created_at = TIMESTAMPTZ '2026-05-07 15:30:00+07'
  );

INSERT INTO pos_receipt_items (receipt_id, variant_id, qty, unit_price)
SELECT receipt.id, pv.id, 1, pv.price
FROM product_variants pv
JOIN LATERAL (
  SELECT pr.id
  FROM pos_receipts pr
  JOIN pos_shifts ps ON ps.id = pr.shift_id
  WHERE pr.created_at = TIMESTAMPTZ '2026-05-07 15:30:00+07'
    AND ps.opened_at = TIMESTAMPTZ '2026-05-07 09:00:00+07'
  ORDER BY pr.id
  LIMIT 1
) receipt ON TRUE
WHERE pv.sku_code = 'THR-93594-A'
  AND NOT EXISTS (
    SELECT 1
    FROM pos_receipt_items pri
    WHERE pri.receipt_id = receipt.id
      AND pri.variant_id = pv.id
  );

INSERT INTO cash_reconciliations (shift_id, expected_cash, actual_cash, variance_amount, reason, approved_by, approved_at)
SELECT ps.id, 803000, 803000, 0, 'Seeded balanced close', admin.id, TIMESTAMPTZ '2026-05-07 21:10:00+07'
FROM pos_shifts ps
JOIN users admin ON admin.username = 'super.admin'
JOIN users cashier ON cashier.id = ps.cashier_user_id
WHERE cashier.username = 'staff.manager'
  AND ps.opened_at = TIMESTAMPTZ '2026-05-07 09:00:00+07'
ON CONFLICT (shift_id) DO NOTHING;

INSERT INTO tax_invoices (invoice_number, order_id, customer_user_id, tax_rate, subtotal_amount, tax_amount, total_amount, issued_at)
SELECT 'INV-2026-0001', o.id, customer.id, 8.00, 1352777.78, 108222.22, 1461000, TIMESTAMPTZ '2026-05-04 11:25:00+07'
FROM orders o
JOIN users customer ON customer.username = 'demo.customer'
WHERE o.order_number = 'ORD-DEMO-2026-0001'
ON CONFLICT (invoice_number) DO NOTHING;

INSERT INTO tax_invoices (invoice_number, pos_receipt_id, customer_user_id, tax_rate, subtotal_amount, tax_amount, total_amount, issued_at)
SELECT 'INV-POS-2026-0001', pr.id, customer.id, 8.00, 743518.52, 59481.48, 803000, TIMESTAMPTZ '2026-05-07 15:31:00+07'
FROM pos_receipts pr
JOIN users customer ON customer.username = 'demo.customer'
WHERE pr.created_at = TIMESTAMPTZ '2026-05-07 15:30:00+07'
ON CONFLICT (invoice_number) DO NOTHING;

INSERT INTO invoice_adjustment_notes (original_invoice_id, adjustment_number, reason, delta_amount, created_by, created_at)
SELECT ti.id, 'ADJ-2026-0001', 'Refunded returned Dickies T-Shirt line', -548000, admin.id, TIMESTAMPTZ '2026-05-07 13:20:00+07'
FROM tax_invoices ti
JOIN users admin ON admin.username = 'super.admin'
WHERE ti.invoice_number = 'INV-2026-0001'
ON CONFLICT (adjustment_number) DO NOTHING;

INSERT INTO instagram_connections (user_id, account_id, token_encrypted, refresh_token_encrypted, token_expires_at, status, updated_at)
SELECT u.id, 'seshop.demo.instagram', 'seed-token-encrypted', 'seed-refresh-token-encrypted', TIMESTAMPTZ '2026-06-08 00:00:00+07', 'CONNECTED', NOW()
FROM users u
WHERE u.username = 'staff.manager'
ON CONFLICT (user_id, account_id) DO NOTHING;

INSERT INTO instagram_drafts (created_by, product_id, caption, hashtags, media_order_json, status, created_at, updated_at)
SELECT u.id, pv.product_id, 'Fresh vintage arrival: ' || p.name, '#vintage #thrifted #seshop', jsonb_build_array(pi.url), 'DRAFT', TIMESTAMPTZ '2026-05-08 12:00:00+07', TIMESTAMPTZ '2026-05-08 12:00:00+07'
FROM users u
JOIN product_variants pv ON pv.sku_code = 'THR-93593-A'
JOIN products p ON p.id = pv.product_id
JOIN product_images pi ON pi.variant_id = pv.id
WHERE u.username = 'staff.manager'
  AND NOT EXISTS (
    SELECT 1
    FROM instagram_drafts d
    WHERE d.created_by = u.id
      AND d.product_id = p.id
      AND d.status = 'DRAFT'
  );

INSERT INTO instagram_drafts (created_by, product_id, caption, hashtags, media_order_json, status, created_at, updated_at)
SELECT u.id, pv.product_id, 'Streetwear pick of the day: ' || p.name, '#streetwear #vintagefashion #seshop', jsonb_build_array(pi.url), 'REVIEW_READY', TIMESTAMPTZ '2026-05-08 12:20:00+07', TIMESTAMPTZ '2026-05-08 12:30:00+07'
FROM users u
JOIN product_variants pv ON pv.sku_code = 'THR-93585-A'
JOIN products p ON p.id = pv.product_id
JOIN product_images pi ON pi.variant_id = pv.id
WHERE u.username = 'staff.manager'
  AND NOT EXISTS (
    SELECT 1
    FROM instagram_drafts d
    WHERE d.created_by = u.id
      AND d.product_id = p.id
      AND d.status = 'REVIEW_READY'
  );

INSERT INTO reviews (order_item_id, customer_user_id, rating, comment, image_url, status, created_at)
SELECT oi.id, customer.id, 5, 'Authentic vintage feel and exactly as pictured.', pi.url, 'PUBLISHED', TIMESTAMPTZ '2026-05-07 20:00:00+07'
FROM orders o
JOIN product_variants pv ON pv.sku_code = 'THR-93579-A'
JOIN order_items oi ON oi.order_id = o.id AND oi.variant_id = pv.id
JOIN product_images pi ON pi.variant_id = pv.id
JOIN users customer ON customer.username = 'demo.customer'
WHERE o.order_number = 'ORD-DEMO-2026-0001'
ON CONFLICT (order_item_id, customer_user_id) DO NOTHING;

INSERT INTO reviews (order_item_id, customer_user_id, rating, comment, image_url, status, created_at)
SELECT oi.id, customer.id, 4, 'Good condition; returned only because the size was not right.', pi.url, 'PUBLISHED', TIMESTAMPTZ '2026-05-07 20:10:00+07'
FROM orders o
JOIN product_variants pv ON pv.sku_code = 'THR-93570-A'
JOIN order_items oi ON oi.order_id = o.id AND oi.variant_id = pv.id
JOIN product_images pi ON pi.variant_id = pv.id
JOIN users customer ON customer.username = 'demo.customer'
WHERE o.order_number = 'ORD-DEMO-2026-0001'
ON CONFLICT (order_item_id, customer_user_id) DO NOTHING;

INSERT INTO audit_logs (actor_user_id, action, target_type, target_id, metadata_json, created_at)
SELECT u.id, 'ROLE_CREATED', 'SeedData', 'SUPER_ADMIN', '{"source":"V3__seed_demo_database"}'::jsonb, TIMESTAMPTZ '2026-05-08 08:00:00+07'
FROM users u WHERE u.username = 'super.admin'
UNION ALL
SELECT u.id, 'USER_ROLE_ASSIGNED', 'User', 'DEMO_CUSTOMER_ID', '{"role":"CUSTOMER","source":"V3__seed_demo_database"}'::jsonb, TIMESTAMPTZ '2026-05-08 08:05:00+07'
FROM users u WHERE u.username = 'super.admin'
UNION ALL
SELECT u.id, 'PRODUCT_CREATED', 'Product', 'THR-LVR-001', '{"name":"Louis Vuitton Monogram Pochette Accessoires"}'::jsonb, TIMESTAMPTZ '2026-05-09 10:00:00+07'
FROM users u WHERE u.username = 'staff.manager'
UNION ALL
SELECT u.id, 'INVENTORY_ADJUSTED', 'Inventory', 'THR-LVR-001', '{"location":"ONLINE-HCM","qty":1}'::jsonb, TIMESTAMPTZ '2026-05-09 10:05:00+07'
FROM users u WHERE u.username = 'staff.manager'
UNION ALL
SELECT u.id, 'ORDER_CONFIRMED', 'Order', 'ORD-DEMO-2026-0003', '{"total":25000000}'::jsonb, TIMESTAMPTZ '2026-05-09 14:05:00+07'
FROM users u WHERE u.username = 'staff.manager'
UNION ALL
SELECT u.id, 'SHIPMENT_CREATED', 'Shipment', 'DHL-DEMO-0003', '{"carrier":"DHL"}'::jsonb, TIMESTAMPTZ '2026-05-09 17:05:00+07'
FROM users u WHERE u.username = 'staff.manager';

DROP TABLE seed_thrifted_products;
