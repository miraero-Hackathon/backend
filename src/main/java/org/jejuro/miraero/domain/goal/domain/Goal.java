package org.jejuro.miraero.domain.goal.domain;


import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class Goal {
    private Long goalId;
    private Long userId;
    private GoalType goalType;
    private String goalName;
    private Long goalAmount;
    private Long startAmount;
    private LocalDate goalDate;
    private LocalDate startDate;
    private GoalStatus goalStatus;
    private boolean isCollected;
    private LocalDate completedDate;
    private LocalDateTime createdAt;

    @Builder
    public Goal(
            Long goalId,
            Long userId,
            GoalType goalType,
            String goalName,
            Long goalAmount,
            Long startAmount,
            LocalDate goalDate,
            LocalDate startDate,
            GoalStatus goalStatus,
            boolean isCollected,
            LocalDate completedDate
    ) {
        this.goalId = goalId;
        this.userId = userId;
        this.goalType = goalType;
        this.goalName = goalName;
        this.goalAmount = goalAmount;
        this.startAmount = startAmount;
        this.goalDate = goalDate;
        this.startDate = startDate;
        this.goalStatus = goalStatus;
        this.isCollected = isCollected;
        this.completedDate = completedDate;
    }

    public void setGoalId(Long goalId){
        this.goalId = goalId;
    }

    public void update(
            String goalName,
            Long goalAmount,
            LocalDate goalDate
    ) {
        if (goalName != null) {
            this.goalName = goalName;
        }

        if (goalAmount != null) {
            this.goalAmount = goalAmount;
        }

        if (goalDate != null) {
            this.goalDate = goalDate;
        }
    }

    public void changeStatus(GoalStatus goalStatus) {
        this.goalStatus = goalStatus;
    }
}
