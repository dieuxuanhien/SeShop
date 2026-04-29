-- SESHOP optimized minimal schema (PostgreSQL)
-- Focus: 3NF-friendly design, clear keys, no derived columns stored.

CREATE TABLE users (
  id BIGSERIAL PRIMARY KEY,
  username VARCHAR(80) NOT NULL UNIQUE,
  email VARCHAR(255) NOT NULL UNIQUE,
  phone_number VARCHAR(20) NOT NULL UNIQUE,
  password_hash TEXT NOT NULL,
  user_type VARCHAR(20) NOT NULL, -- ADMIN, STAFF, CUSTOMER
  status VARCHAR(20) NOT NULL,    -- ACTIVE, INACTIVE, LOCKED
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE roles (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(80) NOT NULL UNIQUE,
  description TEXT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE permissions (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(120) NOT NULL UNIQUE,
  description TEXT
);

CREATE TABLE role_permissions (
  role_id BIGINT NOT NULL REFERENCES roles(id),
  permission_id BIGINT NOT NULL REFERENCES permissions(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE user_roles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  role_id BIGINT NOT NULL REFERENCES roles(id),
  assigned_by BIGINT REFERENCES users(id),
  assigned_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  revoked_at TIMESTAMPTZ,
  UNIQUE (user_id, role_id, revoked_at)
);

CREATE TABLE audit_logs (
  id BIGSERIAL PRIMARY KEY,
  actor_user_id BIGINT REFERENCES users(id),
  action VARCHAR(120) NOT NULL,
  target_type VARCHAR(80) NOT NULL,
  target_id VARCHAR(80),
  metadata_json JSONB,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE categories (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(120) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE products (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  brand VARCHAR(120),
  description TEXT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE product_categories (
  product_id BIGINT NOT NULL REFERENCES products(id),
  category_id BIGINT NOT NULL REFERENCES categories(id),
  PRIMARY KEY (product_id, category_id)
);

CREATE TABLE product_variants (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id),
  sku_code VARCHAR(80) NOT NULL UNIQUE,
  size VARCHAR(30),
  color VARCHAR(30),
  price NUMERIC(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE product_images (
  id BIGSERIAL PRIMARY KEY,
  product_id BIGINT NOT NULL REFERENCES products(id),
  variant_id BIGINT REFERENCES product_variants(id),
  url TEXT NOT NULL,
  sort_order INT NOT NULL DEFAULT 0,
  is_instagram_ready BOOLEAN NOT NULL DEFAULT FALSE,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE locations (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(40) NOT NULL UNIQUE,
  display_name VARCHAR(120) NOT NULL,
  location_type VARCHAR(20) NOT NULL, -- STORE, STORAGE
  status VARCHAR(20) NOT NULL
);

CREATE TABLE inventory_balances (
  id BIGSERIAL PRIMARY KEY,
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  location_id BIGINT NOT NULL REFERENCES locations(id),
  on_hand_qty INT NOT NULL DEFAULT 0,
  reserved_qty INT NOT NULL DEFAULT 0,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (variant_id, location_id)
);

CREATE TABLE inventory_transfers (
  id BIGSERIAL PRIMARY KEY,
  source_location_id BIGINT NOT NULL REFERENCES locations(id),
  destination_location_id BIGINT NOT NULL REFERENCES locations(id),
  status VARCHAR(20) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  completed_at TIMESTAMPTZ
);

CREATE TABLE inventory_transfer_items (
  id BIGSERIAL PRIMARY KEY,
  transfer_id BIGINT NOT NULL REFERENCES inventory_transfers(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  qty INT NOT NULL,
  received_qty INT,
  damaged_qty INT,
  UNIQUE (transfer_id, variant_id)
);

CREATE TABLE cycle_counts (
  id BIGSERIAL PRIMARY KEY,
  location_id BIGINT NOT NULL REFERENCES locations(id),
  status VARCHAR(20) NOT NULL,
  started_by BIGINT NOT NULL REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  started_at TIMESTAMPTZ NOT NULL,
  approved_at TIMESTAMPTZ
);

CREATE TABLE cycle_count_items (
  id BIGSERIAL PRIMARY KEY,
  cycle_count_id BIGINT NOT NULL REFERENCES cycle_counts(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  system_qty INT NOT NULL,
  counted_qty INT NOT NULL,
  reason_code VARCHAR(50),
  UNIQUE (cycle_count_id, variant_id)
);

CREATE TABLE suppliers (
  id BIGSERIAL PRIMARY KEY,
  name VARCHAR(180) NOT NULL,
  tax_code VARCHAR(80),
  phone VARCHAR(20),
  email VARCHAR(255),
  address TEXT,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE purchase_orders (
  id BIGSERIAL PRIMARY KEY,
  po_number VARCHAR(60) NOT NULL UNIQUE,
  supplier_id BIGINT NOT NULL REFERENCES suppliers(id),
  destination_location_id BIGINT NOT NULL REFERENCES locations(id),
  status VARCHAR(20) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  approved_by BIGINT REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  approved_at TIMESTAMPTZ
);

CREATE TABLE purchase_order_items (
  id BIGSERIAL PRIMARY KEY,
  purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  ordered_qty INT NOT NULL,
  unit_cost NUMERIC(12,2) NOT NULL,
  UNIQUE (purchase_order_id, variant_id)
);

CREATE TABLE goods_receipts (
  id BIGSERIAL PRIMARY KEY,
  purchase_order_id BIGINT NOT NULL REFERENCES purchase_orders(id),
  received_by BIGINT NOT NULL REFERENCES users(id),
  received_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE goods_receipt_items (
  id BIGSERIAL PRIMARY KEY,
  goods_receipt_id BIGINT NOT NULL REFERENCES goods_receipts(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  received_qty INT NOT NULL,
  damaged_qty INT NOT NULL DEFAULT 0,
  UNIQUE (goods_receipt_id, variant_id)
);

CREATE TABLE carts (
  id BIGSERIAL PRIMARY KEY,
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  status VARCHAR(20) NOT NULL, -- ACTIVE, ORDERED, ABANDONED
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE cart_items (
  id BIGSERIAL PRIMARY KEY,
  cart_id BIGINT NOT NULL REFERENCES carts(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  qty INT NOT NULL,
  unit_price NUMERIC(12,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (cart_id, variant_id)
);

CREATE TABLE orders (
  id BIGSERIAL PRIMARY KEY,
  order_number VARCHAR(60) NOT NULL UNIQUE,
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  status VARCHAR(20) NOT NULL,
  payment_status VARCHAR(20) NOT NULL,
  shipment_status VARCHAR(20) NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  currency CHAR(3) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE order_items (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  qty INT NOT NULL,
  unit_price NUMERIC(12,2) NOT NULL,
  discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0
);

CREATE TABLE order_allocations (
  id BIGSERIAL PRIMARY KEY,
  order_item_id BIGINT NOT NULL REFERENCES order_items(id),
  location_id BIGINT NOT NULL REFERENCES locations(id),
  allocated_qty INT NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE shipments (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  carrier VARCHAR(80) NOT NULL,
  tracking_number VARCHAR(120),
  status VARCHAR(20) NOT NULL,
  shipped_at TIMESTAMPTZ,
  delivered_at TIMESTAMPTZ,
  UNIQUE (carrier, tracking_number)
);

CREATE TABLE payments (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  provider VARCHAR(50) NOT NULL,
  method VARCHAR(30) NOT NULL,
  status VARCHAR(20) NOT NULL,
  amount NUMERIC(12,2) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE discount_codes (
  id BIGSERIAL PRIMARY KEY,
  code VARCHAR(60) NOT NULL UNIQUE,
  discount_type VARCHAR(20) NOT NULL,
  discount_value NUMERIC(12,2) NOT NULL,
  min_spend NUMERIC(12,2) NOT NULL DEFAULT 0,
  max_uses INT,
  start_at TIMESTAMPTZ NOT NULL,
  end_at TIMESTAMPTZ NOT NULL,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE discount_redemptions (
  id BIGSERIAL PRIMARY KEY,
  discount_code_id BIGINT NOT NULL REFERENCES discount_codes(id),
  order_id BIGINT NOT NULL REFERENCES orders(id),
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  redeemed_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (discount_code_id, order_id)
);

CREATE TABLE return_requests (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  reason VARCHAR(255) NOT NULL,
  status VARCHAR(20) NOT NULL,
  requested_at TIMESTAMPTZ NOT NULL,
  approved_by BIGINT REFERENCES users(id),
  approved_at TIMESTAMPTZ
);

CREATE TABLE return_items (
  id BIGSERIAL PRIMARY KEY,
  return_request_id BIGINT NOT NULL REFERENCES return_requests(id),
  order_item_id BIGINT NOT NULL REFERENCES order_items(id),
  qty INT NOT NULL,
  disposition VARCHAR(20), -- RESTOCK, REFURBISH, DISPOSE
  inspected_by BIGINT REFERENCES users(id),
  inspected_at TIMESTAMPTZ
);

CREATE TABLE refunds (
  id BIGSERIAL PRIMARY KEY,
  order_id BIGINT NOT NULL REFERENCES orders(id),
  payment_id BIGINT REFERENCES payments(id),
  return_request_id BIGINT REFERENCES return_requests(id),
  amount NUMERIC(12,2) NOT NULL,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE exchanges (
  id BIGSERIAL PRIMARY KEY,
  return_item_id BIGINT NOT NULL REFERENCES return_items(id),
  replacement_variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  replacement_order_id BIGINT REFERENCES orders(id),
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pos_shifts (
  id BIGSERIAL PRIMARY KEY,
  location_id BIGINT NOT NULL REFERENCES locations(id),
  cashier_user_id BIGINT NOT NULL REFERENCES users(id),
  opened_at TIMESTAMPTZ NOT NULL,
  closed_at TIMESTAMPTZ,
  status VARCHAR(20) NOT NULL
);

CREATE TABLE pos_receipts (
  id BIGSERIAL PRIMARY KEY,
  shift_id BIGINT NOT NULL REFERENCES pos_shifts(id),
  customer_user_id BIGINT REFERENCES users(id),
  total_amount NUMERIC(12,2) NOT NULL,
  payment_method VARCHAR(30) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE pos_receipt_items (
  id BIGSERIAL PRIMARY KEY,
  receipt_id BIGINT NOT NULL REFERENCES pos_receipts(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  qty INT NOT NULL,
  unit_price NUMERIC(12,2) NOT NULL
);

CREATE TABLE cash_reconciliations (
  id BIGSERIAL PRIMARY KEY,
  shift_id BIGINT NOT NULL UNIQUE REFERENCES pos_shifts(id),
  expected_cash NUMERIC(12,2) NOT NULL,
  actual_cash NUMERIC(12,2) NOT NULL,
  variance_amount NUMERIC(12,2) NOT NULL,
  reason VARCHAR(255),
  approved_by BIGINT REFERENCES users(id),
  approved_at TIMESTAMPTZ
);

CREATE TABLE tax_invoices (
  id BIGSERIAL PRIMARY KEY,
  invoice_number VARCHAR(80) NOT NULL UNIQUE,
  order_id BIGINT REFERENCES orders(id),
  pos_receipt_id BIGINT REFERENCES pos_receipts(id),
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  tax_rate NUMERIC(5,2) NOT NULL,
  subtotal_amount NUMERIC(12,2) NOT NULL,
  tax_amount NUMERIC(12,2) NOT NULL,
  total_amount NUMERIC(12,2) NOT NULL,
  issued_at TIMESTAMPTZ NOT NULL
);

CREATE TABLE invoice_adjustment_notes (
  id BIGSERIAL PRIMARY KEY,
  original_invoice_id BIGINT NOT NULL REFERENCES tax_invoices(id),
  adjustment_number VARCHAR(80) NOT NULL UNIQUE,
  reason VARCHAR(255) NOT NULL,
  delta_amount NUMERIC(12,2) NOT NULL,
  created_by BIGINT NOT NULL REFERENCES users(id),
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE instagram_connections (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id),
  account_id VARCHAR(120) NOT NULL,
  token_encrypted TEXT NOT NULL,
  refresh_token_encrypted TEXT,
  token_expires_at TIMESTAMPTZ,
  status VARCHAR(20) NOT NULL,
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (user_id, account_id)
);

CREATE TABLE instagram_drafts (
  id BIGSERIAL PRIMARY KEY,
  created_by BIGINT NOT NULL REFERENCES users(id),
  product_id BIGINT NOT NULL REFERENCES products(id),
  caption TEXT,
  hashtags TEXT,
  media_order_json JSONB,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE TABLE reviews (
  id BIGSERIAL PRIMARY KEY,
  order_item_id BIGINT NOT NULL REFERENCES order_items(id),
  customer_user_id BIGINT NOT NULL REFERENCES users(id),
  rating INT NOT NULL,
  comment TEXT,
  image_url TEXT,
  status VARCHAR(20) NOT NULL,
  created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
  UNIQUE (order_item_id, customer_user_id)
);

-- Recommended indexes
CREATE INDEX idx_inventory_balances_variant_location ON inventory_balances (variant_id, location_id);
CREATE INDEX idx_orders_customer_created_at ON orders (customer_user_id, created_at DESC);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_shipments_order_id ON shipments (order_id);
CREATE INDEX idx_payments_order_id ON payments (order_id);
CREATE INDEX idx_returns_order_id ON return_requests (order_id);
CREATE INDEX idx_pos_receipts_shift_id ON pos_receipts (shift_id);
CREATE INDEX idx_audit_logs_actor_created_at ON audit_logs (actor_user_id, created_at DESC);
