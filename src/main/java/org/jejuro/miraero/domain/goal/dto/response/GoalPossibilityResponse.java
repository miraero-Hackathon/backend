package org.jejuro.miraero.domain.goal.dto.response;


import lombok.Builder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.domain.GoalPossibility;

@Getter
@Builder
@ApiModel(description = "목표 달성 가능성 계산 결과")
public class GoalPossibilityResponse {
    @ApiModelProperty(value = "목표 달성을 위해 필요한 월 저축액(원)")
    private Long requiredMonthly;
    @ApiModelProperty(value = "현재 기준 월 가용 금액(원)")
    private Long availableMonthly;
    @ApiModelProperty(value = "목표 달성 가능 여부")
    private GoalPossibility possible;
}
