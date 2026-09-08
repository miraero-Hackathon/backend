package org.jejuro.miraero.domain.loansimulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.loansimulation.domain.GoalDelinquencyState;
import org.jejuro.miraero.domain.loansimulation.mapper.GoalDelinquencyStateMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanDelinquencyExecutorTest {

    @Mock
    private GoalAssetService goalAssetService;

    @Mock
    private GoalPaceCalculator goalPaceCalculator;

    @Mock
    private GoalDelinquencyStateMapper goalDelinquencyStateMapper;

    @Mock
    private CreditScoreService creditScoreService;

    @InjectMocks
    private LoanDelinquencyExecutor loanDelinquencyExecutor;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;

    // GoalPaceCalculator 내부 날짜 계산은 통째로 목업하므로, 여기서는 goalName/userId만 의미 있다.
    private Goal goalOf() {
        return Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .goalName("전세보증금")
                .goalAmount(20_000_000L)
                .startAmount(0L)
                .startDate(LocalDate.of(2026, 1, 1))
                .goalDate(LocalDate.of(2027, 1, 1))
                .build();
    }

    private void stubPace(Goal goal, PaceStatus status) {
        given(goalAssetService.calculateCurrentAmount(goal.getUserId(), goal.getGoalId()))
                .willReturn(1_000_000L);
        given(goalPaceCalculator.calculate(eq(goal), any()))
                .willReturn(GoalPaceResponse.builder().paceStatus(status).build());
    }

    private GoalDelinquencyState.GoalDelinquencyStateBuilder stateBuilder() {
        return GoalDelinquencyState.builder().goalId(GOAL_ID);
    }

    // ---------- BEHIND(부족) 상태 ----------

    @Test
    @DisplayName("처음 부족해진 날은 시작일만 기록하고 아직 감점하지 않는다")
    void execute_behind_firstDay_recordsStartDateOnly() {
        // given
        Goal goal = goalOf();
        LocalDate today = LocalDate.of(2026, 6, 1);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(null);

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());

        ArgumentCaptor<GoalDelinquencyState> captor = ArgumentCaptor.forClass(GoalDelinquencyState.class);
        verify(goalDelinquencyStateMapper).upsert(captor.capture());
        assertEquals(today, captor.getValue().getBehindSince());
        assertFalse(captor.getValue().isShortTermFired());
    }

    @Test
    @DisplayName("부족한 지 30일이 안 됐으면 아직 감점하지 않는다")
    void execute_behind_under30Days_noPenalty() {
        // given
        Goal goal = goalOf();
        LocalDate behindSince = LocalDate.of(2026, 6, 1);
        LocalDate today = behindSince.plusDays(29);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().behindSince(behindSince).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }

    @Test
    @DisplayName("부족한 지 정확히 30일이 되면 단기연체로 -100점 감점한다")
    void execute_behind_exactly30Days_appliesShortTermPenalty() {
        // given
        Goal goal = goalOf();
        LocalDate behindSince = LocalDate.of(2026, 6, 1);
        LocalDate today = behindSince.plusDays(30);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().behindSince(behindSince).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(-100),
                eq(CreditScoreReasonCode.SHORT_TERM_OVERDUE),
                eq("저축목표 '전세보증금' 자동이체 30일 이상 미납"),
                eq(today.atStartOfDay())
        );
        verify(creditScoreService, never())
                .applyEvent(any(), eq(-250), any(), any(), any());

        ArgumentCaptor<GoalDelinquencyState> captor = ArgumentCaptor.forClass(GoalDelinquencyState.class);
        verify(goalDelinquencyStateMapper).upsert(captor.capture());
        assertTrue(captor.getValue().isShortTermFired());
    }

    @Test
    @DisplayName("이미 단기연체가 기록된 상태면 30일이 지나도 중복 감점하지 않는다")
    void execute_behind_alreadyShortTermFired_doesNotDuplicate() {
        // given
        Goal goal = goalOf();
        LocalDate behindSince = LocalDate.of(2026, 6, 1);
        LocalDate today = behindSince.plusDays(45);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().behindSince(behindSince).shortTermFired(true).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, never())
                .applyEvent(any(), eq(-100), any(), any(), any());
    }

    @Test
    @DisplayName("부족한 지 90일이 지나면 장기연체로 -250점을 추가로 감점한다")
    void execute_behind_90Days_appliesLongTermPenalty() {
        // given
        Goal goal = goalOf();
        LocalDate behindSince = LocalDate.of(2026, 1, 1);
        LocalDate today = behindSince.plusDays(90);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().behindSince(behindSince).shortTermFired(true).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(-250),
                eq(CreditScoreReasonCode.LONG_TERM_OVERDUE),
                eq("저축목표 '전세보증금' 자동이체 90일 이상 미납"),
                eq(today.atStartOfDay())
        );
        // 이미 단기연체는 기록돼 있었으므로 이번엔 중복으로 또 나가지 않아야 함
        verify(creditScoreService, never())
                .applyEvent(any(), eq(-100), any(), any(), any());
    }

    @Test
    @DisplayName("한 번에 90일 이상 밀린 걸 처음 감지하면 단기·장기연체가 같은 회차에 함께 발생한다")
    void execute_behind_detectedLateAt90Days_firesBothPenaltiesTogether() {
        // given: 이 목표 상태를 이번에 처음 판정하는데, 이미 90일 전부터 부족했던 것으로 기록된 상황
        Goal goal = goalOf();
        LocalDate behindSince = LocalDate.of(2026, 1, 1);
        LocalDate today = behindSince.plusDays(95);
        stubPace(goal, PaceStatus.BEHIND);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().behindSince(behindSince).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, times(1))
                .applyEvent(eq(USER_ID), eq(-100), eq(CreditScoreReasonCode.SHORT_TERM_OVERDUE), any(), any());
        verify(creditScoreService, times(1))
                .applyEvent(eq(USER_ID), eq(-250), eq(CreditScoreReasonCode.LONG_TERM_OVERDUE), any(), any());
    }

    @Test
    @DisplayName("정상으로 회복하면 연체 기록(시작일·플래그)이 초기화된다")
    void execute_recoversToOnTrack_resetsBehindFields() {
        // given
        Goal goal = goalOf();
        LocalDate today = LocalDate.of(2026, 6, 1);
        stubPace(goal, PaceStatus.ON_TRACK);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder()
                        .behindSince(LocalDate.of(2026, 1, 1))
                        .shortTermFired(true)
                        .longTermFired(true)
                        .build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        ArgumentCaptor<GoalDelinquencyState> captor = ArgumentCaptor.forClass(GoalDelinquencyState.class);
        verify(goalDelinquencyStateMapper).upsert(captor.capture());

        GoalDelinquencyState saved = captor.getValue();
        assertNull(saved.getBehindSince());
        assertFalse(saved.isShortTermFired());
        assertFalse(saved.isLongTermFired());
        assertEquals(today, saved.getOnTrackSince());
    }

    // ---------- ON_TRACK / AHEAD(정상) 상태 ----------

    @Test
    @DisplayName("정상 유지 6개월이 안 됐으면 아직 가점하지 않는다")
    void execute_onTrack_under6Months_noBonus() {
        // given
        Goal goal = goalOf();
        LocalDate onTrackSince = LocalDate.of(2026, 1, 1);
        LocalDate today = onTrackSince.plusMonths(5);
        stubPace(goal, PaceStatus.ON_TRACK);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().onTrackSince(onTrackSince).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }

    @Test
    @DisplayName("정상 유지가 6개월 이상 되면 연속상환으로 +50점 가점한다")
    void execute_onTrack_6Months_appliesConsecutiveBonus() {
        // given
        Goal goal = goalOf();
        LocalDate onTrackSince = LocalDate.of(2026, 1, 1);
        LocalDate today = onTrackSince.plusMonths(6);
        stubPace(goal, PaceStatus.ON_TRACK);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().onTrackSince(onTrackSince).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService).applyEvent(
                eq(USER_ID),
                eq(50),
                eq(CreditScoreReasonCode.CONSECUTIVE_REPAYMENT),
                eq("저축목표 '전세보증금' 6개월 이상 연속 정상 상환"),
                eq(today.atStartOfDay())
        );
    }

    @Test
    @DisplayName("이미 연속상환 가점을 받은 상태면 6개월이 더 지나도 중복 가점하지 않는다")
    void execute_onTrack_alreadyFired_doesNotDuplicate() {
        // given
        Goal goal = goalOf();
        LocalDate onTrackSince = LocalDate.of(2026, 1, 1);
        LocalDate today = onTrackSince.plusMonths(8);
        stubPace(goal, PaceStatus.ON_TRACK);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID))
                .willReturn(stateBuilder().onTrackSince(onTrackSince).consecutiveFired(true).build());

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        verify(creditScoreService, never()).applyEvent(any(), any(Integer.class), any(), any(), any());
    }

    @Test
    @DisplayName("AHEAD(목표 초과 달성)도 BEHIND가 아니므로 정상 경로로 처리된다")
    void execute_ahead_treatedAsOnTrack() {
        // given
        Goal goal = goalOf();
        LocalDate today = LocalDate.of(2026, 6, 1);
        stubPace(goal, PaceStatus.AHEAD);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(null);

        // when
        loanDelinquencyExecutor.execute(goal, today);

        // then
        ArgumentCaptor<GoalDelinquencyState> captor = ArgumentCaptor.forClass(GoalDelinquencyState.class);
        verify(goalDelinquencyStateMapper).upsert(captor.capture());
        // BEHIND 경로가 아니라 정상 경로를 탔다는 증거: onTrackSince가 오늘로 기록됨
        assertEquals(today, captor.getValue().getOnTrackSince());
        assertNull(captor.getValue().getBehindSince());
    }
}
