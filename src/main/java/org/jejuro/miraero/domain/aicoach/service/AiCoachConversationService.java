package org.jejuro.miraero.domain.aicoach.service;

import java.util.List;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationCreateResponse;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachConversationResponse;

public interface AiCoachConversationService {

    AiCoachConversationResponse getLatestConversation(Long userId);

    List<AiCoachConversationResponse> getConversations(Long userId);

    AiCoachConversationCreateResponse createConversation(Long userId);

    void deleteConversation(Long userId, Long conversationId);
}
