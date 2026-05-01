# SeShop — Architecturally Significant Requirements (ASR)

**Project:** SeShop
**Domain:** Omnichannel clothing & accessories platform
**Standard:** SEI / Carnegie Mellon — Quality Attribute Workshop output
**Version:** 1.0
**Date:** 2026-04-30

---

## Revision History

| Date | Version | Author | Description |
|---|---:|---|---|
| 2026-04-30 | 1.0 | Architecture Team | Initial ASR derived from BRD and SRS |

---

## Table of Contents

1. [Purpose](#1-purpose)
2. [Source Documents](#2-source-documents)
3. [Business Goals](#3-business-goals)
4. [Architectural Constraints](#4-architectural-constraints)
5. [Quality Attribute Scenarios](#5-quality-attribute-scenarios)
6. [ASR Priority Matrix](#6-asr-priority-matrix)
7. [Traceability](#7-traceability)

---

## 1. Purpose

This document captures the Architecturally Significant Requirements (ASRs) for the SeShop platform using the SEI Quality Attribute Workshop method. An ASR is any requirement that has a measurable effect on the system's architecture.

This document does **not** repeat functional specifications. For use case definitions, see the [SRS](../10.SRS/SESHOP%20SRS.md). For business context, see the [BRD](../1.BRD/SESHOP%20BRD.md).

---

## 2. Source Documents

The ASRs in this document are derived from the following project artifacts:

| Document | Role |
|---|---|
| [SESHOP BRD](../1.BRD/SESHOP%20BRD.md) | Business goals, NFR definitions (NFR-01 through NFR-07) |
| [SESHOP SRS](../10.SRS/SESHOP%20SRS.md) | Functional use cases (UC1–UC27), access matrix, performance targets |
| [SESHOP HLD](SESHOP%20HLD.md) | Architecture drivers, quality attribute driver table |
| [SeShop Views Desc](../4.%20View%20descriptions/SeShop%20Views%20Desc.md) | UX performance and workflow expectations |

---

## 3. Business Goals

The following business goals drive architectural decisions. They are summarized here and defined in detail in [BRD Section 3](../1.BRD/SESHOP%20BRD.md).

| ID | Business Goal | Architecture Impact |
|---|---|---|
| BG-1 | Single source of truth for stock across all locations | Data model must enforce SKU-location uniqueness; no eventual consistency for stock |
| BG-2 | Prevent overselling during concurrent operations | Requires transactional isolation for checkout, POS, and transfer flows |
| BG-3 | Fast staff operational workflows | API latency targets; optimized UI for keyboard-driven dense forms |
| BG-4 | Secure governance through RBAC and audit | Permission-driven authorization; append-only audit log |
| BG-5 | Better customer conversion through discovery and recommendations | AI integration adapter; stock freshness for availability display |
| BG-6 | Marketing efficiency through Instagram workflows | External OAuth integration; compose-review-approve lifecycle |
| BG-7 | Maintainability through modular structure | Module boundaries; inter-module communication rules |

---

## 4. Architectural Constraints

These are non-negotiable constraints that limit the design space. They are established in the [BRD Section 2.4](../1.BRD/SESHOP%20BRD.md) and [IMPLEMENTATION_PLAN.md](../../IMPLEMENTATION_PLAN.md).

| ID | Constraint | Source |
|---|---|---|
| CON-1 | Backend must use Java 21 and Spring Boot 3.3 | Technology decision, BRD |
| CON-2 | Frontend must use React 18 + TypeScript 5 + Vite | Technology decision, BRD |
| CON-3 | Database must be PostgreSQL 15 | Technology decision, BRD |
| CON-4 | Architecture style is Modular Monolith (not microservices) | HLD decision |
| CON-5 | Payment integration limited to Stripe and Cash on Delivery | BRD scope |
| CON-6 | No auto-publish to Instagram; manual review gate required | SRS UC11 business rule |
| CON-7 | `available_qty` must be computed (`on_hand_qty - reserved_qty`), never stored | Schema design constraint |
| CON-8 | Flyway for database migrations | Technology decision |
| CON-9 | Single discount code per order in v1 | SRS UC10 business rule |
| CON-10 | UI must support Vietnamese and English localization | SRS Section 3.3 |

---

## 5. Quality Attribute Scenarios

Each scenario follows the SEI 6-part quality attribute scenario format:

**Source → Stimulus → Artifact → Environment → Response → Response Measure**

---

### QAS-1: Performance — Product Search Latency

| Part | Description |
|---|---|
| **Source** | Customer |
| **Stimulus** | Submits a product search/filter query with category, size, color, and price range |
| **Artifact** | Product catalog service and database |
| **Environment** | Normal operating conditions (typical daily load) |
| **Response** | System returns paginated, filtered product list with availability badges |
| **Response Measure** | 95th percentile response time ≤ 2 seconds |

**Traceability:** BRD NFR-01, SRS Section 3.2

---

### QAS-2: Performance — Inventory Update Latency

| Part | Description |
|---|---|
| **Source** | Authorized staff |
| **Stimulus** | Submits an inventory adjustment, transfer confirmation, or POS stock decrement |
| **Artifact** | Inventory service and `inventory_balances` table |
| **Environment** | Normal operating conditions |
| **Response** | Stock mutation is committed atomically and audit log written |
| **Response Measure** | Commit latency ≤ 500 ms for standard operations |

**Traceability:** SRS Section 3.2

---

### QAS-3: Reliability — Checkout Consistency

| Part | Description |
|---|---|
| **Source** | Customer |
| **Stimulus** | Completes checkout with payment (Stripe or COD) while another customer is simultaneously checking out the same SKU |
| **Artifact** | Commerce service (order, payment, inventory reservation) |
| **Environment** | Concurrent user access to limited stock |
| **Response** | Exactly one customer's order is created if only one unit is available; the other receives a clear "insufficient stock" error with no partial state |
| **Response Measure** | Zero oversell events; failed checkout leaves no orphaned orders or payment records |

**Traceability:** BRD NFR-02, BG-2, SRS UC15 business rules

---

### QAS-4: Reliability — System Availability

| Part | Description |
|---|---|
| **Source** | All users (customers, staff, admin) |
| **Stimulus** | Attempt to access core commerce functionality at any time |
| **Artifact** | SeShop platform (backend API + database) |
| **Environment** | Production deployment |
| **Response** | System processes the request normally |
| **Response Measure** | 99.9% monthly availability for core commerce services |

**Traceability:** BRD NFR-03, SRS Section 3.2

---

### QAS-5: Security — RBAC Enforcement

| Part | Description |
|---|---|
| **Source** | Authenticated staff user |
| **Stimulus** | Attempts to invoke an API endpoint for which their effective permissions (union of assigned roles) do not include the required permission code |
| **Artifact** | Authorization layer (Spring Security + RBAC service) |
| **Environment** | Normal operations |
| **Response** | System returns HTTP 403 with stable error code, logs the attempt, performs no side effects |
| **Response Measure** | 100% enforcement — no privilege escalation is possible through API, regardless of frontend hiding |

**Traceability:** BRD NFR-04, SRS Section 3.1, HLD Security Architecture

---

### QAS-6: Security — Sensitive Data Protection

| Part | Description |
|---|---|
| **Source** | External attacker or unauthorized internal access |
| **Stimulus** | Attempt to access passwords, payment tokens, Instagram OAuth tokens, or PII |
| **Artifact** | Database, API transport layer, external integration adapters |
| **Environment** | Production deployment |
| **Response** | Passwords stored as salted hashes; tokens encrypted at rest; all API traffic over TLS |
| **Response Measure** | Zero plaintext secrets in database or logs; TLS on all external-facing endpoints |

**Traceability:** BRD NFR-04, HLD Section 13 (Security Architecture)

---

### QAS-7: Auditability — Immutable Audit Trail

| Part | Description |
|---|---|
| **Source** | Authorized staff or automated process |
| **Stimulus** | Performs any sensitive operation (role assignment, inventory adjustment, refund, POS close, payment, Instagram connection change) |
| **Artifact** | Audit log subsystem (`audit_logs` table) |
| **Environment** | Normal operations |
| **Response** | An append-only audit record is created containing actor, action, target, timestamp, and before/after metadata |
| **Response Measure** | 100% coverage of sensitive operations listed in HLD Section 13; no delete or update operations available on audit records |

**Traceability:** BRD NFR-05, SRS UC4, HLD Section 13

---

### QAS-8: Scalability — Location and SKU Growth

| Part | Description |
|---|---|
| **Source** | Business operations team |
| **Stimulus** | Adds new store or storage locations and new product SKUs as business grows |
| **Artifact** | Data model and inventory system |
| **Environment** | Normal operations; no planned downtime |
| **Response** | New locations and SKUs are added through normal operational UI/API without requiring schema migrations or code changes |
| **Response Measure** | Zero schema changes required for adding locations/SKUs; system performance remains within QAS-1 and QAS-2 targets after growth |

**Traceability:** BRD NFR-06

---

### QAS-9: Modifiability — Module Boundary Independence

| Part | Description |
|---|---|
| **Source** | Development team |
| **Stimulus** | Needs to change business rules in one domain module (e.g., new discount rule, modified return policy) |
| **Artifact** | Backend module structure |
| **Environment** | Development and deployment |
| **Response** | Changes are confined to the affected module's domain and application layers; no code changes required in other modules |
| **Response Measure** | Blast radius of a single business rule change is limited to one module; no cross-module recompilation or redeployment required beyond the shared interface |

**Traceability:** BG-7, HLD Section 6 (Domain Decomposition), HLD Section 9 (Backend Module Interaction Rules)

---

### QAS-10: Modifiability — External Integration Replacement

| Part | Description |
|---|---|
| **Source** | Business decision |
| **Stimulus** | Business decides to switch payment provider (e.g., from Stripe to another) or shipping carrier |
| **Artifact** | Integration adapters in infrastructure layer |
| **Environment** | Development |
| **Response** | A new adapter is implemented against the existing provider interface; no domain or application layer changes required |
| **Response Measure** | Integration swap requires changes only in the `infrastructure` package of the affected module; zero changes to domain services, API controllers, or database schema |

**Traceability:** HLD Section 12 (Integration Architecture), HLD Section 6 (Adapter pattern)

---

### QAS-11: Usability — Staff Workflow Efficiency

| Part | Description |
|---|---|
| **Source** | Trained staff member |
| **Stimulus** | Performs a standard POS sale transaction (scan items, apply discount, process payment, print receipt) |
| **Artifact** | Staff-facing frontend (POS module) |
| **Environment** | Normal store operations |
| **Response** | Transaction completes end-to-end without requiring mouse interaction for common flows |
| **Response Measure** | A trained cashier can complete a 3-item POS sale in ≤ 60 seconds using keyboard-primary interaction |

**Traceability:** BRD NFR-07, Views Description STAFF_007, HLD Section 10 (UX Notes)

---

### QAS-12: Usability — Customer Checkout Friction

| Part | Description |
|---|---|
| **Source** | Customer |
| **Stimulus** | Proceeds from cart to order confirmation |
| **Artifact** | Customer-facing frontend (checkout flow) |
| **Environment** | Normal conditions, desktop or mobile |
| **Response** | Multi-step checkout with real-time validation, immediate error feedback, and clear progress indication |
| **Response Measure** | Checkout requires ≤ 5 steps with no page reloads between steps; validation errors appear within 500ms of field blur |

**Traceability:** Views Description CUST_005, HLD Section 10 (UX Notes)

---

### QAS-13: Testability — Domain Logic Isolation

| Part | Description |
|---|---|
| **Source** | QA/Development team |
| **Stimulus** | Needs to unit test a domain service (e.g., inventory reservation logic) without external dependencies |
| **Artifact** | Domain layer of any module |
| **Environment** | CI/CD test pipeline |
| **Response** | Domain services are testable with in-memory mocks; no database, network, or filesystem access required |
| **Response Measure** | 80%+ code coverage on domain and application layers; all domain tests execute without container startup |

**Traceability:** HLD Section 6 (Hexagonal Architecture), HLD Quality Attribute Drivers (Testability)

---

### QAS-14: Reliability — Graceful External Failure Handling

| Part | Description |
|---|---|
| **Source** | External system (payment gateway, shipping provider, AI service, Instagram API) |
| **Stimulus** | External system is unavailable or returns an error |
| **Artifact** | Integration adapters |
| **Environment** | Production, degraded external services |
| **Response** | System returns a clear error to the user; retries are bounded and idempotent; no inconsistent internal state is created |
| **Response Measure** | Zero data corruption from external failures; user receives actionable error message within 5 seconds of timeout |

**Traceability:** HLD Section 12 (Integration Rules), HLD Section 19 (Risks — External API failure)

---

## 6. ASR Priority Matrix

Priority is assessed on two dimensions: **importance to business** (H/M/L) and **difficulty to achieve** (H/M/L).

| ID | Quality Attribute | Business Importance | Architecture Difficulty | Priority |
|---|---|---|---|---|
| QAS-3 | Reliability — Checkout Consistency | **H** | **H** | **Critical** |
| QAS-5 | Security — RBAC Enforcement | **H** | **M** | **Critical** |
| QAS-7 | Auditability — Immutable Audit Trail | **H** | **M** | **Critical** |
| QAS-1 | Performance — Product Search | **H** | **M** | **High** |
| QAS-2 | Performance — Inventory Update | **H** | **M** | **High** |
| QAS-4 | Reliability — Availability | **H** | **M** | **High** |
| QAS-6 | Security — Data Protection | **H** | **L** | **High** |
| QAS-9 | Modifiability — Module Boundaries | **H** | **H** | **High** |
| QAS-8 | Scalability — Location/SKU Growth | **M** | **L** | **Medium** |
| QAS-10 | Modifiability — Integration Swap | **M** | **M** | **Medium** |
| QAS-11 | Usability — Staff Efficiency | **M** | **M** | **Medium** |
| QAS-12 | Usability — Checkout Friction | **M** | **L** | **Medium** |
| QAS-13 | Testability — Domain Isolation | **M** | **M** | **Medium** |
| QAS-14 | Reliability — External Failure | **M** | **M** | **Medium** |

---

## 7. Traceability

| ASR | BRD NFR | SRS Section | HLD Section |
|---|---|---|---|
| QAS-1 | NFR-01 | 3.2 | Quality Attribute Drivers |
| QAS-2 | — | 3.2 | Performance, Scalability, and Reliability |
| QAS-3 | NFR-02 | UC15 | Reliability Approach; Concurrency Considerations |
| QAS-4 | NFR-03 | 3.2 | Performance, Scalability, and Reliability |
| QAS-5 | NFR-04 | 3.1 | Security Architecture |
| QAS-6 | NFR-04 | 3.1 | Security Architecture |
| QAS-7 | NFR-05 | UC4 | Security Architecture; Observability |
| QAS-8 | NFR-06 | — | Data Architecture |
| QAS-9 | — | — | Domain Decomposition; Backend Architecture |
| QAS-10 | — | — | Integration Architecture |
| QAS-11 | NFR-07 | — | Frontend Architecture (UX Notes) |
| QAS-12 | — | UC15 | Frontend Architecture (UX Notes) |
| QAS-13 | — | — | Quality Attribute Drivers (Testability) |
| QAS-14 | — | — | Integration Architecture; Risks |
