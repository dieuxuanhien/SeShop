package com.seshop.marketing.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
@SuppressWarnings("null")
public class MetaGraphClient {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(MetaGraphClient.class);

    private static final String INSTAGRAM_ACCOUNT_FIELDS = "id,name,access_token,instagram_business_account{id,username},connected_instagram_account{id,username}";

    private final MetaGraphProperties properties;
    private final ObjectMapper objectMapper;

    public MetaGraphClient(MetaGraphProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String buildAuthorizationUrl(String state) {
        if (!properties.isEnabled() || properties.getAppId() == null || properties.getAppSecret() == null) {
            String redirect = properties.getRedirectUri();
            if (redirect == null || redirect.isBlank()) {
                redirect = "http://localhost:8080/api/v1/webhooks/instagram/callback";
            }
            return redirect + "?code=mock_code&state=" + state;
        }
        ensureConfigured();
        return authorizationDialogBaseUrl()
            + "?client_id=" + encode(Objects.requireNonNull(properties.getAppId()))
            + "&redirect_uri=" + encode(Objects.requireNonNull(properties.getRedirectUri()))
                + "&scope=" + encode(properties.getScopes())
                + "&response_type=code"
                + "&state=" + encode(state);
    }

    public MetaTokenResult exchangeCode(String code) {
        ensureConfigured();
        String baseUrl = baseUrl();
        String response = RestClient.builder()
            .baseUrl(baseUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/access_token")
                .queryParam("client_id", Objects.requireNonNull(properties.getAppId()))
                .queryParam("client_secret", Objects.requireNonNull(properties.getAppSecret()))
                .queryParam("redirect_uri", Objects.requireNonNull(properties.getRedirectUri()))
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .body(String.class);

        log.info("Meta Graph /oauth/access_token response: {}", response);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            String accessToken = (String) payload.get("access_token");
            Number expiresIn = (Number) payload.getOrDefault("expires_in", 0);
            if (!StringUtils.hasText(accessToken)) {
                throw new BusinessException("SOC_001", "Meta Graph did not return access token");
            }
            return new MetaTokenResult(accessToken, expiresIn.longValue());
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("SOC_001", "Cannot parse Meta Graph token response");
        }
    }

    public void verifyScopes(String accessToken) {
        String baseUrl = baseUrl();
        String response = RestClient.builder()
            .baseUrl(baseUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/permissions")
                        .queryParam("access_token", accessToken)
                        .build())
                .retrieve()
                .body(String.class);

        log.info("Meta Graph /me/permissions response: {}", response);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            Object data = payload.get("data");
            if (data instanceof List<?> permissions) {
                List<String> requiredScopes = List.of(properties.getScopes().split(","));
                for (String scope : requiredScopes) {
                    boolean granted = false;
                    for (Object permNode : permissions) {
                        if (permNode instanceof Map<?, ?> perm) {
                            if (scope.equals(perm.get("permission")) && "granted".equals(perm.get("status"))) {
                                granted = true;
                                break;
                            }
                        }
                    }
                    if (!granted) {
                        throw new BusinessException("SOC_001", "Missing required scope: " + scope);
                    }
                }
            } else {
                throw new BusinessException("SOC_001", "Cannot parse Meta Graph permissions response");
            }
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("SOC_001", "Cannot parse Meta Graph permissions response");
        }
    }

    public MetaAccountResult getAccount(String accessToken) {
        String baseUrl = baseUrl();
        String response = RestClient.builder()
            .baseUrl(baseUrl)
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/accounts")
                        .queryParam("fields", "{fields}")
                        .queryParam("access_token", accessToken)
                .build(Map.of("fields", (Object) INSTAGRAM_ACCOUNT_FIELDS)))
                .retrieve()
                .body(String.class);

        log.info("Meta Graph /me/accounts response: {}", response);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            Object data = payload.get("data");
            if (data instanceof List<?> pages) {
                for (Object pageNode : pages) {
                    if (!(pageNode instanceof Map<?, ?> page)) {
                        continue;
                    }
                    Object instagramNode = page.get("instagram_business_account");
                    if (!(instagramNode instanceof Map<?, ?>)) {
                        instagramNode = page.get("connected_instagram_account");
                    }
                    if (!(instagramNode instanceof Map<?, ?> instagramAccount)) {
                        continue;
                    }

                    String accountId = asString(instagramAccount.get("id"));
                    String username = asString(instagramAccount.get("username"));
                    String pageAccessToken = asString(page.get("access_token"));
                    if (StringUtils.hasText(accountId) && StringUtils.hasText(pageAccessToken)) {
                        return new MetaAccountResult(
                                accountId,
                                StringUtils.hasText(username) ? username : "instagram_account",
                                pageAccessToken
                        );
                    }
                }
            }
            throw new BusinessException("SOC_001", "No Instagram business account found on accessible Facebook pages");
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("SOC_001", "Cannot parse Meta Graph account response");
        }
    }

    public MetaPublishResult publishImagePost(String accountId, String accessToken, String imageUrl, String caption) {
        ensureConfigured();
        String baseUrl = baseUrl();
        try {
            String creationResponse = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build()
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + accountId + "/media")
                            .queryParam("image_url", imageUrl)
                            .queryParam("caption", caption)
                            .queryParam("access_token", accessToken)
                            .build())
                    .retrieve()
                    .body(String.class);

            String creationId = extractId(creationResponse, "Cannot parse Meta Graph media creation response");

            String publishResponse = RestClient.builder()
                    .baseUrl(baseUrl)
                    .build()
                    .post()
                    .uri(uriBuilder -> uriBuilder
                            .path("/" + accountId + "/media_publish")
                            .queryParam("creation_id", creationId)
                            .queryParam("access_token", accessToken)
                            .build())
                    .retrieve()
                    .body(String.class);

            String mediaId = extractId(publishResponse, "Cannot parse Meta Graph publish response");
            return new MetaPublishResult(creationId, mediaId);
        } catch (org.springframework.web.client.RestClientResponseException e) {
            String errorBody = e.getResponseBodyAsString();
            try {
                Map<String, Object> errorPayload = objectMapper.readValue(errorBody, new com.fasterxml.jackson.core.type.TypeReference<>() {});
                if (errorPayload.containsKey("error")) {
                    Map<?, ?> errorMap = (Map<?, ?>) errorPayload.get("error");
                    String metaMessage = (String) errorMap.get("message");
                    if (org.springframework.util.StringUtils.hasText(metaMessage)) {
                        throw new BusinessException("SOC_001", "Meta Graph API Error: " + metaMessage);
                    }
                }
            } catch (BusinessException ex) {
                throw ex;
            } catch (Exception ignored) {}
            throw new BusinessException("SOC_001", "Meta Graph API responded with status " + e.getStatusCode() + ": " + errorBody);
        }
    }

    private void ensureConfigured() {
        if (!properties.isEnabled()) {
            throw new BusinessException("SOC_001", "Meta Graph integration is disabled");
        }
        if (!StringUtils.hasText(properties.getAppId())
                || !StringUtils.hasText(properties.getAppSecret())
                || !StringUtils.hasText(properties.getRedirectUri())) {
            throw new BusinessException("SOC_001", "Meta Graph credentials are not configured");
        }
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private String asString(Object value) {
        return value instanceof String text ? text : null;
    }

    private String baseUrl() {
        return Objects.requireNonNull(properties.getBaseUrl());
    }

    private String extractId(String response, String errorMessage) {
        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            String id = asString(payload.get("id"));
            if (!StringUtils.hasText(id)) {
                throw new BusinessException("SOC_003", "Meta Graph did not return a publish identifier");
            }
            return id;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException("SOC_003", errorMessage);
        }
    }

    private String authorizationDialogBaseUrl() {
        return baseUrl()
                .replace("://graph.facebook.com/", "://www.facebook.com/")
                + "/dialog/oauth";
    }

    public record MetaTokenResult(String accessToken, long expiresInSeconds) {
    }

    public record MetaAccountResult(String accountId, String username, String accessToken) {
    }

    public record MetaPublishResult(String creationId, String mediaId) {
    }
}
