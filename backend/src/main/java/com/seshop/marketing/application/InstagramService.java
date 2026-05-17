package com.seshop.marketing.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.audit.application.AuditService;
import com.seshop.audit.domain.AuditAction;
import com.seshop.marketing.api.dto.InstagramPublishResultDto;
import com.seshop.marketing.api.dto.InstagramConnectionDto;
import com.seshop.marketing.api.dto.InstagramDraftDto;
import com.seshop.marketing.infrastructure.MetaGraphClient;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionRepository;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftRepository;
import com.seshop.catalog.infrastructure.persistence.ProductEntity;
import com.seshop.catalog.infrastructure.persistence.ProductImageEntity;
import com.seshop.catalog.infrastructure.persistence.ProductRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.encrypt.Encryptors;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

@Service
public class InstagramService {

    private final InstagramConnectionRepository connectionRepository;
    private final InstagramDraftRepository draftRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper objectMapper;
    private final MetaGraphClient metaGraphClient;
    private final AuditService auditService;
    private final TextEncryptor encryptor;
    private final String secret;

    public InstagramService(
            InstagramConnectionRepository connectionRepository,
            InstagramDraftRepository draftRepository,
            ProductRepository productRepository,
            ObjectMapper objectMapper,
            MetaGraphClient metaGraphClient,
            AuditService auditService,
            @Value("${seshop.security.jwt.secret}") String secret) {
        this.connectionRepository = connectionRepository;
        this.draftRepository = draftRepository;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
        this.metaGraphClient = metaGraphClient;
        this.auditService = auditService;
        this.secret = secret;
        this.encryptor = Encryptors.text(secret, "5c0744940b5c369b");
    }

    @Transactional(readOnly = true)
    public InstagramConnectionDto getConnectionStatus(Long userId) {
        return connectionRepository.findByUserId(userId)
                .map(this::mapConnectionToDto)
                .orElseGet(() -> {
                    InstagramConnectionDto dto = new InstagramConnectionDto();
                    dto.setStatus("DISCONNECTED");
                    return dto;
                });
    }

    @Transactional
    public String startConnection(Long userId) {
        InstagramConnectionEntity entity = connectionRepository.findByUserId(userId)
                .orElse(new InstagramConnectionEntity());
        entity.setUserId(userId);
        entity.setAccountId("pending");
        entity.setTokenEncrypted("pending");
        entity.setTokenExpiresAt(OffsetDateTime.now().plusMinutes(10));
        entity.setStatus("PENDING_AUTH");
        connectionRepository.save(entity);
        return metaGraphClient.buildAuthorizationUrl(generateState(userId));
    }

    @Transactional
    public InstagramConnectionDto completeConnection(String state, String code) {
        Long userId = verifyState(state);
        MetaGraphClient.MetaTokenResult tokenResult = metaGraphClient.exchangeCode(code);
        metaGraphClient.verifyScopes(tokenResult.accessToken());
        MetaGraphClient.MetaAccountResult account = metaGraphClient.getAccount(tokenResult.accessToken());

        InstagramConnectionEntity entity = connectionRepository.findByUserId(userId)
                .orElse(new InstagramConnectionEntity());
        entity.setUserId(userId);
        entity.setAccountId(account.accountId());
        entity.setTokenEncrypted(encryptor.encrypt(account.accessToken()));
        entity.setTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResult.expiresInSeconds()));
        entity.setStatus("CONNECTED");
        InstagramConnectionDto result = mapConnectionToDto(connectionRepository.save(entity));

        Map<String, Object> connectMeta = new LinkedHashMap<>();
        connectMeta.put("userId", userId);
        connectMeta.put("accountId", account.accountId());
        connectMeta.put("status", "CONNECTED");
        auditService.write(AuditAction.INSTAGRAM_CONNECTION_CHANGED, "InstagramConnection",
                String.valueOf(userId), connectMeta);

        return result;
    }

    @Transactional
    public InstagramDraftDto createDraft(Long userId, InstagramDraftDto request) {
        InstagramConnectionEntity connection = connectionRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException("SOC_001", "Instagram connection expired"));
        if (!"CONNECTED".equals(connection.getStatus())) {
            throw new BusinessException("SOC_001", "Instagram connection expired");
        }

        InstagramDraftEntity entity = new InstagramDraftEntity();
        entity.setCreatedBy(userId);
        
        if (request.getProductId() != null) {
            ProductEntity product = productRepository.findById(request.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("CAT_404", "Product not found"));
            
            entity.setProductId(product.getId());
            
            String caption = request.getCaption();
            if (caption == null || caption.isBlank()) {
                caption = product.getName() + "\n\n" + (product.getDescription() != null ? product.getDescription() : "");
            }
            entity.setCaption(caption);

            String hashtags = request.getHashtags();
            if (hashtags == null || hashtags.isBlank()) {
                hashtags = "#" + (product.getBrand() != null ? product.getBrand().replaceAll("\\s+", "") : "SeShop") + " #fashion";
            }
            entity.setHashtags(hashtags);

            List<String> mediaOrder = request.getMediaOrder();
            if (mediaOrder == null || mediaOrder.isEmpty()) {
                mediaOrder = product.getImages().stream()
                        .filter(img -> Boolean.TRUE.equals(img.getIsInstagramReady()))
                        .map(ProductImageEntity::getUrl)
                        .collect(Collectors.toList());
                if (mediaOrder.isEmpty() && !product.getImages().isEmpty()) {
                    mediaOrder = product.getImages().stream()
                            .map(ProductImageEntity::getUrl)
                            .collect(Collectors.toList());
                }
            }
            try {
                entity.setMediaOrderJson(objectMapper.writeValueAsString(mediaOrder));
            } catch (JsonProcessingException e) {
                entity.setMediaOrderJson("[]");
            }
        } else {
            entity.setProductId(null);
            entity.setCaption(request.getCaption());
            entity.setHashtags(request.getHashtags());
            try {
                entity.setMediaOrderJson(objectMapper.writeValueAsString(request.getMediaOrder()));
            } catch (JsonProcessingException e) {
                entity.setMediaOrderJson("[]");
            }
        }

        entity.setStatus("DRAFT");
        return mapDraftToDto(draftRepository.save(entity));
    }

    @Transactional
    public InstagramDraftDto updateDraft(@NonNull Long draftId, InstagramDraftDto request) {
        InstagramDraftEntity entity = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("SOC_404", "Draft not found"));

        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException("SOC_002", "Draft approval required");
        }

        entity.setCaption(request.getCaption());
        entity.setHashtags(request.getHashtags());
        try {
            entity.setMediaOrderJson(objectMapper.writeValueAsString(request.getMediaOrder()));
        } catch (JsonProcessingException e) {
            // keep existing
        }

        return mapDraftToDto(draftRepository.save(entity));
    }

    @Transactional
    public InstagramDraftDto submitReview(@NonNull Long draftId) {
        InstagramDraftEntity entity = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("SOC_404", "Draft not found"));
        if (!"DRAFT".equals(entity.getStatus())) {
            throw new BusinessException("SOC_002", "Draft approval required");
        }
        entity.setStatus("REVIEW_READY");
        return mapDraftToDto(draftRepository.save(entity));
    }

    @Transactional
    public InstagramDraftDto approveDraft(@NonNull Long draftId) {
        InstagramDraftEntity entity = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("SOC_404", "Draft not found"));
        if (!"REVIEW_READY".equals(entity.getStatus())) {
            throw new BusinessException("SOC_002", "Draft approval required");
        }
        entity.setStatus("APPROVED");
        return mapDraftToDto(draftRepository.save(entity));
    }

    @Transactional
    public InstagramPublishResultDto publishDraft(@NonNull Long draftId) {
        InstagramDraftEntity entity = draftRepository.findById(draftId)
                .orElseThrow(() -> new ResourceNotFoundException("SOC_404", "Draft not found"));
        if (!"APPROVED".equals(entity.getStatus())) {
            throw new BusinessException("SOC_002", "Draft approval required");
        }

        InstagramConnectionEntity connection = connectionRepository.findByUserId(entity.getCreatedBy())
                .orElseThrow(() -> new BusinessException("SOC_001", "Instagram connection expired"));
        if (!"CONNECTED".equals(connection.getStatus())) {
            throw new BusinessException("SOC_001", "Instagram connection expired");
        }

        List<String> mediaOrder = readMediaOrder(entity.getMediaOrderJson());
        if (mediaOrder.isEmpty() || !org.springframework.util.StringUtils.hasText(mediaOrder.getFirst())) {
            throw new BusinessException("SOC_003", "Instagram draft has no media to publish");
        }

        String caption = buildPublishCaption(entity.getCaption(), entity.getHashtags());
        MetaGraphClient.MetaPublishResult publishResult = metaGraphClient.publishImagePost(
                connection.getAccountId(),
                encryptor.decrypt(connection.getTokenEncrypted()),
                mediaOrder.getFirst(),
                caption);

        entity.setStatus("PUBLISHED");
        draftRepository.save(entity);

        Map<String, Object> publishMeta = new LinkedHashMap<>();
        publishMeta.put("draftId", entity.getId());
        publishMeta.put("productId", entity.getProductId());
        publishMeta.put("createdBy", entity.getCreatedBy());
        publishMeta.put("instagramMediaId", publishResult.mediaId());
        publishMeta.put("permalink", buildPermalink(publishResult.mediaId()));
        auditService.write(AuditAction.INSTAGRAM_POST_PUBLISHED, "InstagramDraft",
                String.valueOf(entity.getId()), publishMeta);

        InstagramPublishResultDto dto = new InstagramPublishResultDto();
        dto.setDraftId(entity.getId());
        dto.setStatus(entity.getStatus());
        dto.setInstagramCreationId(publishResult.creationId());
        dto.setInstagramMediaId(publishResult.mediaId());
        dto.setInstagramPermalink(buildPermalink(publishResult.mediaId()));
        dto.setPublishedAt(OffsetDateTime.now());
        return dto;
    }

    @Transactional(readOnly = true)
    public List<InstagramDraftDto> listDrafts() {
        return draftRepository.findAll().stream()
                .map(this::mapDraftToDto)
                .collect(Collectors.toList());
    }

    private InstagramConnectionDto mapConnectionToDto(InstagramConnectionEntity entity) {
        InstagramConnectionDto dto = new InstagramConnectionDto();
        dto.setAccountId(entity.getAccountId());
        dto.setAccountName("seshop.vn");
        dto.setStatus(entity.getStatus());
        dto.setTokenExpiresAt(entity.getTokenExpiresAt());
        return dto;
    }

    private InstagramDraftDto mapDraftToDto(InstagramDraftEntity entity) {
        InstagramDraftDto dto = new InstagramDraftDto();
        dto.setId(entity.getId());
        dto.setProductId(entity.getProductId());
        dto.setCaption(entity.getCaption());
        dto.setHashtags(entity.getHashtags());
        dto.setStatus(entity.getStatus());
        dto.setCreatedAt(entity.getCreatedAt());
        try {
            dto.setMediaOrder(objectMapper.readValue(entity.getMediaOrderJson(), new TypeReference<List<String>>() {
            }));
        } catch (Exception e) {
            dto.setMediaOrder(Collections.emptyList());
        }
        return dto;
    }

    private List<String> readMediaOrder(String mediaOrderJson) {
        try {
            if (mediaOrderJson == null || mediaOrderJson.isBlank()) {
                return Collections.emptyList();
            }
            return objectMapper.readValue(mediaOrderJson, new TypeReference<List<String>>() {
            });
        } catch (Exception exception) {
            return Collections.emptyList();
        }
    }

    private String buildPublishCaption(String caption, String hashtags) {
        StringBuilder builder = new StringBuilder();
        if (caption != null && !caption.isBlank()) {
            builder.append(caption.trim());
        }
        if (hashtags != null && !hashtags.isBlank()) {
            if (builder.length() > 0) {
                builder.append("\n\n");
            }
            builder.append(hashtags.trim());
        }
        return builder.toString();
    }

    private String buildPermalink(String mediaId) {
        return "https://www.instagram.com/p/" + mediaId;
    }

    private String generateState(Long userId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(String.valueOf(userId).getBytes(StandardCharsets.UTF_8));
            return userId + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (Exception e) {
            throw new BusinessException("SOC_001", "Cannot generate state");
        }
    }

    private Long verifyState(String state) {
        try {
            String[] parts = state.split("\\.");
            if (parts.length != 2) {
                throw new BusinessException("SOC_001", "Invalid OAuth state format");
            }
            Long userId = Long.parseLong(parts[0]);
            String expected = generateState(userId);
            if (!expected.equals(state)) {
                throw new BusinessException("SOC_001", "OAuth state signature mismatch");
            }
            return userId;
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException("SOC_001", "Invalid OAuth state");
        }
    }
}
