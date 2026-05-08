package com.seshop.marketing.infrastructure;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.shared.exception.BusinessException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Component
public class MetaGraphClient {

    private static final String INSTAGRAM_ACCOUNT_FIELDS = "id,name,access_token,instagram_business_account{id,username}";

    private final MetaGraphProperties properties;
    private final ObjectMapper objectMapper;

    public MetaGraphClient(MetaGraphProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    public String buildAuthorizationUrl(String state) {
        ensureConfigured();
        return authorizationDialogBaseUrl()
                + "?client_id=" + encode(properties.getAppId())
                + "&redirect_uri=" + encode(properties.getRedirectUri())
                + "&scope=" + encode(properties.getScopes())
                + "&response_type=code"
                + "&state=" + encode(state);
    }

    public MetaTokenResult exchangeCode(String code) {
        ensureConfigured();
        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/oauth/access_token")
                        .queryParam("client_id", properties.getAppId())
                        .queryParam("client_secret", properties.getAppSecret())
                        .queryParam("redirect_uri", properties.getRedirectUri())
                        .queryParam("code", code)
                        .build())
                .retrieve()
                .body(String.class);

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

    public MetaAccountResult getAccount(String accessToken) {
        String response = RestClient.builder()
                .baseUrl(properties.getBaseUrl())
                .build()
                .get()
                .uri(uriBuilder -> uriBuilder
                        .path("/me/accounts")
                        .queryParam("fields", "{fields}")
                        .queryParam("access_token", accessToken)
                        .build(Map.of("fields", INSTAGRAM_ACCOUNT_FIELDS)))
                .retrieve()
                .body(String.class);

        try {
            Map<String, Object> payload = objectMapper.readValue(response, new TypeReference<>() {});
            Object data = payload.get("data");
            if (data instanceof List<?> pages) {
                for (Object pageNode : pages) {
                    if (!(pageNode instanceof Map<?, ?> page)) {
                        continue;
                    }
                    Object instagramNode = page.get("instagram_business_account");
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

    private String authorizationDialogBaseUrl() {
        return properties.getBaseUrl()
                .replace("://graph.facebook.com/", "://www.facebook.com/")
                + "/dialog/oauth";
    }

    public record MetaTokenResult(String accessToken, long expiresInSeconds) {
    }

    public record MetaAccountResult(String accountId, String username, String accessToken) {
    }
}
