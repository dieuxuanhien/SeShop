---
description: "API specification and REST contract for SeShop. Use when: designing endpoints, validating request/response formats, checking error codes, or reviewing API consistency."
name: api-specification
---

# SeShop API Specification Skill

This skill provides the REST API contract and specification for SeShop platform.

## When to Use

- Designing or implementing REST endpoints
- Validating request/response payloads
- Checking HTTP status codes and error codes
- Implementing API security (authentication, authorization)
- Testing API endpoints

## API Design Principles

1. **RESTful**: Resource-oriented endpoints with HTTP verbs
2. **JSON**: All request/response bodies in JSON
3. **HTTPS**: Encrypted communication
4. **Stateless**: Each request contains all info needed
5. **Versioning**: `/api/v1/` prefix for future compatibility
6. **Pagination**: Large result sets paginated (limit, offset)
7. **Idempotency**: Critical operations support idempotency keys

## Base URL

```
https://api.seshop.local/api/v1
```

## Authentication

All endpoints require JWT token in Authorization header:

```
Authorization: Bearer <jwt_token>
```

**Token Format**: HS256 signed JWT with claims:
- `userId`: User ID
- `userType`: ADMIN, STAFF, or CUSTOMER
- `permissions`: List of permission codes (e.g., `["INV:VIEW", "INV:ADJUST"]`)
- `exp`: Expiration timestamp

## Common Response Structure

### Success Response (200, 201)
```json
{
  "data": { ... },
  "meta": {
    "timestamp": "2026-04-30T10:00:00Z",
    "requestId": "req-12345"
  }
}
```

### Error Response (400, 401, 403, 404, 409, 500)
```json
{
  "error": {
    "code": "INVENTORY_INSUFFICIENT",
    "message": "Not enough stock available",
    "details": {
      "required": 10,
      "available": 5
    }
  },
  "meta": {
    "timestamp": "2026-04-30T10:00:00Z",
    "requestId": "req-12345"
  }
}
```

## HTTP Status Codes

| Status | Meaning |
|--------|---------|
| 200 | OK - Request succeeded, returning data |
| 201 | Created - Resource created successfully |
| 204 | No Content - Request succeeded, no data returned |
| 400 | Bad Request - Invalid input |
| 401 | Unauthorized - Missing/invalid token |
| 403 | Forbidden - Insufficient permissions |
| 404 | Not Found - Resource does not exist |
| 409 | Conflict - Duplicate/concurrency issue |
| 500 | Internal Server Error - Server error |

## Common Error Codes

| Code | HTTP | Module | Meaning |
|------|------|--------|---------|
| `AUTH_001` | 401 | AUTH | Invalid credentials |
| `AUTH_002` | 403 | AUTH | Insufficient permissions |
| `PRD_001` | 404 | CAT | Product not found |
| `CAT_001` | 409 | CAT | Duplicate SKU code |
| `INV_001` | 400 | INV | Invalid location |
| `INV_002` | 409 | INV | Insufficient stock |
| `ORD_001` | 400 | ORD | Invalid order state |
| `ORD_002` | 404 | ORD | Order not found |
| `PAY_001` | 409 | PAY | Payment failed |
| `REF_001` | 400 | REF | Invalid refund request |

See [SESHOP API Spec](../../docs/3.Design/SESHOP%20API%20Spec.md) for full list.

## API Endpoint Categories

### Authentication

**POST /auth/login**
```json
Request: { "username": "...", "password": "..." }
Response: { "token": "jwt...", "user": { "id": 1, "name": "...", "permissions": [...] } }
```

**POST /auth/logout**
```json
Response: { "message": "Logged out successfully" }
```

### Products & Catalog

**GET /products**
```
Query params: page=1, limit=20, category=1, search=shirt
Response: { "data": [products], "total": 100, "page": 1 }
```

**POST /products**
```json
Request: { "name": "...", "description": "...", "categoryIds": [...] }
Response: { "id": 123, "name": "...", ... }
```

**GET /products/{id}**
```
Response: { "id": 123, "name": "...", "variants": [...] }
```

**GET /products/{id}/skus**
```
Response: [{ "code": "SKU001", "size": "M", "color": "Blue", ... }]
```

**POST /products/{id}/skus**
```json
Request: { "code": "SKU001", "size": "M", "color": "Blue", "price": 99.99 }
Response: { "id": 456, "code": "SKU001", ... }
```

### Inventory

**GET /inventory/locations**
```
Response: [{ "id": 1, "name": "Store A", "type": "STORE" }]
```

**GET /inventory/balances**
```
Query params: locationId=1, skuId=123
Response: [{ "skuId": 123, "locationId": 1, "total": 100, "reserved": 10, "available": 90 }]
```

**POST /inventory/adjustments**
```json
Request: { "skuId": 123, "locationId": 1, "quantity": 5, "reason": "damage", "notes": "..." }
Response: { "id": 789, "status": "PENDING", "createdAt": "..." }
Permissions: INV:ADJUST
```

**POST /inventory/transfers**
```json
Request: { "items": [{ "skuId": 123, "qty": 10 }], "fromLocationId": 1, "toLocationId": 2, "notes": "..." }
Response: { "id": 789, "status": "DRAFT", "items": [...] }
Permissions: INV:TRANSFER
```

### Orders

**POST /orders**
```json
Request: { "items": [{ "skuId": 123, "quantity": 1 }], "shippingAddress": { ... }, "paymentMethod": "stripe" }
Response: { "id": 999, "status": "PENDING", "total": 99.99, "items": [...] }
```

**GET /orders**
```
Query params: status=pending, page=1, limit=20
Response: { "data": [orders], "total": 50, "page": 1 }
```

**GET /orders/{id}**
```
Response: { "id": 999, "status": "CONFIRMED", "items": [...], "shipment": {...} }
```

**POST /orders/{id}/payment**
```json
Request: { "method": "stripe", "stripePaymentIntentId": "pi_..." }
Response: { "status": "PAID", "paymentId": "..." }
```

**POST /orders/{id}/allocate**
```json
Request: { "locationId": 1 }
Response: { "status": "ALLOCATED", "fulfilmentLocationId": 1 }
Permissions: ORDER:ALLOCATE
```

### Refunds & Returns

**POST /returns**
```json
Request: { "orderId": 999, "reason": "damaged", "items": [{ "skuId": 123 }] }
Response: { "id": 777, "status": "REQUESTED", "returnLabel": "..." }
```

**GET /returns/{id}**
```
Response: { "id": 777, "status": "RECEIVED", "inspectionNotes": "..." }
```

**POST /refunds**
```json
Request: { "returnId": 777, "decision": "approve" }
Response: { "id": 888, "status": "PROCESSING", "amount": 99.99 }
Permissions: REFUND:PROCESS
```

### Discounts

**POST /discounts**
```json
Request: { "code": "SAVE10", "type": "percentage", "value": 10, "validFrom": "...", "validTo": "..." }
Response: { "id": 111, "code": "SAVE10", "status": "ACTIVE" }
Permissions: MARKETING:MANAGE
```

**GET /discounts**
```
Response: [{ "id": 111, "code": "SAVE10", "status": "ACTIVE" }]
```

**POST /discounts/validate**
```json
Request: { "code": "SAVE10", "cartTotal": 299.99 }
Response: { "valid": true, "discount": { "type": "percentage", "value": 10, "estimatedSavings": 30 } }
```

### AI Recommendations

**POST /recommendations/chat**
```json
Request: { "message": "I'm looking for winter jackets", "context": { "viewedProducts": [...], "cartItems": [...] } }
Response: { "message": "Here are some jackets I recommend", "products": [{ "id": 1, "name": "...", "reasoning": "..." }], "suggestedItems": [...] }
Permissions: (public)
```

### Instagram Integration

**POST /instagram/connect**
```json
Request: { "authorizationCode": "..." }
Response: { "connected": true, "instagramUserId": "123", "accountName": "@seshop" }
Permissions: MARKETING:MANAGE
```

**POST /instagram/drafts**
```json
Request: { "caption": "...", "images": [multipart file], "productIds": [1, 2, 3] }
Response: { "id": 222, "status": "DRAFT", "previewUrl": "...", "products": [...] }
Permissions: MARKETING:CREATE_DRAFT
```

**GET /instagram/drafts**
```
Response: [{ "id": 222, "caption": "...", "status": "DRAFT", "createdAt": "..." }]
```

### Admin Operations

**POST /admin/users**
```json
Request: { "username": "...", "email": "...", "userType": "STAFF" }
Response: { "id": 555, "username": "...", "email": "..." }
Permissions: USER:CREATE
```

**POST /admin/roles**
```json
Request: { "name": "Inventory Manager", "description": "...", "permissionIds": [1, 2, 3] }
Response: { "id": 666, "name": "Inventory Manager", "permissions": [...] }
Permissions: ROLE:CREATE
```

**GET /audit-logs**
```
Query params: page=1, limit=100, action=user_created, dateFrom=..., dateTo=...
Response: { "data": [{ "timestamp": "...", "actor": {...}, "action": "...", "target": {...} }], "total": 500 }
Permissions: AUDIT:VIEW
```

## Pagination

All list endpoints support pagination:

```
GET /products?page=2&limit=50

Response headers:
  X-Total-Count: 1000
  X-Page: 2
  X-Limit: 50
  X-Offset: 50
```

## Filtering & Sorting

Query parameters for filtering:

```
GET /orders?status=pending&status=confirmed&sortBy=-createdAt

Filters: comma-separated values or repeated params
Sorting: prefix with `-` for descending order
```

## Idempotency

Critical operations support idempotency keys:

```
POST /orders
Header: Idempotency-Key: uuid-v4

If the same key is used within 24 hours, the same response is returned without creating a duplicate order.
```

## Webhooks (External Services)

### Stripe Payment Webhook
```
POST /webhooks/stripe
Body: { "type": "charge.succeeded", "data": { "object": { "id": "...", "amount": 9999 } } }
```

Handles: payment confirmation, refund completion

### Instagram Webhook
```
POST /webhooks/instagram
Body: { "object": "instagram", "entry": [...] }
```

Handles: message received, follower events

## References

- [SESHOP API Spec (full)](../../docs/3.Design/SESHOP%20API%20Spec.md)
- [SESHOP SRS](../../docs/10.SRS/SESHOP%20SRS.md)
- [SESHOP HLD](../../docs/3.Design/SESHOP%20HLD.md)

