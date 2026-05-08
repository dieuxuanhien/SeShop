# SESHOP Folder Structure

**Last updated**: 2026-05-07

This document records the folder-structure rules used by SeShop after reviewing the official guidance for the current stack.

## Research Basis

- Spring Boot recommends keeping the main application class in a root package above the rest of the code so component scanning and JPA entity scanning stay inside the project package: [Spring Boot - Structuring Your Code](https://docs.spring.io/spring-boot/reference/using/structuring-your-code.html).
- Spring Modulith treats each direct sub-package of the main package as an application module by default, which matches a domain-first modular monolith: [Spring Modulith - Fundamentals](https://docs.spring.io/spring-modulith/reference/fundamentals.html).
- Maven documents the standard Java project layout and recommends conforming to it unless there is a specific reason to customize it: [Maven Standard Directory Layout](https://maven.apache.org/guides/introduction/introduction-to-the-standard-directory-layout.html).
- Feature-Sliced Design defines frontend layers such as `app`, `pages`, `features`, `entities`, and `shared`: [FSD Layers](https://fsd.how/docs/reference/layers/).
- FSD also recommends public APIs for slices so other modules import from a stable entrypoint instead of internal folders: [FSD Public API](https://fsd.how/docs/reference/public-api/) and [FSD Slices and Segments](https://fsd.how/docs/reference/slices-segments/).

## Backend Decision

SeShop is a Spring Boot modular monolith. Keep `SeShopApplication` at `com.seshop`, then place business modules as direct sub-packages:

```text
backend/src/main/java/com/seshop/
├── SeShopApplication.java
├── audit/
├── catalog/
├── commerce/
├── identity/
├── inventory/
├── marketing/
├── notification/
├── payment/
├── pos/
├── refund/
├── review/
├── shipping/
└── shared/
```

Each business module should use the same internal vocabulary:

```text
<module>/
├── api/                  # Controllers and request/response DTOs
├── application/          # Use cases, orchestration, transactions
├── domain/               # Domain concepts, enums, policies
└── infrastructure/       # Persistence, provider clients, adapters
```

`shared/` is reserved for cross-cutting framework code such as API envelopes, configuration, security, exceptions, and small utilities. Business-owned concepts must stay inside their module; for example, audit logging lives in `com.seshop.audit`, not in `com.seshop.shared.audit`.

## Frontend Decision

SeShop uses a Feature-Sliced Design-inspired React layout:

```text
frontend/src/
├── app/                  # Providers, layouts, router
├── pages/                # Route-level screens grouped by portal
├── features/             # User actions and feature logic
├── entities/             # Business entity types and helpers
├── shared/               # UI kit, API client, config, generic libs
├── styles/               # Global Tailwind/CSS entrypoints
└── test/                 # Test setup
```

Feature and entity slices should expose a deliberate `index.ts` public API when they are consumed by another layer. Internal files in `api/`, `model/`, and `ui/` can move later without forcing app-level imports to change.

For `shared/ui`, keep direct component imports such as `@/shared/ui/Button` until components are split into their own folders. A single large barrel for unrelated shared UI can hurt tree-shaking and obscure ownership.

## Refactoring Applied

- Removed the unused `backend/src/main/java/com/seshop/shared/audit` package. GitNexus impact analysis found zero direct callers and zero affected execution flows for the stale shared audit classes.
- Kept the canonical audit implementation under `backend/src/main/java/com/seshop/audit`.
- Added `frontend/src/features/auth/index.ts` as the auth slice public API.
- Updated app/layout/page imports to consume auth through `@/features/auth` instead of deep paths into `ui/` and `model/`.

## Rules For Future Files

- Add backend code to the owning business module first; use `shared/` only for code that is genuinely generic and used by more than one module.
- Do not create parallel implementations of the same business concept in both a business module and `shared/`.
- Add frontend API calls to the feature that owns the user workflow, not to a global services folder.
- Add reusable entity types to `entities/<entity>/types.ts`; expose them through a public API when other slices need them.
- Prefer route screens in `pages/<portal>/` and reusable interaction logic in `features/<slice>/`.
