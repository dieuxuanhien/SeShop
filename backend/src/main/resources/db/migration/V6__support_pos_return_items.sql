ALTER TABLE pos_returns
    ADD COLUMN IF NOT EXISTS original_receipt_id BIGINT REFERENCES pos_receipts(id);

ALTER TABLE pos_returns
    ALTER COLUMN original_order_id DROP NOT NULL;

CREATE TABLE IF NOT EXISTS pos_return_items (
  id BIGSERIAL PRIMARY KEY,
  pos_return_id BIGINT NOT NULL REFERENCES pos_returns(id),
  variant_id BIGINT NOT NULL REFERENCES product_variants(id),
  qty INT NOT NULL,
  disposition VARCHAR(20) NOT NULL,
  refund_amount NUMERIC(12,2) NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_pos_returns_original_receipt_id
    ON pos_returns (original_receipt_id);

CREATE INDEX IF NOT EXISTS idx_pos_return_items_return_id
    ON pos_return_items (pos_return_id);

CREATE INDEX IF NOT EXISTS idx_pos_return_items_variant_id
    ON pos_return_items (variant_id);
