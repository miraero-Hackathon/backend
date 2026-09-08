package org.jejuro.miraero.domain.credit.dto.response;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.credit.domain.CreditScoreEvent;

@Getter
@ApiModel(description = "신용점수 변동 이력 응답")
public class CreditScoreEventResponse {

    @ApiModelProperty(value = "변동 사유 코드", example = "SHORT_TERM_OVERDUE")
    private final String reasonCode;

    // 화면에 그대로 찍을 한글 문구. 예: "단기 연체"
    @ApiModelProperty(value = "변동 사유 설명", example = "단기 연체")
    private final String reasonDisplayName;

    @ApiModelProperty(value = "변동 폭", example = "-100")
    private final int delta;

    @ApiModelProperty(value = "변동 후 점수", example = "565")
    private final int scoreAfter;

    @ApiModelProperty(value = "부가 설명", example = "저축목표 '전세보증금' 자동이체 30일 미납")
    private final String description;

    @ApiModelProperty(value = "발생 시각")
    private final LocalDateTime occurredAt;

    private CreditScoreEventResponse(
            String reasonCode,
            String reasonDisplayName,
            int delta,
            int scoreAfter,
            String description,
            LocalDateTime occurredAt
    ) {
        this.reasonCode = reasonCode;
        this.reasonDisplayName = reasonDisplayName;
        this.delta = delta;
        this.scoreAfter = scoreAfter;
        this.description = description;
        this.occurredAt = occurredAt;
    }
    public static CreditScoreEventResponse of(CreditScoreEvent event) {
        return new CreditScoreEventResponse(
                // .name()  → enum 상수 이름 그대로("SHORT_TERM_OVERDUE") - 코드값
                event.getReasonCode().name(),
                // .getDisplayName() → enum이 들고 있는 한글 설명("단기 연체") - 화면 표시용
                event.getReasonCode().getDisplayName(),
                event.getDelta(),
                event.getScoreAfter(),
                event.getDescription(),
                event.getOccurredAt()
        );
    }
}
