package org.jejuro.miraero.domain.goal.milestone.service;

import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;

import java.util.List;

public interface MilestoneReportService {

    void generateReport(Long milestoneId, Long goalId);
    void generateReports(List<Milestone> milestones, Long goalId);
}
