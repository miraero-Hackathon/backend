package org.jejuro.miraero.domain.loansimulation.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.loansimulation.domain.GoalDelinquencyState;
import org.jejuro.miraero.domain.loansimulation.dto.response.LoanSimulationDetailResponse;
import org.jejuro.miraero.domain.loansimulation.mapper.GoalDelinquencyStateMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LoanSimulationQueryServiceImplTest {

    @Mock
    private GoalMapper goalMapper;

    @Mock
    private GoalAssetService goalAssetService;

    @Mock
    private GoalPaceCalculator goalPaceCalculator;

    @Mock
    private GoalDelinquencyStateMapper goalDelinquencyStateMapper;

    @Mock
    private SavingHistoryMapper savingHistoryMapper;

    @InjectMocks
    private LoanSimulationQueryServiceImpl loanSimulationQueryService;

    private static final Long USER_ID = 1L;
    private static final Long GOAL_ID = 10L;

    // startDate~goalDate가 정확히 12개월이라, calculateRequiredMonthly(goalAmount, startAmount, 12L)로 스텁한다.
    private Goal goalOf() {
        return Goal.builder()
                .goalId(GOAL_ID)
                .userId(USER_ID)
                .goalName("전세보증금")
                .goalAmount(30_000_000L)
                .startAmount(0L)
                .startDate(LocalDate.of(2026, 3, 1))
                .goalDate(LocalDate.of(2027, 3, 1))
                .build();
    }

    private GoalDelinquencyState.GoalDelinquencyStateBuilder stateBuilder() {
        return GoalDelinquencyState.builder().goalId(GOAL_ID);
    }

    private void stubCommon(Goal goal, Long currentAmount) {
        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID)).willReturn(goal);
        given(goalAssetService.calculateCurrentAmount(USER_ID, GOAL_ID)).willReturn(currentAmount);
        given(goalPaceCalculator.calculate(eq(goal), any()))
                .willReturn(GoalPaceResponse.builder().paceStatus(PaceStatus.ON_TRACK).build());
        given(goalPaceCalculator.calculateRequiredMonthly(30_000_000L, 0L, 12L))
                .willReturn(2_500_000L);
        given(savingHistoryMapper.findSavedAmountByGoal(eq(GOAL_ID), any(), any()))
                .willReturn(800_000L);
    }

    @Test
    @DisplayName("소유하지 않은(또는 존재하지 않는) 목표를 조회하면 GOAL_NOT_FOUND 예외가 발생한다")
    void getDetail_goalNotFound_throws() {
        // given
        given(goalMapper.findByIdAndUserId(USER_ID, GOAL_ID)).willReturn(null);

        // when & then
        BusinessException exception = assertThrows(BusinessException.class,
                () -> loanSimulationQueryService.getDetail(USER_ID, GOAL_ID));
        assertEquals(GoalErrorCode.GOAL_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("연체 상태 기록이 아직 없으면 연체 관련 필드는 전부 기본값으로 채워진다")
    void getDetail_noDelinquencyState_returnsDefaults() {
        // given
        Goal goal = goalOf();
        stubCommon(goal, 11_500_000L);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(null);

        // when
        LoanSimulationDetailResponse response = loanSimulationQueryService.getDetail(USER_ID, GOAL_ID);

        // then
        assertEquals(0, response.getOverdueCount());
        assertFalse(response.isShortTermFired());
        assertFalse(response.isLongTermFired());
        assertFalse(response.isConsecutiveFired());
        assertNull(response.getBehindSince());
        assertNull(response.getDaysBehind());
        assertNull(response.getOnTrackSince());
        assertNull(response.getMonthsOnTrack());
    }

    @Test
    @DisplayName("연체 중이면 behindSince를 기준으로 경과일수를 계산해서 채운다")
    void getDetail_behindState_calculatesDaysBehind() {
        // given
        Goal goal = goalOf();
        stubCommon(goal, 5_000_000L);
        LocalDate behindSince = LocalDate.now().minusDays(35);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(
                stateBuilder()
                        .behindSince(behindSince)
                        .shortTermFired(true)
                        .overdueCount(1)
                        .build()
        );

        // when
        LoanSimulationDetailResponse response = loanSimulationQueryService.getDetail(USER_ID, GOAL_ID);

        // then
        assertEquals(behindSince, response.getBehindSince());
        assertEquals(35, response.getDaysBehind());
        assertTrue(response.isShortTermFired());
        assertEquals(1, response.getOverdueCount());
    }

    @Test
    @DisplayName("정상 상환 중이면 onTrackSince를 기준으로 연속 개월수를 계산해서 채운다")
    void getDetail_onTrackState_calculatesMonthsOnTrack() {
        // given
        Goal goal = goalOf();
        stubCommon(goal, 20_000_000L);
        LocalDate onTrackSince = LocalDate.now().minusMonths(6);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(
                stateBuilder()
                        .onTrackSince(onTrackSince)
                        .consecutiveFired(true)
                        .build()
        );

        // when
        LoanSimulationDetailResponse response = loanSimulationQueryService.getDetail(USER_ID, GOAL_ID);

        // then
        assertEquals(onTrackSince, response.getOnTrackSince());
        assertEquals(6, response.getMonthsOnTrack());
        assertTrue(response.isConsecutiveFired());
    }

    @Test
    @DisplayName("진행률은 100%를 넘지 않도록 상한이 걸린다")
    void getDetail_currentAmountExceedsGoal_progressRateCappedAt100() {
        // given
        Goal goal = goalOf();
        stubCommon(goal, 35_000_000L); // 목표(3천만원)보다 많이 모인 상황
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(null);

        // when
        LoanSimulationDetailResponse response = loanSimulationQueryService.getDetail(USER_ID, GOAL_ID);

        // then
        assertEquals(100, response.getProgressRate());
    }

    @Test
    @DisplayName("기본 정보(원금·개시일·페이스·임계값)가 응답에 그대로 반영된다")
    void getDetail_mapsBasicFieldsAndThresholds() {
        // given
        Goal goal = goalOf();
        stubCommon(goal, 11_500_000L);
        given(goalDelinquencyStateMapper.findByGoalId(GOAL_ID)).willReturn(null);

        // when
        LoanSimulationDetailResponse response = loanSimulationQueryService.getDetail(USER_ID, GOAL_ID);

        // then
        assertEquals(GOAL_ID, response.getGoalId());
        assertEquals("전세보증금", response.getGoalName());
        assertEquals(30_000_000L, response.getGoalAmount());
        assertEquals(11_500_000L, response.getCurrentAmount());
        assertEquals(LocalDate.of(2026, 3, 1), response.getStartDate());
        assertEquals(2_500_000L, response.getRequiredMonthlyAmount());
        assertEquals(800_000L, response.getMonthlyRepaidAmount());
        assertEquals(38, response.getProgressRate());
        assertEquals(30, response.getShortTermOverdueDays());
        assertEquals(90, response.getLongTermOverdueDays());
        assertEquals(6, response.getConsecutiveRepaymentMonths());
    }
}
