package com.seshop.shipping.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * GHN shipping client integration tests using a local HTTP stub.
 * Verifies the createShippingOrder and getShippingStatus methods against a
 * local HttpServer, identical pattern to MetaGraphClientTest.
 * UC16: Fulfillment and shipping label.
 */
class GhnClientTest {

    private HttpServer server;
    private GhnProperties properties;
    private GhnClient client;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.start();

        int port = server.getAddress().getPort();
        properties = new GhnProperties();
        properties.setEnabled(true);
        properties.setBaseUrl("http://localhost:" + port);
        properties.setToken("test-token");
        properties.setShopId("123456");
        properties.setCreateOrderPath("/shiip/public-api/v2/shipping-order/create");
        properties.setTrackPath("/shiip/public-api/v2/shipping-order/detail");

        client = new GhnClient(properties, new ObjectMapper());
    }

    @AfterEach
    void stopServer() {
        server.stop(0);
    }

    // ── createShippingOrder ─────────────────────────────────────────────────

    @Test
    void createShippingOrderReturnsTrackingNumber() throws Exception {
        String responseJson = """
                {
                  "code": 200,
                  "data": {
                    "order_code": "GHN-ABC123",
                    "status": "ready_to_pick"
                  }
                }
                """;

        server.createContext("/shiip/public-api/v2/shipping-order/create", exchange -> {
            byte[] body = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        GhnClient.GhnShipmentResult result = client.createShippingOrder(
                "ORD-TEST001", "Nguyen Van A", "+84901000001", "123 Le Loi, Quan 1, HCM"
        );

        assertThat(result.trackingNumber()).isEqualTo("GHN-ABC123");
        assertThat(result.status()).isEqualTo("ready_to_pick");
    }

    @Test
    void createShippingOrderThrowsWhenTrackingNumberMissing() throws Exception {
        String responseJson = """
                {
                  "code": 200,
                  "data": {}
                }
                """;

        server.createContext("/shiip/public-api/v2/shipping-order/create", exchange -> {
            byte[] body = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        assertThatThrownBy(() -> client.createShippingOrder(
                "ORD-TEST002", "Test User", "+84900000000", "Test Address"
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("tracking number");
    }

    @Test
    void createShippingOrderThrowsWhenDisabled() {
        properties.setEnabled(false);

        assertThatThrownBy(() -> client.createShippingOrder(
                "ORD-TEST003", "Test", "+84900000000", "Addr"
        ))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("disabled");
    }

    // ── getShippingStatus ───────────────────────────────────────────────────

    @Test
    void getShippingStatusReturnsParsedStatus() throws Exception {
        String responseJson = """
                {
                  "code": 200,
                  "data": {
                    "status": "delivering"
                  }
                }
                """;

        server.createContext("/shiip/public-api/v2/shipping-order/detail", exchange -> {
            byte[] body = responseJson.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "application/json");
            exchange.sendResponseHeaders(200, body.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(body);
            }
        });

        String status = client.getShippingStatus("GHN-ABC123");

        assertThat(status).isEqualTo("delivering");
    }

    @Test
    void getShippingStatusReturnsPendingWhenDisabled() {
        properties.setEnabled(false);

        String status = client.getShippingStatus("GHN-IGNORED");

        assertThat(status).isEqualTo("PENDING");
    }
}
