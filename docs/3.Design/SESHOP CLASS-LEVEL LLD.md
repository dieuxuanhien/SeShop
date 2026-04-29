# SE SHOP CLASS-LEVEL DESIGN

**Project:** SeShop  
**Domain:** Omnichannel clothing & accessories platform  
**Scope:** Class-level LLD for backend implementation, aligned with BRD, SRS, database schema, diagrams, HLD, LLD, and API spec  
**Backend:** Java + Spring Boot  
**Last updated:** 2026-04-29

---

## Revision History

| Date | Version | Author | Description |
|---|---:|---|---|
| 2026-04-29 | 1.0 | GitHub Copilot | Class-level design for SeShop backend modules |

---

## Table of Contents

1. [Purpose](#purpose)
2. [Design Consistency Rules](#design-consistency-rules)
3. [Common Class Patterns](#common-class-patterns)
4. [Identity and RBAC Classes](#identity-and-rbac-classes)
5. [Catalog Classes](#catalog-classes)
6. [Inventory Classes](#inventory-classes)
7. [Commerce Classes](#commerce-classes)
8. [POS and Returns Classes](#pos-and-returns-classes)
9. [Marketing and Social Classes](#marketing-and-social-classes)
10. [Customer Engagement Classes](#customer-engagement-classes)
11. [Shared Platform Service Classes](#shared-platform-service-classes)
12. [Exception Model](#exception-model)
13. [DTO Model](#dto-model)
14. [Repository Model](#repository-model)
15. [Service Interaction Model](#service-interaction-model)
16. [Implementation Order Recommendation](#implementation-order-recommendation)

---

## Purpose

This document gives class-level design guidance for implementing SeShop in Java. It is the next layer after HLD, LLD, and API specification. The purpose is to define the main entities, services, controllers, repositories, mappers, and domain methods required to implement the approved SRS use cases.

This design deliberately avoids introducing microservices. It assumes a modular monolith with clean internal boundaries.

---

## Design Consistency Rules

### Confirmed repository constraints
- Use `user_roles` rather than `staff_role_assignments`.
- Do not persist `available_qty`; compute it from `on_hand_qty - reserved_qty`.
- Do not persist `variance_qty`; calculate variance in memory or as a derived report value.
- Keep transfer states consistent with `DRAFT`, `IN_TRANSIT`, `COMPLETED`, `CANCELLED`.
- Keep Instagram draft flow manual-review based.
- Keep POS payment methods limited to cash and Stripe/card for v1.
- Keep shipping-only fulfillment in v1.
- Keep procurement classes because purchase order and goods receipt are in schema and SRS.

### General class rules
- Controllers must be thin.
- Application services orchestrate transactions.
- Domain entities enforce invariants where practical.
- Repositories hide persistence details.
- External integrations must be wrapped in adapters.

---

## Common Class Patterns

### Base architecture pattern
Each bounded context should follow this shape:
- `XxxController`
- `XxxApplicationService`
- `XxxDomainService`
- `XxxEntity` / `XxxAggregate`
- `XxxRepository`
- `XxxMapper`
- `XxxDto`
- `XxxCommand` / `XxxQuery`
- `XxxException`

### Cross-cutting abstract types

#### `BaseEntity`
Common fields:
- `id`
- `createdAt`
- `updatedAt`

#### `AuditableAction`
Marker interface for actions requiring audit logging.

#### `DomainEvent`
Base type for internal events such as stock changes and order transitions.

#### `CommandHandler<TCommand, TResult>`
Command handler contract for use case execution.

#### `QueryHandler<TQuery, TResult>`
Query handler contract for read operations.

---

## Identity and RBAC Classes

### 1. `User`
**Responsibility:** represent an authenticated account holder.

**Key fields**
- `id`
- `username`
- `email`
- `phoneNumber`
- `passwordHash`
- `userType`
- `status`

**Important methods**
- `activate()`
- `lock()`
- `deactivate()`
- `changePassword(newHash)`

### 2. `Role`
**Responsibility:** group permissions for a staff capability set.

**Key methods**
- `activate()`
- `deactivate()`
- `addPermission(permission)`
- `removePermission(permission)`

### 3. `Permission`
**Responsibility:** atomic authorization capability.

### 4. `UserRoleAssignment`
**Responsibility:** assignment lifecycle for user-role mapping.

**Key methods**
- `revoke()`
- `isActive()`

### 5. `RolePermission`
**Responsibility:** mapping between roles and permissions.

### 6. `AuditLog`
**Responsibility:** append-only audit record.

**Key fields**
- `actorUserId`
- `action`
- `targetType`
- `targetId`
- `metadataJson`

### 7. `RoleApplicationService`
**Methods**
- `createRole(CreateRoleCommand)`
- `assignPermissions(AssignPermissionsCommand)`
- `assignRoleToUser(AssignRoleCommand)`
- `revokeRoleFromUser(RevokeRoleCommand)`
- `listRoles(RoleSearchQuery)`
- `listAuditLogs(AuditSearchQuery)`

### 8. `AuthorizationService`
**Responsibility:** evaluate permission checks.

**Methods**
- `hasPermission(userId, permissionCode)`
- `hasAnyPermission(userId, permissionCodes)`
- `getEffectivePermissions(userId)`

### 9. RBAC invariants
- Role name unique.
- Permission code unique.
- Only active roles may be assigned.
- Audit entry required for every mutation.

---

## Catalog Classes

### 1. `Category`
**Responsibility:** merchandising taxonomy node.

### 2. `Product`
**Responsibility:** parent product master record.

**Key fields**
- `name`
- `brand`
- `description`
- `status`

**Methods**
- `publish()`
- `archive()`
- `draft()`
- `rename(name)`

### 3. `ProductVariant`
**Responsibility:** SKU-level sellable unit.

**Key fields**
- `skuCode`
- `size`
- `color`
- `price`
- `status`

**Methods**
- `activate()`
- `deactivate()`
- `changePrice(price)`

### 4. `ProductImage`
**Responsibility:** product or variant media metadata.

**Key fields**
- `url`
- `sortOrder`
- `isInstagramReady`

### 5. `CatalogApplicationService`
**Methods**
- `createProduct(CreateProductCommand)`
- `updateProduct(UpdateProductCommand)`
- `addVariants(AddVariantsCommand)`
- `attachImages(AttachImagesCommand)`
- `listProducts(ProductSearchQuery)`
- `getProductDetail(productId)`
- `publishProduct(productId)`

### 6. `ProductPolicy`
**Responsibility:** enforce business rules around publishability and SKU uniqueness.

### 7. Catalog invariants
- SKU code unique across system.
- Product should not be published with no active variants.
- Media reference must be preserved for social draft use.

---

## Inventory Classes

### 1. `Location`
**Responsibility:** inventory node for store or storage.

**Key fields**
- `code`
- `displayName`
- `locationType`
- `status`

**Methods**
- `activate()`
- `deactivate()`

### 2. `InventoryBalance`
**Responsibility:** stock position by SKU and location.

**Key fields**
- `variantId`
- `locationId`
- `onHandQty`
- `reservedQty`

**Derived behavior**
- `availableToSell()` returns `onHandQty - reservedQty`

**Methods**
- `increaseOnHand(qty)`
- `decreaseOnHand(qty)`
- `reserve(qty)`
- `releaseReserved(qty)`
- `availableToSell()`

### 3. `InventoryTransfer`
**Responsibility:** header aggregate for movement between locations.

**Methods**
- `submit()`
- `approve()`
- `markInTransit()`
- `receive()`
- `complete()`
- `cancel()`

### 4. `InventoryTransferItem`
**Responsibility:** line quantity per SKU in a transfer.

**Methods**
- `markReceived(receivedQty, damagedQty)`

### 5. `CycleCount`
**Responsibility:** stock counting session.

**Methods**
- `open()`
- `review()`
- `approve()`
- `close()`

### 6. `CycleCountItem`
**Responsibility:** count line per SKU.

**Methods**
- `recordCount(countedQty, reasonCode)`
- `variance()`

### 7. `Supplier`
**Responsibility:** procurement partner.

### 8. `PurchaseOrder`
**Responsibility:** inbound procurement header.

**Methods**
- `submit()`
- `approve()`
- `reject()`
- `receive()`

### 9. `PurchaseOrderItem`
**Responsibility:** purchase line quantity and cost.

### 10. `GoodsReceipt`
**Responsibility:** receipt document against purchase order.

**Methods**
- `post()`
- `voidReceipt()`

### 11. `GoodsReceiptItem`
**Responsibility:** received quantity and damaged quantity.

### 12. `InventoryApplicationService`
**Methods**
- `adjustInventory(AdjustInventoryCommand)`
- `createTransfer(CreateTransferCommand)`
- `approveTransfer(ApproveTransferCommand)`
- `receiveTransfer(ReceiveTransferCommand)`
- `createCycleCount(CreateCycleCountCommand)`
- `postCycleCount(PostCycleCountCommand)`
- `createPurchaseOrder(CreatePurchaseOrderCommand)`
- `postGoodsReceipt(PostGoodsReceiptCommand)`
- `getAvailability(GetAvailabilityQuery)`

### 13. `InventoryPolicy`
**Responsibility:** validate negative stock, transfer availability, and reconciliation rules.

### 14. Inventory invariants
- Never persist available quantity.
- Source quantity must be sufficient before transfer.
- Only completed transfer updates destination stock.
- Cycle count snapshot must be immutable.

---

## Commerce Classes

### 1. `Cart`
**Responsibility:** customer shopping cart.

**Methods**
- `addItem(variantId, qty)`
- `updateItem(itemId, qty)`
- `removeItem(itemId)`
- `clear()`

### 2. `CartItem`
**Responsibility:** item line inside cart.

### 3. `Order`
**Responsibility:** canonical order aggregate.

**Key fields**
- `orderNumber`
- `status`
- `paymentStatus`
- `shipmentStatus`
- `totalAmount`
- `currency`

**Methods**
- `allocate()`
- `startPicking()`
- `markPacked()`
- `markShipped()`
- `markDelivered()`
- `complete()`
- `cancel()`
- `markReturned()`

### 4. `OrderItem`
**Responsibility:** order line with variant, qty, price, and discount.

### 5. `OrderAllocation`
**Responsibility:** assignment of order items to a fulfillment location.

### 6. `Shipment`
**Responsibility:** tracking and delivery state.

**Methods**
- `setTracking(carrier, trackingNumber)`
- `markPacked()`
- `markShipped()`
- `markInTransit()`
- `markDelivered()`

### 7. `Payment`
**Responsibility:** payment transaction record.

**Methods**
- `markPending()`
- `markPaid()`
- `markFailed()`
- `markRefunded()`

### 8. `DiscountCode`
**Responsibility:** promotion rule.

**Methods**
- `activate()`
- `deactivate()`
- `validateFor(orderSubtotal)`
- `consume()`

### 9. `DiscountRedemption`
**Responsibility:** applied discount history.

### 10. `CartApplicationService`
**Methods**
- `getMyCart(customerId)`
- `addItem(AddCartItemCommand)`
- `updateItem(UpdateCartItemCommand)`
- `removeItem(RemoveCartItemCommand)`
- `validateCart(cartId)`

### 11. `CheckoutApplicationService`
**Methods**
- `checkout(CheckoutCommand)`
- `validateDiscount(ValidateDiscountCommand)`
- `calculateTotals(cartId)`
- `reserveStock(cartId)`
- `createOrderAndPayment(CheckoutCommand)`

### 12. `OrderApplicationService`
**Methods**
- `listCustomerOrders(customerId)`
- `getOrderDetail(orderId)`
- `allocateOrder(AllocateOrderCommand)`
- `packOrder(PackOrderCommand)`
- `shipOrder(ShipOrderCommand)`
- `cancelOrder(CancelOrderCommand)`
- `trackShipment(orderId)`

### 13. Commerce invariants
- Checkout must be idempotent.
- Discount redemption must be unique per order.
- Payment state must align with order state.
- Stock reservation timeout must be enforced.

---

## POS and Returns Classes

### 1. `POSShift`
**Responsibility:** cashier shift lifecycle.

**Methods**
- `open()`
- `close()`
- `markDiscrepancyReview()`

### 2. `POSReceipt`
**Responsibility:** receipt header.

**Methods**
- `finalizeReceipt()`
- `voidReceipt()`

### 3. `POSReceiptItem`
**Responsibility:** sold line item.

### 4. `CashReconciliation`
**Responsibility:** expected vs actual cash comparison.

**Methods**
- `calculateVariance()`
- `approve(reason)`

### 5. `ReturnRequest`
**Responsibility:** reverse logistics header.

**Methods**
- `approve()`
- `reject()`
- `receiveItem()`
- `markInspected()`
- `markRefunded()`

### 6. `ReturnItem`
**Responsibility:** returned line and disposition.

**Methods**
- `setDisposition(disposition)`
- `inspect(inspectedBy)`

### 7. `Refund`
**Responsibility:** monetary reversal.

**Methods**
- `approve()`
- `process()`
- `complete()`
- `reject()`

### 8. `Exchange`
**Responsibility:** replacement flow linked to return item.

**Methods**
- `createReplacement()`
- `linkReplacementOrder()`

### 9. `TaxInvoice`
**Responsibility:** financial document issuance.

**Methods**
- `issue()`
- `lock()`

### 10. `InvoiceAdjustmentNote`
**Responsibility:** correction document linked to invoice.

**Methods**
- `issue()`
- `lock()`

### 11. `POSApplicationService`
**Methods**
- `openShift(OpenShiftCommand)`
- `createReceipt(CreateReceiptCommand)`
- `closeShift(CloseShiftCommand)`

### 12. `RefundApplicationService`
**Methods**
- `createReturn(CreateReturnCommand)`
- `approveReturn(ApproveReturnCommand)`
- `createRefund(CreateRefundCommand)`
- `createExchange(CreateExchangeCommand)`
- `issueTaxInvoice(IssueTaxInvoiceCommand)`
- `issueAdjustmentNote(IssueAdjustmentNoteCommand)`

### 13. POS and return invariants
- Active shift required for POS sale.
- Cash/card only in v1.
- Refund amount cannot exceed eligible amount.
- Financial documents must be immutable after issue.

---

## Marketing and Social Classes

### 1. `InstagramConnection`
**Responsibility:** store OAuth connection state and encrypted tokens.

**Methods**
- `connect()`
- `reconnect()`
- `disconnect()`
- `refreshToken()`

### 2. `InstagramDraft`
**Responsibility:** compose-ready social draft.

**Key fields**
- `caption`
- `hashtags`
- `mediaOrderJson`
- `status`

**Methods**
- `editCaption()`
- `reorderMedia()`
- `submitForReview()`
- `approve()`
- `archive()`

### 3. `MarketingApplicationService`
**Methods**
- `connectInstagram(ConnectInstagramCommand)`
- `reconnectInstagram(ReconnectInstagramCommand)`
- `getInstagramStatus(userId)`
- `createDraft(CreateDraftCommand)`
- `updateDraft(UpdateDraftCommand)`
- `submitDraftForReview(SubmitDraftReviewCommand)`
- `approveDraft(ApproveDraftCommand)`

### 4. `InstagramAdapter`
**Responsibility:** isolate OAuth and integration details.

### 5. Marketing invariants
- Tokens must be encrypted.
- Draft media order must be preserved.
- No auto-publish functionality in v1.

---

## Customer Engagement Classes

### 1. `Review`
**Responsibility:** customer-generated product feedback.

**Methods**
- `submit()`
- `approve()`
- `reject()`
- `attachImage(imageUrl)`

### 2. `RecommendationRequest`
**Responsibility:** AI query input wrapper.

### 3. `RecommendationResponse`
**Responsibility:** AI recommendation output wrapper.

### 4. `AssistantApplicationService`
**Methods**
- `recommend(RecommendationRequest)`
- `validateRecommendationItems(items)`
- `addRecommendedItemToCart(command)`

### 5. `RecommendationEngineAdapter`
**Responsibility:** AI service integration.

### 6. Customer engagement invariants
- Reviews require verified purchase.
- AI recommendations must prefer in-stock items.
- Availability display must not expose hidden internal details unnecessarily.

---

## Shared Platform Service Classes

### 1. `AuditService`
**Methods**
- `write(action, targetType, targetId, metadata)`
- `writeForCommand(command, target)`

### 2. `NotificationService`
**Methods**
- `sendOrderConfirmation(orderId)`
- `sendShipmentUpdate(orderId)`
- `sendRefundUpdate(refundId)`
- `sendAdminAlert(type, payload)`

### 3. `FileStorageService`
**Methods**
- `upload(file, metadata)`
- `delete(objectKey)`
- `generateSignedUrl(objectKey)`

### 4. `IdempotencyService`
**Methods**
- `checkAndReserve(key, scope)`
- `complete(key, result)`
- `fail(key, error)`

### 5. `LocalizationService`
**Methods**
- `resolveMessage(code, locale)`
- `resolveValidationMessage(field, locale)`

### 6. `DomainEventPublisher`
**Methods**
- `publish(event)`
- `publishAfterCommit(event)`

---

## Exception Model

### Base exception
`SeShopException`

### Domain exceptions
- `AuthenticationRequiredException`
- `ForbiddenOperationException`
- `DuplicateResourceException`
- `InvalidStateTransitionException`
- `InsufficientStockException`
- `InvalidPaymentMethodException`
- `RefundNotEligibleException`
- `ReviewNotEligibleException`
- `InstagramConnectionException`
- `ValidationException`

### Exception mapping
| Exception | HTTP status | Error code family |
|---|---|---|
| AuthenticationRequiredException | 401 | AUTH |
| ForbiddenOperationException | 403 | AUTH |
| DuplicateResourceException | 409 | GEN / CAT / INV / ORD |
| InvalidStateTransitionException | 422 | ORD / INV / POS / SOC |
| InsufficientStockException | 422 | INV |
| InvalidPaymentMethodException | 422 | PAY |
| RefundNotEligibleException | 422 | REF |
| ReviewNotEligibleException | 422 | REV |
| InstagramConnectionException | 422 | SOC |
| ValidationException | 400 | GEN |

---

## DTO Model

### Common DTO groups
- `CreateXxxRequest`
- `UpdateXxxRequest`
- `XxxResponse`
- `XxxSummaryDto`
- `XxxDetailDto`
- `PageResponse<T>`
- `ErrorResponse`

### Example DTOs
- `CreateRoleRequest`
- `AssignPermissionsRequest`
- `CreateProductRequest`
- `CreateVariantRequest`
- `AdjustInventoryRequest`
- `CreateTransferRequest`
- `CheckoutRequest`
- `CreateReceiptRequest`
- `CreateRefundRequest`
- `CreateDraftRequest`
- `RecommendationRequestDto`

### DTO rules
- DTOs must not expose JPA entities directly.
- DTOs should be stable for frontend consumption.
- Response DTOs should include only necessary fields.

---

## Repository Model

### Repository interfaces
- `UserRepository`
- `RoleRepository`
- `PermissionRepository`
- `UserRoleRepository`
- `AuditLogRepository`
- `CategoryRepository`
- `ProductRepository`
- `ProductVariantRepository`
- `ProductImageRepository`
- `LocationRepository`
- `InventoryBalanceRepository`
- `InventoryTransferRepository`
- `InventoryTransferItemRepository`
- `CycleCountRepository`
- `CycleCountItemRepository`
- `SupplierRepository`
- `PurchaseOrderRepository`
- `GoodsReceiptRepository`
- `CartRepository`
- `OrderRepository`
- `ShipmentRepository`
- `PaymentRepository`
- `DiscountCodeRepository`
- `ReturnRequestRepository`
- `RefundRepository`
- `POSShiftRepository`
- `POSReceiptRepository`
- `TaxInvoiceRepository`
- `InstagramConnectionRepository`
- `InstagramDraftRepository`
- `ReviewRepository`

### Repository conventions
- Use Spring Data JPA or equivalent.
- Add explicit query methods for frequent list screens.
- Use locking queries for stock-sensitive updates.
- Use pagination for all high-cardinality repositories.

---

## Service Interaction Model

### Command flow pattern
1. Controller receives request.
2. Validates basic shape.
3. Application service starts transaction.
4. Domain services validate rules.
5. Repository persists state.
6. Audit service records event.
7. Domain event publisher dispatches post-commit events.
8. API returns DTO.

### Example interactions

#### Checkout
`CheckoutController` → `CheckoutApplicationService` → `InventoryService` → `OrderService` → `PaymentAdapter` → `AuditService`

#### Transfer approval
`TransferController` → `InventoryApplicationService` → `InventoryPolicy` → `InventoryTransferRepository` → `AuditService`

#### Instagram draft creation
`DraftController` → `MarketingApplicationService` → `FileStorageService` → `InstagramAdapter` → `AuditService`

---

## Implementation Order Recommendation

1. Identity and RBAC
2. Catalog
3. Inventory
4. Commerce cart and checkout
5. Shipment tracking
6. POS and returns
7. Discount management
8. Instagram connect and drafts
9. Reviews
10. AI recommendation integration
11. Audit and notification hardening

This order reduces dependency risk because identity, catalog, and inventory are foundational for the rest of the system.

---

## Conclusion

This class-level design document is the next-step implementation guide for SeShop backend development. It is consistent with the repository’s business requirements, SRS, diagrams, schema, HLD, LLD, and API spec. It is designed to support a clean modular monolith implementation in Java.
