package org.jejuro.miraero.domain.goal.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.GoalType;

@Getter
@Builder
public class GoalListResponse {
    private Long goalId;
    private String goalName;
    private GoalType goalType;
    private Integer progressRate;
    private GoalStatus status;

    public static GoalListResponse from(Goal goal, Integer progressRate){
        return GoalListResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .goalType(goal.getGoalType())
                .progressRate(progressRate)
                .status(goal.getGoalStatus())
                .build();
    }
}
