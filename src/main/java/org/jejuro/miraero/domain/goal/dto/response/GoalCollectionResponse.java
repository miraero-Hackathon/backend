package org.jejuro.miraero.domain.goal.dto.response;


import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalType;

import java.time.LocalDate;

@Getter
@Builder
public class GoalCollectionResponse {

    private Long goalId;
    private String goalName;
    private GoalType goalType;
    private Long goalAmount;
    private LocalDate completedDate;

    public static GoalCollectionResponse from(Goal goal){
        return GoalCollectionResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .goalType(goal.getGoalType())
                .goalAmount(goal.getGoalAmount())
                .completedDate(goal.getCompletedDate())
                .build();
    }
}
