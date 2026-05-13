package com.seshop.marketing.infrastructure;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class MetaGraphClientTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private HttpServer server;

    @AfterEach
    void stopServer() {
        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    void defaultAuthorizationUrlTargetsMetaGraphV25() {
        MetaGraphClient client = new MetaGraphClient(configuredProperties(), objectMapper);

        URI authorizationUri = URI.create(client.buildAuthorizationUrl("42"));
        Map<String, List<String>> query = queryParams(authorizationUri);

        assertThat(authorizationUri.getScheme()).isEqualTo("https");
        assertThat(authorizationUri.getHost()).isEqualTo("www.facebook.com");
        assertThat(authorizationUri.getPath()).isEqualTo("/v25.0/dialog/oauth");
        assertThat(first(query, "client_id")).isEqualTo("app-id");
        assertThat(first(query, "redirect_uri")).isEqualTo("http://localhost:8080/api/v1/webhooks/instagram/callback");
        assertThat(first(query, "scope"))
                .isEqualTo("instagram_basic,pages_show_list,pages_read_engagement,instagram_content_publish");
        assertThat(first(query, "response_type")).isEqualTo("code");
        assertThat(first(query, "state")).isEqualTo("42");
    }

    @Test
    void exchangesCodeAndFindsLinkedInstagramBusinessAccountThroughV25Flow() throws IOException {
        List<URI> requests = new ArrayList<>();
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/v25.0/oauth/access_token", exchange -> {
            requests.add(exchange.getRequestURI());
            respond(exchange, """
                    {
                      "access_token": "user-token",
                      "expires_in": 5184000
                    }
                    """);
        });
        server.createContext("/v25.0/me/accounts", exchange -> {
            requests.add(exchange.getRequestURI());
            respond(exchange, """
                    {
                      "data": [
                        {
                          "id": "page-1",
                          "name": "SeShop",
                          "access_token": "page-token",
                          "instagram_business_account": {
                            "id": "ig-123",
                            "username": "seshop"
                          }
                        }
                      ]
                    }
                    """);
        });
        server.start();

        MetaGraphProperties properties = configuredProperties();
        properties.setBaseUrl("http://localhost:" + server.getAddress().getPort() + "/v25.0");
        MetaGraphClient client = new MetaGraphClient(properties, objectMapper);

        MetaGraphClient.MetaTokenResult token = client.exchangeCode("auth-code");
        MetaGraphClient.MetaAccountResult account = client.getAccount(token.accessToken());

        assertThat(token.accessToken()).isEqualTo("user-token");
        assertThat(token.expiresInSeconds()).isEqualTo(5184000);
        assertThat(account.accountId()).isEqualTo("ig-123");
        assertThat(account.username()).isEqualTo("seshop");
        assertThat(account.accessToken()).isEqualTo("page-token");

        assertThat(requests).hasSize(2);
        assertThat(requests.get(0).getPath()).isEqualTo("/v25.0/oauth/access_token");
        Map<String, List<String>> tokenQuery = queryParams(requests.get(0));
        assertThat(first(tokenQuery, "client_id")).isEqualTo("app-id");
        assertThat(first(tokenQuery, "client_secret")).isEqualTo("app-secret");
        assertThat(first(tokenQuery, "redirect_uri")).isEqualTo("http://localhost:8080/api/v1/webhooks/instagram/callback");
        assertThat(first(tokenQuery, "code")).isEqualTo("auth-code");

        assertThat(requests.get(1).getPath()).isEqualTo("/v25.0/me/accounts");
        Map<String, List<String>> accountsQuery = queryParams(requests.get(1));
        assertThat(first(accountsQuery, "fields"))
                .isEqualTo("id,name,access_token,instagram_business_account{id,username}");
        assertThat(first(accountsQuery, "access_token")).isEqualTo("user-token");
    }

        @Test
        void publishesAnInstagramImagePostThroughTheGraphApi() throws IOException {
                List<URI> requests = new ArrayList<>();
                server = HttpServer.create(new InetSocketAddress(0), 0);
                server.createContext("/v25.0/ig-123/media", exchange -> {
                        requests.add(exchange.getRequestURI());
                        respond(exchange, """
                                        {
                                            "id": "creation-7"
                                        }
                                        """);
                });
                server.createContext("/v25.0/ig-123/media_publish", exchange -> {
                        requests.add(exchange.getRequestURI());
                        respond(exchange, """
                                        {
                                            "id": "media-99"
                                        }
                                        """);
                });
                server.start();

                MetaGraphProperties properties = configuredProperties();
                properties.setBaseUrl("http://localhost:" + server.getAddress().getPort() + "/v25.0");
                MetaGraphClient client = new MetaGraphClient(properties, objectMapper);

                MetaGraphClient.MetaPublishResult result = client.publishImagePost(
                                "ig-123",
                                "page-token",
                                "https://cdn.example.com/image-1.jpg",
                                "Fresh vintage drop"
                );

                assertThat(result.creationId()).isEqualTo("creation-7");
                assertThat(result.mediaId()).isEqualTo("media-99");
                assertThat(requests).hasSize(2);
                assertThat(requests.get(0).getPath()).isEqualTo("/v25.0/ig-123/media");
                Map<String, List<String>> createQuery = queryParams(requests.get(0));
                assertThat(first(createQuery, "image_url")).isEqualTo("https://cdn.example.com/image-1.jpg");
                assertThat(first(createQuery, "caption")).isEqualTo("Fresh vintage drop");
                assertThat(first(createQuery, "access_token")).isEqualTo("page-token");

                assertThat(requests.get(1).getPath()).isEqualTo("/v25.0/ig-123/media_publish");
                Map<String, List<String>> publishQuery = queryParams(requests.get(1));
                assertThat(first(publishQuery, "creation_id")).isEqualTo("creation-7");
                assertThat(first(publishQuery, "access_token")).isEqualTo("page-token");
        }

    private MetaGraphProperties configuredProperties() {
        MetaGraphProperties properties = new MetaGraphProperties();
        properties.setEnabled(true);
        properties.setAppId("app-id");
        properties.setAppSecret("app-secret");
        properties.setRedirectUri("http://localhost:8080/api/v1/webhooks/instagram/callback");
        return properties;
    }

    private Map<String, List<String>> queryParams(URI uri) {
        Map<String, List<String>> params = new HashMap<>();
        String query = uri.getRawQuery();
        if (query == null || query.isBlank()) {
            return params;
        }
        for (String pair : query.split("&")) {
            String[] parts = pair.split("=", 2);
            String key = decode(parts[0]);
            String value = parts.length > 1 ? decode(parts[1]) : "";
            params.computeIfAbsent(key, ignored -> new ArrayList<>()).add(value);
        }
        return params;
    }

    private String first(Map<String, List<String>> params, String key) {
        return params.getOrDefault(key, List.of()).getFirst();
    }

    private String decode(String value) {
        return URLDecoder.decode(value, StandardCharsets.UTF_8);
    }

    private void respond(com.sun.net.httpserver.HttpExchange exchange, String json) throws IOException {
        byte[] response = json.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(200, response.length);
        try (OutputStream body = exchange.getResponseBody()) {
            body.write(response);
        }
    }
}
