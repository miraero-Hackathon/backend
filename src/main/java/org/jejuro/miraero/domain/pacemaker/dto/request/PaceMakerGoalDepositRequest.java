package org.jejuro.miraero.domain.pacemaker.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Getter @NoArgsConstructor @AllArgsConstructor
@ApiModel(description = "목표 저축금 입금 요청")
public class PaceMakerGoalDepositRequest {
  @ApiModelProperty(value = "출금할 머니박스 ID", required = true, example = "1")
  @NotNull(message = "MoneyBox id is required.") private Long moneyBoxId;

  @ApiModelProperty(value = "입금할 목표 연결 자산 유형. ACCOUNT 또는 MONEY_BOX", required = true, example = "MONEY_BOX")
  @NotNull(message = "Asset type is required.") private AssetType assetType;

  @ApiModelProperty(value = "입금할 목표 연결 자산 ID. GET /api/goals/{goalId}/assets 응답의 assetId", required = true, example = "2")
  @NotNull(message = "Asset id is required.") private Long assetId;

  @ApiModelProperty(value = "입금 금액(원)", required = true, example = "50000")
  @NotNull(message = "Deposit amount is required.")
  @Positive(message = "Deposit amount must be greater than 0.") private Long amount;
}
