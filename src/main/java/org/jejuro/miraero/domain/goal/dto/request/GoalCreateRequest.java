package org.jejuro.miraero.domain.goal.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalType;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "목표 생성 요청")
public class GoalCreateRequest {
    @ApiModelProperty(value = "목표 유형", required = true, example = "HOUSE")
    @NotNull
    private GoalType goalType;
    @ApiModelProperty(value = "목표명", required = true, example = "내 집 마련")
    @NotBlank
    private String goalName;
    @ApiModelProperty(value = "목표 금액(원)", required = true, example = "100000000")
    @NotNull @Positive
    private Long goalAmount;
    @ApiModelProperty(value = "목표 기간(개월)", required = true, example = "60")
    @NotNull @Positive
    private Integer goalMonths;
    @ApiModelProperty(value = "시작 자금(원)", required = true, example = "10000000")
    @NotNull @PositiveOrZero
    private Long startAmount;
    @ApiModelProperty(value = "목표에 연결할 자산 목록")
    private List<GoalAssetRequest> assets;
}
