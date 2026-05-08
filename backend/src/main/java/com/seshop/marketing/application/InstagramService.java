package com.seshop.marketing.application;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.marketing.api.dto.InstagramConnectionDto;
import com.seshop.marketing.api.dto.InstagramDraftDto;
import com.seshop.marketing.infrastructure.MetaGraphClient;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionRepository;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftRepository;
import com.seshop.shared.exception.BusinessException;
import com.seshop.shared.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class InstagramService {

    private final InstagramConnectionRepository connectionRepository;
    private final InstagramDraftRepository draftRepository;
    private final ObjectMapper objectMapper;
    private final MetaGraphClient metaGraphClient;

    public InstagramService(
            InstagramConnectionRepository connectionRepository,
            InstagramDraftRepository draftRepository,
            ObjectMapper objectMapper,
            MetaGraphClient metaGraphClient
    ) {
        this.connectionRepository = connectionRepository;
        this.draftRepository = draftRepository;
        this.objectMapper = objectMapper;
        this.metaGraphClient = metaGraphClient;
    }

    @Transactional(readOnly = true)
    public InstagramConnectionDto getConnectionStatus(Long userId) {
        return connectionRepository.findByUserId(userId)
                .map(this::mapConnectionToDto)
                .orElseThrow(() -> new ResourceNotFoundException("SOC_404", "Instagram connection not found"));
    }

    @Transactional
    public String startConnection(Long userId) {
        InstagramConnectionEntity entity = connectionRepository.findByUserId(userId).orElse(new InstagramConnectionEntity());
        entity.setUserId(userId);
        entity.setAccountId("pending");
        entity.setTokenEncrypted("pending");
        entity.setTokenExpiresAt(OffsetDateTime.now().plusMinutes(10));
        entity.setStatus("PENDING_AUTH");
        connectionRepository.save(entity);
        return metaGraphClient.buildAuthorizationUrl(String.valueOf(userId));
    }

    @Transactional
    public InstagramConnectionDto completeConnection(Long userId, String code) {
        MetaGraphClient.MetaTokenResult tokenResult = metaGraphClient.exchangeCode(code);
        MetaGraphClient.MetaAccountResult account = metaGraphClient.getAccount(tokenResult.accessToken());

        InstagramConnectionEntity entity = connectionRepository.findByUserId(userId).orElse(new InstagramConnectionEntity());
        entity.setUserId(userId);
        entity.setAccountId(account.accountId());
        entity.setTokenEncrypted(account.accessToken());
        entity.setTokenExpiresAt(OffsetDateTime.now().plusSeconds(tokenResult.expiresInSeconds()));
        entity.setStatus("CONNECTED");
        return mapConnectionToDto(connectionRepository.save(entity));
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
        entity.setProductId(request.getProductId());
        entity.setCaption(request.getCaption());
        entity.setHashtags(request.getHashtags());
        
        try {
            entity.setMediaOrderJson(objectMapper.writeValueAsString(request.getMediaOrder()));
        } catch (JsonProcessingException e) {
            entity.setMediaOrderJson("[]");
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
            dto.setMediaOrder(objectMapper.readValue(entity.getMediaOrderJson(), new TypeReference<List<String>>() {}));
        } catch (Exception e) {
            dto.setMediaOrder(Collections.emptyList());
        }
        return dto;
    }
}
