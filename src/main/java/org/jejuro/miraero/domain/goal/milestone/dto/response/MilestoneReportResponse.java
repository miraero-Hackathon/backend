package org.jejuro.miraero.domain.goal.milestone.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;

@Getter
@Builder
public class MilestoneReportResponse {

    private Long milestoneReportId;
    private String status;
    private String title;
    private String content;

    public static MilestoneReportResponse from(
            MilestoneReport report
    ) {
        if (report == null) {
            return null;
        }

        return MilestoneReportResponse.builder()
                .milestoneReportId(report.getMilestoneReportId())
                .status(report.getStatus().name())
                .title(report.getTitle())
                .content(report.getContent())
                .build();
    }
}