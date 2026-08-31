package org.jejuro.miraero.domain.goal.milestone.domain;

import lombok.Builder;
import lombok.Getter;

@Getter
public class MilestoneReport {

    private final Long milestoneReportId;
    private final Long milestoneId;

    private String title;
    private String content;
    private ReportStatus status;

    @Builder
    public MilestoneReport(
            Long milestoneReportId,
            Long milestoneId,
            String title,
            String content,
            ReportStatus status
    ) {
        this.milestoneReportId = milestoneReportId;
        this.milestoneId = milestoneId;
        this.title = title;
        this.content = content;
        this.status = status;
    }

    public void completed(
            String title,
            String content
    ) {
        this.title = title;
        this.content = content;
        this.status = ReportStatus.COMPLETED;
    }

    public void fail() {
        this.status = ReportStatus.FAILED;
    }
}