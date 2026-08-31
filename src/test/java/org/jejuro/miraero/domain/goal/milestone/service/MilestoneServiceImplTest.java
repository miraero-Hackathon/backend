package org.jejuro.miraero.domain.goal.milestone.service;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.domain.MilestoneReport;
import org.jejuro.miraero.domain.goal.milestone.domain.ReportStatus;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneReportMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * 리포트 생성 대상 선별 로직 검증.
 *
 * 외부 API 장애로 한 번 FAILED가 되면 영구히 복구되지 않던 문제의 재발 방지용이다.
 */
@ExtendWith(MockitoExtension.class)
class MilestoneServiceImplTest {

    private static final Long GOAL_ID = 10L;
    private static final Long MILESTONE_ID = 100L;
    private static final Long CURRENT_AMOUNT = 6_000_000L;

    @Mock
    private GoalMapper goalMapper;
    @Mock
    private MilestoneMapper milestoneMapper;
    @Mock
    private MilestoneReportMapper milestoneReportMapper;
    @Mock
    private MilestoneReportService milestoneReportService;

    @InjectMocks
    private MilestoneServiceImpl milestoneService;

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    @Test
    @DisplayName("이전에 실패한 리포트는 다시 생성 대상이 된다")
    void failedReport_isRetried() {
        givenMilestoneWithReport(ReportStatus.FAILED);

        runInSynchronization();

        verify(milestoneReportService).generateReports(anyList(), eq(GOAL_ID));
    }

    @Test
    @DisplayName("이미 완료된 리포트는 다시 생성하지 않는다")
    void completedReport_isNotRegenerated() {
        givenMilestoneWithReport(ReportStatus.COMPLETED);

        runInSynchronization();

        verify(milestoneReportService, never()).generateReports(anyList(), eq(GOAL_ID));
    }

    @Test
    @DisplayName("생성 중인 리포트는 중복 생성하지 않는다")
    void pendingReport_isNotRegenerated() {
        givenMilestoneWithReport(ReportStatus.PENDING);

        runInSynchronization();

        verify(milestoneReportService, never()).generateReports(anyList(), eq(GOAL_ID));
    }

    private void givenMilestoneWithReport(ReportStatus status) {
        Milestone milestone = Milestone.builder()
                .milestoneId(MILESTONE_ID)
                .goalId(GOAL_ID)
                .milestonePercentage(25)
                .milestoneAmount(5_000_000L)
                .build();

        when(milestoneMapper.findByGoalId(GOAL_ID)).thenReturn(List.of(milestone));
        when(milestoneReportMapper.findByMilestoneIds(anyList()))
                .thenReturn(List.of(
                        MilestoneReport.builder()
                                .milestoneReportId(1L)
                                .milestoneId(MILESTONE_ID)
                                .status(status)
                                .build()
                ));
    }

    /**
     * 리포트 생성은 afterCommit에서 일어나므로 동기화를 직접 열고 커밋을 흉내낸다.
     */
    private void runInSynchronization() {
        TransactionSynchronizationManager.initSynchronization();

        milestoneService.updatedMilestoneAchievement(GOAL_ID, CURRENT_AMOUNT);

        List.copyOf(TransactionSynchronizationManager.getSynchronizations())
                .forEach(TransactionSynchronization::afterCommit);
    }
}
