package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerWithdrawalAccountResponse {

  private Long accountId;
  private String financialInstitutionName;
  private String maskedAccountNumber;
  private Long balance;
}
