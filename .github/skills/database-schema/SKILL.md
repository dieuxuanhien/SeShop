---
description: "SeShop database schema and data model. Use when: understanding table structures, designing entities, writing database queries, or validating data models against schema."
name: database-schema
---

# SeShop Database Schema Skill

This skill provides comprehensive knowledge of the SeShop database schema, table relationships, and data modeling guidelines.

## When to Use

- Designing domain entities and JPA mappings
- Writing database queries and migrations
- Validating data integrity constraints
- Understanding table relationships
- Troubleshooting data consistency issues

## Core Design Principles

1. **Normalization**: 3NF to balance performance and consistency
2. **SKU-Location Granularity**: Inventory tracked at (SKU, Location) level
3. **Immutable Audit Logs**: Append-only, never updated
4. **Soft Deletes**: Use `status` column instead of DELETE
5. **Temporal Data**: Track `created_at`, `updated_at` for auditability
6. **Referential Integrity**: Foreign key constraints enforced
7. **Indexing**: Strategic indexes on frequently-queried columns

## Master Tables

### Users (`users`)
Stores all authenticated identities (admin, staff, customer).

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | User ID |
| username | VARCHAR(80) | NN, UQ | Login name |
| email | VARCHAR(255) | NN, UQ | Email address |
| phone_number | VARCHAR(20) | NN, UQ | Phone number |
| password_hash | TEXT | NN | Password hash (bcrypt) |
| user_type | VARCHAR(20) | NN | ADMIN, STAFF, CUSTOMER |
| status | VARCHAR(20) | NN | ACTIVE, INACTIVE, SUSPENDED |
| created_at | TIMESTAMPTZ | NN | Creation time |
| updated_at | TIMESTAMPTZ | NN | Last update time |

**Indexes**:
- `idx_users_username` - For login
- `idx_users_email` - For email lookups
- `idx_users_user_type` - For user type filtering

### Roles (`roles`) & Permissions
Flexible RBAC with atomic permission codes.

**Roles Table**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Role ID |
| name | VARCHAR(80) | NN, UQ | Role name |
| description | TEXT | | Role description |
| status | VARCHAR(20) | NN | ACTIVE, INACTIVE |
| created_at | TIMESTAMPTZ | NN | Creation time |

**Permissions Table**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Permission ID |
| code | VARCHAR(120) | NN, UQ | Permission code (e.g., `INV:ADJUST`) |
| description | TEXT | | Description |

**Role-Permissions Junction**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| role_id | BIGINT | PK, FK | Role reference |
| permission_id | BIGINT | PK, FK | Permission reference |
| created_at | TIMESTAMPTZ | NN | Grant time |

**User-Roles Junction**:
| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Assignment ID |
| user_id | BIGINT | NN, FK | User reference |
| role_id | BIGINT | NN, FK | Role reference |
| assigned_by | BIGINT | FK | Admin who assigned |
| assigned_at | TIMESTAMPTZ | NN | Assignment time |
| revoked_at | TIMESTAMPTZ | | Revocation time (if revoked) |

### Audit Logs (`audit_logs`)
Immutable security and operations audit trail.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Log entry ID |
| actor_user_id | BIGINT | NN, FK | Who performed action |
| action | VARCHAR(120) | NN | Action code (e.g., `USER_CREATED`) |
| target_type | VARCHAR(80) | NN | Entity type (e.g., `User`, `Order`) |
| target_id | VARCHAR(80) | | Entity ID affected |
| old_value | JSONB | | Previous value (if update) |
| new_value | JSONB | | New value (if update) |
| metadata_json | JSONB | | Additional context |
| ip_address | VARCHAR(45) | | Actor IP address |
| created_at | TIMESTAMPTZ | NN | Action timestamp |

**Indexes**:
- `idx_audit_logs_actor_user_id` - Trace user actions
- `idx_audit_logs_target_type_id` - Find actions on resource
- `idx_audit_logs_created_at` - Time-based queries

## Product Catalog Tables

### Categories (`categories`)
Product taxonomy for browsing and filtering.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Category ID |
| name | VARCHAR(120) | NN, UQ | Category name |
| description | TEXT | | Category description |
| parent_category_id | BIGINT | FK | Parent category (for hierarchy) |
| status | VARCHAR(20) | NN | ACTIVE, INACTIVE |
| created_at | TIMESTAMPTZ | NN | |
| updated_at | TIMESTAMPTZ | NN | |

### Products (`products`)
Product master records (parent level).

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Product ID |
| name | VARCHAR(200) | NN | Product name |
| brand | VARCHAR(120) | | Brand name |
| description | TEXT | | Long description |
| status | VARCHAR(20) | NN | DRAFT, PUBLISHED, ARCHIVED |
| created_at | TIMESTAMPTZ | NN | |
| updated_at | TIMESTAMPTZ | NN | |

### Product Variants/SKUs (`product_variants`)
SKU-level representation (size, color combinations) - **KEY TABLE FOR INVENTORY**.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Variant/SKU ID |
| product_id | BIGINT | NN, FK | Product reference |
| sku_code | VARCHAR(50) | NN, UQ | Barcode/SKU code |
| size | VARCHAR(20) | | Size (e.g., M, L, XL) |
| color | VARCHAR(50) | | Color (e.g., Blue) |
| cost_price | NUMERIC(10,2) | | Cost to business |
| selling_price | NUMERIC(10,2) | NN | Selling price to customer |
| weight | NUMERIC(8,3) | | Weight (kg) |
| supplier_id | BIGINT | FK | Supplier reference |
| status | VARCHAR(20) | NN | ACTIVE, INACTIVE, DISCONTINUED |
| created_at | TIMESTAMPTZ | NN | |
| updated_at | TIMESTAMPTZ | NN | |

**Indexes**:
- `idx_product_variants_product_id` - Find variants for product
- `idx_product_variants_sku_code` - Find by SKU code

### Product Images (`product_images`)
Media storage for products.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Image ID |
| product_id | BIGINT | NN, FK | Product reference |
| image_url | TEXT | NN | Image file path |
| display_order | INT | | Order in gallery |
| is_primary | BOOLEAN | | Primary image flag |
| created_at | TIMESTAMPTZ | NN | |

## Inventory Tables

### Locations (`locations`)
Physical store and storage locations.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Location ID |
| name | VARCHAR(120) | NN, UQ | Location name |
| location_type | VARCHAR(20) | NN | STORE or STORAGE |
| address | TEXT | | Full address |
| phone | VARCHAR(20) | | Contact phone |
| status | VARCHAR(20) | NN | ACTIVE, INACTIVE, CLOSED |
| created_at | TIMESTAMPTZ | NN | |
| updated_at | TIMESTAMPTZ | NN | |

### Inventory Balance (`inventory_balances`)
**CRITICAL TABLE**: Tracks stock at (SKU, Location) level.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Balance ID |
| sku_id | BIGINT | NN, FK | Product variant |
| location_id | BIGINT | NN, FK | Location |
| total_qty | INT | NN, CHECK >= 0 | Total quantity |
| reserved_qty | INT | NN, CHECK >= 0 | Reserved for pending orders |
| allocated_qty | INT | NN, CHECK >= 0 | Allocated to fulfillment |
| available_qty | GENERATED AS (total_qty - reserved_qty - allocated_qty) | | Calculated field |
| updated_at | TIMESTAMPTZ | NN | |

**Constraint**: `available_qty >= 0` (stock cannot be negative)

**Indexes**:
- `idx_inventory_balances_sku_location` (UNIQUE) - Primary lookup
- `idx_inventory_balances_location_id` - Find all stock at location
- `idx_inventory_balances_available_qty` - Find low-stock items

### Inventory Transfers (`inventory_transfers`)
Inter-location stock movements.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Transfer ID |
| from_location_id | BIGINT | NN, FK | Origin |
| to_location_id | BIGINT | NN, FK | Destination |
| status | VARCHAR(20) | NN | DRAFT, IN_TRANSIT, RECEIVED, COMPLETED, CANCELLED |
| created_by | BIGINT | NN, FK | User who initiated |
| created_at | TIMESTAMPTZ | NN | |
| expected_delivery_date | DATE | | Estimated arrival |
| received_at | TIMESTAMPTZ | | Actual receipt time |

### Transfer Items (`transfer_items`)
Line items in a transfer.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| transfer_id | BIGINT | NN, FK | Transfer reference |
| sku_id | BIGINT | NN, FK | Product variant |
| quantity | INT | NN, CHECK > 0 | Quantity transferred |
| received_qty | INT | | Actual received (for reconciliation) |

## Order Management Tables

### Orders (`orders`)
Main order records (online and POS).

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Order ID |
| customer_id | BIGINT | NN, FK | Customer reference |
| order_number | VARCHAR(50) | NN, UQ | Display order number |
| channel | VARCHAR(20) | NN | ONLINE or POS |
| status | VARCHAR(20) | NN | PENDING, CONFIRMED, PACKED, SHIPPED, DELIVERED, RETURNED, CANCELLED |
| total_amount | NUMERIC(12,2) | NN | Order total |
| discount_amount | NUMERIC(12,2) | | Applied discount |
| final_amount | NUMERIC(12,2) | NN | Amount to pay |
| fulfillment_location_id | BIGINT | FK | Location that will fulfill |
| created_at | TIMESTAMPTZ | NN | |
| updated_at | TIMESTAMPTZ | NN | |

**Indexes**:
- `idx_orders_customer_id` - Find customer orders
- `idx_orders_status` - Find orders by status
- `idx_orders_created_at` - Time-based queries

### Order Items (`order_items`)
Line items in orders.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| order_id | BIGINT | NN, FK | Order reference |
| sku_id | BIGINT | NN, FK | Product variant |
| quantity | INT | NN, CHECK > 0 | Quantity ordered |
| unit_price | NUMERIC(10,2) | NN | Price at order time |
| subtotal | NUMERIC(12,2) | NN | Quantity × price |

### Shipments (`shipments`)
Fulfillment and tracking information.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Shipment ID |
| order_id | BIGINT | NN, UQ, FK | Order reference (one shipment per order for v1) |
| tracking_number | VARCHAR(100) | | Carrier tracking number |
| carrier | VARCHAR(50) | | Carrier name (e.g., DHL, FedEx) |
| status | VARCHAR(20) | NN | PENDING, IN_TRANSIT, DELIVERED, LOST, RETURNED |
| shipped_at | TIMESTAMPTZ | | Ship timestamp |
| delivered_at | TIMESTAMPTZ | | Delivery timestamp |
| created_at | TIMESTAMPTZ | NN | |

### Payments (`payments`)
Payment transaction records.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Payment ID |
| order_id | BIGINT | NN, FK | Order reference |
| amount | NUMERIC(12,2) | NN | Amount paid |
| currency | VARCHAR(3) | NN | Currency code (USD, VND, etc.) |
| payment_method | VARCHAR(50) | NN | stripe, cod, cash |
| external_reference | VARCHAR(255) | | Stripe payment intent ID, etc. |
| status | VARCHAR(20) | NN | PENDING, COMPLETED, FAILED, REFUNDED |
| created_at | TIMESTAMPTZ | NN | |

## Returns & Refunds Tables

### Returns (`returns`)
Customer return requests.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Return ID |
| order_id | BIGINT | NN, FK | Original order |
| customer_id | BIGINT | NN, FK | Customer |
| reason | VARCHAR(100) | | Return reason |
| status | VARCHAR(20) | NN | REQUESTED, LABEL_GENERATED, RETURNED, RECEIVED, INSPECTED, APPROVED, REJECTED |
| return_label_url | TEXT | | Return shipping label |
| received_at | TIMESTAMPTZ | | When return received |
| created_at | TIMESTAMPTZ | NN | |

### Return Items (`return_items`)
Items in a return.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| return_id | BIGINT | NN, FK | Return reference |
| order_item_id | BIGINT | NN, FK | Original order item |
| quantity | INT | NN | Quantity returned |
| condition | VARCHAR(20) | NN | NEW, USED, DAMAGED |
| inspection_notes | TEXT | | Condition assessment |

### Refunds (`refunds`)
Refund processing records.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Refund ID |
| return_id | BIGINT | NN, FK | Return reference |
| amount | NUMERIC(12,2) | NN | Refund amount |
| reason | VARCHAR(100) | NN | Refund reason |
| status | VARCHAR(20) | NN | PENDING, APPROVED, PROCESSING, COMPLETED, REJECTED |
| processed_at | TIMESTAMPTZ | | Actual refund time |
| created_at | TIMESTAMPTZ | NN | |

## POS Tables

### POS Receipts (`pos_receipts`)
Point-of-sale transaction records.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Receipt ID |
| location_id | BIGINT | NN, FK | Store location |
| staff_id | BIGINT | NN, FK | Staff member |
| total_amount | NUMERIC(12,2) | NN | Sale total |
| payment_method | VARCHAR(50) | NN | cash, card, etc. |
| status | VARCHAR(20) | NN | COMPLETED, VOID |
| created_at | TIMESTAMPTZ | NN | |

### POS Receipt Items (`pos_receipt_items`)
Line items in receipts.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| receipt_id | BIGINT | NN, FK | Receipt reference |
| sku_id | BIGINT | NN, FK | Product |
| quantity | INT | NN | Quantity sold |
| unit_price | NUMERIC(10,2) | NN | Price at sale |
| subtotal | NUMERIC(12,2) | NN | |

### POS Shifts (`pos_shifts`)
Shift management and reconciliation.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Shift ID |
| location_id | BIGINT | NN, FK | Store location |
| staff_id | BIGINT | NN, FK | Staff member |
| started_at | TIMESTAMPTZ | NN | Shift start |
| ended_at | TIMESTAMPTZ | | Shift end (NULL if open) |
| opening_cash | NUMERIC(12,2) | | Cash drawer opening amount |
| closing_cash | NUMERIC(12,2) | | Cash drawer closing amount |
| expected_total | NUMERIC(12,2) | | Expected from receipts |
| variance | NUMERIC(12,2) | | Closing - expected |
| status | VARCHAR(20) | NN | OPEN, CLOSED, RECONCILED |

## Marketing Tables

### Discount Codes (`discount_codes`)
Promotion and discount management.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Discount ID |
| code | VARCHAR(50) | NN, UQ | Promo code |
| type | VARCHAR(20) | NN | percentage or fixed_amount |
| value | NUMERIC(10,2) | NN | Discount value |
| min_cart_value | NUMERIC(12,2) | | Minimum cart to apply |
| max_uses | INT | | Max times usable (NULL = unlimited) |
| valid_from | TIMESTAMPTZ | NN | Start date |
| valid_to | TIMESTAMPTZ | NN | End date |
| status | VARCHAR(20) | NN | ACTIVE, EXPIRED, INACTIVE |
| created_by | BIGINT | NN, FK | Creator |
| created_at | TIMESTAMPTZ | NN | |

### Discount Usage (`discount_usage`)
Track discount application.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| discount_id | BIGINT | NN, FK | Discount code |
| order_id | BIGINT | NN, FK | Applied to order |
| amount_saved | NUMERIC(12,2) | NN | Actual savings |
| created_at | TIMESTAMPTZ | NN | |

## Instagram Integration Tables

### Instagram Connections (`instagram_connections`)
User account connections to Instagram.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Connection ID |
| business_user_id | BIGINT | NN, FK | SeShop admin user |
| instagram_user_id | VARCHAR(100) | NN, UQ | Instagram ID |
| instagram_account_name | VARCHAR(255) | | @account_name |
| access_token | TEXT | NN | Instagram API token |
| token_expires_at | TIMESTAMPTZ | | Token expiration |
| status | VARCHAR(20) | NN | CONNECTED, DISCONNECTED, TOKEN_EXPIRED |
| connected_at | TIMESTAMPTZ | NN | Connection time |

### Instagram Drafts (`instagram_drafts`)
Composed social media drafts.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | Draft ID |
| business_user_id | BIGINT | NN, FK | Creator |
| caption | TEXT | NN | Draft caption |
| status | VARCHAR(20) | NN | DRAFT, PUBLISHED, ARCHIVED |
| created_at | TIMESTAMPTZ | NN | |
| published_at | TIMESTAMPTZ | | Publication time |

### Instagram Draft Items (`instagram_draft_items`)
Products linked to drafts.

| Column | Type | Constraints | Purpose |
|--------|------|-------------|---------|
| id | BIGSERIAL | PK | |
| draft_id | BIGINT | NN, FK | Draft reference |
| product_id | BIGINT | NN, FK | Product |
| display_order | INT | | Order in draft |

## Queries & Optimization

### Common Query Patterns

**Find available stock by location**:
```sql
SELECT sku_id, available_qty
FROM inventory_balances
WHERE location_id = ? AND available_qty > 0
ORDER BY available_qty DESC;
```

**Check SKU availability across all locations**:
```sql
SELECT location_id, available_qty
FROM inventory_balances
WHERE sku_id = ? AND available_qty > 0
ORDER BY location_id;
```

**Find low-stock items**:
```sql
SELECT sku_id, location_id, available_qty
FROM inventory_balances
WHERE available_qty < 10
ORDER BY available_qty;
```

**Audit trail for an order**:
```sql
SELECT * FROM audit_logs
WHERE target_type = 'Order' AND target_id = ?
ORDER BY created_at DESC;
```

## References

- [SESHOP Data Dictionary](../../docs/5.Database/SESHOP%20Data%20Dictionary.md)
- [SESHOP schema.sql](../../docs/5.Database/SESHOP%20schema.sql)
- [SESHOP LLD](../../docs/3.Design/SESHOP%20LLD.md)

