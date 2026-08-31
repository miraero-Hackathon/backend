package org.jejuro.miraero.domain.aicoach.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AiCoachConversationServiceImplTest {

    private static final Long USER_ID = 1L;
    private static final Long CONVERSATION_ID = 10L;

    @Mock
    private AiCoachConversationMapper aiCoachConversationMapper;

    private AiCoachConversationService aiCoachConversationService;

    @BeforeEach
    void setUp() {
        aiCoachConversationService = new AiCoachConversationServiceImpl(aiCoachConversationMapper);
    }

    @Test
    @DisplayName("최근 대화방이 있으면 응답 DTO로 반환한다")
    void getLatestConversation_returnsResponseWhenConversationExists() {
        AiCoachConversation conversation = createConversation(CONVERSATION_ID, "자산 관리 상담");
        when(aiCoachConversationMapper.findLatestByUserId(USER_ID)).thenReturn(conversation);

        AiCoachConversationResponse response = aiCoachConversationService.getLatestConversation(USER_ID);

        assertNotNull(response);
        assertEquals(CONVERSATION_ID, response.getAiCoachConversationId());
        assertEquals("자산 관리 상담", response.getTitle());
        assertEquals(conversation.getLastMessageAt(), response.getLastMessageAt());
        assertEquals(conversation.getCreatedAt(), response.getCreatedAt());
        verify(aiCoachConversationMapper).findLatestByUserId(USER_ID);
    }

    @Test
    @DisplayName("최근 대화방이 없으면 null을 반환한다")
    void getLatestConversation_returnsNullWhenConversationDoesNotExist() {
        when(aiCoachConversationMapper.findLatestByUserId(USER_ID)).thenReturn(null);

        AiCoachConversationResponse response = aiCoachConversationService.getLatestConversation(USER_ID);

        assertNull(response);
        verify(aiCoachConversationMapper).findLatestByUserId(USER_ID);
    }

    @Test
    @DisplayName("사용자 대화방 목록을 응답 DTO 목록으로 변환한다")
    void getConversations_returnsResponseList() {
        AiCoachConversation firstConversation = createConversation(10L, "첫 번째 대화");
        AiCoachConversation secondConversation = createConversation(20L, "두 번째 대화");
        when(aiCoachConversationMapper.findAllByUserId(USER_ID))
                .thenReturn(List.of(firstConversation, secondConversation));

        List<AiCoachConversationResponse> responses = aiCoachConversationService.getConversations(USER_ID);

        assertEquals(2, responses.size());
        assertEquals(10L, responses.get(0).getAiCoachConversationId());
        assertEquals("첫 번째 대화", responses.get(0).getTitle());
        assertEquals(20L, responses.get(1).getAiCoachConversationId());
        assertEquals("두 번째 대화", responses.get(1).getTitle());
        verify(aiCoachConversationMapper).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("대화방 목록이 없으면 빈 목록을 반환한다")
    void getConversations_returnsEmptyListWhenConversationDoesNotExist() {
        when(aiCoachConversationMapper.findAllByUserId(USER_ID)).thenReturn(List.of());

        List<AiCoachConversationResponse> responses = aiCoachConversationService.getConversations(USER_ID);

        assertTrue(responses.isEmpty());
        verify(aiCoachConversationMapper).findAllByUserId(USER_ID);
    }

    @Test
    @DisplayName("새 대화방을 기본 제목과 마지막 메시지 시각으로 생성한다")
    void createConversation_savesDefaultTitleAndLastMessageAt() {
        LocalDateTime createdAt = LocalDateTime.of(2026, 8, 5, 10, 0);
        AiCoachConversation persistedConversation = AiCoachConversation.builder()
                .aiCoachConversationId(CONVERSATION_ID)
                .userId(USER_ID)
                .title("새 대화")
                .lastMessageAt(createdAt)
                .createdAt(createdAt)
                .build();
        ArgumentCaptor<AiCoachConversation> conversationCaptor =
                ArgumentCaptor.forClass(AiCoachConversation.class);
        ArgumentCaptor<LocalDateTime> lastMessageAtCaptor = ArgumentCaptor.forClass(LocalDateTime.class);

        doAnswer(invocation -> {
            AiCoachConversation conversation = invocation.getArgument(0);
            ReflectionTestUtils.setField(conversation, "aiCoachConversationId", CONVERSATION_ID);
            return 1;
        }).when(aiCoachConversationMapper).save(any(AiCoachConversation.class));
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(persistedConversation);

        AiCoachConversationCreateResponse response = aiCoachConversationService.createConversation(USER_ID);

        verify(aiCoachConversationMapper).save(conversationCaptor.capture());
        verify(aiCoachConversationMapper).updateLastMessageAt(
                eq(USER_ID),
                eq(CONVERSATION_ID),
                lastMessageAtCaptor.capture()
        );
        assertEquals("새 대화", conversationCaptor.getValue().getTitle());
        assertNotNull(conversationCaptor.getValue().getLastMessageAt());
        assertNotNull(lastMessageAtCaptor.getValue());
        assertEquals(conversationCaptor.getValue().getLastMessageAt(), lastMessageAtCaptor.getValue());
        assertEquals(CONVERSATION_ID, response.getAiCoachConversationId());
        assertEquals("새 대화", response.getTitle());
        assertEquals(createdAt, response.getCreatedAt());
    }

    @Test
    @DisplayName("사용자 소유 대화방을 삭제한다")
    void deleteConversation_deletesOwnedConversation() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID))
                .thenReturn(createConversation(CONVERSATION_ID, "삭제할 대화"));

        aiCoachConversationService.deleteConversation(USER_ID, CONVERSATION_ID);

        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachConversationMapper).deleteByIdAndUserId(USER_ID, CONVERSATION_ID);
    }

    @Test
    @DisplayName("대화방이 없으면 RESOURCE_NOT_FOUND 예외를 발생시키고 삭제하지 않는다")
    void deleteConversation_throwsNotFoundWhenConversationDoesNotExist() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachConversationService.deleteConversation(USER_ID, CONVERSATION_ID)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachConversationMapper, never()).deleteByIdAndUserId(anyLong(), anyLong());
    }

    @Test
    @DisplayName("다른 사용자 소유 대화방이면 RESOURCE_NOT_FOUND 예외를 발생시키고 삭제하지 않는다")
    void deleteConversation_throwsNotFoundWhenConversationIsNotOwnedByUser() {
        when(aiCoachConversationMapper.findByIdAndUserId(USER_ID, CONVERSATION_ID)).thenReturn(null);

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> aiCoachConversationService.deleteConversation(USER_ID, CONVERSATION_ID)
        );

        assertEquals(CommonErrorCode.RESOURCE_NOT_FOUND, exception.getErrorCode());
        verify(aiCoachConversationMapper).findByIdAndUserId(USER_ID, CONVERSATION_ID);
        verify(aiCoachConversationMapper, never()).deleteByIdAndUserId(anyLong(), anyLong());
    }

    private AiCoachConversation createConversation(Long conversationId, String title) {
        LocalDateTime now = LocalDateTime.of(2026, 8, 5, 10, 0);
        return AiCoachConversation.builder()
                .aiCoachConversationId(conversationId)
                .userId(USER_ID)
                .title(title)
                .lastMessageAt(now)
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
