package org.jejuro.miraero.domain.goal.milestone.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;

import java.time.LocalDateTime;

@Getter
@Builder
public class MilestoneResponse {

    private int step;
    private int percentage;
    private long milestoneAmount;
    private boolean achieved;
    private LocalDateTime achievedAt;
    private MilestoneReportResponse report;

    public static MilestoneResponse from(
            int step,
            Milestone milestone,
            MilestoneReportResponse report
    ) {
        return MilestoneResponse.builder()
                .step(step)
                .percentage(milestone.getMilestonePercentage())
                .milestoneAmount(milestone.getMilestoneAmount())
                .achieved(milestone.isAchieved())
                .achievedAt(milestone.getAchievedAt())
                .report(report)
                .build();
    }
}