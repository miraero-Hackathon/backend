package org.jejuro.miraero.domain.pacemaker.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerGoalResponse {

  private Long goalId;
  private String goalName;
  private String goalType;
  private Long goalAmount;
  private Long totalSavedAmount;
  private List<PaceMakerDepositAssetResponse> depositAssets;
  private List<PaceMakerWithdrawalAccountResponse> withdrawalAccounts;
}
