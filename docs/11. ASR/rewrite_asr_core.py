import sys

with open('/home/xuanhien/Documents/repo/KTPM/docs/11. ASR/SESHOP ASR.md', 'r') as f:
    lines = f.readlines()

header = ""
for line in lines:
    if line.startswith("## 5. Quality Attribute Scenarios"):
        break
    header += line

new_content = """## 5. Quality Attribute Scenarios

Each scenario follows the SEI 6-part quality attribute scenario format. This section focuses specifically on the 5 core architectural drivers of the SeShop system: Performance, Reliability, Security, Auditability, and Modifiability.

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
| **Response Measure** | 95th percentile response time <= 2 seconds |

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
| **Response Measure** | Commit latency <= 500 ms for standard operations |

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

### QAS-4: Security — RBAC Enforcement

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

### QAS-5: Security — Sensitive Data Protection

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

### QAS-6: Auditability — Immutable Audit Trail

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

### QAS-7: Modifiability — Module Boundary Independence

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

### QAS-8: Modifiability — External Integration Replacement

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

## 6. ASR Priority Matrix

Priority is assessed on two dimensions: **importance to business** (H/M/L) and **difficulty to achieve** (H/M/L).

| ID | Quality Attribute | Business Importance | Architecture Difficulty | Priority |
|---|---|---|---|---|
| QAS-3 | Reliability — Checkout Consistency | **H** | **H** | **Critical** |
| QAS-4 | Security — RBAC Enforcement | **H** | **M** | **Critical** |
| QAS-6 | Auditability — Immutable Audit Trail | **H** | **M** | **Critical** |
| QAS-1 | Performance — Product Search | **H** | **M** | **High** |
| QAS-2 | Performance — Inventory Update | **H** | **M** | **High** |
| QAS-5 | Security — Data Protection | **H** | **L** | **High** |
| QAS-7 | Modifiability — Module Boundaries | **H** | **H** | **High** |
| QAS-8 | Modifiability — Integration Swap | **M** | **M** | **Medium** |

---

## 7. Traceability

| ASR | BRD NFR | SRS Section | HLD Section |
|---|---|---|---|
| QAS-1 | NFR-01 | 3.2 | Quality Attribute Drivers |
| QAS-2 | — | 3.2 | Performance, Scalability, and Reliability |
| QAS-3 | NFR-02 | UC15 | Reliability Approach; Concurrency Considerations |
| QAS-4 | NFR-04 | 3.1 | Security Architecture |
| QAS-5 | NFR-04 | 3.1 | Security Architecture |
| QAS-6 | NFR-05 | UC4 | Security Architecture; Observability |
| QAS-7 | — | — | Domain Decomposition; Backend Architecture |
| QAS-8 | — | — | Integration Architecture |

"""

with open('/home/xuanhien/Documents/repo/KTPM/docs/11. ASR/SESHOP ASR.md', 'w') as f:
    f.write(header + new_content)

print("Rewrite successful.")
