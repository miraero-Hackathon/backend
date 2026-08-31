package org.jejuro.miraero.domain.goal.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "끌어쓰기 응답")
public class GoalPullFundsResponse {

  @ApiModelProperty(value = "끌어온 금액(원)", example = "100000")
  private Long pulledAmount;

  @ApiModelProperty(value = "끌어온 뒤 목표 자산의 현재 금액(원)", example = "500000")
  private Long currentAmount;
}
