package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PaceMakerGoalSummaryResponse {

  private Long goalId;
  private String goalName;
  private String goalType;
  private Long goalAmount;
}
