package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerDepositAssetResponse {

  private String assetType;
  private Long assetId;
  private String financialInstitutionName;
  private String maskedAccountNumber;
  private Long balance;
}
