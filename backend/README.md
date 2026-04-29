# SESHOP Backend - Hướng dẫn Triển khai

**Backend**: Java + Spring Boot + PostgreSQL  
**Status**: Planning Phase  
**Last Updated**: 2026-04-29

---

## 📋 Cấu trúc Backend

```
src/main/java/com/seshop/
├── api/                          # REST Controllers
│   ├── auth/                      # UC1, UC2, UC3 (Auth APIs)
│   ├── catalog/                   # UC5 (Product & SKU management)
│   ├── inventory/                 # UC6, UC7, UC16, UC25 (Stock management)
│   ├── commerce/                  # UC13, UC14, UC15 (Shopping)
│   ├── pos/                       # UC8, UC26 (POS operations)
│   ├── order/                     # UC15, UC19, UC20, UC23 (Order management)
│   ├── return/                    # UC9, UC24 (Return/refund)
│   ├── marketing/                 # UC10, UC11, UC21 (Marketing)
│   ├── admin/                     # Admin operations
│   └── common/                    # Common endpoints
│
├── domain/                        # Domain Models & Business Logic
│   ├── auth/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── Permission.java
│   │   ├── AuthService.java
│   │   └── RBACService.java
│   │
│   ├── catalog/
│   │   ├── Product.java
│   │   ├── ProductVariant.java
│   │   ├── ProductImage.java
│   │   ├── Category.java
│   │   ├── CatalogService.java
│   │   └── SearchService.java
│   │
│   ├── inventory/
│   │   ├── Location.java
│   │   ├── InventoryBalance.java
│   │   ├── InventoryTransfer.java
│   │   ├── GoodsReceipt.java
│   │   ├── CycleCount.java
│   │   ├── InventoryService.java
│   │   ├── TransferService.java
│   │   └── ReservationService.java
│   │
│   ├── commerce/
│   │   ├── Cart.java
│   │   ├── CartItem.java
│   │   ├── CartService.java
│   │   └── PricingService.java
│   │
│   ├── order/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderAllocation.java
│   │   ├── OrderService.java
│   │   └── FulfillmentService.java
│   │
│   ├── pos/
│   │   ├── POSReceipt.java
│   │   ├── POSReceiptItem.java
│   │   ├── POSShift.java
│   │   ├── CashReconciliation.java
│   │   ├── POSService.java
│   │   └── ShiftService.java
│   │
│   ├── return/
│   │   ├── ReturnRequest.java
│   │   ├── ReturnItem.java
│   │   ├── Refund.java
│   │   ├── ReturnService.java
│   │   └── RefundService.java
│   │
│   ├── marketing/
│   │   ├── DiscountCode.java
│   │   ├── DiscountService.java
│   │   ├── InstagramConnection.java
│   │   ├── InstagramDraft.java
│   │   ├── InstagramService.java
│   │   ├── AIRecommendationService.java
│   │   └── RecommendationChat.java
│   │
│   └── shared/
│       ├── Money.java
│       ├── Address.java
│       ├── ContactInfo.java
│       └── PeriodRange.java
│
├── persistence/                   # Repositories & Entities
│   ├── auth/
│   │   ├── UserRepository.java
│   │   ├── RoleRepository.java
│   │   └── PermissionRepository.java
│   │
│   ├── catalog/
│   │   ├── ProductRepository.java
│   │   ├── ProductVariantRepository.java
│   │   └── CategoryRepository.java
│   │
│   ├── inventory/
│   │   ├── LocationRepository.java
│   │   ├── InventoryBalanceRepository.java
│   │   ├── InventoryTransferRepository.java
│   │   └── GoodsReceiptRepository.java
│   │
│   ├── order/
│   │   ├── OrderRepository.java
│   │   └── OrderItemRepository.java
│   │
│   └── [other domain repositories]
│
├── application/                   # Use Cases & Orchestration
│   ├── auth/
│   │   ├── LoginUseCase.java
│   │   ├── RegisterUseCase.java
│   │   └── AssignRoleUseCase.java
│   │
│   ├── catalog/
│   │   ├── AddProductUseCase.java
│   │   ├── SearchProductsUseCase.java
│   │   └── AddVariantUseCase.java
│   │
│   ├── inventory/
│   │   ├── AdjustInventoryUseCase.java
│   │   ├── CreateTransferUseCase.java
│   │   ├── ApproveTransferUseCase.java
│   │   └── ReconcileInventoryUseCase.java
│   │
│   ├── commerce/
│   │   ├── CheckoutUseCase.java
│   │   ├── CreateOrderUseCase.java
│   │   └── ProcessPaymentUseCase.java
│   │
│   ├── pos/
│   │   ├── CreatePOSReceiptUseCase.java
│   │   ├── OpenShiftUseCase.java
│   │   ├── CloseShiftUseCase.java
│   │   └── ReconcileCashUseCase.java
│   │
│   ├── return/
│   │   ├── CreateReturnRequestUseCase.java
│   │   └── ProcessRefundUseCase.java
│   │
│   ├── marketing/
│   │   ├── CreateDiscountUseCase.java
│   │   ├── ComposeDraftUseCase.java
│   │   ├── GetRecommendationsUseCase.java
│   │   └── ChatAIUseCase.java
│   │
│   └── admin/
│       ├── CreateRoleUseCase.java
│       └── ViewAuditLogUseCase.java
│
├── infrastructure/                # External Integrations
│   ├── payment/
│   │   ├── PaymentProvider.java (interface)
│   │   ├── StripePaymentProvider.java
│   │   ├── CODPaymentProvider.java
│   │   └── PaymentGateway.java
│   │
│   ├── shipping/
│   │   ├── ShippingProvider.java (interface)
│   │   ├── GHNShippingProvider.java
│   │   └── ShippingService.java
│   │
│   ├── instagram/
│   │   ├── InstagramClient.java
│   │   ├── InstagramMediaUploader.java
│   │   └── InstagramOAuthHandler.java
│   │
│   ├── email/
│   │   ├── EmailService.java
│   │   └── EmailTemplate.java
│   │
│   ├── storage/
│   │   ├── FileStorage.java (interface)
│   │   ├── S3Storage.java
│   │   └── LocalFileStorage.java
│   │
│   └── ai/
│       ├── AIProvider.java (interface)
│       ├── GPTRecommendationProvider.java
│       └── RecommendationEngine.java
│
├── shared/                        # Shared Components
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ValidationException.java
│   │   ├── AuthenticationException.java
│   │   ├── AuthorizationException.java
│   │   ├── GlobalExceptionHandler.java
│   │   └── ErrorResponse.java
│   │
│   ├── security/
│   │   ├── JwtTokenProvider.java
│   │   ├── SecurityConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   ├── PermissionValidator.java
│   │   └── SecurityContext.java
│   │
│   ├── audit/
│   │   ├── AuditLog.java
│   │   ├── AuditAspect.java
│   │   ├── AuditRepository.java
│   │   ├── AuditService.java
│   │   └── Auditable.java (interface)
│   │
│   ├── config/
│   │   ├── DatabaseConfig.java
│   │   ├── WebConfig.java
│   │   ├── CacheConfig.java
│   │   └── JacksonConfig.java
│   │
│   └── util/
│       ├── PageUtils.java
│       ├── FilterUtils.java
│       ├── ValidationUtils.java
│       ├── DateTimeUtils.java
│       └── IdempotencyUtils.java
```

---

## 🎯 Use Cases & API Reference

Use cases, endpoints, and acceptance criteria are the single source of truth in:

- [docs/10.SRS/SESHOP SRS.md](../docs/10.SRS/SESHOP%20SRS.md)
- [docs/3.Design/SESHOP API Spec.md](../docs/3.Design/SESHOP%20API%20Spec.md)

## 🔄 Workflows

Workflow details are defined in:

- [docs/10.SRS/SESHOP SRS.md](../docs/10.SRS/SESHOP%20SRS.md)
- [docs/2. Diagrams/](../docs/2.%20Diagrams/)

## 📊 Data Model

Use the data dictionary and schema as the canonical source:

- [docs/5.Database/SESHOP Data Dictionary.md](../docs/5.Database/SESHOP%20Data%20Dictionary.md)
- [docs/5.Database/SESHOP schema.sql](../docs/5.Database/SESHOP%20schema.sql)

## 🔐 Security & Permissions

RBAC and permission definitions are defined in:

- [docs/1.BRD/SESHOP BRD.md](../docs/1.BRD/SESHOP%20BRD.md)
- [docs/10.SRS/SESHOP SRS.md](../docs/10.SRS/SESHOP%20SRS.md)

## 📚 Implementation Order

See [IMPLEMENTATION_PLAN.md](../IMPLEMENTATION_PLAN.md) for the implementation sequence.

