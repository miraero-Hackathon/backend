package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaceMakerGoalDepositAssetRowResponse {

  private Long goalId;
  private String assetType;
  private Long assetId;
  private String financialInstitutionName;
  private String maskedAccountNumber;
  private Long balance;
}
