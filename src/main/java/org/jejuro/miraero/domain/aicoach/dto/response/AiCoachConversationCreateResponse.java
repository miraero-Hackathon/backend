package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;

@Getter
@AllArgsConstructor
@ApiModel(description = "AI 코치 대화 생성 응답")
public class AiCoachConversationCreateResponse {

    @ApiModelProperty(value = "AI 코치 대화 ID", example = "1")
    private Long aiCoachConversationId;
    @ApiModelProperty(value = "대화 제목. 첫 질문 후 갱신될 수 있음", example = "AI 코치 상담")
    private String title;
    @ApiModelProperty(value = "대화 생성 일시", example = "2026-08-12T14:30:00")
    private LocalDateTime createdAt;

    public static AiCoachConversationCreateResponse from(AiCoachConversation conversation) {
        return new AiCoachConversationCreateResponse(
                conversation.getAiCoachConversationId(),
                conversation.getTitle(),
                conversation.getCreatedAt()
        );
    }
}
