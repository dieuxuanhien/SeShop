# SeShop — Architecture Design Document (ADD)

**Project:** SeShop
**Domain:** Omnichannel clothing & accessories platform
**Standard:** SEI / Carnegie Mellon — Attribute-Driven Design 3.0
**Version:** 1.0
**Date:** 2026-04-30

---

## Revision History

| Date | Version | Author | Description |
|---|---:|---|---|
| 2026-04-30 | 1.0 | Architecture Team | Initial ADD using iterative decomposition from ASR |

---

## Table of Contents

1. [Purpose](#1-purpose)
2. [Design Inputs](#2-design-inputs)
3. [ADD Process Overview](#3-add-process-overview)
4. [Iteration 1 — Establish Overall System Structure](#4-iteration-1--establish-overall-system-structure)
5. [Iteration 2 — Address Reliability and Consistency](#5-iteration-2--address-reliability-and-consistency)
6. [Iteration 3 — Address Security and Auditability](#6-iteration-3--address-security-and-auditability)
7. [Iteration 4 — Address Performance and Scalability](#7-iteration-4--address-performance-and-scalability)
8. [Iteration 5 — Address Modifiability and Testability](#8-iteration-5--address-modifiability-and-testability)
9. [Design Summary](#9-design-summary)
10. [Verification Against ASR](#10-verification-against-asr)

---

## 1. Purpose

This document records the architectural design process for SeShop using the Attribute-Driven Design (ADD) method as defined by the Software Engineering Institute (SEI). ADD is an iterative decomposition method where each iteration selects a subset of quality attribute scenarios and produces design decisions that address them.

This document does **not** repeat the architecture itself — for the full architecture description, see the [SAD](SESHOP%20SAD.md). For quality attribute requirements, see the [ASR](SESHOP%20ASR.md). For detailed system decomposition, see the [HLD](SESHOP%20HLD.md) and [LLD](SESHOP%20LLD.md).

---

## 2. Design Inputs

### 2.1 Primary Functional Requirements

The system must support 27 use cases (UC1–UC27) across three actor classes (Customer, Authorized Staff, Super Admin), as defined in the [SRS](../10.SRS/SESHOP%20SRS.md). Key functional drivers include:

- Omnichannel commerce (online checkout + in-store POS)
- Multi-location inventory management with real-time stock synchronization
- Configurable RBAC with granular permissions
- External integrations (payment, shipping, Instagram, AI recommendations)
- Social marketing draft workflows

### 2.2 Quality Attribute Scenarios (from ASR)

The following scenarios from [SESHOP ASR.md](SESHOP%20ASR.md) are prioritized as design drivers:

| Priority | ID | Quality Attribute | Brief |
|---|---|---|---|
| Critical | QAS-3 | Reliability | Checkout consistency under concurrency |
| Critical | QAS-5 | Security | RBAC enforcement — zero privilege escalation |
| Critical | QAS-7 | Auditability | Immutable append-only audit trail |
| High | QAS-1 | Performance | Product search ≤ 2s (p95) |
| High | QAS-2 | Performance | Inventory update ≤ 500ms |
| High | QAS-9 | Modifiability | Module boundary independence |

### 2.3 Constraints

Constraints CON-1 through CON-10 from the [ASR](SESHOP%20ASR.md) Section 4 are binding throughout all iterations.

---

## 3. ADD Process Overview

ADD proceeds through iterative refinement:

```
Iteration 1: Decompose the system into coarse-grained elements
Iteration 2: Refine elements to address highest-priority quality attributes
Iteration 3–N: Continue refinement until all ASRs are addressed
```

Each iteration follows:
1. **Select element to decompose** — choose the element being refined
2. **Choose drivers** — select ASRs to address
3. **Choose design concepts** — select architectural patterns/tactics
4. **Instantiate elements** — map concepts to concrete design elements
5. **Record rationale** — document why each choice was made

---

## 4. Iteration 1 — Establish Overall System Structure

### 4.1 Element to Decompose

The entire SeShop system (greenfield).

### 4.2 Selected Drivers

- QAS-9 (Modifiability — Module Boundaries)
- QAS-3 (Reliability — Checkout Consistency)
- CON-1 through CON-4 (Technology and architecture style constraints)

### 4.3 Design Concepts Chosen

| Concept | Rationale |
|---|---|
| **Modular Monolith** (CON-4) | Single deployable unit reduces operational complexity while providing internal module boundaries. Cross-domain transactions (checkout touches cart + order + payment + inventory + shipment) are ACID-safe without distributed coordination. |
| **Domain-Driven Decomposition** | Modules are organized by business capability (Identity, Catalog, Inventory, Commerce, POS, Marketing, Engagement, Shared), not by technical layer. This aligns change frequency with business rule ownership. |
| **3-tier web topology** | React SPA frontend → Spring Boot REST API → PostgreSQL database. Clean separation of presentation, logic, and persistence. |

### 4.4 Instantiated Elements

| Element | Technology | Responsibility |
|---|---|---|
| React Frontend | React 18, TypeScript 5, Vite | Customer, Staff, and Admin UI |
| Java Backend API | Java 21, Spring Boot 3.3 | Business logic, security, orchestration |
| PostgreSQL Database | PostgreSQL 15 | System of record for all transactional data |
| Redis Cache | Redis | Read optimization for catalog and availability |
| Object Storage | S3-compatible | Product/review images, Instagram media |
| Async Worker | Spring @Async / scheduled jobs | Notifications, media processing, reports |

### 4.5 Domain Module Decomposition

Eight bounded contexts as defined in [HLD Section 7](SESHOP%20HLD.md):

1. **Identity & RBAC** — users, roles, permissions, audit
2. **Catalog** — products, variants, categories, images
3. **Inventory** — locations, balances, transfers, cycle counts, procurement
4. **Commerce** — carts, orders, payments, shipments, discounts
5. **POS & Returns** — shifts, receipts, refunds, exchanges, invoices
6. **Marketing & Social** — Instagram connections, drafts
7. **Customer Engagement** — reviews, AI recommendation chat
8. **Shared Platform Services** — audit dispatch, notifications, file storage, utilities

### 4.6 Inter-Module Communication Rule

Modules communicate through:
- **Application service interfaces** (synchronous, in-process method calls)
- **Domain events** (asynchronous, for side effects like notifications and audit)

Modules must **not** directly access each other's database tables.

### 4.7 Rationale

**Why not microservices?** Checkout (UC15) and POS (UC8) are cross-domain transactions touching 4–6 modules each. In a microservice architecture, these would require distributed sagas, eventual consistency, and compensating transactions — adding complexity disproportionate to the single-business scope. The modular monolith provides the same module isolation benefits without network overhead.

**Alternatives considered:** See [HLD Section 17](SESHOP%20HLD.md) (Trade-offs and Alternatives).

---

## 5. Iteration 2 — Address Reliability and Consistency

### 5.1 Element to Decompose

Commerce module (orders, payments, inventory reservation) and POS module.

### 5.2 Selected Drivers

- QAS-3 (Checkout consistency under concurrency) — **Critical**
- QAS-4 (99.9% monthly availability) — **High**
- QAS-14 (Graceful external failure handling) — **Medium**

### 5.3 Design Concepts Chosen

| Tactic | Addresses | Description |
|---|---|---|
| **ACID transactions** | QAS-3 | All stock-affecting writes (checkout, POS, transfer, refund) execute within a single database transaction. If any step fails, the entire operation rolls back. |
| **Pessimistic locking on inventory rows** | QAS-3 | `SELECT ... FOR UPDATE` on `inventory_balances` during reservation and decrement operations prevents concurrent oversell. |
| **Idempotency keys** | QAS-3, QAS-14 | Checkout, payment, refund, and transfer confirmation endpoints require an `Idempotency-Key` header. Retried requests produce the same result without duplication. |
| **Reservation with timeout** | QAS-3 | Stock is reserved in `reserved_qty` during checkout. If payment is not confirmed within 15 minutes, a background worker releases the reservation. |
| **Adapter + retry with bounded retries** | QAS-14 | External calls (Stripe, shipping, Instagram) are wrapped in adapters with configurable retry limits and exponential backoff. Failures return clear error codes without corrupting internal state. |
| **Outbox pattern for domain events** | QAS-4 | Domain events (e.g., `OrderPaid`, `StockReserved`) are written to an outbox table within the same transaction. A background poller delivers them, ensuring reliable event propagation. |
| **Health check endpoints** | QAS-4 | Liveness and readiness probes enable container orchestration to detect and replace unhealthy instances. |

### 5.4 Instantiated Design Rules

1. **Transaction boundaries** are defined in [HLD Section 9](SESHOP%20HLD.md) (Transaction Boundaries): checkout, payment confirmation, POS sale, transfer confirmation, refund, cycle count post, and invoice issuance.
2. **State machines** with explicit legal transitions for Order, Transfer, Draft, and Refund lifecycles as defined in [LLD — Detailed State Models](SESHOP%20LLD.md).
3. **No "fire and forget" for money/stock operations** — every mutation is either committed or rolled back within the request scope.

### 5.5 Rationale

The modular monolith (Iteration 1) enables ACID transactions across module boundaries using a single PostgreSQL connection. This is the primary architectural advantage over microservices for the reliability driver. The alternative (distributed sagas) was rejected because it introduces complexity that exceeds the team's operational capacity.

---

## 6. Iteration 3 — Address Security and Auditability

### 6.1 Element to Decompose

Identity & RBAC module, and cross-cutting security infrastructure.

### 6.2 Selected Drivers

- QAS-5 (RBAC enforcement — zero privilege escalation) — **Critical**
- QAS-6 (Sensitive data protection) — **High**
- QAS-7 (Immutable audit trail) — **Critical**

### 6.3 Design Concepts Chosen

| Tactic | Addresses | Description |
|---|---|---|
| **Permission-driven RBAC (not role-name-driven)** | QAS-5 | Authorization checks are performed against atomic permission codes (e.g., `inventory.adjust`), not role names. A user's effective permissions are the union of all assigned active roles. |
| **Server-side enforcement** | QAS-5 | Every API controller validates permissions via Spring Security filters. Frontend hiding is cosmetic only — the backend is authoritative. |
| **JWT with short-lived access tokens** | QAS-5, QAS-6 | Stateless authentication with configurable token TTL. Token revocation for critical role changes. |
| **Salted password hashing** | QAS-6 | Passwords stored using bcrypt with per-user salt. |
| **Token encryption at rest** | QAS-6 | Instagram OAuth tokens and external API keys encrypted in database using AES-256. |
| **TLS everywhere** | QAS-6 | All API traffic encrypted in transit. |
| **Append-only audit log** | QAS-7 | `audit_logs` table has no UPDATE or DELETE operations exposed at any layer. Service, API, and database constraints enforce append-only semantics. |
| **AOP-based audit capture** | QAS-7 | An `@Auditable` annotation and `AuditAspect` automatically capture actor, action, target, and metadata for annotated service methods. |

### 6.4 Instantiated Design Rules

1. **Sensitive operations requiring audit** are enumerated in [HLD Section 13](SESHOP%20HLD.md) (Sensitive Actions to Audit).
2. **Permission codes** follow a canonical `domain.action` format (e.g., `role.create`, `inventory.adjust`, `refund.process`) as listed in the [API Spec](SESHOP%20API%20Spec.md) Section 5.
3. **Access matrix** mapping functions to actor types is defined in [SRS Section 3.1](../10.SRS/SESHOP%20SRS.md).

### 6.5 Rationale

Permission-driven (not role-name-driven) authorization allows the business to create custom roles with arbitrary permission combinations without code changes. This directly supports BG-4 (secure governance) and QAS-5. The audit log is intentionally separated from technical application logs to prevent noise and ensure compliance reviewability.

---

## 7. Iteration 4 — Address Performance and Scalability

### 7.1 Element to Decompose

Catalog read path, Inventory query path, and data access patterns.

### 7.2 Selected Drivers

- QAS-1 (Product search ≤ 2s p95) — **High**
- QAS-2 (Inventory update ≤ 500ms) — **High**
- QAS-8 (Location/SKU growth without schema change) — **Medium**

### 7.3 Design Concepts Chosen

| Tactic | Addresses | Description |
|---|---|---|
| **Database indexing strategy** | QAS-1, QAS-2 | Composite indexes on high-frequency query paths: `(variant_id, location_id)` on `inventory_balances`, `(customer_user_id, created_at)` on `orders`, etc. Full index list in [schema.sql](../5.Database/SESHOP%20schema.sql). |
| **Redis caching for hot reads** | QAS-1 | Product catalog browsing and location availability use Redis as a read-through cache. Cache invalidation on product/inventory mutation. |
| **Computed availability (not stored)** | QAS-2, QAS-8 | `available_qty = on_hand_qty - reserved_qty` is computed at query time. This eliminates update races on a stored derived field and supports CON-7. |
| **Pagination on all list endpoints** | QAS-1 | Standard `page/size/sort` parameters prevent unbounded result sets. API convention defined in [API Spec](SESHOP%20API%20Spec.md) Section 8. |
| **Horizontal scaling at application tier** | QAS-8 | Stateless backend (JWT, no server-side sessions) allows multiple instances behind a load balancer. |
| **Normalized schema with extension-safe design** | QAS-8 | Adding new locations or SKUs is a data insert, not a schema change. The `locations` and `product_variants` tables use no hardcoded enum constraints on business-variable attributes. |

### 7.4 Rationale

The ≤ 2s product search target (QAS-1) is achievable with proper indexing and optional Redis caching. The modular monolith avoids the network latency that microservices would add to catalog queries that join products, variants, categories, and availability. Direct SQL joins on a single PostgreSQL instance outperform API composition patterns for this scale.

---

## 8. Iteration 5 — Address Modifiability and Testability

### 8.1 Element to Decompose

Internal structure of each backend module.

### 8.2 Selected Drivers

- QAS-9 (Module boundary independence) — **High**
- QAS-10 (External integration replacement) — **Medium**
- QAS-13 (Domain logic isolation for testing) — **Medium**

### 8.3 Design Concepts Chosen

| Tactic | Addresses | Description |
|---|---|---|
| **Hexagonal Architecture (Ports & Adapters) per module** | QAS-9, QAS-10, QAS-13 | Each module has four internal layers: `api` → `application` → `domain` → `infrastructure`. Domain layer has no dependency on infrastructure — it defines interfaces (ports) that infrastructure implements (adapters). |
| **Strategy pattern for business policies** | QAS-9, QAS-10 | Pricing, discount application, payment processing, allocation, and recommendation policies are implemented as strategy interfaces. Swapping a payment provider means implementing a new adapter — zero changes to domain. |
| **Domain events for cross-module side effects** | QAS-9 | When one module needs to trigger behavior in another (e.g., `OrderPaid` → send notification), it publishes a domain event. The consuming module subscribes independently. No direct service-to-service coupling. |
| **Repository pattern for data access** | QAS-13 | Domain services depend on repository interfaces, not JPA entities. Tests can provide in-memory implementations. |
| **Testcontainers for integration tests** | QAS-13 | Integration tests use Testcontainers to spin up PostgreSQL and Redis in Docker — no shared test database required. |

### 8.4 Module Internal Layer Dependency Rule

```
api → application → domain ← infrastructure
                       ↑
              (interfaces defined here,
               implemented by infrastructure)
```

The `domain` layer is the core with zero outward dependencies. `application` orchestrates domain services. `api` handles HTTP. `infrastructure` implements persistence and external adapters.

This is detailed in [LLD — Backend Package and Module Structure](SESHOP%20LLD.md) and [HLD Section 9](SESHOP%20HLD.md).

### 8.5 Rationale

Hexagonal architecture with the dependency inversion rule means that domain logic (the most business-critical and frequently changing code) is testable in isolation. Integration adapters (the most volatile external dependency) can be replaced without modifying business rules. This directly addresses the modifiability, testability, and integration-swap ASRs.

---

## 9. Design Summary

| Iteration | Focus | Key Decision | Primary Tactic |
|---|---|---|---|
| 1 | System Structure | Modular Monolith with 8 bounded contexts | Domain decomposition; 3-tier topology |
| 2 | Reliability | ACID transactions across modules; pessimistic locking | Transaction management; idempotency; outbox |
| 3 | Security & Audit | Permission-driven RBAC; append-only audit log | Least privilege; AOP audit; token encryption |
| 4 | Performance | Indexed queries; Redis caching; computed availability | Caching; indexing; pagination |
| 5 | Modifiability | Hexagonal architecture per module; strategy pattern | Ports & adapters; dependency inversion |

### Cross-Cutting Design Decisions

| Decision | Scope | Reference |
|---|---|---|
| REST API with `/api/v1` versioning | All modules | [API Spec](SESHOP%20API%20Spec.md) |
| Standard error envelope (`code`, `message`, `details`, `traceId`) | All modules | [API Spec](SESHOP%20API%20Spec.md) Section 7 |
| Structured logging with correlation IDs | All modules | [HLD Section 15](SESHOP%20HLD.md) |
| Flyway for migration management | Database | CON-8 |
| Vi/En localization | UI & messages | CON-10, [SRS Section 3.3](../10.SRS/SESHOP%20SRS.md) |

---

## 10. Verification Against ASR

| ASR | Addressed In | Design Decision | Verification Method |
|---|---|---|---|
| QAS-1 (Search ≤ 2s) | Iteration 4 | Indexing + Redis cache | Load test product search endpoint |
| QAS-2 (Inventory ≤ 500ms) | Iteration 4 | Direct SQL with indexes | Benchmark inventory adjustment latency |
| QAS-3 (Checkout consistency) | Iteration 2 | ACID + pessimistic locks + idempotency | Concurrent checkout stress test |
| QAS-4 (99.9% availability) | Iteration 2 | Health probes + horizontal scaling | Uptime monitoring |
| QAS-5 (RBAC enforcement) | Iteration 3 | Permission-driven auth, server-enforced | Penetration test; role-based API test suite |
| QAS-6 (Data protection) | Iteration 3 | Hashing, encryption, TLS | Security audit; credential scan |
| QAS-7 (Audit trail) | Iteration 3 | Append-only table + AOP | Verify no DELETE/UPDATE on `audit_logs`; coverage test |
| QAS-8 (Scalability) | Iteration 4 | Normalized schema; stateless app | Add location/SKU without migration |
| QAS-9 (Module boundaries) | Iteration 1 + 5 | Domain decomposition + hexagonal | Compile-time dependency analysis |
| QAS-10 (Integration swap) | Iteration 5 | Strategy + adapter pattern | Implement mock adapter; verify zero domain changes |
| QAS-11 (Staff UX) | Iteration 1 | Keyboard-first POS design | UX testing with trained staff |
| QAS-12 (Checkout UX) | Iteration 1 | Multi-step SPA flow | Usability testing |
| QAS-13 (Testability) | Iteration 5 | Repository interfaces + Testcontainers | Run domain tests without DB |
| QAS-14 (External failure) | Iteration 2 | Bounded retry + adapter | Fault injection testing |
