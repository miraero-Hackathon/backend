package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.domain.AssetType;

@Getter
@Builder
public class PaceMakerGoalDepositResponse {

  private AssetType assetType;
  private Long assetId;
  private Long depositedAmount;
  private Long remainingBalance;
}
