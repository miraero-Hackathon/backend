package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.milestone.exception.MilestoneErrorCode;
import org.jejuro.miraero.global.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;
import org.jejuro.miraero.domain.goal.milestone.domain.ReportStatus;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MilestoneReportServiceImpl
        implements MilestoneReportService {

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;
    private final MilestoneReportAsyncService milestoneReportAsyncService;

    @Override
    @Transactional
    public void generateReport(
            Long milestoneId,
            Long goalId
    ) {
        if (milestoneId == null || goalId == null) {
            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_NOT_FOUND
            );
        }

        Milestone milestone =
                milestoneMapper.findById(milestoneId);


        if (milestone == null) {
            throw new BusinessException(
                    MilestoneErrorCode.MILESTONE_NOT_FOUND
            );
        }
        if (!milestone.isAchieved()) {
            return;
        }


        Goal goal =
                goalMapper.findById(goalId);

        if (goal == null) {
            throw new BusinessException(
                    GoalErrorCode.GOAL_NOT_FOUND
            );
        }

        // 마일스톤과 목표 관계 검증
        if (milestone.getGoalId() != null
                && !goalId.equals(milestone.getGoalId())) {
            return;
        }

        MilestoneReport existingReport =
                milestoneReportMapper.findByMilestoneId(
                        milestoneId
                );


        /*
         * 기존 리포트가 있는 경우
         */
        if (existingReport != null) {

            /*
             * PENDING 또는 SUCCESS
             * → 이미 처리 중이거나 완료된 상태이므로 종료
             */
            if (existingReport.getStatus()
                    != ReportStatus.FAILED) {
                return;
            }

            /*
             * FAILED → PENDING
             *
             * FAILED 상태인 경우에만 변경되도록
             * 조건부 UPDATE를 사용한다.
             */
            int updated =
                    milestoneReportMapper.updatePending(
                            existingReport.getMilestoneReportId()
                    );

            /*
             * 다른 요청이 먼저 재시작했다면 종료
             */
            if (updated == 0) {
                return;
            }

            /*
             * 기존 reportId를 그대로 사용하여
             * 비동기 AI 생성
             */
            milestoneReportAsyncService.generate(
                    milestoneId,
                    goalId,
                    existingReport.getMilestoneReportId()
            );

            return;
        }

        /*
         * 최초 리포트 생성
         */
        MilestoneReport report =
                MilestoneReport.builder()
                        .milestoneId(milestoneId)
                        .title("마일스톤 리포트 생성 중")
                        .content("")
                        .status(ReportStatus.PENDING)
                        .build();

        int inserted =
                milestoneReportMapper.insertIfAbsent(report);

        /*
         * 다른 요청이 먼저 생성했다면 종료
         */
        if (inserted == 0) {
            return;
        }

        /*
         * PENDING 상태로 저장된 후
         * 실제 AI 생성은 비동기로 처리
         */
        milestoneReportAsyncService.generate(
                milestoneId,
                goalId,
                report.getMilestoneReportId()
        );
    }

    @Override
    @Async("milestoneReportExecutor")
    public void generateReports(
            List<Milestone> milestones,
            Long goalId
    ) {
        if (milestones == null || milestones.isEmpty()) {
            return;
        }

        Milestone lastMilestone = milestones.stream()
                .max(Comparator.comparing(Milestone::getMilestonePercentage))
                .orElseThrow();

        generateReport(lastMilestone.getMilestoneId(),goalId);
    }
}