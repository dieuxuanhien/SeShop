# SESHOP - KẾ HOẠCH TRIỂN KHAI BACKEND & FRONTEND

**Dự án**: SeShop - Nền tảng bán lẻ Omnichannel cho quần áo và phụ kiện  
**Ngày**: 2026-04-29  
**Kiến trúc**: Modular Monolith  
**Backend**: Java + Spring Boot + PostgreSQL  
**Frontend**: React + TypeScript + Vite

---

## 📋 MỤC LỤC
1. [Tổng quan dự án](#tổng-quan-dự-án)
2. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
3. [Kế hoạch Backend](#kế-hoạch-backend)
4. [Kế hoạch Frontend](#kế-hoạch-frontend)
5. [Timeline triển khai](#timeline-triển-khai)
6. [Công nghệ và Công cụ](#công-nghệ-và-công-cụ)

---

## 🎯 TỔNG QUAN DỰ ÁN

### Phạm vi và nguồn sự thật (Single Source of Truth)
- **Phạm vi kinh doanh & ngoài phạm vi**: xem [docs/1.BRD/SESHOP BRD.md](docs/1.BRD/SESHOP%20BRD.md).
- **Yêu cầu chức năng & phi chức năng**: xem [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md).
- **Thiết kế kiến trúc**: xem [docs/3.Design/SESHOP HLD.md](docs/3.Design/SESHOP%20HLD.md).
- **Thiết kế chi tiết**: xem [docs/3.Design/SESHOP LLD.md](docs/3.Design/SESHOP%20LLD.md).
- **API chuẩn**: xem [docs/3.Design/SESHOP API Spec.md](docs/3.Design/SESHOP%20API%20Spec.md).
- **Giao diện**: xem [docs/4. View descriptions/SeShop Views Desc.md](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md).
- **Dữ liệu & schema**: xem [docs/5.Database/SESHOP Data Dictionary.md](docs/5.Database/SESHOP%20Data%20Dictionary.md) và [docs/5.Database/SESHOP schema.sql](docs/5.Database/SESHOP%20schema.sql).

---

## 📁 CẤU TRÚC THƯ MỤC

Xem cấu trúc dự án trong:
- [backend/README.md](backend/README.md)
- [frontend/README.md](frontend/README.md)

---

## 🔧 KẾ HOẠCH BACKEND

### Phase 1: Setup & Foundation (Tuần 1-2)
**Deliverables**:
- Khởi tạo Spring Boot project và cấu hình PostgreSQL
- Flyway migrations, cấu hình môi trường, logging cơ bản
- Security nền tảng (JWT), chuẩn hóa error model, audit logging

### Phase 2: Core Domains - Part 1 (Tuần 2-3)
**Modules**: Identity & RBAC, Catalog, Inventory, Shared Infrastructure

**SRS coverage**: UC1–UC7, UC16, UC22, UC25

**References**:
- API endpoints: [docs/3.Design/SESHOP API Spec.md](docs/3.Design/SESHOP%20API%20Spec.md)
- Module design: [docs/3.Design/SESHOP LLD.md](docs/3.Design/SESHOP%20LLD.md)
- Data model: [docs/5.Database/SESHOP Data Dictionary.md](docs/5.Database/SESHOP%20Data%20Dictionary.md)

**Deliverables**:
- Identity, catalog, and inventory modules with transactional rules and audit logging
- Shared exception/error model and pagination/filtering utilities

### Phase 3: Core Domains - Part 2 (Tuần 4-5)
**Modules**: Commerce, Orders, POS, Returns/Refunds

**SRS coverage**: UC8–UC9, UC12–UC20, UC24, UC26–UC27

**References**:
- API endpoints: [docs/3.Design/SESHOP API Spec.md](docs/3.Design/SESHOP%20API%20Spec.md)
- Module design: [docs/3.Design/SESHOP LLD.md](docs/3.Design/SESHOP%20LLD.md)
- Data model: [docs/5.Database/SESHOP Data Dictionary.md](docs/5.Database/SESHOP%20Data%20Dictionary.md)

**Deliverables**:
- Cart/checkout, order lifecycle, POS, returns/refunds, and invoicing flows
- Payment and shipment integration with audit-safe operations

### Phase 4: Advanced Features (Tuần 6-7)
**Modules**: Marketing/Discounts, Instagram, AI Recommendation

**SRS coverage**: UC10–UC11, UC14, UC21

**References**:
- API endpoints: [docs/3.Design/SESHOP API Spec.md](docs/3.Design/SESHOP%20API%20Spec.md)
- Module design: [docs/3.Design/SESHOP LLD.md](docs/3.Design/SESHOP%20LLD.md)

**Deliverables**:
- Discount lifecycle, Instagram connection/draft flows, AI recommendation adapter

### Phase 5: Testing & Quality (Tuần 7-8)
**Deliverables**:
- Unit tests, integration tests, and API documentation (OpenAPI)
- Performance testing and stability checks

---

## 🎨 KẾ HOẠCH FRONTEND

### Phase 1: Setup & Foundation (Tuần 1)
**Deliverables**:
- Vite + React + TypeScript setup
- Tailwind CSS setup
- Routing skeleton
- API client + auth context
- State management baseline

### Phase 2: Customer Portal (Tuần 1-2)
**Views**: CUST_001–CUST_009

**References**:
- View definitions: [docs/4. View descriptions/SeShop Views Desc.md](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- SRS use cases: [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md)

**Deliverables**:
- Customer browse, cart, checkout, order tracking, profile, review, and AI chat flows

### Phase 3: Staff Portal (Tuần 2-3)
**Views**: STAFF_001–STAFF_010

**References**:
- View definitions: [docs/4. View descriptions/SeShop Views Desc.md](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- SRS use cases: [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md)

**Deliverables**:
- Inventory, transfer, procurement, order management, POS, returns, discounts, and Instagram workflows

### Phase 4: Admin Portal (Tuần 3-4)
**Views**: ADMIN_001–ADMIN_005

**References**:
- View definitions: [docs/4. View descriptions/SeShop Views Desc.md](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- SRS use cases: [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md)

**Deliverables**:
- Admin dashboard, user/role management, audit logs, system settings

### Phase 5: Instagram Integration (Tuần 4)
**Views**: STAFF_009–STAFF_010

**References**:
- View definitions: [docs/4. View descriptions/SeShop Views Desc.md](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md)
- SRS use cases: [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md)

**Deliverables**:
- Instagram connect/reconnect and draft compose with review flow

### Phase 6: Shared Components & Utilities (Tuần 4-5)
**Deliverables**:
- Reusable UI components, hooks, utilities, and type definitions

### Phase 7: Testing & Polish (Tuần 5)
**Deliverables**:
- Component tests (unit + UI)
- E2E tests
- Performance and accessibility verification

---

## 📅 TIMELINE TRIỂN KHAI

```
TUẦN 1: Backend Setup + Frontend Setup + Customer Auth
├─ Backend: Spring Boot, DB, Security, Auth APIs
├─ Frontend: Vite setup, Routing, Auth UI, API client
└─ Deliverable: Login/Register working

TUẦN 2: Core Domain Modules + Customer Portal
├─ Backend: Auth, Catalog, Inventory Phase 1
├─ Frontend: Product Catalog, Cart, Search
└─ Deliverable: Browse products, add to cart

TUẦN 3: Commerce & Order + Staff Portal
├─ Backend: Commerce, Order, POS APIs
├─ Frontend: Checkout, Staff inventory UI
└─ Deliverable: Checkout flow, staff dashboard

TUẦN 4: Advanced Features + Admin Portal
├─ Backend: Discount, Instagram, AI APIs
├─ Frontend: Instagram draft, Admin management
└─ Deliverable: Discount codes, Instagram connect

TUẦN 5: Testing, Polish, Deployment
├─ Backend: Tests, API docs, optimization
├─ Frontend: Component tests, E2E, optimization
└─ Deliverable: Production-ready v1.0
```

---

## 🛠️ CÔNG NGHỆ & CÔNG CỤ

### Backend Stack
| Category | Technology |
|----------|-----------|
| Framework | Spring Boot 3.3 |
| Language | Java 21 |
| Database | PostgreSQL 15 |
| ORM | Spring Data JPA |
| API | REST + OpenAPI/Swagger |
| Migration | Flyway |
| Testing | JUnit 5 + Mockito + TestContainers |

### Frontend Stack
| Category | Technology |
|----------|-----------|
| Framework | React 18 |
| Language | TypeScript 5 |
| Build Tool | Vite |
| Styling | Tailwind CSS 3 |
| Routing | React Router 6 |
| State Mgmt | Redux Toolkit / Zustand |
| HTTP Client | Axios |
| Data Fetching | React Query / SWR |
| Form | React Hook Form |
| Validation | Zod / Yup |
| Testing | Vitest + React Testing Library |
| E2E Testing | Cypress / Playwright |
| Component Lib | Headless UI + Radix UI |

### DevOps & Tools
| Tool | Purpose |
|------|---------|
| Docker | Containerization |
| Docker Compose | Local development |
| Git | Version control |
| GitHub Actions | CI/CD |
| Sonarqube | Code quality |
| Postman | API testing |
| VS Code | IDE |
| IntelliJ IDEA | Backend IDE |

---

## 📝 KỲ TIẾP THEO

1. **Tuần 1**: Khởi tạo cấu trúc backend & frontend
2. **Tuần 2-3**: Phát triển core APIs
3. **Tuần 4-5**: Xây dựng UI interfaces
4. **Tuần 6-7**: Integration testing & QA
5. **Tuần 8**: Deployment & Documentation

---

## 📞 THAM KHẢO

- [docs/1.BRD/SESHOP BRD.md](docs/1.BRD/SESHOP%20BRD.md)
- [docs/10.SRS/SESHOP SRS.md](docs/10.SRS/SESHOP%20SRS.md)
- [docs/3.Design/SESHOP HLD.md](docs/3.Design/SESHOP%20HLD.md)
- [docs/3.Design/SESHOP LLD.md](docs/3.Design/SESHOP%20LLD.md)
- [docs/3.Design/SESHOP API Spec.md](docs/3.Design/SESHOP%20API%20Spec.md)
- [docs/5.Database/SESHOP schema.sql](docs/5.Database/SESHOP%20schema.sql)