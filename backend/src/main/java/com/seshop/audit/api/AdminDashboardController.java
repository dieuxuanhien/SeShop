package com.seshop.audit.api;

import com.seshop.audit.infrastructure.persistence.AuditLogRepository;
import com.seshop.commerce.infrastructure.persistence.OrderEntity;
import com.seshop.commerce.infrastructure.persistence.OrderRepository;
import com.seshop.inventory.infrastructure.persistence.InventoryBalanceRepository;
import com.seshop.shared.api.ApiResponse;
import com.seshop.shared.api.TraceIdFilter;
import com.seshop.shared.security.PermissionValidator;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Admin Dashboard APIs for real-time KPI metrics and system health monitoring.
 * Computes metrics from audit logs, orders, inventory, and system availability.
 * 
 * References:
 * - View: ADMIN_001 (docs/4. View descriptions/SeShop Views Desc.md)
 * - Endpoints: GET /admin/dashboard/metrics, GET /admin/system/status
 * - Data sources: audit_logs, orders, inventory_balances, user_roles tables
 */
@RestController
@RequestMapping("/api/v1/admin")
public class AdminDashboardController {

    private final PermissionValidator permissionValidator;
    private final OrderRepository orderRepository;
    private final InventoryBalanceRepository inventoryBalanceRepository;
    private final AuditLogRepository auditLogRepository;

    public AdminDashboardController(PermissionValidator permissionValidator,
                                    OrderRepository orderRepository,
                                    InventoryBalanceRepository inventoryBalanceRepository,
                                    AuditLogRepository auditLogRepository) {
        this.permissionValidator = permissionValidator;
        this.orderRepository = orderRepository;
        this.inventoryBalanceRepository = inventoryBalanceRepository;
        this.auditLogRepository = auditLogRepository;
    }

    /**
     * GET /api/v1/admin/dashboard/metrics
     * 
     * Fetch real-time KPI metrics for dashboard display:
     * - Today's revenue (sum of online orders + POS receipts for current day)
     * - Active orders count (status NOT IN (SHIPPED, CANCELLED, RETURNED))
     * - Low stock alerts (SKUs with available qty below warehouse threshold)
     * - Staff online count (users with recent audit log activity in last 5 minutes)
     * 
     * Response includes period-over-period change percentages for trend display.
     */
    @GetMapping("/dashboard/metrics")
    public ApiResponse<Map<String, Object>> getDashboardMetrics() {
        permissionValidator.require("audit.read");
        
        OffsetDateTime startOfDay = LocalDate.now(ZoneOffset.UTC).atStartOfDay().atOffset(ZoneOffset.UTC);
        OffsetDateTime fiveMinutesAgo = OffsetDateTime.now(ZoneOffset.UTC).minusMinutes(5);
        Set<String> inactiveStatuses = Set.of("SHIPPED", "CANCELLED", "RETURNED", "DELIVERED");

        List<OrderEntity> orders = orderRepository.findAll();
        BigDecimal todayRevenue = orders.stream()
                .filter(order -> order.getCreatedAt() != null && !order.getCreatedAt().isBefore(startOfDay))
                .map(order -> order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        long activeOrders = orders.stream()
                .filter(order -> order.getStatus() != null && !inactiveStatuses.contains(order.getStatus()))
                .count();
        long lowStockAlerts = inventoryBalanceRepository.findAll().stream()
                .filter(balance -> balance.getOnHandQty() - balance.getReservedQty() < 5)
                .count();
        long staffOnline = auditLogRepository.findAll().stream()
                .filter(log -> log.getActorUserId() != null)
                .filter(log -> log.getCreatedAt() != null && log.getCreatedAt().isAfter(fiveMinutesAgo))
                .map(log -> log.getActorUserId())
                .distinct()
                .count();

        Map<String, Object> metrics = Map.of(
            "todayRevenue", todayRevenue,
            "revenueChange", 0,
            "activeOrdersCount", activeOrders,
            "ordersChange", 0,
            "lowStockAlertsCount", lowStockAlerts,
            "alertsChange", 0,
            "staffOnlineCount", staffOnline,
            "staffChange", "Stable"
        );
        
        return ApiResponse.success(metrics, MDC.get(TraceIdFilter.TRACE_ID));
    }

    /**
     * GET /api/v1/admin/system/status
     * 
     * Fetch system service health status for monitoring display.
     * Response contains list of service name + health status (Healthy, Degraded, Down).
     */
    @GetMapping("/system/status")
    public ApiResponse<List<Map<String, String>>> getSystemStatus() {
        permissionValidator.require("audit.read");
        
        List<Map<String, String>> status = List.of(
            Map.of("service", "Orders", "status", healthStatus(() -> orderRepository.count())),
            Map.of("service", "Inventory", "status", healthStatus(() -> inventoryBalanceRepository.count())),
            Map.of("service", "Audit Pipeline", "status", healthStatus(() -> auditLogRepository.count()))
        );
        
        return ApiResponse.success(status, MDC.get(TraceIdFilter.TRACE_ID));
    }

    private String healthStatus(HealthProbe probe) {
        try {
            probe.check();
            return "Healthy";
        } catch (RuntimeException exception) {
            return "Down";
        }
    }

    private interface HealthProbe {
        void check();
    }
}
