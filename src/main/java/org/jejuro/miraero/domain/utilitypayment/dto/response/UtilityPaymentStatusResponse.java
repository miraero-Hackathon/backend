package org.jejuro.miraero.domain.utilitypayment.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityPaymentState;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityType;

@Getter
@ApiModel(description = "통신비/공과금 항목별 진행 상태")
public class UtilityPaymentStatusResponse {

    private static final int STREAK_TARGET_MONTHS = 6;

    @ApiModelProperty(value = "항목 코드", example = "TELECOM")
    private final String utilityType;

    @ApiModelProperty(value = "항목명", example = "통신비")
    private final String displayName;

    @ApiModelProperty(value = "연속 납부 개월 수", example = "5")
    private final int consecutivePaidMonths;

    @ApiModelProperty(value = "가점을 받기 위한 목표 개월 수", example = "6")
    private final int requiredMonths;

    @ApiModelProperty(value = "연속 미납 개월 수", example = "0")
    private final int consecutiveMissedMonths;

    private UtilityPaymentStatusResponse(
            String utilityType,
            String displayName,
            int consecutivePaidMonths,
            int requiredMonths,
            int consecutiveMissedMonths
    ) {
        this.utilityType = utilityType;
        this.displayName = displayName;
        this.consecutivePaidMonths = consecutivePaidMonths;
        this.requiredMonths = requiredMonths;
        this.consecutiveMissedMonths = consecutiveMissedMonths;
    }

    // state가 null이면(=한 번도 판정된 적 없으면) 전부 0으로 시작한 것처럼 응답
    public static UtilityPaymentStatusResponse of(UtilityType type, UtilityPaymentState state) {
        int paidMonths = state == null ? 0 : state.getConsecutivePaidMonths();
        int missedMonths = state == null ? 0 : state.getConsecutiveMissedMonths();

        return new UtilityPaymentStatusResponse(
                type.name(),
                type.getDisplayName(),
                paidMonths,
                STREAK_TARGET_MONTHS,
                missedMonths
        );
    }
}
