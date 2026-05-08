# SESHOP - IMPLEMENTATION PLAN & PROGRESS TRACKER

This document serves as the active progress tracker for the SeShop project. It is continuously updated by the AI workflow.

## 🎯 Backend - Các Domain & Modules

The implementation now follows a Spring Boot modular monolith structure. `SeShopApplication` is the root package entrypoint at `com.seshop`, while direct sub-packages represent modules.

Current modules:
1. **identity** - Authentication & RBAC (UC1-4, UC12)
2. **audit** - Audit logs and admin dashboard support (UC4)
3. **catalog** - Product Management (UC5, UC13)
4. **inventory** - Stock Management (UC6, UC7, UC16, UC22, UC25)
5. **commerce** - Shopping, checkout, orders, invoices (UC15, UC17, UC19, UC20, UC23, UC27)
6. **pos** - Point of Sale, returns, shifts (UC8, UC9, UC24, UC26)
7. **refund** - Return/refund API boundary
8. **marketing** - Discounts, Instagram, AI recommendations (UC10, UC11, UC14, UC21)
9. **review** - Reviews & ratings (UC18)
10. **payment** - Payment provider integration
11. **shipping** - Shipment provider integration
12. **notification** - Notification extension point
13. **shared** - Cross-cutting API/config/security/exception/util code

Folder structure rules are documented in [docs/3.Design/SESHOP Folder Structure.md](docs/3.Design/SESHOP%20Folder%20Structure.md).

## 📊 Use Case Mapping & Status

| Use Case | Endpoints | Status |
|----------|-----------|--------|
| UC1: Create Role | `POST /api/v1/admin/roles` | ✅ Phase 1 |
| UC2: Assign Permission | `POST /api/v1/admin/roles/{id}/permissions` | ✅ Phase 1 |
| UC3: Assign Role | `POST /api/v1/admin/users/{id}/roles` | ✅ Phase 1 |
| UC4: Audit Logs | `GET /api/v1/admin/audit-logs` | ✅ Phase 1 |
| UC5: Add Products | `POST /api/v1/staff/products` | ✅ Phase 2 |
| UC6: Adjust Inventory | `POST /api/v1/staff/inventory/adjustments` | ✅ Phase 2 |
| UC7: Transfer Stock | `POST /api/v1/staff/inventory/transfers` | ✅ Phase 2 |
| UC8: POS Sale | `POST /api/v1/pos/receipts` | ✅ Phase 3 |
| UC9: Process Refund | `POST /api/v1/refunds` | ✅ Phase 3 |
| UC10: Discount Codes | `POST /api/v1/staff/discounts` | ✅ Phase 4 |
| UC11: Instagram Draft | `POST /api/v1/marketing/drafts` | ✅ Phase 4 |
| UC12: Register | `POST /api/v1/auth/register` | ✅ Phase 1 |
| UC13: Browse Products | `GET /api/v1/products` | ✅ Phase 2 |
| UC14: AI Chat | `POST /api/v1/assistant/recommendations` | ✅ Phase 4 |
| UC15: Checkout | `POST /api/v1/checkout` | ✅ Phase 3 |
| UC16: View Stock | `GET /api/v1/products/{productId}/availability` | ✅ Phase 2 |
| UC17: Track Shipment | `GET /api/v1/orders/{orderId}` | ✅ Phase 3 |
| UC18: Leave Review | `POST /api/v1/reviews` | ✅ Phase 3 |
| UC19: Pending Orders | `GET /api/v1/staff/orders` | ✅ Phase 3 |
| UC20: Mark Shipped | `POST /api/v1/staff/orders/{orderId}/ship` | ✅ Phase 3 |
| UC21: Instagram Connect | `POST /api/v1/marketing/instagram/connect` | ✅ Phase 4 |
| UC22: Purchase Orders | `POST /api/v1/staff/purchase-orders` | ✅ Phase 5 |
| UC23: Allocate Order | `POST /api/v1/staff/orders/{orderId}/allocate` | ✅ Phase 3 |
| UC24: Return Intake | `POST /api/v1/returns` | ✅ Phase 3 |
| UC25: Cycle Count | `POST /api/v1/staff/cycle-counts` | ✅ Phase 2 |
| UC26: Shift Close | `POST /api/v1/pos/shifts/{shiftId}/close` | ✅ Phase 3 |
| UC27: Tax Invoice | `POST /api/v1/invoices/tax` | ✅ Phase 3 |

## 📅 Timeline Chi Tiết

**TUẦN 1 (3-4 ngày work) - HOÀN THÀNH ✅**
- Backend: Spring Boot setup, PostgreSQL, JWT, Auth APIs
- Frontend: Vite setup, React Router, Tailwind, Login/Register UI

**TUẦN 2-3 - ĐANG TIẾN HÀNH 🔄**
- Backend: Catalog APIs, Inventory APIs
- Frontend: Product Catalog, Shopping Cart

**TUẦN 3-4 (HOÀN THÀNH) ✅**
- Backend: Commerce/Order APIs, POS APIs
- Frontend: Checkout, Staff Portal

**TUẦN 5 - CHƯA BẮT ĐẦU ⏳**
- Backend: Advanced APIs (Discount, Instagram, AI), Testing
- Frontend: Admin Portal, Instagram Integration

**TUẦN 6-8 - CHƯA BẮT ĐẦU ⏳**
- Testing, Polish, Deployment, Go-live ready

## 📝 Active Tasks

### 3. Backend Development (Weeks 1-3)
- [x] Initialize Spring Boot project
- [x] Setup database & migrations
- [x] Implement Core Modules
  - [x] Auth Module
  - [x] Catalog Module
  - [x] Inventory Module

### 4. Frontend Development (Weeks 1-3)
- [x] Initialize React + Vite project
- [x] Setup routing & state management
- [x] Build UI for authentication & catalog

### 5. Backend Development (Weeks 3-4)
- [x] Implement Commerce Module (Orders, Checkout)
- [x] Implement POS Module (Receipts, Returns, Shifts)

### 6. Frontend Development (Weeks 3-4)
- [x] Implement Checkout UI
- [x] Implement Staff Portal (Orders, Inventory, POS)

### 7. Backend Development (Week 5)
- [x] Implement Advanced APIs (Discount, Instagram, AI)
  - [x] Add discount usage validation and stronger request checks
  - [x] Enforce Instagram draft state rules and connection checks
  - [x] Validate AI recommendation input
- [x] Implement Purchase Orders APIs
  - [x] Create purchase order and goods receipt endpoints
  - [x] Update inventory balances on goods receipt

### 8. Frontend Development (Week 5)
- [x] Implement Admin Portal
  - [x] Build dashboard, user/role, locations, audit, and settings shells
  - [x] Align admin navigation routes
- [x] Implement Instagram Integration
  - [x] Build Instagram drafts gallery and composer shell
  - [x] Build Instagram connection status and permissions shell

### 9. Integration Testing (Weeks 4-5)
- [x] API testing
  - [x] Add backend API contract tests for staff inventory and POS endpoints
- [x] Component testing
  - [x] Add checkout component test for live cart, discount validation, and order submission flow
  - [x] Add POS component test for SKU lookup and cash-sale receipt flow
- [ ] E2E testing
- [x] Frontend mock API cleanup for catalog, checkout, and staff order flows
- [x] Complete live backend endpoints for staff inventory and POS flows
  - [x] Align POS receipt and shift persistence with Flyway schema
  - [x] Decrement inventory balances when POS receipts are created
  - [x] Persist cash reconciliation data when shifts close

### 10. Deployment (Week 8)
- [ ] Docker build & push
- [ ] Production deployment
- [ ] Monitoring & observability
