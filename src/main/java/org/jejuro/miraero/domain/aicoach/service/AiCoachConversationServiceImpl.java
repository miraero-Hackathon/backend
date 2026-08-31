package org.jejuro.miraero.domain.aicoach.service;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;
import org.jejuro.miraero.domain.aicoach.mapper.AiCoachConversationMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiCoachConversationServiceImpl implements AiCoachConversationService {

    private static final String DEFAULT_CONVERSATION_TITLE = "새 대화";

    private final AiCoachConversationMapper aiCoachConversationMapper;

    @Override
    public AiCoachConversationResponse getLatestConversation(Long userId) {
        AiCoachConversation conversation = aiCoachConversationMapper.findLatestByUserId(userId);
        return conversation == null ? null : AiCoachConversationResponse.from(conversation);
    }

    @Override
    public List<AiCoachConversationResponse> getConversations(Long userId) {
        return aiCoachConversationMapper.findAllByUserId(userId).stream()
                .map(AiCoachConversationResponse::from)
                .toList();
    }

    @Override
    @Transactional
    public AiCoachConversationCreateResponse createConversation(Long userId) {
        LocalDateTime now = LocalDateTime.now();
        AiCoachConversation conversation = AiCoachConversation.builder()
                .userId(userId)
                .title(DEFAULT_CONVERSATION_TITLE)
                .lastMessageAt(now)
                .build();

        aiCoachConversationMapper.save(conversation);
        aiCoachConversationMapper.updateLastMessageAt(
                userId,
                conversation.getAiCoachConversationId(),
                now
        );

        return AiCoachConversationCreateResponse.from(
                getOwnedConversation(userId, conversation.getAiCoachConversationId())
        );
    }

    @Override
    @Transactional
    public void deleteConversation(Long userId, Long conversationId) {
        getOwnedConversation(userId, conversationId);
        aiCoachConversationMapper.deleteByIdAndUserId(userId, conversationId);
    }

    private AiCoachConversation getOwnedConversation(Long userId, Long conversationId) {
        AiCoachConversation conversation = aiCoachConversationMapper.findByIdAndUserId(userId, conversationId);

        if (conversation == null) {
            throw new BusinessException(CommonErrorCode.RESOURCE_NOT_FOUND);
        }
        return conversation;
    }
}
