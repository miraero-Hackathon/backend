package org.jejuro.miraero.domain.aicoach.service;

import java.util.List;
import org.jejuro.miraero.domain.aicoach.dto.request.AiCoachMessageCreateRequest;
import org.jejuro.miraero.domain.aicoach.dto.response.AiCoachMessageResponse;

public interface AiCoachMessageService {

    List<AiCoachMessageResponse> getMessages(Long userId, Long conversationId);

    AiCoachMessageResponse saveUserMessage(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    );

    AiCoachMessageResponse sendQuestion(
            Long userId,
            Long conversationId,
            AiCoachMessageCreateRequest request
    );
}
