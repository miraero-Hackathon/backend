package org.jejuro.miraero.domain.goal.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder @NoArgsConstructor @AllArgsConstructor
@ApiModel(description = "목표 달성 가능성 확인 요청")
public class GoalPossibilityRequest {
    @ApiModelProperty(value = "목표 금액(원)", required = true, example = "100000000")
    @NotNull @Positive private Long goalAmount;
    @ApiModelProperty(value = "목표 기간(개월)", required = true, example = "60")
    @NotNull @Positive private Integer goalMonths;
    @ApiModelProperty(value = "시작 자금(원)", required = true, example = "10000000")
    @NotNull @PositiveOrZero private Long startAmount;
}
