# BUSINESS REQUIREMENTS DOCUMENT (BRD)

Prepared for `SeShop`

Date: `2026-03-27`
Version: `2.0 (Omnichannel Baseline)`

Prepared by: `GitHub Copilot (GPT-5.3-Codex) - Draft for Project Team Review`

## Revision History

| Date       | Version | Author        | Change Description                                                                                       |
| ---------- | ------- | ------------- | -------------------------------------------------------------------------------------------------------- |
| 2024-01-05 | 1.0     | Previous Team | Legacy BRD baseline                                                                                      |
| 2026-03-27 | 2.0     | Copilot Draft | Reframed as omnichannel clothing/accessories platform with detailed user stories and acceptance criteria |

## Approval

| Date | Version | Approver Name | Position        | Status  |
| ---- | ------- | ------------- | --------------- | ------- |
| TBD  | 2.0     | TBD           | Business Owner  | Pending |
| TBD  | 2.0     | TBD           | Product Manager | Pending |
| TBD  | 2.0     | TBD           | Technical Lead  | Pending |

## Table of Contents

1. Objective and Scope
2. Pre-computation (Design Reasoning)
3. Business Requirements
4. Application Overview
5. Domain Model
6. Workflows
7. Use Cases and Actors
8. Security Matrix (RBAC)
9. User Stories (Detailed and Specific)
10. Functional Requirements
11. Non-Functional Requirements
12. Assumptions and Dependencies
13. Out of Scope
14. Change Requirements
15. Appendix (Glossary, Open Issues)

---

## 1. Objective and Scope

This BRD defines the business requirements for `SeShop`, a single-business omnichannel e-commerce platform for clothing and accessories that unifies:

- Online storefront shopping.
- Multiple physical locations with POS operations.
- Centralized inventory visibility by item variant (`SKU` by size/color).
- Cross-location stock transfer (store-to-store, store-to-storage, storage-to-store).
- Role-based staff operations configured by Super Admin with flexible single-function roles.
- Social marketing workflow with Instagram-ready assets and compose-first publishing.

The document aligns business, product, and engineering teams on what must be built for `v1`.

---

## 2. Pre-computation (Design Reasoning)

### 2.1 Problem Understanding

SeShop needs one platform where online and offline commerce share the same inventory truth. The main value is omnichannel continuity: customer-visible stock by location, staff-driven operations across channels, and fast marketing execution via Instagram compose and export.

### 2.2 Stakeholder Perspective

- Business Owner: wants revenue growth, stock accuracy, and auditable operations.
- Super Admin: wants strict but flexible RBAC using small, composable permissions.
- Authorized Staff: wants clear operational workflows (catalog, inventory, order fulfillment, POS, transfers, refunds, promotions, social drafting).
- Customer: wants trustworthy stock visibility, easy checkout, AI-assisted discovery, and order transparency.

### 2.3 System Boundary Definition

In scope:

- Omnichannel catalog, inventory, order, POS, and role management.
- Customer storefront and checkout.
- Instagram compose flow and draft generation.
- Audit logging.

Out of scope for v1:

- Marketplace/multi-vendor model.
- Multi-business tenancy.
- Loyalty points and gift cards.
- Fully automated direct social publishing without human approval.

### 2.4 Constraints and Assumptions

- Single business brand: `SeShop`.
- Multiple locations are modeled uniformly in inventory logic; location type can be `STORE` or `STORAGE` internally.
- Storage locations are treated as normal inventory nodes in DB behavior.
- Customer UI shows location availability without exposing internal operational type when not needed.
- Payment in v1 includes `Stripe`, `Cash on Delivery`, and `In-store POS payment`.
- Shipping-only fulfillment in v1.
- Instagram flow is `compose draft + manual publish approval`.

### 2.5 High-Level Architecture Direction

- Modular monolith (or modular services) with clear domains: Identity/RBAC, Catalog, Inventory, Orders, POS, Marketing, AI Assistant.
- Event-driven updates for stock-affecting operations (`order_paid`, `pos_sale_completed`, `refund_completed`, `transfer_confirmed`).
- Immutable audit logs for privileged and operational actions.

---

## 3. Business Requirements

1. Unify online and offline stock in real time at SKU-location level.
2. Reduce overselling and manual reconciliation errors.
3. Enable configurable least-privilege staff operations.
4. Improve product discovery and conversion with AI recommendations.
5. Reduce social marketing turnaround time through Instagram compose templates.
6. Provide trustworthy customer experience through live location availability and order tracking.

---

## 4. Application Overview

`SeShop` consists of the following modules:

- Customer Web Storefront: browsing, filtering, cart, checkout, account, reviews.
- AI Recommendation Assistant: chat-based recommendation and direct add-to-cart actions.
- Omnichannel Inventory and Location Management: SKU stock by location and transfers.
- Order and Fulfillment: online order lifecycle and shipment tracking.
- POS and Refunds: in-store sales and return processing synchronized with central inventory.
- Access Management (RBAC): custom roles, permissions, assignments, revocation, auditability.
- Promotion Management: discount code lifecycle and campaign controls.
- Social Compose: Instagram-sized media handling, draft generation, manual editing and publish handoff.

---

## 5. Domain Model

### 5.1 Core Entities

| #  | Entity               | Description                                                            |
| -- | -------------------- | ---------------------------------------------------------------------- |
| 1  | User                 | Authentication identity (customer or staff account holder).            |
| 2  | StaffProfile         | Operational profile linked to user account for staff users.            |
| 3  | Role                 | Custom role created by Super Admin (single-function or combined).      |
| 4  | Permission           | Atomic action grant (for example `inventory.transfer.create`).       |
| 5  | RolePermission       | Mapping between role and permissions.                                  |
| 6  | UserRoleAssignment   | Mapping between user account and assigned role(s).                     |
| 7  | AuditLog             | Immutable record of sensitive/operational actions.                     |
| 8  | Product              | Parent clothing/accessory item.                                        |
| 9  | ProductVariant (SKU) | Sellable variant by attributes (size, color, etc.).                    |
| 10 | ProductImage         | Image assets with ordering metadata and target aspect options.         |
| 11 | Location             | Physical inventory node; internally typed as `STORE` or `STORAGE`. |
| 12 | InventoryBalance     | On-hand/available/reserved stock per SKU per location.                 |
| 13 | InventoryTransfer    | Movement of stock between locations with status trail.                 |
| 14 | Cart                 | Customer shopping cart.                                                |
| 15 | Order                | Online customer order aggregate.                                       |
| 16 | OrderItem            | SKU lines and quantities in order.                                     |
| 17 | Shipment             | Shipping record and tracking details.                                  |
| 18 | Payment              | Payment transaction metadata and status.                               |
| 19 | POSReceipt           | In-store sale receipt and line items.                                  |
| 20 | Refund               | Refund request and settlement for order/POS transactions.              |
| 21 | DiscountCode         | Promotional code rule and validity definition.                         |
| 22 | InstagramDraft       | Compose-ready social post draft with editable text/media ordering.     |
| 23 | Review               | Customer feedback with optional image uploads.                         |

### 5.2 Data Modeling Note for Stores and Storage

`Location` is a single inventory node entity. `STORE` and `STORAGE` are internal classifications only. Both are first-class stock holders in the database and transfer engine.

---

## 6. Workflows

### 6.1 Omnichannel Inventory Sync

1. Any stock-affecting operation occurs (order paid, POS sale, refund, transfer confirmation).
2. Inventory service updates `InventoryBalance` atomically by SKU-location.
3. Availability cache/search index is refreshed.
4. Customer and staff views reflect updated stock counts.

### 6.2 Staff Role Administration

1. Super Admin creates custom role.
2. Super Admin binds one or more atomic permissions.
3. Super Admin assigns role to staff.
4. Access enforcement updates instantly (or within defined propagation SLA).
5. All changes are written to immutable audit logs.

### 6.3 Instagram Compose Workflow

1. Staff selects product/variant assets.
2. System prepares Instagram-compatible media representation.
3. System generates draft caption and media list.
4. Staff edits caption, reorders images, adjusts details.
5. Staff saves draft and sends for manual publish.

---

## 7. Use Cases and Actors

### 7.1 Actors

| # | Actor            | Definition                                                         |
| - | ---------------- | ------------------------------------------------------------------ |
| 1 | Super Admin      | Full governance over RBAC, audit access, and high-risk operations. |
| 2 | Authorized Staff | Operational user with limited permissions assigned by role.        |
| 3 | Customer         | End user shopping through storefront.                              |
| 4 | External Service | Stripe, shipping providers, Instagram integration.             |

### 7.2 Primary Use Cases

| #  | Use Case                         | Primary Actor    | Brief Outcome                                 |
| -- | -------------------------------- | ---------------- | --------------------------------------------- |
| 1  | Create custom role               | Super Admin      | New role available for assignment.            |
| 2  | Assign permission to role        | Super Admin      | Role gains exact capability.                  |
| 3  | Assign/revoke role for staff     | Super Admin      | Staff access updated based on responsibility. |
| 4  | View audit logs                  | Super Admin      | Full traceability of system actions.          |
| 5  | Add product and SKUs             | Authorized Staff | New sellable catalog entries.                 |
| 6  | Adjust SKU inventory             | Authorized Staff | Corrected stock levels by location.           |
| 7  | Transfer stock between locations | Authorized Staff | Balanced stock availability.                  |
| 8  | Process POS sale                 | Authorized Staff | In-store sale completed and synced.           |
| 9  | Process refund                   | Authorized Staff | Customer refunded and stock corrected.        |
| 10 | Manage discount codes            | Authorized Staff | Campaign-ready promotions.                    |
| 11 | Compose Instagram draft          | Authorized Staff | Editable social post draft prepared.          |
| 12 | Register account                 | Customer         | New customer identity created.                |
| 13 | Browse/filter/compare variants   | Customer         | Product discovery and selection improved.     |
| 14 | AI recommendation chat           | Customer         | Personalized recommendations returned.        |
| 15 | Checkout and pay                 | Customer         | Purchase completed.                           |
| 16 | View stock by location           | Customer         | Informed shopping decision by location count. |
| 17 | Track shipment                   | Customer         | Real-time delivery visibility.                |
| 18 | Leave review with image          | Customer         | Post-purchase trust content created.          |

---

## 8. Security Matrix (RBAC)

`X` indicates capability is permitted by default role policy; final access is permission-driven.

| Function                          | Super Admin | Authorized Staff | Customer |
| --------------------------------- | ----------- | ---------------- | -------- |
| Sign up / Sign in                 | X           | X                | X        |
| Create custom role                | X           |                  |          |
| Assign permission to role         | X           |                  |          |
| Assign/revoke staff role          | X           |                  |          |
| View audit log                    | X           |                  |          |
| Create/update product and variant | X           | X                |          |
| Adjust inventory by location      | X           | X                |          |
| Create/approve inventory transfer | X           | X                |          |
| Process POS sale                  | X           | X                |          |
| Process refund                    | X           | X                |          |
| Manage discount codes             | X           | X                |          |
| Generate/edit Instagram draft     | X           | X                |          |
| Browse/filter catalog             |             |                  | X        |
| AI chat recommendations           |             |                  | X        |
| Add AI recommendation to cart     |             |                  | X        |
| Checkout and pay                  |             |                  | X        |
| View location-level stock         |             |                  | X        |
| Track shipment                    |             |                  | X        |
| Leave review with image           |             |                  | X        |

---

## 9. User Stories (Detailed and Specific)

### 9.1 RBAC and Governance Epic

| Story ID   | User Story                                                                                                               | Priority | Acceptance Criteria                                                                                                                                               |
| ---------- | ------------------------------------------------------------------------------------------------------------------------ | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| US-RBAC-01 | As a Super Admin, I want to create custom roles so that I can define single-function jobs in the system.                 | Must     | Role name is unique; role description required; role starts inactive until at least one permission is attached; action logged in `AuditLog`.                    |
| US-RBAC-02 | As a Super Admin, I want to assign atomic permissions to custom roles so that I can enforce least-privilege access.      | Must     | Permission list shows atomic scopes; duplicate mapping prevented; role-permission change effective within configured SLA; action logged with before/after values. |
| US-RBAC-03 | As a Super Admin, I want to assign a custom role to a staff account so that staff can perform designated functions only. | Must     | Assignment requires active staff account; permission changes reflected at next token refresh/session validation; assignment logged.                               |
| US-RBAC-04 | As a Super Admin, I want to revoke or modify a staff role so that access remains secure when responsibilities change.    | Must     | Revocation takes effect immediately for critical permissions; blocked attempts return authorization error; change reason is mandatory; action logged.             |
| US-RBAC-05 | As a Super Admin, I want to view immutable audit logs so that I can trace who did what and when.                         | Must     | Logs include actor, action, target, timestamp, IP/client metadata; filtering by date/user/action supported; export CSV available for compliance review.           |

### 9.2 Catalog and Inventory Epic

| Story ID  | User Story                                                                                                                                | Priority | Acceptance Criteria                                                                                                                                                                              |
| --------- | ----------------------------------------------------------------------------------------------------------------------------------------- | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------ |
| US-CAT-01 | As an Authorized Staff member, I want to add products and variants (SKU by size/color) so that items become sellable online and in-store. | Must     | Product must include title, category, base price, at least one image; each SKU has unique code and attributes; SKU visibility toggle supported.                                                  |
| US-CAT-02 | As an Authorized Staff member, I want product images stored in Instagram-compatible dimensions so that social export is seamless.         | Should   | System validates or auto-generates Instagram-safe renditions (default 4:5 portrait); original image preserved; preview available before save.                                                    |
| US-INV-01 | As an Authorized Staff member, I want to manually adjust SKU inventory by location so that digital stock matches physical counts.         | Must     | Adjustment requires reason code; stock cannot go negative without override permission; before/after quantities logged.                                                                           |
| US-INV-02 | As an Authorized Staff member, I want to transfer inventory between locations so that stock is balanced across stores and storage nodes.  | Must     | Transfer has source, destination, SKU, quantity, status (`draft`, `in_transit`, `completed`, `cancelled`); destination receives stock only on completion; transfer audit trail retained. |
| US-INV-03 | As a Customer, I want to see SKU stock counts by location so that I can decide where to shop.                                             | Must     | Product page displays per-location availability count; unavailable locations show `Out of stock`; counts update after stock-affecting events.                                                  |

### 9.3 Order, Fulfillment, POS, and Refund Epic

| Story ID  | User Story                                                                                                                           | Priority | Acceptance Criteria                                                                                                                              |
| --------- | ------------------------------------------------------------------------------------------------------------------------------------ | -------- | ------------------------------------------------------------------------------------------------------------------------------------------------ |
| US-ORD-01 | As an Authorized Staff member, I want to view pending online orders so that I can prepare fulfillment.                               | Must     | Filter by status/date/location; order detail includes SKU, qty, payment status, shipping address; pick/pack status updates supported.            |
| US-ORD-02 | As an Authorized Staff member, I want to mark an order as shipped with tracking number so that customers are notified.               | Must     | Tracking number format validated; status transition rules enforced; customer receives shipment notification.                                     |
| US-POS-01 | As an Authorized Staff member, I want to process in-store POS sales so that stock syncs centrally in real time.                      | Must     | POS transaction decrements location stock atomically; receipt generated; failure handling prevents partial commits.                              |
| US-REF-01 | As an Authorized Staff member, I want to process returns/refunds so that customer reimbursement and stock correction are consistent. | Must     | Refund supports full/partial line amounts; approved return increases designated location stock; financial record linked to original transaction. |

### 9.4 Promotion and Social Marketing Epic

| Story ID  | User Story                                                                                                                                   | Priority | Acceptance Criteria                                                                                                                                               |
| --------- | -------------------------------------------------------------------------------------------------------------------------------------------- | -------- | ----------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| US-MKT-01 | As an Authorized Staff member, I want to generate and manage discount codes so that promotional campaigns can run safely.                    | Must     | Code uniqueness enforced; supports validity window, usage cap, and minimum spend; redemption tracked.                                         |
| US-MKT-02 | As an Authorized Staff member, I want to connect Instagram account integrations so that social operations are centralized.                   | Should   | OAuth token lifecycle handled securely; reconnect flow supported; permission scope validation visible to staff.                                                   |
| US-MKT-03 | As an Authorized Staff member, I want compose-first Instagram draft generation from catalog items so that I can edit details before publish. | Must     | Draft includes editable caption, hashtags, tagged product references, and reorderable image list; user can remove/rearrange media; no auto-post without approval. |

### 9.5 Customer Experience Epic

| Story ID  | User Story                                                                                                            | Priority | Acceptance Criteria                                                                                                                      |
| --------- | --------------------------------------------------------------------------------------------------------------------- | -------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| US-CUS-01 | As a Customer, I want to register an account so that I can track orders and save shipping details.                    | Must     | Email/phone uniqueness enforced; verification flow configurable; customer profile includes default shipping addresses.                   |
| US-CUS-02 | As a Customer, I want to filter and compare clothing variants by size/color/price so that I can choose confidently.   | Must     | Multi-filter combination supported; variant availability shown inline; compare view supports at least 2 variants.                        |
| US-CUS-03 | As a Customer, I want AI chat recommendations with fit guidance so that I can discover suitable items quickly.        | Should   | AI response references currently available SKUs; recommendation includes reason/explanation; customer can open product detail from chat. |
| US-CUS-04 | As a Customer, I want to add AI-recommended products directly to cart so that purchase flow is faster.                | Must     | Add-to-cart checks stock before commit; unavailable SKU returns actionable message; cart updates without page reload.                    |
| US-CUS-05 | As a Customer, I want secure checkout and payment so that I can complete purchase confidently.                        | Must     | Supports Stripe, COD; payment status clearly reflected in order; failed payments do not create paid orders.                              |
| US-CUS-06 | As a Customer, I want to track shipment status so that I know delivery progress.                                      | Must     | Timeline includes at least `confirmed`, `packed`, `shipped`, `in_transit`, `delivered`; tracking link shown when available.    |
| US-CUS-07 | As a Customer, I want to leave photo reviews for purchased items so that others can assess real-life fit and quality. | Should   | Only verified purchasers can review; photo upload supports moderation workflow; ratings affect product aggregate score.                  |

### 9.6 Omnichannel Operations Control Epic

| Story ID   | User Story                                                                                                                       | Priority | Acceptance Criteria                                                                                                                                      |
| ---------- | -------------------------------------------------------------------------------------------------------------------------------- | -------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| US-OPS-01 | As an Authorized Staff member, I want to create purchase orders and receive stock so that inbound inventory is controlled.       | Must     | PO includes supplier/location/SKU lines; partial receiving supported; inventory increases only after posted goods receipt; full audit trail retained.   |
| US-OPS-02 | As an Authorized Staff member, I want order lines allocated to fulfillment locations so that SLA and stock constraints are met.  | Must     | Allocation policy considers stock and SLA; split shipments supported; reservation applied to allocated stock; allocation event is auditable.            |
| US-OPS-03 | As an Authorized Staff member, I want return intake and exchange workflows so that reverse logistics are operationally complete. | Must     | Return eligibility check required; inspection disposition (`RESTOCK`/`DISPOSE`) recorded; exchange links to original line; refund/exchange is auditable. |
| US-OPS-04 | As an Authorized Staff member, I want cycle counts and variance approvals so that inventory accuracy remains reliable.            | Must     | Count scope snapshot is immutable; threshold variances require approval; reconciliation report includes reason codes and references.                     |
| US-OPS-05 | As an Authorized Staff member, I want POS shift close and cash reconciliation so that store controls are enforceable.             | Must     | Shift cannot close with pending transactions; discrepancy workflow captures reason/approver; end-of-shift report includes payment method totals.         |
| US-OPS-06 | As an Authorized Staff member, I want tax invoice and adjustment-note issuance so that financial compliance is maintained.        | Must     | Invoices include required tax fields; adjustment note links to original invoice; issued financial documents remain immutable and fully traceable.         |

---

## 10. Functional Requirements

### 10.1 Identity and Access

- FR-01: System shall support customer and staff authentication.
- FR-02: System shall allow Super Admin to create custom roles.
- FR-03: System shall allow Super Admin to assign atomic permissions to roles.
- FR-04: System shall allow role assignment/revocation for staff accounts.
- FR-05: System shall enforce permission checks at API and UI levels.

### 10.2 Catalog and Inventory

- FR-06: System shall manage products and SKU variants.
- FR-07: System shall support location-level inventory quantities by SKU.
- FR-08: System shall support inventory adjustments with reason codes.
- FR-09: System shall support inventory transfer workflow across locations.
- FR-10: System shall expose location-specific availability to customers.

### 10.3 Orders, POS, and Refunds

- FR-11: System shall process online orders and shipping lifecycle.
- FR-12: System shall process in-store POS transactions.
- FR-13: System shall support full and partial refunds.
- FR-14: Stock synchronization shall run for online and in-store transactions.

### 10.4 Promotion, Social, and AI

- FR-15: System shall manage discount code lifecycle and constraints.
- FR-16: System shall generate Instagram compose drafts from catalog data.
- FR-17: System shall allow image reorder/edit before social publication.
- FR-18: System shall provide AI recommendation chat integrated with current stock.

### 10.5 Auditability

- FR-19: System shall keep immutable audit logs for critical actions.
- FR-20: System shall provide searchable and exportable audit views.

### 10.6 Omnichannel Operational Controls

- FR-21: System shall support purchase order creation, approval, and inbound stock receiving with goods receipt posting.
- FR-22: System shall support fulfillment location allocation policies with optional split-shipment orchestration and stock reservation.
- FR-23: System shall support return intake, inspection disposition, exchange linkage, and reverse-logistics traceability.
- FR-24: System shall support cycle count batching, variance threshold approval, and reconciliation reporting.
- FR-25: System shall support POS shift close workflows with cash reconciliation and discrepancy approval controls.
- FR-26: System shall support tax invoice issuance and adjustment-note workflows with immutable financial document references.

---

## 11. Non-Functional Requirements

- NFR-01 (Performance): 95th percentile product search/filter response <= 2 seconds under normal load.
- NFR-02 (Consistency): Stock-affecting writes must be atomic per transaction.
- NFR-03 (Reliability): Core commerce modules target 99.9% monthly availability.
- NFR-04 (Security): RBAC enforced with least privilege; sensitive data encrypted at rest and in transit.
- NFR-05 (Auditability): Audit records are append-only and tamper-evident.
- NFR-06 (Scalability): Location and SKU model must support adding new stores/storage nodes without schema redesign.
- NFR-07 (Usability): Back-office workflows should be executable by trained staff without engineering support.

---

## 12. Assumptions and Dependencies

- Stripe is available and approved for payment integration.
- Instagram integration permissions are obtainable for compose/review workflow.
- Shipping provider integration will provide tracking updates.
- Organization provides product content moderation policy for user-generated review images.
- Supplier master data and approval authority model are available for procurement workflows.
- Tax rule configuration for the operating jurisdiction is available for invoice generation.

---

## 13. Out of Scope

- Automated direct social publishing without manual review gate.
- Multi-currency expansion in v1.
- Advanced loyalty system.

---

## 14. Change Requirements

| # | Item Name               | Change Description                                                      |
| - | ----------------------- | ----------------------------------------------------------------------- |
| 1 | BRD Scope Lock          | Lock v1 scope to shipping-only omnichannel commerce and RBAC baseline.  |
| 2 | Role Permission Catalog | Finalize atomic permission taxonomy with engineering and security.      |
| 3 | Inventory Transfer SOP  | Define operational SOP for transfer approval and reconciliation.        |
| 4 | Instagram Asset Rules   | Finalize image aspect presets and metadata standards for compose flow.  |
| 5 | Audit Retention Policy  | Define legal/compliance retention period and export process.            |
| 6 | Refund Policy Mapping   | Map business refund policy to system statuses and approvals.            |
| 7 | Notification Templates  | Approve customer notification templates for shipping and refund events. |

---

## 15. Appendix

### 15.1 Glossary

| Term          | Description                                       |
| ------------- | ------------------------------------------------- |
| BRD           | Business Requirements Document                    |
| Omnichannel   | Unified online and offline commerce operations    |
| RBAC          | Role-Based Access Control                         |
| SKU           | Stock Keeping Unit (variant-level sellable unit)  |
| POS           | Point of Sale                                     |
| Location Node | Any inventory-holding location (store or storage) |

### 15.2 Finalized Policy Decisions

1. Staff role model: staff accounts can hold multiple roles concurrently; effective permissions are the union of assigned role permissions.
2. Stock reservation: v1 includes checkout stock reservation timeout of 15 minutes for online cart-to-checkout flow.
3. Discount stacking: v1 is single-code only per order; stacking is not permitted.
4. Language scope: v1 supports bilingual customer-facing and back-office text (`Vietnamese`, `English`).
5. Location visibility: customer-facing UI does not expose internal location type labels (`STORE`/`STORAGE`); it only shows location display name and availability count.

### 15.3 Deferred Enhancements

1. Role conflict policy engine (for explicit deny precedence and separation-of-duties rules) is deferred beyond v1.
2. Configurable reservation timeout per campaign or SKU is deferred beyond v1.
3. Multi-code discount orchestration is deferred beyond v1.

### 15.4 SRS Coverage Status

Reference SRS: `docs/10.SRS/SESHOP SRS.md`

| Coverage Item | BRD Source | SRS Status |
| --- | --- | --- |
| Must stories in RBAC, Catalog, Inventory, POS, Refund, Checkout, Tracking, Review | Section 9 User Stories | Covered |
| Staff fulfillment operations (`US-ORD-01`, `US-ORD-02`) | `US-ORD-01`, `US-ORD-02` | Covered via `UC19`, `UC20` |
| Instagram account integration (`US-MKT-02`) | `US-MKT-02` | Covered via `UC21` |
| Story-to-Use Case traceability | Section 9 + Section 10 | Covered via `Requirements Traceability Matrix` |
| Bilingual policy (`vi`, `en`) | Section 15.2 Policy Decision #4 | Covered in SRS localization policy |
| Stock reservation timeout (15 minutes) | Section 15.2 Policy Decision #2 | Covered in checkout business rule |
| Omnichannel operations controls (`US-OPS-01` ... `US-OPS-06`) | Section 9.6 + Section 10.6 | Covered via `UC22` ... `UC27` |

**Status Summary**

- BRD-to-SRS functional coverage is aligned for current v1 scope.
- Future changes in BRD stories or policies must be mirrored in SRS use cases and traceability mapping in the same change cycle.
