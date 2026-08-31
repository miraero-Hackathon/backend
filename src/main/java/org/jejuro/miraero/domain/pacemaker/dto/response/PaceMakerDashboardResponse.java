package org.jejuro.miraero.domain.pacemaker.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerDashboardResponse {

  private Long autoSavingId;
  private String status;
  private Long maxAmount;

  private PaceMakerMoneyBoxResponse moneyBox;
  private PaceMakerTodaySavingResponse todaySaving;

  private Integer currentStreak;
  private List<PaceMakerWeeklyStreakResponse> weeklyStreak;
  private Integer monthlySuccessCount;
}
