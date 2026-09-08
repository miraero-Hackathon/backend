package org.jejuro.miraero.domain.checkcard.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardUsageState;

@Getter
@ApiModel(description = "체크카드 사용 실적 진행 상태 (카드 1장 기준)")
public class CheckCardUsageStatusResponse {

    private static final int STREAK_TARGET_MONTHS = 6;

    @ApiModelProperty(value = "카드 ID", example = "12")
    private final Long cardId;

    @ApiModelProperty(value = "연속으로 월 30만원 이상 사용한 개월 수", example = "3")
    private final int consecutiveQualifiedMonths;

    @ApiModelProperty(value = "가점을 받기 위한 목표 개월 수", example = "6")
    private final int requiredMonths;

    private CheckCardUsageStatusResponse(Long cardId, int consecutiveQualifiedMonths, int requiredMonths) {
        this.cardId = cardId;
        this.consecutiveQualifiedMonths = consecutiveQualifiedMonths;
        this.requiredMonths = requiredMonths;
    }

    public static CheckCardUsageStatusResponse of(CheckCardUsageState state) {
        return new CheckCardUsageStatusResponse(
                state.getCardId(),
                state.getConsecutiveQualifiedMonths(),
                STREAK_TARGET_MONTHS
        );
    }
}
