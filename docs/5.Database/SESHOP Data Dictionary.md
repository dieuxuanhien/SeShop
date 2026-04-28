# SESHOP Data Dictionary (Minimal, Optimized)

This document explains every table in `docs/5.Database/SESHOP schema.sql`.

## Conventions

- PK: Primary Key
- FK: Foreign Key
- NN: Not Null
- UQ: Unique
- Timestamps use `TIMESTAMPTZ`

## 1. `users`

### Table Function
Stores all authenticated identities (admin, staff, customer) as the canonical account source. This table is the root entity for authorization, ownership, and auditing across the platform.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Internal user identifier |
| username | VARCHAR(80) | NN, UQ | Login name |
| email | VARCHAR(255) | NN, UQ | User email |
| phone_number | VARCHAR(20) | NN, UQ | User phone |
| password_hash | TEXT | NN | Password hash (never plaintext) |
| user_type | VARCHAR(20) | NN | ADMIN, STAFF, CUSTOMER |
| status | VARCHAR(20) | NN | Account state |
| created_at | TIMESTAMPTZ | NN | Creation time |
| updated_at | TIMESTAMPTZ | NN | Last update time |

## 2. `roles`

### Table Function
Defines role profiles used by RBAC. Each role groups permissions into business responsibilities such as operations, inventory control, or finance compliance.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Role ID |
| name | VARCHAR(80) | NN, UQ | Role name |
| description | TEXT |  | Role description |
| status | VARCHAR(20) | NN | ACTIVE/INACTIVE |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 3. `permissions`

### Table Function
Defines atomic system capabilities (for example, inventory transfer approval). It is the permission catalog used to enforce least-privilege access control.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Permission ID |
| code | VARCHAR(120) | NN, UQ | Atomic permission code |
| description | TEXT |  | Human explanation |

## 4. `role_permissions`

### Table Function
Junction table mapping roles to permissions. It materializes which capabilities are granted by each role and supports deterministic authorization checks.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| role_id | BIGINT | PK, FK | Role reference |
| permission_id | BIGINT | PK, FK | Permission reference |
| created_at | TIMESTAMPTZ | NN | Grant creation time |

## 5. `user_roles`

### Table Function
Stores role assignments for each user over time, including assignment and revocation history. It enables temporary or changing responsibilities without losing auditability.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Assignment ID |
| user_id | BIGINT | NN, FK | Assigned user |
| role_id | BIGINT | NN, FK | Assigned role |
| assigned_by | BIGINT | FK | Admin who assigned |
| assigned_at | TIMESTAMPTZ | NN | Assign time |
| revoked_at | TIMESTAMPTZ |  | Revoke time |

## 6. `audit_logs`

### Table Function
Append-only security and operations audit trail. It records who performed what action, against which target, and when, enabling traceability and compliance review.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Audit record ID |
| actor_user_id | BIGINT | FK | Who did the action |
| action | VARCHAR(120) | NN | Action code/name |
| target_type | VARCHAR(80) | NN | Entity type affected |
| target_id | VARCHAR(80) |  | Entity ID affected |
| metadata_json | JSONB |  | Context payload |
| created_at | TIMESTAMPTZ | NN | Action time |

## 7. `categories`

### Table Function
Master list of product categories used for browsing, filtering, and merchandising organization. It standardizes catalog taxonomy across channels.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Category ID |
| name | VARCHAR(120) | NN, UQ | Category name |
| description | TEXT |  | Category detail |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 8. `products`

### Table Function
Product master records representing sellable items at parent level. This table stores shared product metadata independent of specific SKU variants.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Product ID |
| name | VARCHAR(200) | NN | Product display name |
| brand | VARCHAR(120) |  | Brand |
| description | TEXT |  | Product description |
| status | VARCHAR(20) | NN | DRAFT/PUBLISHED/ARCHIVED |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 9. `product_categories`

### Table Function
Many-to-many bridge between products and categories. It allows one product to appear in multiple taxonomies and supports flexible catalog navigation.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| product_id | BIGINT | PK, FK | Product reference |
| category_id | BIGINT | PK, FK | Category reference |

## 10. `product_variants`

### Table Function
SKU-level representation (size, color, etc.) used for inventory, pricing, allocation, and order lines. This is the operational sellable unit in omnichannel flows.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Variant ID |
| product_id | BIGINT | NN, FK | Parent product |
| sku_code | VARCHAR(80) | NN, UQ | Unique SKU |
| size | VARCHAR(30) |  | Size attribute |
| color | VARCHAR(30) |  | Color attribute |
| price | NUMERIC(12,2) | NN | Selling price |
| status | VARCHAR(20) | NN | ACTIVE/INACTIVE |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 11. `product_images`

### Table Function
Stores image assets associated with products or specific variants. It controls media ordering and channel-readiness (such as Instagram-compatible assets).


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Image row ID |
| product_id | BIGINT | NN, FK | Product reference |
| variant_id | BIGINT | FK | Optional variant reference |
| url | TEXT | NN | Image URL |
| sort_order | INT | NN | Display order |
| is_instagram_ready | BOOLEAN | NN | Social-ready flag |
| created_at | TIMESTAMPTZ | NN | Created time |

## 12. `locations`

### Table Function
Master list of inventory nodes (store or storage). It is the base reference for stock balances, transfers, allocations, and POS operations.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Location ID |
| code | VARCHAR(40) | NN, UQ | Location code |
| display_name | VARCHAR(120) | NN | Location label |
| location_type | VARCHAR(20) | NN | STORE/STORAGE |
| status | VARCHAR(20) | NN | ACTIVE/INACTIVE |

## 13. `inventory_balances`

### Table Function
Current stock position by variant and location. It maintains on-hand and reserved quantities used to compute available-to-sell values in real time.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Inventory row ID |
| variant_id | BIGINT | NN, FK, UQ(pair) | SKU reference |
| location_id | BIGINT | NN, FK, UQ(pair) | Location reference |
| on_hand_qty | INT | NN | Physical quantity |
| reserved_qty | INT | NN | Reserved for allocations |
| updated_at | TIMESTAMPTZ | NN | Last stock update |

## 14. `inventory_transfers`

### Table Function
Transfer headers for moving stock between locations. It tracks transfer lifecycle and accountability from creation to completion or cancellation.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Transfer ID |
| source_location_id | BIGINT | NN, FK | Source location |
| destination_location_id | BIGINT | NN, FK | Destination location |
| status | VARCHAR(20) | NN | DRAFT/IN_TRANSIT/COMPLETED/CANCELLED |
| created_by | BIGINT | NN, FK | Creator |
| created_at | TIMESTAMPTZ | NN | Created time |
| completed_at | TIMESTAMPTZ |  | Completion time |

## 15. `inventory_transfer_items`

### Table Function
Line-level quantities within each transfer. It captures requested, received, and damaged amounts for reconciliation and discrepancy control.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Transfer item ID |
| transfer_id | BIGINT | NN, FK, UQ(pair) | Transfer reference |
| variant_id | BIGINT | NN, FK, UQ(pair) | SKU transferred |
| qty | INT | NN | Requested quantity |
| received_qty | INT |  | Received quantity |
| damaged_qty | INT |  | Damaged quantity |

## 16. `cycle_counts`

### Table Function
Cycle count session headers used for periodic inventory verification. It governs count workflow status, ownership, and approval checkpoints.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Cycle count ID |
| location_id | BIGINT | NN, FK | Counted location |
| status | VARCHAR(20) | NN | OPEN/REVIEWED/APPROVED |
| started_by | BIGINT | NN, FK | Counter owner |
| approved_by | BIGINT | FK | Approver |
| started_at | TIMESTAMPTZ | NN | Start time |
| approved_at | TIMESTAMPTZ |  | Approval time |

## 17. `cycle_count_items`

### Table Function
Counted SKU lines inside a cycle count session. It captures system-vs-physical comparison inputs and reason codes for approved adjustments.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Count line ID |
| cycle_count_id | BIGINT | NN, FK, UQ(pair) | Count batch |
| variant_id | BIGINT | NN, FK, UQ(pair) | Counted SKU |
| system_qty | INT | NN | System quantity at snapshot |
| counted_qty | INT | NN | Physical counted quantity |
| reason_code | VARCHAR(50) |  | Variance reason |

## 18. `suppliers`

### Table Function
Supplier master data used in procurement and inbound stock workflows. It centralizes partner identity for purchase orders and compliance references.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Supplier ID |
| name | VARCHAR(180) | NN | Supplier name |
| tax_code | VARCHAR(80) |  | Supplier tax code |
| phone | VARCHAR(20) |  | Supplier phone |
| email | VARCHAR(255) |  | Supplier email |
| address | TEXT |  | Supplier address |
| status | VARCHAR(20) | NN | ACTIVE/INACTIVE |

## 19. `purchase_orders`

### Table Function
Inbound procurement order headers raised to suppliers. It controls what is being ordered, where it will be received, and who approved the commitment.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | PO ID |
| po_number | VARCHAR(60) | NN, UQ | Purchase order number |
| supplier_id | BIGINT | NN, FK | Supplier |
| destination_location_id | BIGINT | NN, FK | Receiving location |
| status | VARCHAR(20) | NN | DRAFT/APPROVED/RECEIVED/CANCELLED |
| created_by | BIGINT | NN, FK | Creator |
| approved_by | BIGINT | FK | Approver |
| created_at | TIMESTAMPTZ | NN | Creation time |
| approved_at | TIMESTAMPTZ |  | Approval time |

## 20. `purchase_order_items`

### Table Function
SKU line items for each purchase order, including quantities and unit cost. It is the commercial detail basis for receiving and inventory increase.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | PO line ID |
| purchase_order_id | BIGINT | NN, FK, UQ(pair) | PO reference |
| variant_id | BIGINT | NN, FK, UQ(pair) | SKU |
| ordered_qty | INT | NN | Ordered quantity |
| unit_cost | NUMERIC(12,2) | NN | Cost per unit |

## 21. `goods_receipts`

### Table Function
Receiving event headers for inbound shipments tied to purchase orders. It marks the operational point where received stock becomes eligible for posting.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Receipt ID |
| purchase_order_id | BIGINT | NN, FK | PO reference |
| received_by | BIGINT | NN, FK | Receiver |
| received_at | TIMESTAMPTZ | NN | Receive time |
| status | VARCHAR(20) | NN | DRAFT/POSTED |

## 22. `goods_receipt_items`

### Table Function
Line-level received and damaged quantities per goods receipt. It provides precise inbound reconciliation before inventory posting.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Receipt line ID |
| goods_receipt_id | BIGINT | NN, FK, UQ(pair) | Receipt reference |
| variant_id | BIGINT | NN, FK, UQ(pair) | SKU |
| received_qty | INT | NN | Received quantity |
| damaged_qty | INT | NN | Damaged quantity |

## 23. `orders`

### Table Function
Customer order headers for online commerce. It stores lifecycle state, financial state, and high-level monetary totals used for fulfillment orchestration.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Order ID |
| order_number | VARCHAR(60) | NN, UQ | Human order code |
| customer_user_id | BIGINT | NN, FK | Buyer |
| status | VARCHAR(20) | NN | CREATED/CONFIRMED/CANCELLED/etc. |
| payment_status | VARCHAR(20) | NN | UNPAID/PAID/REFUNDED |
| shipment_status | VARCHAR(20) | NN | PENDING/SHIPPED/DELIVERED |
| total_amount | NUMERIC(12,2) | NN | Order total stored snapshot |
| currency | CHAR(3) | NN | Currency code |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 24. `order_items`

### Table Function
SKU line items belonging to customer orders. It is the granular source for allocation, shipping, return, exchange, and customer review eligibility.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Order line ID |
| order_id | BIGINT | NN, FK | Order reference |
| variant_id | BIGINT | NN, FK | SKU sold |
| qty | INT | NN | Quantity |
| unit_price | NUMERIC(12,2) | NN | Unit sale price |
| discount_amount | NUMERIC(12,2) | NN | Discount value |

## 25. `order_allocations`

### Table Function
Allocation decisions mapping order items to fulfillment locations. It implements omnichannel sourcing logic and reservation-backed fulfillment planning.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Allocation ID |
| order_item_id | BIGINT | NN, FK | Order line |
| location_id | BIGINT | NN, FK | Fulfillment location |
| allocated_qty | INT | NN | Allocated quantity |
| status | VARCHAR(20) | NN | ALLOCATED/PICKED/RELEASED |
| created_at | TIMESTAMPTZ | NN | Allocation time |

## 26. `shipments`

### Table Function
Shipment records for order delivery execution. It tracks carrier, tracking identifiers, and delivery progression from dispatch to completion.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Shipment ID |
| order_id | BIGINT | NN, FK | Order reference |
| carrier | VARCHAR(80) | NN | Carrier name |
| tracking_number | VARCHAR(120) | UQ(pair) | Tracking ID |
| status | VARCHAR(20) | NN | SHIPPED/IN_TRANSIT/DELIVERED |
| shipped_at | TIMESTAMPTZ |  | Ship time |
| delivered_at | TIMESTAMPTZ |  | Delivery time |

## 27. `payments`

### Table Function
Payment transactions linked to orders. It stores provider/method/status/amount to support checkout, settlement, and refund processes.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Payment ID |
| order_id | BIGINT | NN, FK | Order reference |
| provider | VARCHAR(50) | NN | Stripe, etc. |
| method | VARCHAR(30) | NN | CARD/COD/WALLET |
| status | VARCHAR(20) | NN | PENDING/COMPLETED/FAILED |
| amount | NUMERIC(12,2) | NN | Amount paid |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 28. `discount_codes`

### Table Function
Promotion code definitions and validity rules. It governs discount behavior such as type, value, spend thresholds, and usage windows.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Discount ID |
| code | VARCHAR(60) | NN, UQ | Coupon code |
| discount_type | VARCHAR(20) | NN | PERCENT/FIXED |
| discount_value | NUMERIC(12,2) | NN | Discount amount/rate |
| min_spend | NUMERIC(12,2) | NN | Minimum spend |
| max_uses | INT |  | Max redemptions |
| start_at | TIMESTAMPTZ | NN | Start time |
| end_at | TIMESTAMPTZ | NN | End time |
| status | VARCHAR(20) | NN | ACTIVE/EXPIRED |

## 29. `discount_redemptions`

### Table Function
Usage records when discount codes are applied to orders. It prevents duplicate application and provides campaign effectiveness data.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Redemption ID |
| discount_code_id | BIGINT | NN, FK | Discount used |
| order_id | BIGINT | NN, FK, UQ(pair) | Order |
| customer_user_id | BIGINT | NN, FK | Customer |
| redeemed_at | TIMESTAMPTZ | NN | Redemption time |

## 30. `return_requests`

### Table Function
Reverse-logistics request headers initiated by customers. It captures return reason, workflow status, and approval control points.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Return request ID |
| order_id | BIGINT | NN, FK | Original order |
| customer_user_id | BIGINT | NN, FK | Requester |
| reason | VARCHAR(255) | NN | Return reason |
| status | VARCHAR(20) | NN | REQUESTED/APPROVED/REJECTED/CLOSED |
| requested_at | TIMESTAMPTZ | NN | Request time |
| approved_by | BIGINT | FK | Approver |
| approved_at | TIMESTAMPTZ |  | Approval time |

## 31. `return_items`

### Table Function
Line-level returned quantities tied to original order items. It stores inspection outcomes and disposition decisions (restock/refurbish/dispose).


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Return line ID |
| return_request_id | BIGINT | NN, FK | Return request |
| order_item_id | BIGINT | NN, FK | Original order line |
| qty | INT | NN | Quantity returned |
| disposition | VARCHAR(20) |  | RESTOCK/REFURBISH/DISPOSE |
| inspected_by | BIGINT | FK | Inspector |
| inspected_at | TIMESTAMPTZ |  | Inspection time |

## 32. `refunds`

### Table Function
Monetary refund transactions associated with returns or other approved scenarios. It links financial reimbursement to the original commercial context.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Refund ID |
| order_id | BIGINT | NN, FK | Related order |
| payment_id | BIGINT | FK | Original payment |
| return_request_id | BIGINT | FK | Return request |
| amount | NUMERIC(12,2) | NN | Refund amount |
| status | VARCHAR(20) | NN | PENDING/COMPLETED/FAILED |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 33. `exchanges`

### Table Function
Replacement flow records for returned items. It links return lines to replacement SKUs/orders and tracks exchange lifecycle progress.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Exchange ID |
| return_item_id | BIGINT | NN, FK | Source return line |
| replacement_variant_id | BIGINT | NN, FK | New SKU |
| replacement_order_id | BIGINT | FK | Generated replacement order |
| status | VARCHAR(20) | NN | CREATED/SHIPPED/COMPLETED |
| created_at | TIMESTAMPTZ | NN | Created time |

## 34. `pos_shifts`

### Table Function
Store cashier shift sessions for POS governance. It defines operational boundaries for receipts, till ownership, and shift-close controls.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Shift ID |
| location_id | BIGINT | NN, FK | Store location |
| cashier_user_id | BIGINT | NN, FK | Cashier |
| opened_at | TIMESTAMPTZ | NN | Open time |
| closed_at | TIMESTAMPTZ |  | Close time |
| status | VARCHAR(20) | NN | OPEN/CLOSED |

## 35. `pos_receipts`

### Table Function
In-store sales headers generated within a POS shift. It captures transaction totals and payment method for store-channel revenue tracking.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | POS receipt ID |
| shift_id | BIGINT | NN, FK | Shift reference |
| customer_user_id | BIGINT | FK | Optional customer |
| total_amount | NUMERIC(12,2) | NN | Receipt total |
| payment_method | VARCHAR(30) | NN | CASH/CARD/etc. |
| created_at | TIMESTAMPTZ | NN | Created time |

## 36. `pos_receipt_items`

### Table Function
SKU line items sold in each POS receipt. It is the detailed basis for stock decrement, reporting, and post-sale support actions.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | POS line ID |
| receipt_id | BIGINT | NN, FK | Receipt |
| variant_id | BIGINT | NN, FK | SKU sold |
| qty | INT | NN | Quantity |
| unit_price | NUMERIC(12,2) | NN | Unit price |

## 37. `cash_reconciliations`

### Table Function
Shift-close reconciliation records comparing expected vs actual cash. It formalizes discrepancy handling and supervisor approval evidence.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Reconciliation ID |
| shift_id | BIGINT | NN, FK, UQ | Shift reference |
| expected_cash | NUMERIC(12,2) | NN | System expected cash |
| actual_cash | NUMERIC(12,2) | NN | Counted cash |
| variance_amount | NUMERIC(12,2) | NN | Difference |
| reason | VARCHAR(255) |  | Discrepancy reason |
| approved_by | BIGINT | FK | Supervisor |
| approved_at | TIMESTAMPTZ |  | Approval time |

## 38. `tax_invoices`

### Table Function
Legally relevant invoice documents for online/POS sales. It stores taxable amounts and issued invoice identity for compliance and accounting.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Invoice ID |
| invoice_number | VARCHAR(80) | NN, UQ | Invoice number |
| order_id | BIGINT | FK | Online order reference |
| pos_receipt_id | BIGINT | FK | POS receipt reference |
| customer_user_id | BIGINT | NN, FK | Billed customer |
| tax_rate | NUMERIC(5,2) | NN | Tax rate |
| subtotal_amount | NUMERIC(12,2) | NN | Pre-tax amount |
| tax_amount | NUMERIC(12,2) | NN | Tax value |
| total_amount | NUMERIC(12,2) | NN | Final amount |
| issued_at | TIMESTAMPTZ | NN | Issue time |

## 39. `invoice_adjustment_notes`

### Table Function
Correction notes linked to original invoices. It preserves immutable amendment history for legally compliant financial adjustments.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Adjustment ID |
| original_invoice_id | BIGINT | NN, FK | Base invoice |
| adjustment_number | VARCHAR(80) | NN, UQ | Adjustment number |
| reason | VARCHAR(255) | NN | Adjustment reason |
| delta_amount | NUMERIC(12,2) | NN | Amount delta (+/-) |
| created_by | BIGINT | NN, FK | Creator |
| created_at | TIMESTAMPTZ | NN | Created time |

## 40. `instagram_connections`

### Table Function
OAuth-based integration records for connected Instagram accounts. It stores encrypted token state used by marketing/social workflows.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Connection ID |
| user_id | BIGINT | NN, FK, UQ(pair) | SeShop owner user |
| account_id | VARCHAR(120) | NN, UQ(pair) | Instagram account ID |
| token_encrypted | TEXT | NN | Encrypted token |
| refresh_token_encrypted | TEXT |  | Encrypted refresh token |
| token_expires_at | TIMESTAMPTZ |  | Token expiry |
| status | VARCHAR(20) | NN | CONNECTED/DISCONNECTED |
| updated_at | TIMESTAMPTZ | NN | Last refresh/update |

## 41. `instagram_drafts`

### Table Function
Draft social posts prepared from product data before publication. It supports controlled marketing operations with editable caption/media ordering.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Draft ID |
| created_by | BIGINT | NN, FK | Staff user |
| product_id | BIGINT | NN, FK | Referenced product |
| caption | TEXT |  | Draft caption |
| hashtags | TEXT |  | Hashtag text |
| media_order_json | JSONB |  | Media order payload |
| status | VARCHAR(20) | NN | DRAFT/READY |
| created_at | TIMESTAMPTZ | NN | Created time |
| updated_at | TIMESTAMPTZ | NN | Updated time |

## 42. `reviews`

### Table Function
Customer product review records tied to purchased order lines. It powers trust signals and quality feedback while enforcing verified-purchase context.


| Column | Type | Constraints | Meaning |
|---|---|---|---|
| id | BIGSERIAL | PK | Review ID |
| order_item_id | BIGINT | NN, FK, UQ(pair) | Purchased line |
| customer_user_id | BIGINT | NN, FK, UQ(pair) | Reviewer |
| rating | INT | NN | Score |
| comment | TEXT |  | Comment |
| image_url | TEXT |  | Optional proof image |
| status | VARCHAR(20) | NN | PENDING/APPROVED/REJECTED |
| created_at | TIMESTAMPTZ | NN | Created time |
