package com.seshop.marketing.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.seshop.marketing.api.dto.InstagramPublishResultDto;
import com.seshop.marketing.infrastructure.MetaGraphClient;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramConnectionRepository;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftEntity;
import com.seshop.marketing.infrastructure.persistence.InstagramDraftRepository;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class InstagramServiceTest {

    @Mock
    private InstagramConnectionRepository connectionRepository;

    @Mock
    private InstagramDraftRepository draftRepository;

    @Mock
    private MetaGraphClient metaGraphClient;

    @Test
    @SuppressWarnings("null")
    void publishesApprovedDraftAndMarksItAsPublished() {
        ObjectMapper objectMapper = new ObjectMapper();
        InstagramService service = new InstagramService(connectionRepository, draftRepository, objectMapper, metaGraphClient);

        InstagramConnectionEntity connection = new InstagramConnectionEntity();
        connection.setUserId(7L);
        connection.setAccountId("ig-123");
        connection.setTokenEncrypted("page-token");
        connection.setStatus("CONNECTED");

        InstagramDraftEntity draft = new InstagramDraftEntity();
        draft.setId(11L);
        draft.setCreatedBy(7L);
        draft.setProductId(501L);
        draft.setCaption("Fresh vintage drop");
        draft.setHashtags("#seshop #newarrival");
        draft.setMediaOrderJson("[\"https://cdn.example.com/image-1.jpg\"]");
        draft.setStatus("APPROVED");

        when(connectionRepository.findByUserId(7L)).thenReturn(Optional.of(connection));
        when(draftRepository.findById(11L)).thenReturn(Optional.of(draft));
        when(metaGraphClient.publishImagePost(eq("ig-123"), eq("page-token"), eq("https://cdn.example.com/image-1.jpg"), eq("Fresh vintage drop\n\n#seshop #newarrival")))
                .thenReturn(new MetaGraphClient.MetaPublishResult("creation-7", "media-99"));
        when(draftRepository.save(any(InstagramDraftEntity.class))).thenAnswer(invocation -> invocation.getArgument(0, InstagramDraftEntity.class));

        InstagramPublishResultDto result = service.publishDraft(11L);

        assertThat(result.getDraftId()).isEqualTo(11L);
        assertThat(result.getStatus()).isEqualTo("PUBLISHED");
        assertThat(result.getInstagramCreationId()).isEqualTo("creation-7");
        assertThat(result.getInstagramMediaId()).isEqualTo("media-99");
        assertThat(result.getInstagramPermalink()).isEqualTo("https://www.instagram.com/p/media-99");
        assertThat(draft.getStatus()).isEqualTo("PUBLISHED");

        verify(metaGraphClient).publishImagePost(eq("ig-123"), eq("page-token"), eq("https://cdn.example.com/image-1.jpg"), eq("Fresh vintage drop\n\n#seshop #newarrival"));
    }
}