package org.jejuro.miraero.domain.aicoach.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCoachMessage {

    private Long aiCoachMessageId;
    private Long aiCoachConversationId;
    private AiCoachMessageSenderType senderType;
    private String content;
    private LocalDateTime createdAt;
}
