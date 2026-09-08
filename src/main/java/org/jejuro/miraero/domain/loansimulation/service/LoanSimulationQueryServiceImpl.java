package org.jejuro.miraero.domain.loansimulation.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.domain.goal.exception.GoalErrorCode;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.loansimulation.domain.GoalDelinquencyState;
import org.jejuro.miraero.domain.loansimulation.dto.response.LoanSimulationDetailResponse;
import org.jejuro.miraero.domain.loansimulation.mapper.GoalDelinquencyStateMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

@Service
@RequiredArgsConstructor
public class LoanSimulationQueryServiceImpl implements LoanSimulationQueryService {
    private final GoalMapper goalMapper;
    private final GoalAssetService goalAssetService;
    private final GoalPaceCalculator goalPaceCalculator;
    private final GoalDelinquencyStateMapper goalDelinquencyStateMapper;
    private final SavingHistoryMapper savingHistoryMapper;

    @Override
    public LoanSimulationDetailResponse getDetail(Long userId, Long goalId) {

        Goal goal = goalMapper.findByIdAndUserId(userId, goalId);
        if (goal == null) {
            throw new BusinessException(GoalErrorCode.GOAL_NOT_FOUND);
        }

        Long currentAmount = goalAssetService.calculateCurrentAmount(userId, goalId);
        GoalPaceResponse pace = goalPaceCalculator.calculate(goal, currentAmount);

        long goalMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(goal.getStartDate()),
                YearMonth.from(goal.getGoalDate())
        );
        Long requiredMonthlyAmount = goalPaceCalculator.calculateRequiredMonthly(
                goal.getGoalAmount(), goal.getStartAmount(), goalMonths);

        YearMonth thisMonth = YearMonth.now();
        Long monthlyRepaidAmount = savingHistoryMapper.findSavedAmountByGoal(
                goalId, thisMonth.atDay(1), thisMonth.atEndOfMonth());

        GoalDelinquencyState state = goalDelinquencyStateMapper.findByGoalId(goalId);
        LocalDate today = LocalDate.now();

        Integer daysBehind = (state != null && state.getBehindSince() != null)
                ? (int) ChronoUnit.DAYS.between(state.getBehindSince(), today) : null;
        Integer monthsOnTrack = (state != null && state.getOnTrackSince() != null)
                ? (int) ChronoUnit.MONTHS.between(state.getOnTrackSince(), today) : null;

        return LoanSimulationDetailResponse.builder()
                .goalId(goal.getGoalId())
                .goalName(goal.getGoalName())
                .goalAmount(goal.getGoalAmount())
                .currentAmount(currentAmount)
                .startDate(goal.getStartDate())
                .progressRate(calculateProgressRate(currentAmount, goal.getGoalAmount()))
                .requiredMonthlyAmount(requiredMonthlyAmount)
                .monthlyRepaidAmount(monthlyRepaidAmount)
                .pace(pace)
                .behindSince(state != null ? state.getBehindSince() : null)
                .daysBehind(daysBehind)
                .shortTermFired(state != null && state.isShortTermFired())
                .longTermFired(state != null && state.isLongTermFired())
                .overdueCount(state != null ? state.getOverdueCount() : 0)
                .onTrackSince(state != null ? state.getOnTrackSince() : null)
                .monthsOnTrack(monthsOnTrack)
                .consecutiveFired(state != null && state.isConsecutiveFired())
                .shortTermOverdueDays(30)
                .longTermOverdueDays(90)
                .consecutiveRepaymentMonths(6)
                .build();
    }

    // GoalServiceImpl.calculateProgressRate와 동일한 공식. 목표 상세 화면과 같은 기준으로 보여줘야 함.
    private Integer calculateProgressRate(Long currentAmount, Long goalAmount) {
        if (goalAmount == null || goalAmount == 0 || currentAmount == null) {
            return 0;
        }
        return Math.min(100, (int) (currentAmount * 100.0 / goalAmount));
    }
}
