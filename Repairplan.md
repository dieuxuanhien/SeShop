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
3. Complete transfer workflow.
   - Add cancel.
   - Persist status trail.
   - Make receive/approve transactional.
4. Implement allocation properly.
   - Replace `allocateOrder` status-only behavior.
   - Add allocation records, split location support, reservation handling, and pick task model if required by SRS.
5. Improve stock views.
   - Frontend should show selected-variant/location availability, not only product totals.

Deliverable: stock cannot be oversold or silently mutated.

**Phase 3: Order, Checkout, Discounts, Payment**
Target UCs: UC10, UC15, UC17, UC19, UC20.

1. Fix checkout stock flow.
   - Validate stock.
   - Reserve stock before payment.
   - Release reservation on payment failure/timeout.
   - Decrement/commit stock only after confirmed order/payment rules.
2. Apply discounts to order totals.
   - Persist redemption.
   - Enforce usage limits, expiry, eligibility.
   - Audit admin discount changes.
3. Strengthen payment state.
   - Add payment status to staff order DTOs.
   - Make Stripe webhook path robust and tested.
   - Define COD state transitions clearly.
4. Fix shipment workflow.
   - Validate order state before shipping.
   - Validate tracking format.
   - Add notification/event hook.
   - Audit shipment creation/status change.
5. Replace static tracking timeline with persisted or partner-derived shipment events.

Deliverable: online order lifecycle is coherent from cart to shipment.

**Phase 4: Returns, Refunds, POS, Invoices**
Target UCs: UC8, UC9, UC24, UC26, UC27.

1. Replace in-memory refund/return/invoice services with database-backed persistence.
   - Use documented tables like `tax_invoices`.
   - Add missing repositories/entities where needed.
2. Implement refund eligibility.
   - Online order ownership/status checks.
   - Delivered/paid constraints.
   - Amount validation.
   - Stock disposition rules.
3. Implement POS return validation.
   - Validate original receipt.
   - Validate item quantities.
   - Update stock according to disposition.
4. Build return intake/exchange model.
   - Eligibility.
   - Inspection.
   - Disposition.
   - Exchange linkage.
   - Reverse logistics tracking.
5. Fix shift close.
   - Real approver, not cashier auto-approval.
   - Enforce discrepancy reason.
   - Threshold approval workflow/report.
6. Implement immutable invoice records.
   - Tax validation.
   - Correction/adjustment note chain.
   - No mutation of finalized financial records.

Deliverable: financial and return workflows stop being skeletons.

**Phase 5: Product, Catalog, Reviews, Instagram, AI**
Target UCs: UC5, UC11-UC14, UC18, UC21.

1. Complete product/SKU creation.
   - Required category, base price, images.
   - Validate SKU attributes.
   - Audit product changes.
2. Fix browse/filter/compare.
   - Backend filters: category, size, color, price, brand.
   - Frontend compare view.
3. Improve AI recommendation.
   - Stock-aware recommendations.
   - Return variant/product IDs usable by frontend.
   - Add-to-cart from recommendation.
4. Implement review rules.
   - Delivered-order check.
   - Review window.
   - Image upload.
   - Moderation state.
   - Aggregate score update.
5. Harden Instagram.
   - Secure OAuth state.
   - Validate scopes.
   - Encrypt tokens.
   - Generate product-derived drafts/media renditions.
   - Audit connect/disconnect/post workflows.

Deliverable: customer-facing catalog and social workflows match the documented rules.

**Phase 6: Frontend Completeness And Localization**
Target all UCs with UI gaps.

1. Add i18n foundation.
   - Pick library.
   - Add locale catalogs.
   - Remove hardcoded user-facing strings from critical flows.
2. Align route guards with backend permissions.
3. Add missing filters/actions in staff pages.
   - Audit filters and CSV export.
   - Online order filters.
   - Stock-by-location variant selector.
4. Add frontend tests for acceptance paths, especially:
   - Unauthorized access.
   - Stock unavailable checkout.
   - Discount applied.
   - Refund/return lifecycle.
   - Invoice creation/adjustment.

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
