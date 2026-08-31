package org.jejuro.miraero.domain.pacemaker.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;

@Getter
@Builder
@ApiModel(description = "페이스메이커 개설 응답")
public class PaceMakerCreateResponse {

  @ApiModelProperty(value = "페이스메이커 ID", example = "1")
  private Long autoSavingId;

  @ApiModelProperty(value = "적립될 저금통 ID", example = "1")
  private Long moneyBoxId;

  @ApiModelProperty(value = "저금통이 속한 입출금 계좌 ID", example = "1")
  private Long accountId;

  @ApiModelProperty(value = "1회 적립 상한. 없으면 null", example = "30000")
  private Long maxAmount;

  @ApiModelProperty(value = "페이스메이커 상태", example = "ACTIVE")
  private String autoSavingStatus;

  public static PaceMakerCreateResponse from(AutoSaving autoSaving) {
    return PaceMakerCreateResponse.builder()
        .autoSavingId(autoSaving.getAutoSavingId())
        .moneyBoxId(autoSaving.getMoneyBoxId())
        .accountId(autoSaving.getAccountId())
        .maxAmount(autoSaving.getMaxAmount())
        .autoSavingStatus(autoSaving.getAutoSavingStatus())
        .build();
  }
}
