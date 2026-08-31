package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Getter;

@Getter
public class PaceMakerDashboardSummaryResponse {

  private Long autoSavingId;
  private String status;
  private Long maxAmount;

  private Long moneyBoxId;
  private Long balance;
  private String maskedAccountNumber;

  private Boolean todaySaved;
  private Long todaySavingAmount;
}
