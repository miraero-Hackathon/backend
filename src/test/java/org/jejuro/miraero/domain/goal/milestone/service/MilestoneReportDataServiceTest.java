package org.jejuro.miraero.domain.goal.milestone.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.jejuro.miraero.domain.autotransfer.domain.SavingHistorySummary;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.GoalType;
import org.jejuro.miraero.domain.goal.milestone.domain.Milestone;
import org.jejuro.miraero.domain.goal.milestone.dto.request.MilestoneReportAiRequest;
import org.jejuro.miraero.domain.goal.milestone.mapper.MilestoneMapper;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.transaction.mapper.TransactionMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * AI 응답 자체는 비결정적이라 검증하지 않는다.
 * 대신 프롬프트에 실려 나가는 데이터가 정확히 조립되는지를 본다.
 */
@ExtendWith(MockitoExtension.class)
class MilestoneReportDataServiceTest {

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;

    @Mock
    private MilestoneMapper milestoneMapper;
    @Mock
    private TransactionMapper transactionMapper;
    @Mock
    private SavingHistoryMapper savingHistoryMapper;
    @Mock
    private GoalAssetService goalAssetService;

    private MilestoneReportDataService dataService;

    @BeforeEach
    void setUp() {
        // 페이스 계산기는 순수 계산이라 실제 구현을 쓴다
        dataService = new MilestoneReportDataService(
                milestoneMapper,
                transactionMapper,
                savingHistoryMapper,
                goalAssetService,
                new GoalPaceCalculator()
        );
    }

    @Test
    @DisplayName("목표유형·진행률·적립이력이 AI 요청에 담긴다")
    void buildAiRequest_includesNewSections() {
        when(goalAssetService.calculateCurrentAmount(USER_ID, GOAL_ID))
                .thenReturn(6_000_000L);
        when(savingHistoryMapper.findSummary(anyLong(), any(), any()))
                .thenReturn(summary(20L, 18L, 1L, 1L, 160_000L, 12L));

        MilestoneReportAiRequest request =
                dataService.buildAiRequest(goal(), milestone());

        assertEquals("INDEPENDENCE", request.getGoal().getGoalType());

        MilestoneReportAiRequest.PaceInfo pace = request.getPace();
        assertNotNull(pace);
        assertEquals(6_000_000L, pace.getCurrentAmount());

        MilestoneReportAiRequest.SavingSummary saving = request.getSavingSummary();
        assertEquals(20L, saving.getAttemptCount());
        assertEquals(18L, saving.getSuccessCount());
        assertEquals(12L, saving.getMaxStreak());
    }

    @Test
    @DisplayName("적립 이력이 없으면 0으로 채워 프롬프트가 언급하지 않게 한다")
    void buildAiRequest_noSavingHistory() {
        when(goalAssetService.calculateCurrentAmount(USER_ID, GOAL_ID))
                .thenReturn(0L);
        when(savingHistoryMapper.findSummary(anyLong(), any(), any()))
                .thenReturn(null);

        MilestoneReportAiRequest request =
                dataService.buildAiRequest(goal(), milestone());

        assertEquals(0L, request.getSavingSummary().getAttemptCount());
        assertEquals(0L, request.getSavingSummary().getMaxStreak());
    }

    @Test
    @DisplayName("목표 기간 정보가 없으면 진행률을 넣지 않는다")
    void buildAiRequest_noPeriod_paceIsNull() {
        Goal goal = Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .goalType(GoalType.EMERGENCY)
                .goalName("비상금")
                .goalAmount(5_000_000L)
                .startAmount(0L)
                .goalStatus(GoalStatus.ACTIVE)
                .build();
        lenient().when(savingHistoryMapper.findSummary(anyLong(), any(), any()))
                .thenReturn(null);

        MilestoneReportAiRequest request =
                dataService.buildAiRequest(goal, milestone());

        assertNull(request.getPace());
    }

    private Goal goal() {
        return Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .goalType(GoalType.INDEPENDENCE)
                .goalName("전세 보증금 마련")
                .goalAmount(20_000_000L)
                .startAmount(0L)
                .startDate(LocalDate.now().minusMonths(6))
                .goalDate(LocalDate.now().plusMonths(18))
                .goalStatus(GoalStatus.ACTIVE)
                .build();
    }

    private Milestone milestone() {
        Milestone milestone = Milestone.builder()
                .goalId(GOAL_ID)
                .milestonePercentage(25)
                .milestoneAmount(5_000_000L)
                .build();
        ReflectionTestUtils.setField(milestone, "milestoneId", 100L);
        ReflectionTestUtils.setField(milestone, "achieved", true);
        ReflectionTestUtils.setField(milestone, "achievedAt", LocalDateTime.now());
        return milestone;
    }

    private SavingHistorySummary summary(
            Long attempt, Long success, Long partial,
            Long failed, Long total, Long streak
    ) {
        SavingHistorySummary summary = new SavingHistorySummary();
        ReflectionTestUtils.setField(summary, "attemptCount", attempt);
        ReflectionTestUtils.setField(summary, "successCount", success);
        ReflectionTestUtils.setField(summary, "partialCount", partial);
        ReflectionTestUtils.setField(summary, "failedCount", failed);
        ReflectionTestUtils.setField(summary, "totalSavedAmount", total);
        ReflectionTestUtils.setField(summary, "maxStreak", streak);
        return summary;
    }
}
