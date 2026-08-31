package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaceMakerGoalWithdrawalAccountRowResponse {

  private Long goalId;
  private Long accountId;
  private String financialInstitutionName;
  private String maskedAccountNumber;
  private Long balance;
}
