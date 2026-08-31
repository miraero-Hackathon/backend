package org.jejuro.miraero.domain.goal.milestone.domain;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class Milestone {

    private Long milestoneId;
    private final Long goalId;
    private final Integer milestonePercentage;
    private final Long milestoneAmount;

    private boolean achieved;
    private LocalDateTime achievedAt;

    @Builder
    public Milestone(
            Long milestoneId,
            Long goalId,
            Integer milestonePercentage,
            Long milestoneAmount,
            boolean achieved,
            LocalDateTime achievedAt
    ) {
        this.milestoneId = milestoneId;
        this.goalId = goalId;
        this.milestonePercentage = milestonePercentage;
        this.milestoneAmount = milestoneAmount;
        this.achieved = achieved;
        this.achievedAt = achievedAt;
    }

    /**
     * 현재 달성 금액이 마일스톤 기준 금액에 도달했다면
     * 마일스톤을 달성 상태로 변경한다.
     *
     * @return 이번 호출에서 새롭게 달성되었으면 true
     */
    public boolean achieveIfReached(Long currentAmount) {
        if (achieved) {
            return false;
        }

        if (currentAmount != null && currentAmount >= milestoneAmount) {
            achieved = true;
            achievedAt = LocalDateTime.now();
            return true;
        }

        return false;
    }

    public int getStep() {
        return switch (this.milestonePercentage) {
            case 25 -> 1;
            case 50 -> 2;
            case 75 -> 3;
            case 100 -> 4;
            default -> throw new IllegalArgumentException("올바르지 않은 마일스톤 비율입니다.");
        };
    }

    public void markAchieved() {
        this.achieved = true;
        this.achievedAt = LocalDateTime.now();
    }
}