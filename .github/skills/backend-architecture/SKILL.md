---
description: "Backend architecture and design patterns for SeShop. Use when: understanding Spring Boot module structure, designing domain services, implementing transactional flows, or querying LLD architecture details."
name: backend-architecture
---

# SeShop Backend Architecture Skill

This skill provides architectural knowledge for SeShop backend implementation using Spring Boot, modular monolith patterns, and domain-driven design.

## When to Use

- Understanding module interactions and dependencies
- Designing new domain services
- Implementing transactional workflows (orders, payments, inventory)
- Applying security patterns (RBAC, audit logging)
- Optimizing database queries

## Architecture Overview

**Style**: Modular Monolith  
**Framework**: Spring Boot + Spring Data JPA  
**Database**: PostgreSQL  
**Package Structure**: Domain-driven design with clear layer separation

### Layers

```
API Layer (REST Controllers)
    ↓
Domain Service Layer (Business Logic)
    ↓
Persistence Layer (Repositories, JPA Entities)
    ↓
Database (PostgreSQL)
```

### Module Structure (see [SESHOP LLD](docs/3.Design/SESHOP%20LLD.md))

```
com.seshop.
├── api/
│   ├── auth/ → UC1-3 (Auth endpoints)
│   ├── catalog/ → UC5 (Product endpoints)
│   ├── inventory/ → UC6-7, UC16, UC25
│   ├── commerce/ → UC13-15
│   ├── order/ → UC19-20, UC23
│   ├── pos/ → UC8, UC26
│   ├── return/ → UC9, UC24
│   ├── marketing/ → UC10, UC21
│   └── admin/ → UC1-3 (Admin operations)
│
├── domain/
│   ├── auth/
│   │   ├── User.java
│   │   ├── Role.java
│   │   ├── AuthService.java
│   │   └── RBACService.java
│   ├── catalog/
│   │   ├── Product.java
│   │   ├── ProductVariant.java
│   │   ├── CatalogService.java
│   │   └── SearchService.java
│   ├── inventory/
│   │   ├── Location.java
│   │   ├── InventoryBalance.java
│   │   ├── InventoryService.java
│   │   ├── TransferService.java
│   │   └── ReservationService.java
│   ├── commerce/
│   │   ├── Cart.java
│   │   ├── CartService.java
│   │   └── PricingService.java
│   ├── order/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   ├── OrderService.java
│   │   └── FulfillmentService.java
│   └── shared/
│       ├── Money.java
│       ├── Address.java
│       └── Event*.java
│
├── persistence/
│   ├── auth/
│   │   ├── User.java (JPA Entity)
│   │   ├── UserRepository.java
│   │   └── ...
│   ├── catalog/
│   │   ├── ProductEntity.java
│   │   ├── ProductRepository.java
│   │   └── ...
│   └── ...
│
├── config/
│   ├── JpaConfig.java
│   ├── SecurityConfig.java
│   └── MvcConfig.java
│
└── util/
    ├── ErrorModel.java
    ├── AuditLogging.java
    └── Pagination.java
```

## Key Design Patterns

### 1. Service Layer Pattern
All business logic in `domain/*Service.java`. Controllers call services, services call repositories.

```java
@Service
public class InventoryService {
  @Autowired InventoryRepository repo;
  
  @Transactional
  public void reserveStock(SKU sku, Location loc, int qty) {
    InventoryBalance balance = repo.findBySKUAndLocation(sku, loc);
    if (balance.getAvailable() < qty) throw new OutOfStockException();
    balance.reserve(qty);
    repo.save(balance);
    auditLog("STOCK_RESERVED", sku.getId());
  }
}
```

### 2. Repository Pattern
One repository per aggregate root. Repositories handle all database access.

```java
@Repository
public interface InventoryRepository extends JpaRepository<InventoryBalance, Long> {
  InventoryBalance findBySKUAndLocation(SKU sku, Location loc);
  List<InventoryBalance> findByLocation(Location loc);
}
```

### 3. Event-Driven Updates
Use Spring Events for stock-affecting operations (order paid, refund completed).

```java
@Service
public class OrderService {
  @Autowired ApplicationEventPublisher events;
  
  @Transactional
  public Order completePayment(Order order) {
    order.markPaid();
    orderRepository.save(order);
    events.publishEvent(new OrderPaidEvent(order));
    return order;
  }
}

@Component
public class OrderPaidListener {
  @EventListener
  public void onOrderPaid(OrderPaidEvent event) {
    inventoryService.reserveStock(event.getOrder().getItems());
  }
}
```

### 4. Audit Logging (Append-Only)
All sensitive actions logged immutably.

```java
@Service
public class AuditService {
  @Autowired AuditLogRepository repo;
  
  public void log(String action, String targetType, String targetId, User actor) {
    AuditLog entry = new AuditLog()
      .setAction(action)
      .setTargetType(targetType)
      .setTargetId(targetId)
      .setActorUserId(actor.getId())
      .setCreatedAt(Instant.now());
    repo.save(entry);
  }
}
```

### 5. State Machines (Orders, Returns, Transfers)
Legal transitions enforced in code.

```java
public class Order {
  public enum Status { PENDING, CONFIRMED, PACKED, SHIPPED, DELIVERED }
  
  private Status status;
  
  public void markConfirmed() {
    if (status != Status.PENDING) 
      throw new InvalidStateException("Order already confirmed");
    this.status = Status.CONFIRMED;
  }
}
```

## Database Design Principles

1. **SKU-Location as Core Unit**: Inventory tracked at (SKU, Location) level
2. **Immutable Audit Logs**: Append-only, never modified
3. **Transactional Consistency**: Order + Inventory updates atomic
4. **Soft Deletes**: Use `status` column instead of delete
5. **Temporal Data**: `created_at`, `updated_at` timestamps

See [SESHOP Data Dictionary](docs/5.Database/SESHOP%20Data%20Dictionary.md) and [schema.sql](docs/5.Database/SESHOP%20schema.sql).

## Transactional Boundaries

**ACID Transactions**:
- Order creation + inventory reservation (single transaction)
- Payment processing + order status update
- Refund processing + inventory unreserve

**Event-Driven (Eventual Consistency)**:
- Order paid → trigger inventory service (can be async)
- Return received → trigger refund calculation (can be async)

## Common Implementation Tasks

**"How do I implement [UC#]?"**
1. Find domain module owning UC (check LLD)
2. Design service method signature
3. Implement domain logic (state validation, calculations)
4. Call repository methods
5. Publish events if needed
6. Write tests

**"How do I prevent overselling?"**
- At checkout: Check `available_stock = total_qty - reserved_qty - allocated_qty`
- On order confirm: Lock row, deduct from `available_stock`, increment `reserved_qty`
- Use optimistic locking or pessimistic locking depending on contention

**"How do I audit sensitive operations?"**
- Use AuditService.log() after every sensitive action
- Include actor (who), action (what), target (which resource)
- Store immutably in audit_logs table

**"How do I handle failed payments?"**
- Order stays PENDING
- Inventory not reserved
- Retry endpoint with idempotency key
- After 3 retries or timeout, auto-cancel order

## Dependencies Between Modules

```
Order Service
  ├─→ Inventory Service (reserve stock)
  ├─→ Pricing Service (calculate total)
  ├─→ Payment Gateway (process payment)
  └─→ Notification Service (send confirmation)

Inventory Service
  ├─→ Location Service (validate location)
  └─→ Audit Service (log movements)

Refund Service
  ├─→ Order Service (verify order)
  ├─→ Inventory Service (unreserve stock)
  └─→ Payment Gateway (refund payment)
```

## Security Considerations

1. **Server-Authoritative**: Never trust client for business decisions
2. **RBAC**: Enforce permission on every API call
3. **Input Validation**: Validate all user inputs
4. **SQL Injection Prevention**: Use parameterized queries (JPA)
5. **Rate Limiting**: Prevent abuse (implement in Spring Gateway)
6. **Audit All Sensitive Ops**: Who did what when

## Performance Optimization

1. **Database Indexing**: Index frequently-filtered columns
2. **Eager vs Lazy Loading**: Carefully choose to avoid N+1 queries
3. **Pagination**: Always paginate large result sets
4. **Caching**: Use Redis for frequently-read data (stock levels, prices)
5. **Connection Pooling**: Use HikariCP (Spring default)

## Testing Strategy

- **Unit Tests**: Test service methods in isolation (mock repositories)
- **Integration Tests**: Test with real database (TestContainers)
- **API Tests**: Test REST endpoints (REST Assured)

See [SeShop QA & Testing Agent](./ seshop-qa-testing.agent.md) for detailed test examples.

## References

- [SESHOP HLD](docs/3.Design/SESHOP%20HLD.md)
- [SESHOP LLD](docs/3.Design/SESHOP%20LLD.md)
- [SESHOP API Spec](docs/3.Design/SESHOP%20API%20Spec.md)
- [SESHOP SRS](docs/10.SRS/SESHOP%20SRS.md)

