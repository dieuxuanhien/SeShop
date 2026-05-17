Here’s a practical repair plan, ordered by dependency and risk. The big idea: fix the cross-cutting foundations first, then complete use cases in clusters instead of chasing UC1-UC27 one by one.

**Phase 0: Stabilize Baseline** - DONE
1. [x] Fix failing backend tests first.
   - Permission catalog seed drift.
   - Stale service mocks.
   - Registration null ID test issue.
   - Stripe webhook `WebMvcTest` missing `OrderRepository`.
2. [x] Add a lightweight “SRS conformance checklist” test/doc file so each UC has:
   - Required permissions.
   - Required audit events.
   - DB tables touched.
   - Happy path.
   - Main rejection paths.
   - Added `docs/SRS_CONFORMANCE_CHECKLIST.md`.
3. [x] Before editing any symbol, follow repo rule:
   - Run `gitnexus_impact(..., direction: "upstream")`.
   - Warn before HIGH/CRITICAL changes.
   - Run `gitnexus_detect_changes()` before commit.

**Phase 1: Backend Security And Audit Foundation**
Target UCs: UC1-UC4, plus all staff/admin workflows.

1. Add consistent permission enforcement.
   - Introduce method-level checks or a shared authorization helper.
   - Apply to role, staff, product, stock, order, refund, POS, invoice, Instagram, PO, cycle count, and shift controllers.
   - Progress: POS receipt endpoints now require `pos.sell`, POS shift endpoints require `pos.shift.manage`, and invoice endpoints require `invoice.manage`.
   - Progress: `SeedPermissionCatalogTest`, `ApiControllerContractTest`, and `InvoiceControllerContractTest` cover the new POS/invoice permission gates.
2. Standardize permission codes.
   - Make seed catalog, frontend route guards, and backend checks use the same permission names.
   - Progress: permission seed and demo STORE_MANAGER role include `pos.sell`, `pos.shift.manage`, and `invoice.manage`.
3. Expand audit logging.
   - Add audit events for before/after changes on sensitive workflows.
   - Cover role changes, staff role changes, stock movement, order status, refund/return, POS, invoice, Instagram connection, PO/receiving, cycle count, and shift close.
   - Progress: UC1-UC3 role create, role permission assignment, staff role assignment, and staff role revocation now emit structured audit metadata.
   - Progress: role permission assignment now activates an inactive role, and staff role assignment rejects inactive users or inactive roles.
   - Progress: POS sale, POS shift close, tax invoice issue, and invoice adjustment now emit structured audit metadata.
   - Progress: return request, return approval, and refund creation now emit structured audit metadata.
4. Add tests proving unauthorized users get rejected.
   - Progress: `RoleServiceTest` covers UC1-UC3 audit events and inactive user/role rejection.
   - Progress: `ReceiptServiceTest`, `ShiftServiceTest`, and `InvoiceServiceTest` cover POS/invoice audit events.
   - Progress: `RefundServiceTest` covers return/refund audit events.

Deliverable: backend RBAC and audit are reliable enough for every later UC.

**Phase 2: Inventory Truth Model**
Target UCs: UC5-UC8, UC16, UC22, UC23, UC25.

1. Normalize stock behavior.
   - Define available, reserved, allocated, damaged, and on-hand quantities.
   - Add/complete reservation and allocation persistence.
2. Fix SKU inventory adjustment.
   - Require reason code.
   - Require override permission for exceptional changes.
   - Audit before/after quantity.
   - Progress: inventory adjustments now require `reasonCode`, reject negative available stock unless the caller has `inventory.adjust.override`, and write before/after audit metadata.
3. Complete transfer workflow.
   - Add cancel.
   - Persist status trail.
   - Make receive/approve transactional.
   - Progress: transfer create, approve, receive, and cancel now emit status-transition audit metadata; cancel restores source stock for in-transit transfers.
   - Progress: transfer approval checks available stock before mutation, and receiving rejects duplicate, missing, unknown, or over-received item quantities.
4. Implement allocation properly.
   - Replace `allocateOrder` status-only behavior.
   - Add allocation records, split location support, reservation handling, and pick task model if required by SRS.
   - Progress: `allocateOrder` now creates persistent `order_allocations`, splits allocation across locked inventory balances, and increases `reserved_qty` so later POS/transfer flows cannot consume allocated stock.
   - Progress: staff shipment now fulfills active allocations by decrementing both `on_hand_qty` and `reserved_qty`; order cancellation releases active allocation reservations.
5. Improve stock views.
   - Frontend should show selected-variant/location availability, not only product totals.
   - Progress: customer stock availability now carries the product-detail selected variant, lets the shopper switch SKUs, and queries location availability per variant.

Deliverable: stock cannot be oversold or silently mutated.

**Phase 3: Order, Checkout, Discounts, Payment**
Target UCs: UC10, UC15, UC17, UC19, UC20.

1. Fix checkout stock flow.
   - Validate stock.
   - Reserve stock before payment.
   - Release reservation on payment failure/timeout.
   - Decrement/commit stock only after confirmed order/payment rules.
   - Progress: checkout now reserves available stock through order allocations before COD/Stripe payment state is created, and Stripe payment failure releases active reservations.
2. Apply discounts to order totals.
   - Persist redemption.
   - Enforce usage limits, expiry, eligibility.
   - Audit admin discount changes.
   - Progress: checkout now redeems the submitted discount code against the saved order, persists `discount_redemptions`, applies the discount to order/payment totals, and rejects invalid or already-redeemed discounts before stock reservation/payment.
   - Progress: discount create, update, and deactivate now write structured audit events with before/after metadata for admin changes.
3. Strengthen payment state.
   - Add payment status to staff order DTOs.
   - Make Stripe webhook path robust and tested.
   - Define COD state transitions clearly.
   - Progress: orders now map persisted payment/shipment status and currency into staff/customer order DTOs, and the staff orders table displays live payment state instead of a placeholder.
   - Progress: Stripe webhook success/failure handling now uses `PAID`/`FAILED` payment states, delegates order payment transitions to `OrderService`, and ignores late failure events for already-paid orders.
   - Progress: COD orders remain unpaid at checkout and their pending COD payment is marked `PAID` when staff delivers the order.
4. Fix shipment workflow.
   - Validate order state before shipping.
   - Validate tracking format.
   - Add notification/event hook.
   - Audit shipment creation/status change.
   - Progress: staff shipment now rejects unshippable order/payment states before stock fulfillment, validates carrier/tracking input, normalizes carrier statuses, and audits shipment creation/status transitions.
5. Replace static tracking timeline with persisted or partner-derived shipment events.
   - Progress: customer tracking now receives status-derived shipment events from the backend instead of rendering a fixed static timeline.

Deliverable: online order lifecycle is coherent from cart to shipment.

**Phase 4: Returns, Refunds, POS, Invoices**
Target UCs: UC8, UC9, UC24, UC26, UC27.

1. Replace in-memory refund/return/invoice services with database-backed persistence.
   - Use documented tables like `tax_invoices`.
   - Add missing repositories/entities where needed.
   - Progress: online return requests, return items, refunds, tax invoices, and invoice adjustment notes now use JPA persistence backed by the existing schema instead of in-memory maps.
2. Implement refund eligibility.
   - Online order ownership/status checks.
   - Delivered/paid constraints.
   - Amount validation.
   - Stock disposition rules.
   - Progress: online returns now require delivered/paid orders and valid order item quantities; refund processing now requires an approved return, a completed payment on the same order, one refund per return request, and an amount not exceeding returned item value.
3. Implement POS return validation.
   - Validate original receipt.
   - Validate item quantities.
   - Update stock according to disposition.
   - Progress: POS returns now validate the original receipt, reject duplicate/over-returned variants, require refund amount to match returned line value, persist return items with disposition, and restock the original POS location only for `RESTOCK` dispositions.
4. Build return intake/exchange model.
   - Eligibility.
   - Inspection.
   - Disposition.
   - Exchange linkage.
   - Reverse logistics tracking.
   - Progress: POS returns now evaluate eligibility, validate requested quantities (inspection), and support RESTOCK, DISPOSE, and REFURBISH disposition states in `ReturnService`.
5. Fix shift close.
   - Real approver, not cashier auto-approval.
   - Enforce discrepancy reason.
   - Threshold approval workflow/report.
   - Progress: `ShiftService` now rejects cashier self-approval for shift close and enforces a discrepancy reason when cash variance exceeds the designated threshold.
6. Implement immutable invoice records.
   - Tax validation.
   - Correction/adjustment note chain.
   - No mutation of finalized financial records.
   - Progress: tax invoice issuance now snapshots order tax/subtotal/total data, rejects duplicate order invoices, and stores corrections as immutable adjustment notes with cumulative adjustment audit metadata.

Deliverable: financial and return workflows stop being skeletons.

**Phase 5: Product, Catalog, Reviews, Instagram, AI**
Target UCs: UC5, UC11-UC14, UC18, UC21.

1. Complete product/SKU creation.
   - Required category, base price, images.
   - Validate SKU attributes.
   - Audit product changes.
   - Progress: CatalogService creates variants and ensures SKU uniqueness, emitting audit metadata.
2. Fix browse/filter/compare.
   - Backend filters: category, size, color, price, brand.
   - Frontend compare view.
   - Progress: PublicCatalogController filters by category, size, color, and price.
3. Improve AI recommendation.
   - Stock-aware recommendations.
   - Return variant/product IDs usable by frontend.
   - Add-to-cart from recommendation.
   - Progress: AiAssistantService checks available stock by summing InventoryBalance quantities.
4. Implement review rules.
   - Delivered-order check.
   - Review window.
   - Image upload.
   - Moderation state.
   - Aggregate score update.
   - Progress: ReviewService enforces ownership, delivered status, review window, and moderation state; provides staff approve/reject endpoints.
5. Harden Instagram.
   - Secure OAuth state.
   - Validate scopes.
   - Encrypt tokens.
   - Generate product-derived drafts/media renditions.
   - Audit connect/disconnect/post workflows.
   - Progress: Instagram `completeConnection` now emits `INSTAGRAM_CONNECTION_CHANGED` audit metadata; `publishDraft` now emits `INSTAGRAM_POST_PUBLISHED` audit metadata with draftId, productId, createdBy, mediaId, and permalink.
   - Progress: InstagramService now uses HMAC to verify OAuth state and Spring Security TextEncryptor to encrypt/decrypt access tokens in the database.
   - Progress: MetaGraphClient now verifies required scopes from /me/permissions and InstagramService automatically populates draft captions, hashtags, and media order from the associated product if left empty.

Deliverable: customer-facing catalog and social workflows match the documented rules.

**Phase 6: Frontend Completeness And Localization**
Target all UCs with UI gaps.

1. Add i18n foundation.
   - Pick library.
   - Add locale catalogs.
   - Remove hardcoded user-facing strings from critical flows.
   - Progress: Installed `react-i18next` and `i18next`. Created `frontend/src/i18n.ts` configuration and established English (`en`) and Vietnamese (`vi`) locale catalogs.
2. Align route guards with backend permissions.
   - Progress: Updated `frontend/src/app/router/routes.tsx` to use specific backend permissions (e.g. `catalog.write`, `inventory.adjust`) in `ProtectedRoute`.
3. Add missing filters/actions in staff pages.
   - Audit filters and CSV export.
     - Progress: Added CSV file download for Audit Logs.
   - Online order filters.
     - Progress: Added order status and payment status filters to `OrdersManagement.tsx`.
   - Stock-by-location variant selector.
     - Progress: Added location and SKU filters to `InventoryAdjustment.tsx`.
4. Add frontend tests for acceptance paths, especially:
    - [x] Unauthorized access.
    - [x] Stock unavailable checkout.
    - [x] Discount applied.
    - [x] Refund/return lifecycle.
    - [x] Invoice creation/adjustment. (Note: Invoice management UI is not yet implemented in the frontend; backend tested only).

Deliverable: UI behavior stops drifting from API/business rules.

**Phase 7: Docs Reconciliation**
1. Update SRS/BRD/SAD/ADD after implementation, not before.
2. Resolve BRD/SRS mismatch:
   - Either BRD has 27 primary use cases, or SRS marks some as derived/secondary.
3. Remove or rewrite false architecture claims:
   - Hexagonal isolation.
   - Full audit coverage.
   - Encrypted tokens.
   - Localization.
   - Module isolation.
4. Make requirements verifiable:
   - Replace TBDs.
   - Define “real-time.”
   - Add measurable acceptance criteria.

**Suggested Order Of Work**
1. Test baseline.
2. RBAC + audit.
3. Inventory reservation/allocation.
4. Checkout/order/payment.
5. Refunds/returns/invoices.
6. Product/catalog/reviews/Instagram/AI.
7. Localization/frontend polish.
8. Documentation truth pass.

I’d treat Phases 1-4 as the critical path. Until permission enforcement, audit, inventory reservation, and persistence are fixed, most individual use cases will keep looking “implemented” while still failing the SRS rules.
