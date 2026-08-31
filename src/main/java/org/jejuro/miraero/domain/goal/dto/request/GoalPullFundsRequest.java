package org.jejuro.miraero.domain.goal.dto.request;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@ApiModel(description = "끌어쓰기 요청")
public class GoalPullFundsRequest {

  @ApiModelProperty(
      value = "끌어올 입출금 계좌 ID. 이미 목표에 연결된 계좌는 쓸 수 없습니다.",
      required = true,
      example = "1"
  )
  @NotNull
  private Long sourceAccountId;

  @ApiModelProperty(value = "끌어올 금액(원). 출처 계좌 잔액까지만 가능합니다.", required = true, example = "100000")
  @NotNull
  @Positive
  private Long amount;
}
