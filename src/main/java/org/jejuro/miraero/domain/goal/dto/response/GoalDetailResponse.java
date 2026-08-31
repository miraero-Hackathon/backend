package org.jejuro.miraero.domain.goal.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.GoalType;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoalDetailResponse {

    private Long goalId;
    private GoalType goalType;
    private String goalName;
    private Long goalAmount;
    private Long currentAmount;
    private Long startAmount;
    private Integer progressRate;
    private GoalStatus status;
    private GoalPeriodResponse period;
    private GoalPaceResponse pace;
}
