# SeShop Implementation Agents & Skills Manifest

**Generated**: 2026-04-30  
**Project**: SeShop - Omnichannel Clothing & Accessories Platform  
**Purpose**: Comprehensive agent and skill system for full-stack implementation

---

## 🎯 Quick Navigation

### **Start Here**
1. **New to the project?** → Read [copilot-instructions.md](copilot-instructions.md)
2. **Need implementation guidance?** → Choose an agent below
3. **Need domain knowledge?** → Choose a skill below
4. **Checking documentation quality?** → See [DOCUMENTATION_DEEP_CHECK_REPORT.md](DOCUMENTATION_DEEP_CHECK_REPORT.md)

---

## 📋 Implementation Agents

### 1. **SeShop Backend Implementation Agent**
**File**: [.github/agents/seshop-backend-impl.agent.md](.github/agents/seshop-backend-impl.agent.md)

**When to use**:
- Implementing backend phases 1-5
- Building Spring Boot modules
- Designing domain services
- Creating database migratiíons
- Implementing REST APIs

**Key Features**:
- Phase-by-phase guidance (Weeks 1-8)
- SRS use case mapping to implementation tasks
- LLD design pattern guidance
- Database schema alignment
- API endpoint specification

**Example Invocations**:
```
"Phase 1 backend setup: initialize Spring Boot, PostgreSQL, JWT"
"Implement UC15 checkout and payment workflow"
"Design Identity & RBAC module for backend"
"Create database migrations for product catalog"
"Set up REST endpoints for inventory management"
```

**Scope**: Phases 1-5, Java/Spring Boot, SQL, REST API design

---

### 2. **SeShop Frontend Implementation Agent**
**File**: [.github/agents/seshop-frontend-impl.agent.md](.github/agents/seshop-frontend-impl.agent.md)

**When to use**:
- Implementing frontend phases 1-4
- Building React components
- Setting up state management
- Creating UI views/pages
- Implementing workflows

**Key Features**:
- View code (e.g., ADMIN_001) to component mapping
- React component architecture guidance
- State management patterns (Redux/Context)
- API integration patterns
- Role-based UI routing

**Example Invocations**:
```
"Implement ADMIN_001 Dashboard Overview page"
"Build Product Cart component with Redux state"
"Set up checkout workflow with Stripe integration"
"Create role-based UI routing for admin/staff/customer"
"Implement AI recommendation chat widget"
```

**Scope**: Phases 1-4, React/TypeScript, Vite, UI/UX

---

### 3. **SeShop QA & Testing Agent**
**File**: [.github/agents/seshop-qa-testing.agent.md](.github/agents/seshop-qa-testing.agent.md)

**When to use**:
- Planning QA strategy
- Designing test cases
- Setting up test automation
- Testing SRS use cases
- Verifying API/UI workflows

**Key Features**:
- Test case design from SRS use cases
- Backend testing strategy (unit/integration/E2E)
- Frontend testing patterns
- Coverage metrics and targets
- Quality assurance checklists

**Example Invocations**:
```
"Design test cases for UC15 checkout workflow"
"Set up JUnit integration tests for order service"
"Create E2E tests with Cypress for customer journey"
"Test API endpoints against SESHOP API Spec"
"Calculate code coverage for Phase 2 modules"
```

**Scope**: Test strategy, test case design, automation, quality metrics

---

### 4. **SeShop Project Orchestrator**
**File**: [.github/agents/seshop-orchestrator.agent.md](.github/agents/seshop-orchestrator.agent.md)

**When to use**:
- Planning overall implementation timeline
- Coordinating between teams
- Tracking phase completion
- Managing dependencies
- Conducting project checkpoints

**Key Features**:
- Phase checkpoints with delivery criteria
- Inter-team dependency matrix
- Cross-team sync format
- Risk management and escalation
- Go-live readiness checklist

**Example Invocations**:
```
"Phase 2 checkpoint review: Is backend ready for frontend integration?"
"Check dependencies: What's blocking the frontend team?"
"Team sync agenda for week 3"
"Go-live checklist: Are we ready to deploy?"
"Risk assessment: Is performance on target?"
```

**Scope**: Project coordination, timeline management, team sync

---

### 5. **SeShop Doc SSoT Guardian** (Pre-existing)
**File**: [.github/agents/seshop-doc-ssot.agent.md](.github/agents/seshop-doc-ssot.agent.md)

**When to use**:
- Ensuring documentation consistency
- Resolving document mismatches
- Removing duplication
- Enforcing single source of truth

**Key Features**:
- Documentation alignment checks
- Duplicate removal
- Reference consistency validation
- Impact analysis for doc changes

**Example Invocations**:
```
"Check if API Spec matches LLD endpoints"
"Resolve scope boundary inconsistencies between BRD and SRS"
"Create traceability matrix for use cases"
"Update documentation after API change"
```

**Scope**: Documentation governance, consistency, SSoT

---

## 🧠 Skills (Domain Knowledge)

### 1. **Backend Architecture Skill**
**File**: [.github/skills/backend-architecture/SKILL.md](.github/skills/backend-architecture/SKILL.md)

**When to use**:
- Understanding Spring Boot module structure
- Designing domain services
- Implementing transactional flows
- Querying LLD architecture details

**Content**:
- Module structure and package organization
- Design patterns (service layer, repository, events, audit logging, state machines)
- Database design principles
- Transactional boundaries
- Dependency management
- Security and performance optimization

**Key Concepts**:
- Service Layer Pattern
- Repository Pattern
- Event-Driven Updates
- Audit Logging (Append-Only)
- State Machines (Orders, Returns, Transfers)
- Transactional Consistency vs Eventual Consistency

---

### 2. **Frontend Architecture Skill**
**File**: [.github/skills/frontend-architecture/SKILL.md](.github/skills/frontend-architecture/SKILL.md)

**When to use**:
- Understanding React component hierarchy
- Designing state management
- Integrating backend APIs
- Querying view specifications

**Content**:
- Directory structure and file organization
- Component design patterns (page, reusable, forms, tables)
- Custom hooks for API calls
- Redux state management patterns
- API integration patterns
- Role-based UI routing
- Performance optimization
- Error handling

**Key Concepts**:
- Page Components (Route-Level)
- Reusable Components
- Custom Hooks for API Calls
- Redux Slices
- API Integration Pattern
- Code Splitting and Memoization
- Responsive Design Breakpoints

---

### 3. **API Specification Skill**
**File**: [.github/skills/api-specification/SKILL.md](.github/skills/api-specification/SKILL.md)

**When to use**:
- Designing REST endpoints
- Validating request/response formats
- Checking HTTP status codes and error codes
- Implementing API security

**Content**:
- API design principles
- Authentication and authorization
- Common response structures
- HTTP status codes
- Error codes and meanings
- All API endpoint categories (Auth, Products, Inventory, Orders, Refunds, Discounts, Instagram, AI, Admin)
- Pagination and filtering
- Webhooks

**Key Concepts**:
- RESTful endpoint design
- JWT authentication
- Standardized error responses
- Idempotency keys
- Rate limiting

---

### 4. **Database Schema Skill**
**File**: [.github/skills/database-schema/SKILL.md](.github/skills/database-schema/SKILL.md)

**When to use**:
- Understanding table structures
- Designing domain entities and JPA mappings
- Writing database queries
- Validating data integrity

**Content**:
- Core design principles
- Master tables (Users, Roles, Permissions, Audit Logs)
- Product catalog tables
- Inventory management tables
- Order management tables
- Returns & refunds tables
- POS tables
- Marketing tables
- Instagram integration tables
- Common query patterns
- Optimization strategies

**Key Concepts**:
- SKU-Location Granularity
- Immutable Audit Logs
- Inventory Balance Calculations (Available = Total - Reserved - Allocated)
- Transactional Consistency
- Soft Deletes and Status Tracking

---

### 5. **Project Guide Skill** (Part of copilot-instructions.md)
**File**: [copilot-instructions.md](copilot-instructions.md)

**When to use**:
- Starting a new feature
- Establishing coding conventions
- Understanding project structure
- Enforcing quality standards

**Content**:
- Project overview and timeline
- Code organization for backend/frontend
- Naming conventions
- Coding standards
- API standards
- Testing standards
- Version control workflow
- Build and deployment
- Quality gates
- Performance standards
- Security standards
- Tools and technologies

---

## 🗂️ Project Structure

```
.github/
├── agents/
│   ├── seshop-backend-impl.agent.md         ← Backend implementation guidance
│   ├── seshop-frontend-impl.agent.md        ← Frontend implementation guidance
│   ├── seshop-qa-testing.agent.md           ← QA and testing guidance
│   ├── seshop-orchestrator.agent.md         ← Project coordination
│   └── seshop-doc-ssot.agent.md             ← Documentation consistency (existing)
│
└── skills/
    ├── backend-architecture/
    │   └── SKILL.md                         ← Backend design patterns & architecture
    ├── frontend-architecture/
    │   └── SKILL.md                         ← React patterns & state management
    ├── api-specification/
    │   └── SKILL.md                         ← REST API contract & endpoints
    ├── database-schema/
    │   └── SKILL.md                         ← DB tables, relationships, queries
    └── [others to be created]

Root/
├── copilot-instructions.md                   ← Project-wide guidelines & standards
├── IMPLEMENTATION_PLAN.md                    ← Phase timeline and deliverables
├── DOCUMENTATION_DEEP_CHECK_REPORT.md        ← Documentation quality assessment
└── README.md                                 ← Project overview

docs/
├── 1.BRD/
│   └── SESHOP BRD.md                        ← Business Requirements (SSoT)
├── 10.SRS/
│   └── SESHOP SRS.md                        ← Software Requirements (SSoT)
├── 3.Design/
│   ├── SESHOP HLD.md                        ← High-Level Design (SSoT)
│   ├── SESHOP LLD.md                        ← Low-Level Design (SSoT)
│   └── SESHOP API Spec.md                   ← API Specification (SSoT)
├── 4. View descriptions/
│   └── SeShop Views Desc.md                 ← UI View Specifications (SSoT)
└── 5.Database/
    ├── SESHOP Data Dictionary.md            ← Data model details (SSoT)
    └── SESHOP schema.sql                    ← Database schema (SSoT)
```

---

## 🚀 How to Use

### For Backend Developers

```
1. Read copilot-instructions.md
2. Invoke: SeShop Backend Implementation Agent
   "Phase 1 setup"
3. Reference skill: backend-architecture/SKILL.md
4. Reference skill: database-schema/SKILL.md
5. Reference skill: api-specification/SKILL.md
```

### For Frontend Developers

```
1. Read copilot-instructions.md
2. Invoke: SeShop Frontend Implementation Agent
   "Implement ADMIN_001 Dashboard"
3. Reference skill: frontend-architecture/SKILL.md
4. Reference skill: api-specification/SKILL.md
5. Check view specs in docs/4. View descriptions/
```

### For QA Engineers

```
1. Read copilot-instructions.md
2. Invoke: SeShop QA & Testing Agent
   "Design tests for UC15 checkout"
3. Reference skill: api-specification/SKILL.md
4. Reference skill: database-schema/SKILL.md
5. Check SRS in docs/10.SRS/
```

### For Project Managers

```
1. Read copilot-instructions.md
2. Invoke: SeShop Project Orchestrator
   "Phase 2 checkpoint"
3. Check IMPLEMENTATION_PLAN.md
4. Reference DOCUMENTATION_DEEP_CHECK_REPORT.md
```

### For Documentation Updates

```
1. Check copilot-instructions.md
2. Invoke: SeShop Doc SSoT Guardian
   "Check if [docs] align with [SSoT]"
3. Reference current SSoT documents
```

---

## 📊 Implementation Roadmap

### Phase 1 (Week 1-2): Setup & Foundation
- Backend: Spring Boot scaffold, PostgreSQL, JWT, audit logging
- Frontend: Vite setup, routing, auth layout
- QA: Test infrastructure setup
- **Lead**: SeShop Backend Implementation Agent

### Phase 2 (Week 2-3): Core Domains Part 1
- Backend: Identity, Catalog, Inventory modules
- Frontend: Admin/Staff base pages
- QA: Unit & integration tests
- **Lead**: SeShop Backend Implementation Agent + Frontend Agent (parallel)

### Phase 3 (Week 4-5): Core Domains Part 2
- Backend: Commerce, Orders, POS, Returns modules
- Frontend: Customer pages, Staff pages
- QA: E2E tests, coverage analysis
- **Lead**: SeShop Backend Implementation Agent + Frontend Agent (parallel)

### Phase 4 (Week 6-7): Advanced Features
- Backend: Marketing, Instagram, AI modules
- Frontend: Advanced features, real-time updates
- QA: Full E2E suite, performance testing
- **Lead**: All agents working together

### Phase 5 (Week 7-8): Testing & Quality + Go-Live
- Backend: Performance tuning, API docs
- Frontend: Accessibility, polish
- QA: Regression testing, UAT support
- **Lead**: SeShop Project Orchestrator

---

## 🎓 Learning Path

### New to SeShop?
1. Read [copilot-instructions.md](copilot-instructions.md) - Project standards
2. Read [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md) - Timeline
3. Review docs/1.BRD/SESHOP BRD.md - Business context
4. Review docs/10.SRS/SESHOP SRS.md - Functional requirements

### Backend Developer Path
1. Backend Architecture Skill - Design patterns
2. Database Schema Skill - Data model
3. API Specification Skill - Endpoints
4. Backend Implementation Agent - Phase guidance

### Frontend Developer Path
1. Frontend Architecture Skill - React patterns
2. API Specification Skill - Endpoints
3. Frontend Implementation Agent - Component guidance
4. Review docs/4. View descriptions/ - UI specs

### QA Engineer Path
1. API Specification Skill - Endpoints & error codes
2. Database Schema Skill - Data models for test setup
3. QA Testing Agent - Test design
4. SRS (docs/10.SRS/) - Requirements & acceptance criteria

---

## ✅ Quality Gates

Before starting implementation:
- [ ] All team members read copilot-instructions.md
- [ ] BRD, SRS, HLD, LLD are signed off by stakeholders
- [ ] IMPLEMENTATION_PLAN.md reviewed and approved
- [ ] Development environment configured
- [ ] Git workflow established
- [ ] CI/CD pipeline configured

During implementation:
- [ ] Weekly phase checkpoint with SeShop Project Orchestrator
- [ ] Code coverage maintained at > 80%
- [ ] API contracts validated against SESHOP API Spec
- [ ] Database schema changes tested and reversible
- [ ] Documentation updated with code changes

---

## 📞 Support & Escalation

### Questions During Development

| Question Type | Resolution Path |
|---------------|-----------------|
| "How do I implement [UC#]?" | → SeShop Backend/Frontend Implementation Agent |
| "What's the API contract for [endpoint]?" | → API Specification Skill |
| "How should I design [component]?" | → Frontend Architecture Skill |
| "What database tables do I need?" | → Database Schema Skill |
| "Is this design correct?" | → Invoke appropriate Agent or Skill |
| "Are we on schedule?" | → SeShop Project Orchestrator |
| "Why does the doc contradict [X]?" | → SeShop Doc SSoT Guardian |

### Blockers & Escalation

1. **Local issue** → Resolve in daily standup
2. **Cross-team blocker** → Escalate to tech lead via SeShop Project Orchestrator
3. **Architectural question** → Review HLD/LLD or invite technical lead
4. **Scope/timeline risk** → Notify project manager immediately

---

## 📚 Key Documents (Single Source of Truth)

All decisions are traced to one of these documents:

| Document | Use For |
|----------|---------|
| [BRD](docs/1.BRD/SESHOP%20BRD.md) | Business scope, requirements, workflows |
| [SRS](docs/10.SRS/SESHOP%20SRS.md) | Functional/non-functional requirements, use cases |
| [HLD](docs/3.Design/SESHOP%20HLD.md) | Architecture, module decomposition, patterns |
| [LLD](docs/3.Design/SESHOP%20LLD.md) | Detailed design, services, API endpoints |
| [API Spec](docs/3.Design/SESHOP%20API%20Spec.md) | REST contract, payloads, errors |
| [Views Desc](docs/4.%20View%20descriptions/SeShop%20Views%20Desc.md) | UI specifications, interactions |
| [Schema](docs/5.Database/SESHOP%20schema.sql) | Database structure, constraints |
| [Data Dict](docs/5.Database/SESHOP%20Data%20Dictionary.md) | Table meanings, relationships |

---

## 🔄 Continuous Improvement

### Documentation Review Cycle
- **Weekly**: Check for doc updates during phase checkpoints
- **Bi-weekly**: SeShop Doc SSoT Guardian consistency review
- **Monthly**: Full documentation audit with DOCUMENTATION_DEEP_CHECK_REPORT

### Agent & Skill Updates
- Agents updated with phase checkpoint learnings
- Skills enhanced based on team feedback
- New skills created as needs emerge
- All changes tracked in Git

---

## 📞 Contact

**Project Owner**: [TBD]  
**Technical Lead**: [TBD]  
**Backend Lead**: [TBD]  
**Frontend Lead**: [TBD]  
**QA Lead**: [TBD]  

**Last Updated**: 2026-04-30  
**Next Review**: 2026-05-07 (after Phase 1 checkpoint)

---

## Quick Links

- **Implementation Plan**: [IMPLEMENTATION_PLAN.md](IMPLEMENTATION_PLAN.md)
- **Documentation Check**: [DOCUMENTATION_DEEP_CHECK_REPORT.md](DOCUMENTATION_DEEP_CHECK_REPORT.md)
- **Project Standards**: [copilot-instructions.md](copilot-instructions.md)
- **Backend Agent**: [seshop-backend-impl.agent.md](.github/agents/seshop-backend-impl.agent.md)
- **Frontend Agent**: [seshop-frontend-impl.agent.md](.github/agents/seshop-frontend-impl.agent.md)
- **QA Agent**: [seshop-qa-testing.agent.md](.github/agents/seshop-qa-testing.agent.md)
- **Orchestrator**: [seshop-orchestrator.agent.md](.github/agents/seshop-orchestrator.agent.md)

