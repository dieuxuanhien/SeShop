# SE SHOP API SPECIFICATION

**Project:** SeShop  
**Domain:** Omnichannel clothing & accessories platform  
**Architecture style:** Modular Monolith  
**Backend:** Java + Spring Boot  
**API style:** REST JSON over HTTPS  
**Version:** 1.0  
**Last updated:** 2026-04-29

---

## Revision History

| Date | Version | Author | Description |
|---|---:|---|---|
| 2026-04-29 | 1.0 | GitHub Copilot | API specification derived from BRD, SRS, database schema, diagrams, view descriptions, HLD, and LLD |

---

## Table of Contents

1. [References](#references)
2. [Purpose and Scope](#purpose-and-scope)
3. [Design Consistency Check](#design-consistency-check)
4. [API Design Principles](#api-design-principles)
5. [Authentication and Authorization](#authentication-and-authorization)
6. [Common Request and Response Conventions](#common-request-and-response-conventions)
7. [Error Model](#error-model)
8. [Pagination, Filtering, and Sorting](#pagination-filtering-and-sorting)
9. [Media Upload Conventions](#media-upload-conventions)
10. [Domain API Specifications](#domain-api-specifications)
11. [Webhook and External Callback APIs](#webhook-and-external-callback-apis)
12. [Representative Payload Examples](#representative-payload-examples)
13. [Security and Operational Rules](#security-and-operational-rules)
14. [Traceability to SRS Use Cases](#traceability-to-srs-use-cases)
15. [Open Items](#open-items)

---

## References

This API spec derives scope and requirements from:

- [docs/1.BRD/SESHOP BRD.md](../1.BRD/SESHOP%20BRD.md)
- [docs/10.SRS/SESHOP SRS.md](../10.SRS/SESHOP%20SRS.md)
- [docs/3.Design/SESHOP LLD.md](SESHOP%20LLD.md)
- [docs/5.Database/SESHOP schema.sql](../5.Database/SESHOP%20schema.sql)

---

## Purpose and Scope

This document defines the stable REST API contract for SeShop. It is intended for frontend integration, backend implementation, QA test design, and future extension planning.

### Supported client types
- Customer web application
- Staff web application
- Admin web application
- External payment/shipping/Instagram providers via callback/webhook surfaces

### API design objectives
- Preserve server-authoritative business state.
- Provide clear and predictable validation errors.
- Support idempotency for retried critical commands.
- Maintain consistency with the documented SRS use cases and data model.

---

## Design Consistency Check

This API spec is aligned with:
- [docs/1.BRD/SESHOP BRD.md](../1.BRD/SESHOP%20BRD.md)
- [docs/10.SRS/SESHOP SRS.md](../10.SRS/SESHOP%20SRS.md)
- [docs/3.Design/SESHOP HLD.md](SESHOP%20HLD.md)
- [docs/3.Design/SESHOP LLD.md](SESHOP%20LLD.md)
- [docs/5.Database/SESHOP schema.sql](../5.Database/SESHOP%20schema.sql)
- [docs/5.Database/SESHOP Database Diagram.md](../5.Database/SESHOP%20Database%20Diagram.md)
- [docs/4. View descriptions/SeShop Views Desc.md](../4.%20View%20descriptions/SeShop%20Views%20Desc.md)

### Explicit consistency rules
- Use `user_roles`, not `staff_role_assignments`.
- Do not expose `available_qty` as a stored field; compute it as `on_hand_qty - reserved_qty`.
- Support `DRAFT`, `IN_TRANSIT`, `COMPLETED`, `CANCELLED` for transfers.
- Support Stripe and Cash on Delivery in online checkout.
- Support cash and card for POS.
- Support manual review flow for Instagram drafts; no auto-publish endpoint is defined.
- Support procurement flows because purchase order and goods receipt tables exist in schema and SRS.

---

## API Design Principles

1. **Resource-oriented REST** for core entities.
2. **Command endpoints** for state transitions and workflow actions.
3. **Read endpoints** optimized for list and detail views.
4. **Idempotency** required for retried money/stock operations.
5. **Consistent error envelope** across all APIs.
6. **Permission checks on server** for every staff/admin endpoint.
7. **Audit logging** for sensitive and operational mutations.
8. **Pagination and filtering** for all list endpoints.
9. **No business-critical decisions in the frontend**.

---

## Authentication and Authorization

### Authentication model
- Token-based authentication for customers, staff, and admins.
- Login endpoint returns access token and user profile summary.
- Refresh token support may be implemented if the platform uses long-lived sessions.

### Authorization model
- RBAC is permission-driven.
- Permissions are checked in backend service and controller layers.
- Frontend can hide unauthorized actions, but backend remains authoritative.

### Permission examples
- `role.create`
- `role.permission.assign`
- `staff.role.assign`
- `catalog.write`
- `inventory.adjust`
- `inventory.transfer`
- `order.read`
- `order.ship`
- `refund.process`
- `promo.manage`
- `social.compose`
- `social.connect`
- `audit.read`

### Authorization rule
If a user lacks permission, the API must return `403 FORBIDDEN` with a stable error code and no side effect.

---

## Common Request and Response Conventions

### Base URL
`/api/v1`

### Standard headers
| Header | Usage |
|---|---|
| `Authorization` | Bearer token for authenticated requests |
| `Content-Type` | `application/json` for JSON payloads |
| `Idempotency-Key` | Required for retry-safe commands such as checkout, refund, transfer confirm |
| `X-Trace-Id` | Optional client trace correlation |

### Standard success response
```json
{
  "data": {
    "id": 123
  },
  "meta": {
    "traceId": "c5d9f0c7a2a14f7b"
  }
}
```

### Standard error response
```json
{
  "code": "INV_001",
  "message": "Insufficient stock",
  "details": [
    {
      "field": "items[0].qty",
      "reason": "Requested quantity exceeds available stock"
    }
  ],
  "traceId": "c5d9f0c7a2a14f7b"
}
```

### Resource identifiers
- Numeric `id` for internal entities.
- Business identifiers such as `orderNumber`, `poNumber`, `invoiceNumber`, `skuCode` are exposed where user-facing.

### Time format
- ISO-8601 with timezone, using `TIMESTAMPTZ` semantics.

### Money format
- Decimal string or numeric value with 2 fractional digits.
- Currency is represented separately when relevant.

---

## Error Model

### Common HTTP status codes
| Status | Meaning |
|---|---|
| 200 | Success |
| 201 | Created |
| 204 | No content |
| 400 | Validation error |
| 401 | Authentication required |
| 403 | Permission denied |
| 404 | Resource not found |
| 409 | Conflict / duplicate / concurrency issue |
| 422 | Business rule violation |
| 500 | Unexpected server error |

### Error code families
| Code family | Meaning |
|---|---|
| `AUTH_*` | Authentication and authorization |
| `CAT_*` | Catalog errors |
| `INV_*` | Inventory errors |
| `ORD_*` | Order and checkout errors |
| `PAY_*` | Payment errors |
| `POS_*` | POS and shift errors |
| `REF_*` | Refund and return errors |
| `SOC_*` | Social / Instagram errors |
| `REV_*` | Review errors |
| `GEN_*` | Generic server or request errors |

### Representative error codes
| Code | Meaning |
|---|---|
| `AUTH_001` | Authentication required |
| `AUTH_002` | Insufficient permission |
| `CAT_001` | Duplicate SKU code |
| `CAT_002` | Product not publishable |
| `INV_001` | Insufficient stock |
| `INV_002` | Invalid transfer state |
| `INV_003` | Invalid location state |
| `ORD_001` | Checkout reservation expired |
| `ORD_002` | Invalid order state |
| `PAY_001` | Payment failed |
| `PAY_002` | Unsupported payment method |
| `POS_001` | Shift already closed |
| `POS_002` | Active shift required |
| `REF_001` | Refund not eligible |
| `SOC_001` | Instagram connection expired |
| `SOC_002` | Draft approval required |
| `REV_001` | Review not eligible |
| `GEN_001` | Invalid request payload |

---

## Pagination, Filtering, and Sorting

### Pagination parameters
- `page` starts at `0`
- `size` default is `20`
- `sort` format: `field,asc` or `field,desc`

### Common filtering conventions
- Text filters use partial or exact match depending on endpoint.
- Date filters use `from` and `to`.
- Status filters accept uppercase enum values.
- Multi-select filters use comma-separated values or repeated query params, depending on endpoint.

### Example
`GET /api/v1/products?page=0&size=20&category=shirts&size=M&color=Black&sort=createdAt,desc`

---

## Media Upload Conventions

### Supported media types
- Product images
- Review images
- Instagram draft images

### Upload rules
- Validate MIME type and extension.
- Enforce file size limits.
- Store binary content outside the relational database.
- Save metadata and object key or URL in DB.

### Upload endpoints use multipart form data
- `multipart/form-data` for file + metadata upload

---

## Domain API Specifications

---

# 1. Identity and RBAC APIs

## 1.1 Login and registration

### POST `/api/v1/auth/register`
Create a customer account.

**Request**
```json
{
  "username": "nguyenanh",
  "email": "anh@example.com",
  "phoneNumber": "0901234567",
  "password": "P@ssw0rd123"
}
```

**Response 201**
```json
{
  "data": {
    "userId": 1001,
    "status": "ACTIVE",
    "userType": "CUSTOMER"
  },
  "meta": {
    "traceId": "trace-001"
  }
}
```

### POST `/api/v1/auth/login`
Authenticate a user and issue token.

**Request**
```json
{
  "usernameOrEmail": "anh@example.com",
  "password": "P@ssw0rd123"
}
```

**Response 200**
```json
{
  "data": {
    "accessToken": "jwt-token",
    "user": {
      "id": 1001,
      "username": "nguyenanh",
      "userType": "CUSTOMER",
      "roles": []
    }
  },
  "meta": {
    "traceId": "trace-002"
  }
}
```

## 1.2 Role management

### POST `/api/v1/admin/roles`
Create a custom role.

**Request**
```json
{
  "name": "Inventory Supervisor",
  "description": "Approves inventory transfers and adjustments"
}
```

**Response 201**
```json
{
  "data": {
    "id": 21,
    "name": "Inventory Supervisor",
    "status": "INACTIVE"
  },
  "meta": {
    "traceId": "trace-003"
  }
}
```

### GET `/api/v1/admin/roles`
List roles with pagination.

### POST `/api/v1/admin/roles/{roleId}/permissions`
Assign permissions to a role.

**Request**
```json
{
  "permissionCodes": [
    "inventory.transfer",
    "inventory.adjust"
  ]
}
```

### POST `/api/v1/admin/users/{userId}/roles`
Assign role to a staff user.

**Request**
```json
{
  "roleId": 21
}
```

### DELETE `/api/v1/admin/users/{userId}/roles/{assignmentId}`
Revoke a role assignment.

### GET `/api/v1/admin/audit-logs`
List immutable audit records.

**Query parameters**
- `from`
- `to`
- `actorUserId`
- `action`
- `targetType`
- `targetId`
- `page`
- `size`

---

# 2. Catalog APIs

## 2.1 Public catalog

### GET `/api/v1/products`
Browse published products.

**Query parameters**
- `keyword`
- `categoryId`
- `size`
- `color`
- `brand`
- `minPrice`
- `maxPrice`
- `sort`
- `page`
- `size`

**Response 200**
```json
{
  "data": {
    "items": [
      {
        "id": 501,
        "name": "Linen Shirt",
        "brand": "SeShop",
        "status": "PUBLISHED",
        "variants": [
          {
            "id": 7001,
            "skuCode": "LSHIRT-BLK-M",
            "size": "M",
            "color": "Black",
            "price": 590000,
            "availability": {
              "totalAvailable": 18
            }
          }
        ]
      }
    ],
    "page": 0,
    "size": 20,
    "totalElements": 1
  },
  "meta": {
    "traceId": "trace-100"
  }
}
```

### GET `/api/v1/products/{productId}`
Get product detail including variants, images, and related categories.

### GET `/api/v1/products/{productId}/availability`
Get stock availability by location for a selected product.

**Query parameters**
- `variantId` or `skuCode`

**Response 200**
```json
{
  "data": {
    "productId": 501,
    "variantId": 7001,
    "locations": [
      {
        "locationId": 11,
        "locationName": "District 1 Store",
        "availableQty": 5,
        "updatedAt": "2026-04-29T09:30:00+07:00"
      }
    ]
  },
  "meta": {
    "traceId": "trace-101"
  }
}
```

## 2.2 Staff catalog management

### POST `/api/v1/staff/products`
Create a product master record.

### PUT `/api/v1/staff/products/{productId}`
Update product metadata.

### POST `/api/v1/staff/products/{productId}/variants`
Create one or more SKUs.

**Request**
```json
{
  "variants": [
    {
      "skuCode": "LSHIRT-BLK-M",
      "size": "M",
      "color": "Black",
      "price": 590000,
      "status": "ACTIVE"
    }
  ]
}
```

### POST `/api/v1/staff/products/{productId}/images`
Upload or register product images.

### GET `/api/v1/categories`
List categories for browse and merchandising.

---

# 3. Inventory APIs

## 3.1 Inventory balance and adjustments

### GET `/api/v1/staff/inventory/balances`
List inventory balances.

**Query parameters**
- `variantId`
- `locationId`
- `skuCode`
- `page`
- `size`

**Note:** The POS UI also uses a convenience endpoint for single-SKU lookup: `GET /api/v1/staff/inventory/balances/sku/{skuCode}`. This endpoint returns a single `ProductVariant` payload (id, skuCode, productName, price) and is implemented for fast barcode scan flows. Documenting both the list endpoint and the SKU convenience path keeps the spec aligned with current implementation.

### POST `/api/v1/staff/inventory/adjustments`
Adjust stock by SKU-location.

**Request**
```json
{
  "variantId": 7001,
  "locationId": 11,
  "deltaQty": -2,
  "reasonCode": "DAMAGE",
  "notes": "Damaged during packing"
}
```

**Response 201**
```json
{
  "data": {
    "inventoryBalanceId": 8801,
    "onHandQty": 16,
    "reservedQty": 1
  },
  "meta": {
    "traceId": "trace-200"
  }
}
```

## 3.2 Inventory transfers

### POST `/api/v1/staff/inventory/transfers`
Create transfer draft.

**Request**
```json
{
  "sourceLocationId": 11,
  "destinationLocationId": 12,
  "items": [
    {
      "variantId": 7001,
      "qty": 3
    }
  ],
  "reason": "Rebalancing stock"
}
```

### POST `/api/v1/staff/inventory/transfers/{transferId}/approve`
Approve and move transfer to in-transit state.

### POST `/api/v1/staff/inventory/transfers/{transferId}/receive`
Confirm receipt at destination.

**Request**
```json
{
  "receivedItems": [
    {
      "variantId": 7001,
      "receivedQty": 3,
      "damagedQty": 0
    }
  ]
}
```

### GET `/api/v1/staff/inventory/transfers`
List transfer headers.

### GET `/api/v1/staff/inventory/transfers/{transferId}`
Get transfer detail.

## 3.3 Cycle counts and procurement

### POST `/api/v1/staff/cycle-counts`
Create cycle count session.

**Request**
```json
{
  "locationId": 11,
  "reason": "Monthly reconciliation"
}
```

### POST `/api/v1/staff/cycle-counts/{cycleCountId}/items`
Submit counted quantities.

### POST `/api/v1/staff/cycle-counts/{cycleCountId}/approve`
Approve and post variances.

### POST `/api/v1/staff/purchase-orders`
Create purchase order.

**Request**
```json
{
  "supplierId": 101,
  "destinationLocationId": 12,
  "items": [
    {
      "variantId": 7001,
      "orderedQty": 20,
      "unitCost": 350000
    }
  ]
}
```

### POST `/api/v1/staff/goods-receipts`
Post goods receipt against PO.

**Request**
```json
{
  "purchaseOrderId": 901,
  "receivedAt": "2026-04-29T10:00:00+07:00",
  "items": [
    {
      "variantId": 7001,
      "receivedQty": 18,
      "damagedQty": 2
    }
  ]
}
```

---

# 4. Commerce APIs

## 4.1 Cart APIs

### GET `/api/v1/carts/me`
Get current customer cart.

### POST `/api/v1/carts/me/items`
Add or update cart item.

**Request**
```json
{
  "variantId": 7001,
  "qty": 2
}
```

### PATCH `/api/v1/carts/me/items/{itemId}`
Update quantity.

### DELETE `/api/v1/carts/me/items/{itemId}`
Remove cart item.

## 4.2 Checkout APIs

### POST `/api/v1/discounts/validate`
Validate discount code before checkout.

**Request**
```json
{
  "code": "SUMMER10",
  "orderSubtotal": 1180000
}
```

### POST `/api/v1/checkout`
Create order and payment record.

**Request**
```json
{
  "cartId": 3001,
  "shippingAddress": {
    "fullName": "Nguyen Anh",
    "phoneNumber": "0901234567",
    "line1": "123 Nguyen Trai",
    "ward": "Ward 1",
    "district": "District 5",
    "city": "Ho Chi Minh City"
  },
  "paymentMethod": "STRIPE",
  "discountCode": "SUMMER10"
}
```

**Response 201**
```json
{
  "data": {
    "orderId": 10001,
    "orderNumber": "ORD-20260429-0001",
    "paymentStatus": "PENDING",
    "shipmentStatus": "PENDING"
  },
  "meta": {
    "traceId": "trace-300"
  }
}
```

### GET `/api/v1/orders/me`
List customer orders.

### GET `/api/v1/orders/{orderId}`
Get order detail, shipment status, and timeline.

### POST `/api/v1/orders/{orderId}/track-shipment`
Refresh shipment status from external provider when allowed.

## 4.3 Discount management (staff)

### POST `/api/v1/staff/discounts`
Create discount code.

### GET `/api/v1/staff/discounts`
List discount codes.

### PUT `/api/v1/staff/discounts/{discountId}`
Update discount code.

### DELETE `/api/v1/staff/discounts/{discountId}`
Deactivate discount code.

## 4.3 Order fulfillment APIs

### GET `/api/v1/staff/orders`
List staff-visible orders.

### POST `/api/v1/staff/orders/{orderId}/allocate`
Allocate order to fulfillment location.

### POST `/api/v1/staff/orders/{orderId}/pack`
Mark order packed.

### POST `/api/v1/staff/orders/{orderId}/ship`
Mark order shipped with tracking metadata.

**Request**
```json
{
  "carrier": "GHN",
  "trackingNumber": "GHN123456789"
}
```

### POST `/api/v1/staff/orders/{orderId}/cancel`
Cancel order if policy permits.

---

# 5. POS APIs

### POST `/api/v1/pos/shifts/open`
Open a POS shift.

**Request**
```json
{
  "locationId": 11
}
```

### POST `/api/v1/pos/receipts`
Create POS sale receipt.

**Request**
```json
{
  "shiftId": 501,
  "customerUserId": 1001,
  "paymentMethod": "CASH",
  "items": [
    {
      "variantId": 7001,
      "qty": 1,
      "unitPrice": 590000
    }
  ]
}
```

### POST `/api/v1/pos/shifts/{shiftId}/close`
Close shift and reconcile cash.

**Request**
```json
{
  "actualCash": 2500000,
  "reason": "End of day close"
}
```

### GET `/api/v1/pos/shifts/{shiftId}`
Get shift summary.

### GET `/api/v1/pos/receipts/{receiptId}`
Get POS receipt detail.

---

# 6. Refund, Return, Exchange, and Finance APIs

### POST `/api/v1/returns`
Create return request.

**Request**
```json
{
  "orderId": 10001,
  "reason": "Wrong size",
  "items": [
    {
      "orderItemId": 20001,
      "qty": 1
    }
  ]
}
```

### POST `/api/v1/returns/{returnId}/approve`
Approve return request.

### POST `/api/v1/refunds`
Create refund.

**Request**
```json
{
  "orderId": 10001,
  "paymentId": 9001,
  "returnRequestId": 8001,
  "amount": 590000
}
```

### GET `/api/v1/refunds/{refundId}`
Get refund detail.

### POST `/api/v1/exchanges`
Create exchange request.

**Request**
```json
{
  "returnItemId": 8101,
  "replacementVariantId": 7002
}
```

### POST `/api/v1/invoices/tax`
Issue tax invoice.

**Request**
```json
{
  "orderId": 10001
}
```

### POST `/api/v1/invoices/{invoiceId}/adjustments`
Create invoice adjustment note.

**Request**
```json
{
  "reason": "Correction of quantity",
  "deltaAmount": -59000
}
```

---

# 7. Marketing and Social APIs

### POST `/api/v1/marketing/instagram/connect`
Start Instagram OAuth connection.

### POST `/api/v1/marketing/instagram/reconnect`
Reconnect expired Instagram account.

### GET `/api/v1/marketing/instagram/status`
Get connection status.

**Response 200**
```json
{
  "data": {
    "status": "CONNECTED",
    "accountId": "ig_123",
    "accountName": "seshop.vn",
    "tokenExpiresAt": "2026-05-10T00:00:00+07:00"
  },
  "meta": {
    "traceId": "trace-400"
  }
}
```

### POST `/api/v1/marketing/drafts`
Create Instagram draft.

**Request**
```json
{
  "productId": 501,
  "caption": "New arrival now available!",
  "hashtags": "#seshop #newarrival",
  "mediaOrder": [
    "https://cdn.example.com/image-1.jpg",
    "https://cdn.example.com/image-2.jpg"
  ]
}
```

### PUT `/api/v1/marketing/drafts/{draftId}`
Update draft content.

### POST `/api/v1/marketing/drafts/{draftId}/submit-review`
Move draft to review-ready state.

### POST `/api/v1/marketing/drafts/{draftId}/approve`
Approve draft for manual publishing handoff.

### GET `/api/v1/marketing/drafts`
List drafts with filters.

---

# 8. Customer Engagement APIs

### POST `/api/v1/assistant/recommendations`
Request AI recommendations.

**Request**
```json
{
  "message": "I need a black shirt for office wear",
  "context": {
    "preferredColor": "Black",
    "occasion": "Office"
  }
}
```

**Response 200**
```json
{
  "data": {
    "answer": "Recommended items based on current stock and office style.",
    "items": [
      {
        "productId": 501,
        "variantId": 7001,
        "reason": "In stock in your size and fits office wear"
      }
    ]
  },
  "meta": {
    "traceId": "trace-500"
  }
}
```

### POST `/api/v1/reviews`
Submit review with optional image.

**Request**
```json
{
  "orderItemId": 20001,
  "rating": 5,
  "comment": "Great fit and material",
  "imageUrl": "https://cdn.example.com/review-1.jpg"
}
```

### GET `/api/v1/reviews/product/{productId}`
List product reviews.

---

## Webhook and External Callback APIs

### Payment gateway callback
`POST /api/v1/webhooks/payments`

Responsibilities:
- verify signature
- update payment status
- trigger order state updates and inventory transitions
- write audit log

### Shipping provider callback
`POST /api/v1/webhooks/shipping`

Responsibilities:
- verify signature
- update shipment status timeline
- notify customer on major transitions

### Instagram OAuth callback
`GET /api/v1/webhooks/instagram/callback`

Responsibilities:
- exchange authorization code
- store encrypted connection tokens
- validate granted scopes

### General webhook rules
- Must be idempotent.
- Must reject invalid signatures.
- Must not expose internal debug details.

---

## Representative Payload Examples

### Product detail response skeleton
```json
{
  "data": {
    "id": 501,
    "name": "Linen Shirt",
    "brand": "SeShop",
    "description": "...",
    "status": "PUBLISHED",
    "categories": [
      {
        "id": 10,
        "name": "Shirts"
      }
    ],
    "images": [
      {
        "id": 901,
        "url": "https://cdn.example.com/product-1.jpg",
        "sortOrder": 1,
        "isInstagramReady": true
      }
    ],
    "variants": [
      {
        "id": 7001,
        "skuCode": "LSHIRT-BLK-M",
        "size": "M",
        "color": "Black",
        "price": 590000,
        "status": "ACTIVE"
      }
    ]
  },
  "meta": {
    "traceId": "trace-600"
  }
}
```

### Order response skeleton
```json
{
  "data": {
    "orderNumber": "ORD-20260429-0001",
    "status": "ALLOCATED",
    "paymentStatus": "PAID",
    "shipmentStatus": "PENDING",
    "items": [
      {
        "skuCode": "LSHIRT-BLK-M",
        "qty": 2,
        "unitPrice": 590000,
        "discountAmount": 0
      }
    ]
  },
  "meta": {
    "traceId": "trace-601"
  }
}
```

### Transfer response skeleton
```json
{
  "data": {
    "transferId": 3001,
    "status": "IN_TRANSIT"
  },
  "meta": {
    "traceId": "trace-602"
  }
}
```

---

## Security and Operational Rules

### Sensitive endpoints require audit logging
- RBAC mutations
- Inventory adjustments and transfer state changes
- Payment and refund actions
- POS close and reconciliation
- Instagram connection changes
- Invoice issuance and adjustment notes

### Idempotency requirements
`Idempotency-Key` must be supported for:
- checkout
- payment confirmation processing
- refund creation
- inventory transfer approval/receipt
- POS receipt creation if retryable in client design

### Concurrency rules
- Use optimistic locking or row-level locking where stock is mutated.
- Reject conflicting duplicate operations with `409 CONFLICT`.

### Rate limiting
- Public auth endpoints require rate limiting.
- Webhook endpoints require signature verification and bounded retries.

---

## Traceability to SRS Use Cases

| UC | API coverage |
|---|---|
| UC1 | `/admin/roles` |
| UC2 | `/admin/roles/{roleId}/permissions` |
| UC3 | `/admin/users/{userId}/roles` |
| UC4 | `/admin/audit-logs` |
| UC5 | `/staff/products`, `/staff/products/{productId}/variants`, `/staff/products/{productId}/images` |
| UC6 | `/staff/inventory/adjustments` |
| UC7 | `/staff/inventory/transfers` |
| UC8 | `/pos/receipts` |
| UC9 | `/refunds` |
| UC10 | `/discounts/validate`, `/staff/discounts` |
| UC11 | `/marketing/drafts`, `/marketing/instagram/*` |
| UC12 | `/auth/register` |
| UC13 | `/products`, `/products/{productId}` |
| UC14 | `/assistant/recommendations` |
| UC15 | `/checkout` |
| UC16 | `/products/{productId}/availability` |
| UC17 | `/orders/{orderId}` |
| UC18 | `/reviews` |
| UC19 | `/staff/orders` |
| UC20 | `/staff/orders/{orderId}/ship` |
| UC21 | `/marketing/instagram/connect`, `/marketing/instagram/reconnect`, `/marketing/instagram/status` |
| UC22 | `/staff/purchase-orders`, `/staff/goods-receipts` |
| UC23 | `/staff/orders/{orderId}/allocate` |
| UC24 | `/returns`, `/exchanges` |
| UC25 | `/staff/cycle-counts` |
| UC26 | `/pos/shifts/{shiftId}/close` |
| UC27 | `/invoices/tax`, `/invoices/{invoiceId}/adjustments` |

---

## Open Items

- Finalize exact permission naming convention for staff actions.
- Confirm whether customer saved payment methods will be included in v1 APIs.
- Finalize external shipping webhook retry and signature policy.
- Confirm final AI safety filtering and prompt guardrails.
- Approve bilingual error/message payload catalog for production.

---

## Conclusion

This API specification is the contract layer for the SeShop modular monolith. It is intentionally aligned with the repository’s BRD, SRS, database schema, diagrams, views, HLD, and LLD. It covers all approved SRS use cases and provides a foundation for implementation, testing, and frontend integration.
