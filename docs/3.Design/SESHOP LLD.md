# SE SHOP LOW-LEVEL DESIGN (LLD)

**Project:** SeShop  
**Domain:** Omnichannel clothing & accessories platform  
**Architecture style:** Modular Monolith  
**Backend:** Java + Spring Boot  
**Frontend:** React + TypeScript  
**Database:** PostgreSQL  
**Last updated:** 2026-04-29

---

## Revision History

| Date | Version | Author | Description |
|---|---:|---|---|
| 2026-04-29 | 1.1 | GitHub Copilot | Expanded LLD with full SRS coverage matrix, detailed use-case design, and doc consistency checks |

---

## Table of Contents

1. [References](#references)
2. [LLD Scope and Design Principles](#lld-scope-and-design-principles)
3. [Design Consistency Check Against Existing Docs](#design-consistency-check-against-existing-docs)
4. [Backend Package and Module Structure](#backend-package-and-module-structure)
5. [Cross-Cutting Technical Conventions](#cross-cutting-technical-conventions)
6. [Identity and RBAC LLD](#identity-and-rbac-lld)
7. [Catalog LLD](#catalog-lld)
8. [Inventory LLD](#inventory-lld)
9. [Commerce LLD](#commerce-lld)
10. [POS and Returns LLD](#pos-and-returns-lld)
11. [Marketing and Social LLD](#marketing-and-social-lld)
12. [Customer Engagement LLD](#customer-engagement-lld)
13. [Shared Platform Services LLD](#shared-platform-services-lld)
14. [Frontend LLD](#frontend-lld)
15. [API Design LLD](#api-design-lld)
16. [Data Mapping LLD](#data-mapping-lld)
17. [Key Sequence Specifications](#key-sequence-specifications)
18. [Validation and Error Model](#validation-and-error-model)
19. [Security and Authorization Rules](#security-and-authorization-rules)
20. [Deployment and Runtime Notes](#deployment-and-runtime-notes)
21. [Open Items and Implementation Notes](#open-items-and-implementation-notes)
22. [Conclusion](#conclusion)

---

## References

This LLD derives scope and requirements from:

- [docs/1.BRD/SESHOP BRD.md](../1.BRD/SESHOP%20BRD.md)
- [docs/10.SRS/SESHOP SRS.md](../10.SRS/SESHOP%20SRS.md)
- [docs/3.Design/SESHOP HLD.md](SESHOP%20HLD.md)
- [docs/4. View descriptions/SeShop Views Desc.md](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- [docs/5.Database/SESHOP schema.sql](../5.Database/SESHOP%20schema.sql)

---

## LLD Scope and Design Principles

### Design Principles
1. **Server-authoritative state** for order, payment, inventory, and permission decisions.
2. **Single source of truth** per aggregate root.
3. **No direct table coupling across modules**; access through services or repositories.
4. **Explicit state machines** for workflows with legal transitions.
5. **Idempotent command handling** for retried requests.
6. **Append-only auditing** for sensitive actions.
7. **Thin controllers, rich domain services**.
8. **Frontend is a consumer of business rules**, not the owner of business rules.

### Primary Implementation Targets
- REST APIs
- Role-based UI routing
- Transactional service methods
- Database entities with constraints and indexes
- Async workers for non-critical background jobs

---

## Design Consistency Check Against Existing Docs

This section is included to explicitly double-check the design against the repository docs.

### Confirmed alignments
- **RBAC model:** `users`, `roles`, `permissions`, `role_permissions`, `user_roles`, `audit_logs` in [docs/5.Database/SESHOP schema.sql](../5.Database/SESHOP%20schema.sql) and [docs/5.Database/SESHOP Data Dictionary.md](../5.Database/SESHOP%20Data%20Dictionary.md).
- **Inventory model:** `inventory_balances` uses only `on_hand_qty` and `reserved_qty`; no stored `available_qty`.
- **Inventory transfer:** lifecycle is `DRAFT`, `IN_TRANSIT`, `COMPLETED`, `CANCELLED`.
- **Instagram workflow:** compose-first with manual review gate; no auto-publish.
- **Discount codes:** use `code`, `discount_type`, `discount_value`, `min_spend`, `max_uses`, `start_at`, `end_at`, `status`.
- **POS payment methods:** card/Stripe and cash only in the current docs.
- **Shipping:** shipping-only fulfillment is in scope; BOPIS is not.
- **Customer stock visibility:** show location availability and quantity; do not expose internal node type labels unless needed.
- **Role assignment:** staff can hold multiple roles; effective permissions are the union of assigned roles.
- **Return and refund:** reverse logistics is supported with inspection and disposition.
- **Invoices:** tax invoice and adjustment note are supported.

### Notes on terminology
- The repository uses `user_roles` in schema and diagrams, not `staff_role_assignments`.
- Instagram is described as integration / connection, not direct publish automation.
- Procurement exists in the schema and SRS via purchase order and goods receipt flows.

---

## SRS Coverage Matrix

This matrix maps each SRS use case to the implementation boundary, key tables, and UI surface. It is the main traceability bridge between the SRS and the LLD.

| UC | Use Case | Primary Module | Key Tables | Primary View(s) | Core API Surface | Notes |
|---|---|---|---|---|---|---|
| UC1 | Create custom role | Identity & RBAC | roles, audit_logs | ADMIN_002 | `/admin/roles` | Create inactive role until permissions exist |
| UC2 | Assign permission to role | Identity & RBAC | role_permissions, roles, audit_logs | ADMIN_002 | `/admin/roles/{roleId}/permissions` | Duplicate mapping forbidden |
| UC3 | Assign or revoke role for staff | Identity & RBAC | user_roles, roles, users, audit_logs | ADMIN_002 | `/admin/users/{userId}/roles` | Multiple active roles allowed |
| UC4 | View audit logs | Identity & RBAC | audit_logs | ADMIN_004 | `/admin/audit-logs` | Append-only, filterable |
| UC5 | Add product and SKUs | Catalog | products, product_variants, product_images, product_categories | STAFF_001 | `/staff/products` | SKU-level sellable unit |
| UC6 | Adjust SKU inventory | Inventory | inventory_balances, audit_logs | STAFF_002 | `/staff/inventory/adjustments` | Atomic stock mutation |
| UC7 | Transfer stock between locations | Inventory | inventory_transfers, inventory_transfer_items, inventory_balances, audit_logs | STAFF_003 | `/staff/inventory/transfers` | DRAFT → IN_TRANSIT → COMPLETED/CANCELLED |
| UC8 | Process POS sale | POS & Returns | pos_shifts, pos_receipts, pos_receipt_items, inventory_balances | STAFF_007 | `/pos/receipts` | Receipt and stock decrement in one transaction |
| UC9 | Process refund | POS & Returns | refunds, return_requests, return_items, payments | STAFF_005 | `/refunds` | Full/partial refund supported |
| UC10 | Manage discount codes | Commerce | discount_codes, discount_redemptions | STAFF_006 | `/discounts` | Single-code per order in v1 |
| UC11 | Compose Instagram draft | Marketing & Social | instagram_connections, instagram_drafts, product_images | STAFF_009 | `/marketing/drafts` | Manual publish approval gate |
| UC12 | Register account | Commerce | users | CUST_001, CUST_008 | `/auth/register` | Customer identity creation |
| UC13 | Browse and filter variants | Catalog + Commerce | products, product_variants, categories, inventory_balances | CUST_001, CUST_002, CUST_003 | `/products`, `/products/{id}` | Stock visibility must remain fresh |
| UC14 | AI recommendation chat | Customer Engagement | products, product_variants, inventory_balances, reviews (context) | CUST_009 | `/assistant/recommendations` | In-stock prioritization |
| UC15 | Checkout and pay | Commerce | orders, order_items, payments, inventory_balances, discount_redemptions | CUST_004, CUST_005 | `/checkout` | Stripe or COD |
| UC16 | View stock by location | Inventory + Commerce | inventory_balances, locations | CUST_003 | `/products/{productId}/availability` | Show location name and count only |
| UC17 | Track shipment | Commerce | shipments, orders | CUST_006 | `/orders/{id}` | Shipping timeline and carrier link |
| UC18 | Leave review with image | Customer Engagement | reviews, order_items, users | CUST_007 | `/reviews` | Verified purchaser only |
| UC19 | View pending online orders | Commerce | orders, order_items, order_allocations | STAFF_004 | `/staff/orders` | Actionable pending queue |
| UC20 | Mark order as shipped with tracking | Commerce | shipments, orders, audit_logs | STAFF_004 | `/staff/orders/{id}/ship` | Tracking format validation required |
| UC21 | Connect or reconnect Instagram account | Marketing & Social | instagram_connections, audit_logs | STAFF_010 | `/marketing/instagram` | OAuth token lifecycle managed securely |
| UC22 | Purchase order and stock receiving | Inventory | suppliers, purchase_orders, purchase_order_items, goods_receipts, goods_receipt_items | STAFF_002, STAFF_003 | `/staff/purchase-orders`, `/staff/goods-receipts` | Partial receiving supported |
| UC23 | Allocate order to fulfillment location | Commerce | orders, order_items, order_allocations, inventory_balances | STAFF_004 | `/staff/orders/{id}/allocate` | Location priority and availability policy |
| UC24 | Return intake and exchange | POS & Returns | return_requests, return_items, refunds, exchanges, inventory_balances | STAFF_005 | `/returns`, `/exchanges` | Disposition: RESTOCK/REFURBISH/DISPOSE |
| UC25 | Cycle count and inventory reconciliation | Inventory | cycle_counts, cycle_count_items, inventory_balances, audit_logs | STAFF_002 | `/staff/cycle-counts` | Freeze scope snapshot |
| UC26 | POS shift close and cash reconciliation | POS & Returns | pos_shifts, cash_reconciliations, pos_receipts | STAFF_008 | `/pos/shifts/{id}/close` | Variance workflow and report generation |
| UC27 | Generate tax invoice and adjustment note | POS & Returns | tax_invoices, invoice_adjustment_notes, orders, pos_receipts | STAFF_008 | `/invoices/tax`, `/invoices/{id}/adjustments` | Financial documents are immutable |

---

## Detailed Use Case Design

### 1. Identity and RBAC use cases (UC1–UC4)

#### UC1: Create custom role
**Primary actor:** Super Admin  
**Backend flow:** validate `roleName` uniqueness → create `roles` row with `INACTIVE` status → write audit log → return role id  
**Tables:** `roles`, `audit_logs`  
**Frontend:** Admin role form in [ADMIN_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)  
**Validation:** role name required, trimmed, unique; description optional  
**State transition:** new role always starts `INACTIVE` until permissions exist  
**Events:** `RoleCreated`  
**Error cases:** duplicate name, unauthorized caller, invalid characters

#### UC2: Assign permission to role
**Primary actor:** Super Admin  
**Backend flow:** load role + permission catalog → reject duplicate pair → insert `role_permissions` rows → activate role if first permission → audit  
**Tables:** `role_permissions`, `roles`, `permissions`, `audit_logs`  
**Validation:** permission code must exist; duplicate mapping forbidden  
**State transition:** `roles.status` may move from `INACTIVE` to `ACTIVE`  
**Events:** `RolePermissionGranted`  
**Edge cases:** multiple permission selection, partial duplicate selection

#### UC3: Assign or revoke role for staff
**Primary actor:** Super Admin  
**Backend flow:** verify staff account active → insert or revoke `user_roles` assignment → recompute effective permission union → invalidate sensitive sessions if required → audit  
**Tables:** `users`, `roles`, `user_roles`, `audit_logs`  
**Validation:** only active roles may be assigned; critical revocation takes effect immediately  
**State transition:** assignment lifecycle tracked by `assigned_at` and `revoked_at`  
**Events:** `StaffRoleAssigned`, `StaffRoleRevoked`  
**Edge cases:** multiple active roles, revocation of last role, inactive target user

#### UC4: View audit logs
**Primary actor:** Super Admin  
**Backend flow:** filter append-only `audit_logs` by date/actor/action/target → paginate results → optionally export CSV  
**Tables:** `audit_logs`  
**Validation:** `audit.read` permission required; export restricted to authorized admins  
**UX:** read-only timeline/table with filters in [ADMIN_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)  
**Edge cases:** large datasets require indexed queries; no edit/delete actions in UI

### 2. Catalog use cases (UC5, UC13, UC18 context)

#### UC5: Add product and SKUs
**Primary actor:** Authorized Staff  
**Backend flow:** create `products` record → insert one or more `product_variants` rows → attach `categories` and `product_images` → generate Instagram-safe renditions if needed → audit  
**Tables:** `products`, `product_variants`, `product_categories`, `product_images`, `audit_logs`  
**Validation:** product title required; each SKU code unique; at least one variant is expected for a sellable product; image metadata preserved externally  
**State transition:** `products.status` can be `DRAFT` then `PUBLISHED`  
**Views:** [STAFF_001](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)  
**Events:** `ProductCreated`, `ProductVariantCreated`, `ProductPublished`

#### UC13: Browse and filter variants
**Primary actor:** Customer  
**Backend flow:** query published products + variants + category filters + stock availability projection → return paginated list with availability badges  
**Tables:** `products`, `product_variants`, `categories`, `product_categories`, `inventory_balances`  
**Validation:** only published products visible to customers; out-of-stock items may still be shown with status  
**Views:** [CUST_001](../4.%20View%20descriptions/SeShop%20Views%20Desc.md), [CUST_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)  
**Edge cases:** filter combinations, no-result state, stock freshness after inventory changes

#### UC18: Leave review with image
**Primary actor:** Customer  
**Backend flow:** verify the review is tied to a delivered order item → accept rating/comment/image → save `reviews` row → moderation workflow if policy requires → audit  
**Tables:** `reviews`, `order_items`, `users`  
**Validation:** verified purchaser only; rating 1–5; image file type/size checked  
**State:** review is either visible immediately or after moderation, depending on policy  
**Views:** [CUST_007](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

### 3. Inventory use cases (UC6, UC7, UC22, UC23, UC25, UC16)

#### UC6: Adjust SKU inventory
**Primary actor:** Authorized Staff  
**Backend flow:** locate `inventory_balances` row → calculate delta → guard against negative stock unless override permission exists → update atomically → write audit  
**Tables:** `inventory_balances`, `audit_logs`  
**Validation:** reason code mandatory; negative stock protected  
**Event:** `InventoryAdjusted`  
**Views:** [STAFF_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC7: Transfer stock between locations
**Primary actor:** Authorized Staff  
**Backend flow:** create `inventory_transfers` in `DRAFT` → add `inventory_transfer_items` → validate source availability → move to `IN_TRANSIT` after approval/confirmation policy → decrement source on transit or approval as per transfer policy → increment destination on receipt → close as `COMPLETED`  
**Tables:** `inventory_transfers`, `inventory_transfer_items`, `inventory_balances`, `audit_logs`  
**Validation:** source stock must be sufficient; destination must be active; state transition must be legal  
**Views:** [STAFF_003](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)  
**Edge cases:** cancelled transfer, partial receive, damage during transfer

#### UC22: Purchase order and stock receiving
**Primary actor:** Authorized Staff  
**Backend flow:** create `purchase_orders` and `purchase_order_items` → approve PO → receive goods into `goods_receipts` and `goods_receipt_items` → update inventory balances after receipt post  
**Tables:** `suppliers`, `purchase_orders`, `purchase_order_items`, `goods_receipts`, `goods_receipt_items`, `inventory_balances`  
**Validation:** supplier required; destination location required; received quantity can be partial; damaged quantity tracked separately  
**Views:** inventory operational views in [STAFF_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md) and [STAFF_003](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC23: Allocate order to fulfillment location
**Primary actor:** Authorized Staff or system-assisted workflow  
**Backend flow:** analyze order item stock across locations → rank candidate locations by configured policy (availability, shipping SLA, priority) → create `order_allocations` → reserve stock  
**Tables:** `orders`, `order_items`, `order_allocations`, `inventory_balances`  
**Validation:** allocated quantity cannot exceed available stock; split allocation allowed when one location cannot fulfill all items  
**Views:** [STAFF_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC25: Cycle count and inventory reconciliation
**Primary actor:** Authorized Staff  
**Backend flow:** create `cycle_counts` with frozen snapshot → capture `cycle_count_items` → compare with system stock → approve variances over threshold → post adjustments → generate reconciliation report  
**Tables:** `cycle_counts`, `cycle_count_items`, `inventory_balances`, `audit_logs`  
**Validation:** snapshot immutable; threshold-based approval required; reason code mandatory  
**Views:** [STAFF_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC16: View stock by location
**Primary actor:** Customer  
**Backend flow:** join `inventory_balances` with `locations` for the selected SKU → return friendly location name + quantity + last updated timestamp  
**Tables:** `inventory_balances`, `locations`  
**Validation:** expose location availability only; hide internal node type labels where unnecessary  
**Views:** [CUST_003](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

### 4. Commerce use cases (UC12, UC14, UC15, UC17, UC19, UC20)

#### UC12: Register account
**Primary actor:** Customer  
**Backend flow:** validate unique identity fields → create `users` record with `CUSTOMER` type → initialize account status → optionally trigger verification  
**Tables:** `users`  
**Validation:** username/email/phone unique; password policy enforced  
**Views:** [CUST_008](../4.%20View%20descriptions/SeShop%20Views%20Desc.md) and [CUST_001](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC14: AI recommendation chat
**Primary actor:** Customer  
**Backend flow:** send prompt + customer context + live stock data to AI adapter → receive ranked products with rationale → allow add-to-cart only after stock revalidation  
**Tables:** uses read data from `products`, `product_variants`, `inventory_balances`  
**Validation:** prioritise in-stock SKUs; response should explain rationale; fallback if AI unavailable  
**Views:** [CUST_009](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC15: Checkout and pay
**Primary actor:** Customer  
**Backend flow:** validate cart and reservation window → calculate totals and discount → create `orders` + `order_items` → create `payments` row → branch into Stripe or COD → finalize order state and release/convert stock reservation  
**Tables:** `orders`, `order_items`, `payments`, `discount_codes`, `discount_redemptions`, `inventory_balances`  
**Validation:** 15-minute reservation timeout; Stripe or COD only; failed payment must not create paid order  
**Views:** [CUST_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md), [CUST_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC17: Track shipment
**Primary actor:** Customer  
**Backend flow:** load `shipments` by order → return timeline status, tracking number, and carrier link if available  
**Tables:** `shipments`, `orders`  
**Validation:** tracking status synchronized with carrier when available; show unavailable state gracefully  
**Views:** [CUST_006](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC19: View pending online orders
**Primary actor:** Authorized Staff  
**Backend flow:** query actionable `orders` by status/date/location → show SKU lines, payment state, address, and fulfillment status → allow pick/pack updates  
**Tables:** `orders`, `order_items`, `order_allocations`, `shipments`  
**Validation:** staff needs `order.read` or equivalent permission; only actionable states visible by default  
**Views:** [STAFF_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC20: Mark order as shipped with tracking
**Primary actor:** Authorized Staff  
**Backend flow:** validate eligible order state → create/update shipment carrier and tracking number → set shipment status to `SHIPPED` → audit and notify customer  
**Tables:** `shipments`, `orders`, `audit_logs`  
**Validation:** tracking format required; only shippable states allowed; immediate customer notification on success  
**Views:** [STAFF_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

### 5. POS and reverse logistics use cases (UC8, UC9, UC24, UC26, UC27)

#### UC8: Process POS sale
**Primary actor:** Authorized Staff  
**Backend flow:** ensure active shift → create receipt and receipt items → charge cash/card payment → decrement location inventory atomically → finalize receipt  
**Tables:** `pos_shifts`, `pos_receipts`, `pos_receipt_items`, `inventory_balances`  
**Validation:** active shift required; cash/card only; transaction rolls back on failure  
**Views:** [STAFF_007](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC9: Process refund
**Primary actor:** Authorized Staff  
**Backend flow:** inspect original order/POS transaction → validate refund policy and refundable amount → create `refunds` row → update payment settlement → restore stock according to disposition policy  
**Tables:** `refunds`, `return_requests`, `return_items`, `payments`, `inventory_balances`  
**Validation:** full or partial refund only within policy; link to source transaction required  
**Views:** [STAFF_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC24: Return intake and exchange
**Primary actor:** Authorized Staff  
**Backend flow:** create return request → receive returned item → inspect condition → decide disposition → create refund or exchange order → restock/dispose/refurbish stock  
**Tables:** `return_requests`, `return_items`, `refunds`, `exchanges`, `inventory_balances`  
**Validation:** return window, condition policy, and final-sale constraints apply  
**Views:** [STAFF_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC26: POS shift close and cash reconciliation
**Primary actor:** Authorized Staff  
**Backend flow:** aggregate shift sales → compare expected vs actual cash → record variance and reason if needed → finalize shift → produce report  
**Tables:** `pos_shifts`, `cash_reconciliations`, `pos_receipts`  
**Validation:** no pending unsaved transactions; variance over threshold requires supervisor approval  
**Views:** [STAFF_008](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC27: Generate tax invoice and adjustment note
**Primary actor:** Authorized Staff  
**Backend flow:** derive taxable totals from completed transaction → create `tax_invoices` row → if correction needed, create `invoice_adjustment_notes` linked to original invoice → audit and lock document  
**Tables:** `tax_invoices`, `invoice_adjustment_notes`, `orders`, `pos_receipts`  
**Validation:** invoice number unique; adjustment note must reference original invoice; documents immutable once issued  
**Views:** [STAFF_008](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

### 6. Promotions and social use cases (UC10, UC11, UC21)

#### UC10: Manage discount codes
**Primary actor:** Authorized Staff  
**Backend flow:** create/update `discount_codes` with validity window, usage cap, and minimum spend → validate uniqueness → use redemption table at checkout  
**Tables:** `discount_codes`, `discount_redemptions`  
**Validation:** one code per order in v1; start/end dates valid; min spend respected  
**Views:** [STAFF_006](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC11: Compose Instagram draft
**Primary actor:** Authorized Staff  
**Backend flow:** verify Instagram connection → select product assets → generate caption suggestions and media order → save draft as `READY_FOR_REVIEW` → wait for manual approval  
**Tables:** `instagram_connections`, `instagram_drafts`, `product_images`  
**Validation:** no auto-publish; image ordering preserved; draft editable prior to approval  
**Views:** [STAFF_009](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

#### UC21: Connect or reconnect Instagram account
**Primary actor:** Authorized Staff  
**Backend flow:** redirect to OAuth consent → exchange code for encrypted tokens → validate scopes → save connection row → audit lifecycle events  
**Tables:** `instagram_connections`, `audit_logs`  
**Validation:** token encryption at rest; reconnect rotates tokens; permissions must match allowed scopes  
**Views:** [STAFF_010](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

---

## Detailed State Models

### Order state transition rules

| Current State | Allowed Next States | Trigger |
|---|---|---|
| `PENDING` | `ALLOCATED`, `CANCELLED` | Checkout completed or cancelled |
| `ALLOCATED` | `PICKING`, `CANCELLED` | Fulfillment assignment |
| `PICKING` | `PACKED`, `CANCELLED` | Pick/pack completed |
| `PACKED` | `SHIPPED`, `CANCELLED` | Shipment created |
| `SHIPPED` | `DELIVERED` | Carrier update |
| `DELIVERED` | `COMPLETED`, `RETURNED` | Post-delivery finalization or return |

### Inventory transfer state transition rules

| Current State | Allowed Next States | Trigger |
|---|---|---|
| `DRAFT` | `IN_TRANSIT`, `CANCELLED` | Approved / dispatched transfer |
| `IN_TRANSIT` | `COMPLETED`, `CANCELLED` | Destination receipt or cancellation |
| `COMPLETED` | None | Terminal state |
| `CANCELLED` | None | Terminal state |

### Draft state transition rules

| Current State | Allowed Next States | Trigger |
|---|---|---|
| `NEW` | `EDITING`, `ARCHIVED` | Staff opens draft |
| `EDITING` | `READY_FOR_REVIEW`, `ARCHIVED` | Staff submits for review |
| `READY_FOR_REVIEW` | `APPROVED`, `EDITING`, `ARCHIVED` | Reviewer action |
| `APPROVED` | `ARCHIVED` | Retired after use |
| `ARCHIVED` | None | Terminal state |

### Refund state transition rules

| Current State | Allowed Next States | Trigger |
|---|---|---|
| `PENDING` | `APPROVED`, `REJECTED` | Staff decision |
| `APPROVED` | `PROCESSING` | Settlement started |
| `PROCESSING` | `COMPLETED`, `REJECTED` | Gateway response |
| `REJECTED` | None | Terminal state |
| `COMPLETED` | None | Terminal state |

---

## Detailed API Surface by Domain

### Identity and RBAC
- `POST /api/v1/admin/roles`
- `POST /api/v1/admin/roles/{roleId}/permissions`
- `POST /api/v1/admin/users/{userId}/roles`
- `DELETE /api/v1/admin/users/{userId}/roles/{assignmentId}`
- `GET /api/v1/admin/audit-logs`

### Catalog
- `POST /api/v1/staff/products`
- `PUT /api/v1/staff/products/{productId}`
- `POST /api/v1/staff/products/{productId}/variants`
- `POST /api/v1/staff/products/{productId}/images`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`

### Inventory
- `POST /api/v1/staff/inventory/adjustments`
- `POST /api/v1/staff/inventory/transfers`
- `POST /api/v1/staff/inventory/transfers/{transferId}/receive`
- `POST /api/v1/staff/cycle-counts`
- `POST /api/v1/staff/purchase-orders`
- `POST /api/v1/staff/goods-receipts`
- `GET /api/v1/products/{productId}/availability`

### Commerce
- `POST /api/v1/auth/register`
- `GET /api/v1/carts/me`
- `POST /api/v1/carts/me/items`
- `POST /api/v1/checkout`
- `GET /api/v1/orders/me`
- `GET /api/v1/orders/{orderId}`
- `POST /api/v1/orders/{orderId}/track-shipment`
- `POST /api/v1/discounts/validate`

### POS and Returns
- `POST /api/v1/pos/shifts/open`
- `POST /api/v1/pos/receipts`
- `POST /api/v1/pos/shifts/{shiftId}/close`
- `POST /api/v1/refunds`
- `POST /api/v1/returns`
- `POST /api/v1/returns/{returnId}/approve`
- `POST /api/v1/invoices/tax`
- `POST /api/v1/invoices/{invoiceId}/adjustments`

### Marketing and Social
- `POST /api/v1/marketing/instagram/connect`
- `POST /api/v1/marketing/instagram/reconnect`
- `GET /api/v1/marketing/instagram/status`
- `POST /api/v1/marketing/drafts`
- `PUT /api/v1/marketing/drafts/{draftId}`
- `POST /api/v1/marketing/drafts/{draftId}/submit-review`
- `POST /api/v1/marketing/drafts/{draftId}/approve`

### Customer Engagement
- `POST /api/v1/reviews`
- `GET /api/v1/reviews/product/{productId}`
- `POST /api/v1/assistant/recommendations`

---

## Detailed UI-to-Backend Mapping Notes

### Admin screens
- [ADMIN_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): role and permission management must call RBAC endpoints directly.
- [ADMIN_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): audit log viewer is read-only and supports export.
- [ADMIN_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): configuration values should be persisted separately from core commerce tables if implemented later.

### Staff screens
- [STAFF_001](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): product creation should support media upload and SKU matrix editing.
- [STAFF_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): inventory adjustment, transfer, receiving, and cycle count are core operational tools.
- [STAFF_003](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): transfer detail screen must show lifecycle and receipt status.
- [STAFF_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): order management screen must support allocation, pack, ship, and label workflows.
- [STAFF_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): returns/refunds screen must map to return, refund, and exchange endpoints.
- [STAFF_006](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): discount management is tied to `discount_codes` only.
- [STAFF_007](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): POS is a fast-touch flow with active shift guard.
- [STAFF_008](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): shift close and invoice flows are finance-sensitive and must be audited.
- [STAFF_009](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): Instagram draft editor must preserve media ordering and manual review gating.
- [STAFF_010](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): account connection view must show token expiry, status, and reconnection action.

### Customer screens
- [CUST_001](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): browse/search filters must be backed by product and availability queries.
- [CUST_002](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): product detail page must show variant choice and stock availability.
- [CUST_003](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): location stock locator must return friendly location names only.
- [CUST_004](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): cart must validate stock on change and on checkout.
- [CUST_005](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): checkout must support Stripe and COD only.
- [CUST_006](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): tracking screen must handle carrier sync delays gracefully.
- [CUST_007](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): review submission only after eligible purchase.
- [CUST_009](../4.%20View%20descriptions/SeShop%20Views%20Desc.md): AI assistant must prioritize available inventory.

---

## Open Items and Implementation Notes

The repository still leaves a few implementation decisions open:
- Finalize external shipping webhook contract and retry policy.
- Confirm AI recommendation safety and output filtering policy.
- Approve bilingual message catalog for production.
- Decide whether customer profile should support saved payment cards immediately or phase later.
- Confirm final permission code naming convention for staff screens and APIs.

Implementation notes:
- Purchase order and goods receipt flows are required because they are present in the schema and SRS.
- The current design must not introduce BOPIS.
- The current design must not introduce direct social auto-publish.
- The current design must not store `available_qty` or `variance_qty` as persistent columns because the schema intentionally avoids them.

---

## Backend Package and Module Structure

### Suggested package layout
- `com.seshop.identity`
- `com.seshop.catalog`
- `com.seshop.inventory`
- `com.seshop.commerce`
- `com.seshop.pos`
- `com.seshop.refund`
- `com.seshop.marketing`
- `com.seshop.review`
- `com.seshop.shipping`
- `com.seshop.payment`
- `com.seshop.notification`
- `com.seshop.audit`
- `com.seshop.shared`

### Internal layers inside each module
- `api` – controllers, request/response DTOs, mappers
- `application` – use cases, commands, queries, orchestration
- `domain` – entities, value objects, services, events, policies
- `infrastructure` – JPA entities, repositories, adapters, external clients

### Dependency rule
`api` -> `application` -> `domain` -> `infrastructure` only through interfaces.

---

## Cross-Cutting Technical Conventions

### API conventions
- Base path: `/api/v1`
- JSON request/response bodies
- Pagination: `page`, `size`, `sort`
- Filtering: query parameters per resource
- Idempotency header: `Idempotency-Key`
- Error response envelope: `code`, `message`, `details`, `traceId`

### Naming conventions
- REST resources plural nouns: `/products`, `/orders`, `/inventory/transfers`
- Commands use verbs in endpoint subpaths: `/orders/{id}/ship`
- State fields use uppercase enum values in persistence, mapped to typed enums in code

### Transaction conventions
- A service method that mutates stock or money must run in a transaction.
- Publish domain events after commit or via outbox.
- Never update inventory and external payment state in separate uncontrolled steps.

### State handling conventions
- Keep canonical state in the main aggregate.
- Store auxiliary histories in child tables or audit logs.
- Use optimistic locking where concurrent updates are likely.

---

## Identity and RBAC LLD

### 1. Purpose
Manage authentication identities, roles, permissions, assignments, and audit trails.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| User | `users` | Canonical account record for admin, staff, customer |
| Role | `roles` | Named role with status |
| Permission | `permissions` | Atomic capability code |
| RolePermission | `role_permissions` | Many-to-many permission mapping |
| UserRoleAssignment | `user_roles` | Assignment lifecycle with revoke support |
| AuditLog | `audit_logs` | Append-only action history |

### 3. Core rules
- Email, phone number, and username must be unique.
- Staff effective permissions are derived from all active roles.
- Role cannot be assigned if inactive.
- Revoked assignments remain in history via `revoked_at`.
- Audit record is required for privileged changes.

### 4. State model

#### User status
- `ACTIVE`
- `INACTIVE`
- `LOCKED`

#### Role status
- `ACTIVE`
- `INACTIVE`

### 5. Main use cases
- Create custom role
- Assign permissions to role
- Assign/revoke role for staff
- View audit logs

### 6. Application service responsibilities
- Validate uniqueness and permission existence
- Enforce access checks
- Update role status based on permission count
- Emit audit records
- Compute effective permissions for current session

### 7. Key endpoints
- `POST /api/v1/admin/roles`
- `GET /api/v1/admin/roles`
- `POST /api/v1/admin/roles/{roleId}/permissions`
- `POST /api/v1/admin/users/{userId}/roles`
- `DELETE /api/v1/admin/users/{userId}/roles/{assignmentId}`
- `GET /api/v1/admin/audit-logs`

### 8. Validation rules
- `roleName` required, trimmed, unique.
- Permission code required, unique, canonical lower-dot format recommended.
- Only Super Admin can mutate RBAC master data.

---

## Catalog LLD

### 1. Purpose
Manage product master data, categories, variants, and media assets.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| Category | `categories` | Master category taxonomy |
| Product | `products` | Parent product record |
| ProductCategory | `product_categories` | Many-to-many mapping |
| ProductVariant | `product_variants` | SKU-level sellable unit |
| ProductImage | `product_images` | Product or variant media |

### 3. Core rules
- SKU code must be unique.
- Product must have at least one variant to be sellable.
- Product publish status controls customer visibility.
- Images are stored externally; DB stores URL and ordering metadata.
- Instagram-ready assets are marked by `is_instagram_ready`.

### 4. Catalog state model
#### Product status
- `DRAFT`
- `PUBLISHED`
- `ARCHIVED`

#### Variant status
- `ACTIVE`
- `INACTIVE`

### 5. Main use cases
- Add product and SKUs
- Browse/filter products
- View product detail and media
- Generate Instagram-safe renditions

### 6. Application service responsibilities
- Validate mandatory fields
- Attach categories and images
- Create variant matrix with size/color combinations
- Generate image renditions for social compose flow
- Publish or archive product based on staff action

### 7. Key endpoints
- `POST /api/v1/staff/products`
- `PUT /api/v1/staff/products/{productId}`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`
- `POST /api/v1/staff/products/{productId}/variants`
- `POST /api/v1/staff/products/{productId}/images`
- `GET /api/v1/categories`

### 8. Validation rules
- Product name required.
- Price required at variant level.
- Category assignment optional in DB but recommended for merchandising.
- Duplicate SKU within system scope is forbidden.

---

## Inventory LLD

### 1. Purpose
Maintain stock by SKU-location, support transfers, receiving, and cycle counts.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| Location | `locations` | Store or storage node |
| InventoryBalance | `inventory_balances` | On-hand and reserved quantities |
| InventoryTransfer | `inventory_transfers` | Transfer header |
| InventoryTransferItem | `inventory_transfer_items` | Line-level quantities |
| CycleCount | `cycle_counts` | Counting session |
| CycleCountItem | `cycle_count_items` | Variance capture |
| Supplier | `suppliers` | Procurement master |
| PurchaseOrder | `purchase_orders` | Inbound procurement header |
| PurchaseOrderItem | `purchase_order_items` | Purchase lines |
| GoodsReceipt | `goods_receipts` | Inbound receipt header |
| GoodsReceiptItem | `goods_receipt_items` | Received line quantities |

### 3. Core rules
- Inventory is tracked per `variant_id` and `location_id`.
- `available_to_sell = on_hand_qty - reserved_qty` is computed, not stored.
- Transfers must not move more than available stock.
- Cycle count snapshot must remain immutable during the session.
- Purchase order receiving can be partial.
- Damaged quantity is captured separately from received quantity.

### 4. Transfer lifecycle
- `DRAFT`
- `IN_TRANSIT`
- `COMPLETED`
- `CANCELLED`

### 5. Cycle count lifecycle
- `OPEN`
- `REVIEWED`
- `APPROVED`

### 6. Main use cases
- Adjust SKU inventory
- Transfer stock between locations
- Cycle count and reconciliation
- Purchase order and stock receiving
- View stock by location

### 7. Application service responsibilities
- Lock inventory rows during mutation
- Validate sufficient quantity
- Reserve or decrement source stock per policy
- Confirm destination stock on receipt
- Record reason codes for manual adjustments
- Write inventory audit logs

### 8. Key endpoints
- `GET /api/v1/staff/inventory/balances`
- `POST /api/v1/staff/inventory/adjustments`
- `POST /api/v1/staff/inventory/transfers`
- `POST /api/v1/staff/inventory/transfers/{transferId}/approve`
- `POST /api/v1/staff/inventory/transfers/{transferId}/receive`
- `POST /api/v1/staff/cycle-counts`
- `POST /api/v1/staff/purchase-orders`
- `POST /api/v1/staff/goods-receipts`
- `GET /api/v1/products/{productId}/availability`

### 9. Validation rules
- Location must be active.
- Source quantity must be sufficient for transfer.
- Negative adjustment requires explicit permission.
- Only completed transfer changes destination stock.
- Count variance beyond threshold requires approval.

---

## Commerce LLD

### 1. Purpose
Handle cart, order creation, payment, shipment, allocation, discount code, and customer order lifecycle.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| Cart | `carts` | Customer cart header |
| CartItem | `cart_items` | Cart lines |
| Order | `orders` | Canonical order aggregate |
| OrderItem | `order_items` | Order lines |
| OrderAllocation | `order_allocations` | Fulfillment assignment |
| Shipment | `shipments` | Shipping status and tracking |
| Payment | `payments` | Payment transaction record |
| DiscountCode | `discount_codes` | Promotion rule |
| DiscountRedemption | `discount_redemptions` | Applied redemption history |

### 3. Core rules
- Cart items must always refer to an existing active SKU.
- Checkout validates cart content, price, stock reservation, and discount logic.
- Single discount code per order in v1.
- Discount rule uses code uniqueness, discount type, amount, minimum spend, usage cap, and validity window.
- Payment state and shipment state are separate fields in `orders`.
- Failed payment must not produce a paid order state.

### 4. Order state model
#### `orders.status`
Suggested lifecycle:
- `PENDING`
- `ALLOCATED`
- `PICKING`
- `PACKED`
- `SHIPPED`
- `DELIVERED`
- `COMPLETED`
- `CANCELLED`
- `RETURNED`

#### `orders.payment_status`
- `PENDING`
- `PAID`
- `FAILED`
- `REFUNDED`
- `PARTIALLY_REFUNDED`

#### `orders.shipment_status`
- `PENDING`
- `SHIPPED`
- `IN_TRANSIT`
- `DELIVERED`

### 5. Main use cases
- Register account
- Browse/filter variants
- AI recommendation chat add-to-cart
- Checkout and pay
- View stock by location
- Track shipment

### 6. Application service responsibilities
- Create and maintain cart state
- Validate order total and discount application
- Create order and payment records transactionally
- Reserve or release inventory on payment state change
- Allocate order to fulfillment location(s)
- Store shipment tracking details and notify customer

### 7. Key endpoints
- `POST /api/v1/auth/register`
- `GET /api/v1/products`
- `GET /api/v1/products/{productId}`
- `GET /api/v1/carts/me`
- `POST /api/v1/carts/me/items`
- `POST /api/v1/checkout`
- `GET /api/v1/orders/me`
- `GET /api/v1/orders/{orderId}`
- `POST /api/v1/orders/{orderId}/track-shipment`
- `POST /api/v1/discounts/validate`

### 8. Validation rules
- Email and phone required for registration.
- Checkout reservation timeout is 15 minutes.
- Shipping address required for shipment flow.
- Discount cannot exceed order eligibility rules.
- Tracking number format must be validated before shipment update.

---

## POS and Returns LLD

### 1. Purpose
Support in-store sale, shift close, cash reconciliation, refund, return intake, exchange, tax invoice, and adjustment note.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| POSShift | `pos_shifts` | Shift header |
| POSReceipt | `pos_receipts` | POS sale header |
| POSReceiptItem | `pos_receipt_items` | Receipt lines |
| CashReconciliation | `cash_reconciliations` | Shift cash variance |
| ReturnRequest | `return_requests` | Reverse logistics header |
| ReturnItem | `return_items` | Return lines |
| Refund | `refunds` | Refund record |
| Exchange | `exchanges` | Replacement flow |
| TaxInvoice | `tax_invoices` | Financial document |
| InvoiceAdjustmentNote | `invoice_adjustment_notes` | Correction document |

### 3. Core rules
- POS sale and inventory decrement must be one transactional boundary.
- Cash and card are the supported POS payment methods in v1.
- Refund must reference original order or POS transaction.
- Return disposition must be one of `RESTOCK`, `REFURBISH`, `DISPOSE`.
- Tax invoice and adjustment note are immutable once issued.

### 4. State models
#### POS shift
- `OPEN`
- `CLOSED`
- `DISCREPANCY_REVIEW`

#### Return request
- `REQUESTED`
- `LABEL_GENERATED`
- `RETURNED`
- `RECEIVED`
- `INSPECTED`
- `APPROVED`
- `REJECTED`
- `REFUNDED`

#### Refund
- `PENDING`
- `APPROVED`
- `PROCESSING`
- `COMPLETED`
- `REJECTED`

### 5. Main use cases
- Process POS sale
- POS shift close and cash reconciliation
- Process refund
- Return intake and exchange
- Generate tax invoice and adjustment note

### 6. Application service responsibilities
- Maintain active shift constraint
- Decrement stock atomically on sale completion
- Calculate and compare cash totals at shift close
- Validate refund policy eligibility
- Create invoice/adjustment note documents
- Restore or reclassify stock based on return disposition

### 7. Key endpoints
- `POST /api/v1/pos/shifts/open`
- `POST /api/v1/pos/receipts`
- `POST /api/v1/pos/shifts/{shiftId}/close`
- `POST /api/v1/refunds`
- `POST /api/v1/returns`
- `POST /api/v1/returns/{returnId}/approve`
- `POST /api/v1/invoices/tax`
- `POST /api/v1/invoices/{invoiceId}/adjustments`

### 8. Validation rules
- Cashier must have active shift.
- No unsaved transactions may exist before shift close.
- Refund amount cannot exceed eligible amount.
- Return window and condition policy must be checked.
- Invoice number and adjustment number must be unique.

---

## Marketing and Social LLD

### 1. Purpose
Support Instagram connection, draft generation, review workflow, and social content management without automatic publish.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| InstagramConnection | `instagram_connections` | OAuth/token state |
| InstagramDraft | `instagram_drafts` | Draft content |

### 3. Core rules
- Tokens are encrypted at rest.
- Drafts move through review states.
- No auto-publish is allowed without manual approval.
- Product media and draft media order are preserved via JSON metadata.

### 4. Draft state model
- `NEW`
- `EDITING`
- `READY_FOR_REVIEW`
- `APPROVED`
- `ARCHIVED`

### 5. Main use cases
- Connect or reconnect Instagram account
- Compose Instagram draft
- Review and approve draft

### 6. Application service responsibilities
- Handle OAuth connection and token storage
- Validate required permissions/scopes
- Generate draft from product and media sources
- Save caption, hashtags, and media ordering
- Transition draft to review-ready or approved state

### 7. Key endpoints
- `POST /api/v1/marketing/instagram/connect`
- `POST /api/v1/marketing/instagram/reconnect`
- `GET /api/v1/marketing/instagram/status`
- `POST /api/v1/marketing/drafts`
- `PUT /api/v1/marketing/drafts/{draftId}`
- `POST /api/v1/marketing/drafts/{draftId}/submit-review`
- `POST /api/v1/marketing/drafts/{draftId}/approve`

### 8. Validation rules
- Instagram connection requires valid OAuth state.
- Draft caption and hashtags are optional but recommended.
- Image order must be preserved.
- Publish action is not implemented in v1.

---

## Customer Engagement LLD

### 1. Purpose
Support reviews, AI recommendation chat, and location-based stock visibility.

### 2. Entities and mapping

| Domain Entity | Table | Notes |
|---|---|---|
| Review | `reviews` | Product review with optional image |

### 3. Core rules
- Only verified purchasers can submit a review.
- One review per order item/customer pair.
- Review images are subject to moderation.
- AI recommendations must prioritize in-stock SKUs.
- Stock availability shows location name and quantity, not internal node type labels.

### 4. Main use cases
- Leave review with image
- AI recommendation chat
- View stock by location

### 5. Application service responsibilities
- Validate verified purchase before review submission
- Create moderation queue entry if needed
- Call AI service with live inventory context
- Return relevant products with rationale
- Return location stock availability in customer-friendly form

### 6. Key endpoints
- `POST /api/v1/reviews`
- `GET /api/v1/reviews/product/{productId}`
- `POST /api/v1/assistant/recommendations`
- `GET /api/v1/products/{productId}/availability`

### 7. Validation rules
- Rating between 1 and 5.
- Review image type/size validated on upload.
- AI response must not recommend unavailable items unless explicitly flagged as out-of-stock alternatives.

---

## Shared Platform Services LLD

### 1. Audit service
- Writes append-only entries to `audit_logs`.
- Accepts actor, action, target type, target id, and metadata JSON.
- Required for privileged operations and stock-sensitive operations.

### 2. Notification service
- Sends order confirmation, shipment updates, return updates, and admin alerts.
- Can use async jobs for email/SMS delivery.

### 3. File/media service
- Stores product images, review images, and generated media renditions.
- Returns storage URL or object key.

### 4. Idempotency service
- Prevents duplicated checkout, payment, refund, and transfer operations on retried requests.
- Uses request key plus actor plus resource scope.

### 5. Localization service
- Supports Vietnamese and English messages.
- Keeps server error codes stable and UI text localized.

---

## Frontend LLD

### 1. Frontend architecture
React + TypeScript with a feature-based structure.

### 2. Suggested folder layout
- `src/app` – app bootstrap, providers, route guards
- `src/pages` – route screens
- `src/widgets` – composed page blocks
- `src/features` – actions such as checkout, inventory adjustment, draft editor
- `src/entities` – product, order, inventory, role, user, location models
- `src/shared` – UI kit, hooks, utilities, API client, constants

### 3. State strategy
- **Server state:** TanStack Query
- **UI state:** local component state or lightweight store
- **Auth/session state:** centralized store
- **Forms:** React Hook Form + Zod

### 4. Route map aligned to docs

| Route | View Target | Purpose |
|---|---|---|
| `/admin/dashboard` | ADMIN_001 | Business metrics and system health |
| `/admin/users-roles` | ADMIN_002 | RBAC management |
| `/admin/locations` | ADMIN_003 | Location management |
| `/admin/audit` | ADMIN_004 | Audit log viewer |
| `/admin/settings` | ADMIN_005 | System configuration |
| `/staff/catalog` | STAFF_001 | Product and SKU management |
| `/staff/inventory` | STAFF_002 | Inventory adjustment and movement |
| `/staff/transfers` | STAFF_003 | Transfer logistics |
| `/staff/orders` | STAFF_004 | Order management |
| `/staff/returns` | STAFF_005 | Refund and return processing |
| `/staff/discounts` | STAFF_006 | Discount management |
| `/staff/pos` | STAFF_007 | POS terminal |
| `/staff/pos/shift-close` | STAFF_008 | Shift close and reconciliation |
| `/staff/marketing/drafts` | STAFF_009 | Instagram draft management |
| `/staff/marketing/instagram` | STAFF_010 | Instagram connection settings |
| `/` | CUST_001 | Home and browsing |
| `/products/:id` | CUST_002 | Product detail and variants |
| `/stock/:productId` | CUST_003 | Stock availability by location |
| `/cart` | CUST_004 | Cart |
| `/checkout` | CUST_005 | Checkout and payment |
| `/orders/:id` | CUST_006 | Tracking |
| `/reviews/:orderItemId` | CUST_007 | Review submission |
| `/account` | CUST_008 | Customer profile |
| `/assistant` | CUST_009 | AI chat |
| `/wishlist` | CUST_010 | Wishlist |

### 5. Feature component responsibilities

#### Catalog pages
- Fetch product list, categories, filters
- Render search, sort, and variant cards
- Show stock and availability badges

#### Checkout feature
- Multi-step form, validation, order summary, payment method selection
- Call checkout API with idempotency key
- Show payment success/failure states

#### Staff grid screens
- Dense tables with filters, bulk actions, and detail drawer panels
- Keyboard-friendly navigation and shortcuts

#### Marketing draft editor
- Rich text caption editor
- Image reorder panel
- Product picker
- Review status workflow

### 6. Frontend validation rules
- Use the same message catalog as backend error codes.
- Do not trust client-side state for final stock or price decisions.
- Disable actions if the UI knows the user lacks permission, but still rely on backend authorization.

---

## API Design LLD

### 1. General response structure

Success response:
- `data`
- `meta`

Error response:
- `code`
- `message`
- `details`
- `traceId`

### 2. Common status codes
- `200` success
- `201` created
- `204` no content
- `400` validation error
- `401` unauthenticated
- `403` forbidden
- `404` not found
- `409` conflict
- `422` business rule violation
- `500` unexpected failure

### 3. Representative endpoint contracts

#### Register account
`POST /api/v1/auth/register`
- Input: username, email, phone, password
- Output: account id, verification status
- Rules: unique email/phone/username

#### Checkout
`POST /api/v1/checkout`
- Input: cart id, shipping address, payment method, discount code, idempotency key
- Output: order number, payment status, shipment status
- Rules: reservation timeout, payment validation, stock validation

#### Create inventory transfer
`POST /api/v1/staff/inventory/transfers`
- Input: source location, destination location, line items, reason
- Output: transfer id, status
- Rules: source stock available, audit log required

#### Save Instagram draft
`POST /api/v1/marketing/drafts`
- Input: product id, caption, hashtags, media order
- Output: draft id, draft state
- Rules: editable draft, manual approval gate

#### Submit review
`POST /api/v1/reviews`
- Input: order item id, rating, comment, optional image
- Output: review id, moderation state
- Rules: verified purchase required

---

## Data Mapping LLD

### 1. Database design principles
- Keep tables aligned with aggregates.
- Use foreign keys for referential integrity.
- Use unique constraints for stable business identifiers.
- Use timestamps consistently in `TIMESTAMPTZ`.
- Use indexes for frequent list and join paths.

### 2. Critical mapping notes

| Business Concept | Table / Column Note |
|---|---|
| Location stock | `inventory_balances.variant_id + location_id` unique pair |
| Available stock | computed as `on_hand_qty - reserved_qty` |
| Transfer item | `inventory_transfer_items.transfer_id + variant_id` unique pair |
| Count item | `cycle_count_items.cycle_count_id + variant_id` unique pair |
| Discount redemption | `discount_redemptions.discount_code_id + order_id` unique pair |
| Review uniqueness | `reviews.order_item_id + customer_user_id` unique pair |

### 3. Index usage
- `idx_inventory_balances_variant_location`
- `idx_orders_customer_created_at`
- `idx_order_items_order_id`
- `idx_shipments_order_id`
- `idx_payments_order_id`
- `idx_returns_order_id`
- `idx_pos_receipts_shift_id`
- `idx_audit_logs_actor_created_at`

### 4. Derived values not stored
- Available-to-sell quantity
- Aggregated stock by product
- Order totals after discounts in display views
- Review average rating

---

## Key Sequence Specifications

### 1. Checkout and payment
1. Customer submits checkout.
2. Backend validates cart and stock.
3. Backend creates order and payment intent/record.
4. Stripe or COD branch executed.
5. On success, order and inventory states are updated.
6. Confirmation notification is sent.

### 2. Inventory transfer approval
1. Staff creates transfer draft.
2. Backend validates source quantity.
3. Transfer enters `DRAFT`.
4. Supervisor approves.
5. Transfer enters `IN_TRANSIT` and source stock is deducted.
6. Destination confirms receipt.
7. Transfer becomes `COMPLETED` and destination stock increases.

### 3. POS sale
1. Cashier starts active shift.
2. Items scanned and totals confirmed.
3. Payment captured.
4. POS receipt created.
5. Inventory decremented.
6. Receipt finalized and audit log written.

### 4. Instagram draft workflow
1. Staff selects product assets.
2. System generates draft body and media order.
3. Staff edits content.
4. Draft is saved as `READY_FOR_REVIEW`.
5. Manager approves.
6. No automatic publish occurs in v1.

### 5. Review submission
1. Customer opens purchased item review form.
2. System verifies purchase eligibility.
3. Review stored and queued for moderation if needed.
4. Approved review affects aggregate rating.

---

## Validation and Error Model

### Validation categories
- **Field validation:** required values, formats, lengths
- **Business validation:** stock availability, status transition, permission checks
- **Conflict validation:** duplicate keys, concurrent updates
- **Policy validation:** return window, discount rules, review eligibility

### Error examples
| Code | Meaning |
|---|---|
| `AUTH_001` | Authentication required |
| `AUTH_002` | Insufficient permission |
| `CAT_001` | Duplicate SKU code |
| `INV_001` | Insufficient stock |
| `INV_002` | Invalid transfer state |
| `ORD_001` | Checkout reservation expired |
| `PAY_001` | Payment failed |
| `POS_001` | Shift already closed |
| `SOC_001` | Instagram connection expired |
| `REV_001` | Review not eligible |

### Frontend behavior
- Show actionable error messages.
- Preserve user input where possible.
- Highlight invalid fields inline.
- Retry only when the request is idempotent or recoverable.

---

## Security and Authorization Rules

### Authentication
- Secure token-based authentication.
- Passwords stored only as hashes.
- Sensitive integrations use encrypted secret storage.

### Authorization
- Permission-based checks at controller and service level.
- Super Admin has governance rights but still passes through permission checks.
- Staff permissions are derived from active roles.

### File upload security
- Validate mime type, file size, and extension.
- Store media outside the relational database.
- Scan or sanitize uploads according to platform policy.

### Auditing
Write audit records for:
- Role changes
- Inventory changes
- Transfers
- POS sale and shift close
- Refund and adjustment note issuance
- Instagram connection changes
- System settings updates

---

## Deployment and Runtime Notes

### Runtime components
- Web frontend
- Java backend API
- Worker process for async jobs
- PostgreSQL
- Redis
- Object storage

### Environment tiers
- Development
- Staging/UAT
- Production

### Operational notes
- Use blue/green or rolling deployment where possible.
- Ensure DB migrations are backward-compatible.
- Keep the same module boundaries across all environments.
- Backup DB and object storage on a scheduled basis.

---

## Open Items and Implementation Notes

### Open items carried from the docs
- Finalize external shipping webhook contract and retry policy.
- Confirm AI recommendation safety and output filtering policy.
- Approve bilingual message catalog for production.
- Decide whether customer profile should support saved payment cards immediately or phase later.
- Confirm final permission code naming convention for staff screens and APIs.

### Implementation notes
- Purchase order and goods receipt flows should be implemented because they are present in the schema and SRS.
- The current design must not introduce BOPIS.
- The current design must not introduce direct social auto-publish.
- The current design must not store `available_qty` or `variance_qty` as persistent columns because the schema intentionally avoids them.

---

## Conclusion

This LLD translates the approved SeShop documentation into implementable backend, frontend, API, and data decisions. It is intentionally aligned with the BRD, SRS, database schema, database diagram, use-case diagram, and view descriptions in the repository.

The next step after this LLD should be either:
- module-by-module implementation tasks,
- API specification with request/response examples, or
- class-level design for a single bounded context.
