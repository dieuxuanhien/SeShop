package com.seshop.shipping.api;

import com.seshop.shipping.infrastructure.persistence.ShipmentEntity;
import com.seshop.shipping.infrastructure.persistence.ShipmentRepository;
import java.time.OffsetDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/webhooks/shipping")
public class ShippingWebhookController {

    private final ShipmentRepository shipmentRepository;

    public ShippingWebhookController(ShipmentRepository shipmentRepository) {
        this.shipmentRepository = shipmentRepository;
    }

    @PostMapping
    public ResponseEntity<Map<String, Object>> handleShippingEvent(@RequestBody Map<String, Object> payload) {
        String trackingNumber = (String) payload.get("trackingNumber");
        String status = (String) payload.getOrDefault("status", "PENDING");

        shipmentRepository.findAll().stream()
                .filter(item -> trackingNumber != null && trackingNumber.equals(item.getTrackingNumber()))
                .findFirst()
                .ifPresent(item -> {
                    item.setStatus(status);
                    if ("DELIVERED".equalsIgnoreCase(status)) {
                        item.setDeliveredAt(OffsetDateTime.now());
                    }
                    shipmentRepository.save(item);
                });

        return ResponseEntity.ok(Map.of("data", Map.of("accepted", true)));
    }
}
