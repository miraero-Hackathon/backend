package org.jejuro.miraero.domain.goal.milestone.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneListResponse;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneReportResponse;
import org.jejuro.miraero.domain.goal.milestone.domain.ReportStatus;
import org.jejuro.miraero.domain.goal.milestone.dto.response.MilestoneResponse;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MilestoneServiceImpl implements MilestoneService {

    private static final List<Integer> MILESTONE_PERCENTAGES =
            List.of(25, 50, 75, 100);

    private final GoalMapper goalMapper;
    private final MilestoneMapper milestoneMapper;
    private final MilestoneReportMapper milestoneReportMapper;
    private final MilestoneReportService milestoneReportService;

    @Override
    @Transactional(readOnly = true)
    public MilestoneListResponse getMilestones(
            Long goalId,
            Long userId
    ) {
        // 목표 소유권 확인
        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);

        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        List<Milestone> milestones =
                milestoneMapper.findByGoalId(goalId);

        if(milestones.isEmpty()){
            return MilestoneListResponse.of(List.of());
        }

        List<Long> milestoneIds = milestones.stream()
                .map(Milestone::getMilestoneId)
                .toList();

        List<MilestoneReport> reports =
                milestoneReportMapper.findByMilestoneIds(milestoneIds);

        Map<Long, MilestoneReport> reportMap =
                reports.stream()
                        .collect(Collectors.toMap(
                                MilestoneReport::getMilestoneId,
                                report -> report
                        ));

        List<MilestoneResponse> responses =
                milestones.stream()
                        .map(milestone ->
                                toResponse(
                                        milestone,
                                        reportMap.get(milestone.getMilestoneId())
                                )
                        )
                        .toList();

        return MilestoneListResponse.of(responses);
    }

    @Override
    @Transactional
    public void createMilestones(
            Long goalId,
            Long goalAmount
    ) {
        List<Milestone> milestones =
                createMilestoneList(goalId, goalAmount);

        milestoneMapper.saveAll(milestones);
    }

    @Override
    @Transactional
    public void recreateMilestones(Long goalId, Long goalAmount) {
        milestoneMapper.deleteByGoalId(goalId);
        createMilestones(goalId, goalAmount);
    }

    @Override
    @Transactional
    public void updatedMilestoneAchievement(
            Long goalId,
            Long currentAmount
    ) {

        List<Milestone> milestones =
                milestoneMapper.findByGoalId(goalId);


        /*
         * 현재 금액 기준으로 새롭게 달성된 마일스톤의 상태를 업데이트
         */
        for (Milestone milestone : milestones) {

            if(!milestone.achieveIfReached(currentAmount)) continue;

            milestoneMapper.updateAchievement(milestone);

        }

        /*
         * 유저의 모든 마일스톤의 리포트 조회
         */
        List<Long> milestoneIds = milestones.stream()
                .map(Milestone::getMilestoneId)
                .toList();

        List<MilestoneReport> reports =
                milestoneReportMapper.findByMilestoneIds(milestoneIds);

        Map<Long, MilestoneReport> reportMap =
                reports.stream()
                        .collect(Collectors.toMap(
                                MilestoneReport::getMilestoneId,
                                report -> report
                        ));

        /*
         * 3. 달성했으면서 리포트가 없거나 이전에 실패한 마일스톤이 생성 대상.
         *
         * FAILED를 제외하면 일시적인 외부 API 장애로 한 번 실패한 리포트가
         * 영구히 복구되지 않는다. 재시도 여부는 generateReport가 판단한다.
         */
        List<Milestone> reportTargets = milestones.stream()
                .filter(Milestone::isAchieved)
                .filter(milestone -> {
                    MilestoneReport report =
                            reportMap.get(milestone.getMilestoneId());
                    return report == null
                            || report.getStatus() == ReportStatus.FAILED;
                })
                .toList();

        if (reportTargets.isEmpty()) {
            return;
        }


        /*
         * 4. 마일스톤 상태 변경 트랜잭션이
         *    정상적으로 커밋된 이후 리포트 생성
         */
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {

                    @Override
                    public void afterCommit() {
                        milestoneReportService.generateReports(
                                reportTargets,
                                goalId
                        );
                    }
                }
        );

    }

    /**
     * 목표 금액을 기준으로
     * 25%, 50%, 75%, 100% 마일스톤을 생성한다.
     */
    private List<Milestone> createMilestoneList(
            Long goalId,
            Long goalAmount
    ) {
        return MILESTONE_PERCENTAGES.stream()
                .map(percentage ->
                        Milestone.builder()
                                .goalId(goalId)
                                .milestonePercentage(percentage)
                                .milestoneAmount(
                                        goalAmount * percentage / 100
                                )
                                .achieved(false)
                                .build()
                )
                .toList();
    }


    private MilestoneResponse toResponse(
            Milestone milestone,
            MilestoneReport report
    ) {
        return MilestoneResponse.from(
                milestone.getStep(),
                milestone,
                MilestoneReportResponse.from(report)
        );
    }

}