package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerMoneyBoxResponse {

  private Long moneyBoxId;
  private Long balance;
  private String maskedAccountNumber;
}
