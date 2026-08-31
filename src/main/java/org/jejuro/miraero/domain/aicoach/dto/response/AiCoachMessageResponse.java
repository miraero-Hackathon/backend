package org.jejuro.miraero.domain.aicoach.dto.response;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessage;
import org.jejuro.miraero.domain.aicoach.domain.AiCoachMessageSenderType;

@Getter
@AllArgsConstructor
@ApiModel(description = "AI 코치 메시지")
public class AiCoachMessageResponse {

    @ApiModelProperty(value = "AI 코치 메시지 ID", example = "1")
    private Long aiCoachMessageId;
    @ApiModelProperty(value = "메시지 발신자 유형. USER 또는 ASSISTANT", example = "ASSISTANT")
    private AiCoachMessageSenderType senderType;
    @ApiModelProperty(value = "메시지 본문")
    private String content;
    @ApiModelProperty(value = "메시지 생성 일시", example = "2026-08-12T14:31:00")
    private LocalDateTime createdAt;

    public static AiCoachMessageResponse from(AiCoachMessage message) {
        return new AiCoachMessageResponse(
                message.getAiCoachMessageId(),
                message.getSenderType(),
                message.getContent(),
                message.getCreatedAt()
        );
    }
}
