package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.exception.MilestoneErrorCode;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MilestoneReportAsyncService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    MilestoneReportAsyncService.class
            );

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;

    private final MilestoneReportDataService dataService;
    private final MilestoneReportAiService aiService;

    public void generate(
            Long milestoneId,
            Long goalId,
            Long reportId
    ) {

        try {

            Goal goal =
                    goalMapper.findById(goalId);

            Milestone milestone =
                    milestoneMapper.findById(milestoneId);

            validate(
                    goal,
                    milestone
            );

            MilestoneReportAiRequest request =
                    dataService.buildAiRequest(
                            goal,
                            milestone
                    );

            MilestoneReportAiService.ParsedReport report =
                    aiService.generateReport(
                            request
                    );

            log.info(
                    "AI REPORT RESULT - reportId={}, title={}, content={}",
                    reportId,
                    report.title(),
                    report.content()
            );

            int updated =
                    milestoneReportMapper.updateCompleted(
                            reportId,
                            report.title(),
                            report.content()
                    );


            if (updated == 0) {
                log.warn(
                        "마일스톤 리포트 성공 상태 업데이트 실패. reportId={}",
                        reportId
                );
            }

        } catch (Exception e) {

            log.error(
                    "마일스톤 AI 리포트 생성 실패. " +
                            "milestoneId={}, goalId={}, reportId={}",
                    milestoneId,
                    goalId,
                    reportId,
                    e
            );

            int updated =
                    milestoneReportMapper.updateFailed(
                            reportId
                    );

            if (updated == 0) {
                log.warn(
                        "마일스톤 리포트 실패 상태 업데이트 대상이 없습니다. " +
                                "reportId={}",
                        reportId
                );
            }
        }
    }

    private void validate(
            Goal goal,
            Milestone milestone
    ) {

        if (goal == null) {
            throw new BusinessException(
                    GoalErrorCode.GOAL_NOT_FOUND
            );
        }

        if (milestone == null) {
            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_NOT_FOUND
            );
        }
    }
}