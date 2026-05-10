ALTER TABLE cart_items
    ADD COLUMN IF NOT EXISTS added_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

UPDATE cart_items
SET added_at = created_at
WHERE created_at IS NOT NULL;

ALTER TABLE cart_items
    ALTER COLUMN unit_price SET DEFAULT 0;

ALTER TABLE orders
    ADD COLUMN IF NOT EXISTS subtotal_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS discount_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0,
    ADD COLUMN IF NOT EXISTS shipping_address TEXT,
    ADD COLUMN IF NOT EXISTS billing_address TEXT;

UPDATE orders
SET subtotal_amount = total_amount
WHERE subtotal_amount = 0;

ALTER TABLE orders
    ALTER COLUMN payment_status SET DEFAULT 'PENDING',
    ALTER COLUMN shipment_status SET DEFAULT 'PENDING',
    ALTER COLUMN currency SET DEFAULT 'VND';

ALTER TABLE order_items
    ADD COLUMN IF NOT EXISTS product_name VARCHAR(200) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS sku VARCHAR(50) NOT NULL DEFAULT '',
    ADD COLUMN IF NOT EXISTS total_price NUMERIC(12,2) NOT NULL DEFAULT 0;

UPDATE order_items oi
SET product_name = p.name,
    sku = pv.sku_code,
    total_price = oi.unit_price * oi.qty
FROM product_variants pv
JOIN products p ON p.id = pv.product_id
WHERE pv.id = oi.variant_id;

ALTER TABLE payments
    ADD COLUMN IF NOT EXISTS transaction_id VARCHAR(100);

ALTER TABLE payments
    ALTER COLUMN method SET DEFAULT 'UNSPECIFIED';

ALTER TABLE shipments
    ADD COLUMN IF NOT EXISTS created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    ADD COLUMN IF NOT EXISTS updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW();

CREATE TABLE IF NOT EXISTS pos_returns (
  id BIGSERIAL PRIMARY KEY,
  original_order_id BIGINT NOT NULL REFERENCES orders(id),
  processed_by BIGINT NOT NULL REFERENCES users(id),
  refund_amount NUMERIC(12,2) NOT NULL,
  reason VARCHAR(255),
  processed_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);
