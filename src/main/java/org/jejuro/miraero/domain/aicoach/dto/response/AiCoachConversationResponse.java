package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachConversation;

@Getter
@AllArgsConstructor
@ApiModel(description = "AI 코치 대화 정보")
public class AiCoachConversationResponse {

    @ApiModelProperty(value = "AI 코치 대화 ID", example = "1")
    private Long aiCoachConversationId;
    @ApiModelProperty(value = "대화 제목")
    private String title;
    @ApiModelProperty(value = "마지막 메시지 일시")
    private LocalDateTime lastMessageAt;
    @ApiModelProperty(value = "대화 생성 일시")
    private LocalDateTime createdAt;

    public static AiCoachConversationResponse from(AiCoachConversation conversation) {
        return new AiCoachConversationResponse(
                conversation.getAiCoachConversationId(),
                conversation.getTitle(),
                conversation.getLastMessageAt(),
                conversation.getCreatedAt()
        );
    }
}
