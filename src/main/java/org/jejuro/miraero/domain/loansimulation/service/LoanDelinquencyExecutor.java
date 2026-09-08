package org.jejuro.miraero.domain.loansimulation.service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.credit.domain.CreditScoreReasonCode;
import org.jejuro.miraero.domain.credit.service.CreditScoreService;
import org.jejuro.miraero.domain.goal.calculator.GoalPaceCalculator;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.domain.goal.service.GoalAssetService;
import org.jejuro.miraero.domain.loansimulation.domain.GoalDelinquencyState;
import org.jejuro.miraero.domain.loansimulation.mapper.GoalDelinquencyStateMapper;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;


@Component
@RequiredArgsConstructor
public class LoanDelinquencyExecutor {

    private static final int SHORT_TERM_OVERDUE_DAYS = 30;
    private static final int LONG_TERM_OVERDUE_DAYS = 90;
    private static final int CONSECUTIVE_REPAYMENT_MONTHS = 6;

    private static final int SHORT_TERM_OVERDUE_DELTA = -100;
    private static final int LONG_TERM_OVERDUE_DELTA = -250;
    private static final int CONSECUTIVE_REPAYMENT_DELTA = 50;

    private final GoalAssetService goalAssetService;
    private final GoalPaceCalculator goalPaceCalculator;
    private final GoalDelinquencyStateMapper goalDelinquencyStateMapper;
    private final CreditScoreService creditScoreService;

    @Transactional
    public void execute(Goal goal, LocalDate today) {

        // 1) 지금까지 실제로 모인 금액 (자동이체든 끌어쓰기든 상관없이 다 반영된 값)
        Long currentAmount = goalAssetService.calculateCurrentAmount(goal.getUserId(), goal.getGoalId());

        // 2) "지금까지 모아놨어야 할 금액"과 비교해서 BEHIND인지 계산 (이미 있는 로직 재사용)
        GoalPaceResponse pace = goalPaceCalculator.calculate(goal, currentAmount);

        // 3) 이 목표의 "지금 상태" 조회. 처음 판정하는 목표면 없을 수 있으니 그 경우 빈 상태로 시작.
        GoalDelinquencyState state = goalDelinquencyStateMapper.findByGoalId(goal.getGoalId());
        if (state == null) {
            state = GoalDelinquencyState.builder()
                    .goalId(goal.getGoalId())
                    .build();
        }

        if (pace.getPaceStatus() == PaceStatus.BEHIND) {
            handleBehind(goal, state, today);
        } else {
            handleOnTrack(goal, state, today);
        }

        // 4) 오늘 판정 결과를 저장 (있으면 갱신, 없으면 생성 — upsert가 알아서 처리)
        goalDelinquencyStateMapper.upsert(state);
    }

    // 부족(BEHIND) 상태일 때의 처리
    private void handleBehind(Goal goal, GoalDelinquencyState state, LocalDate today) {

        // 반대 상태(정상 유지) 기록은 부족 상태로 전환되는 순간 리셋한다.
        // 안 그러면 "정상 유지 몇 개월째"가 부족한 동안에도 계속 남아있게 됨.
        state.setOnTrackSince(null);
        state.setConsecutiveFired(false);

        if (state.getBehindSince() == null) {
            // 오늘 막 부족해지기 시작한 경우 — 시작일만 기록하고, 판정은 다음날부터.
            state.setBehindSince(today);
            return;
        }

        long daysBehind = ChronoUnit.DAYS.between(state.getBehindSince(), today);

        if (daysBehind >= SHORT_TERM_OVERDUE_DAYS && !state.isShortTermFired()) {
            creditScoreService.applyEvent(
                    goal.getUserId(),
                    SHORT_TERM_OVERDUE_DELTA,
                    CreditScoreReasonCode.SHORT_TERM_OVERDUE,
                    "저축목표 '" + goal.getGoalName() + "' 자동이체 30일 이상 미납",
                    today.atStartOfDay()
            );
            state.setShortTermFired(true);
        }

        if (daysBehind >= LONG_TERM_OVERDUE_DAYS && !state.isLongTermFired()) {
            creditScoreService.applyEvent(
                    goal.getUserId(),
                    LONG_TERM_OVERDUE_DELTA,
                    CreditScoreReasonCode.LONG_TERM_OVERDUE,
                    "저축목표 '" + goal.getGoalName() + "' 자동이체 90일 이상 미납",
                    today.atStartOfDay()
            );
            state.setLongTermFired(true);
        }
    }

    // 정상(BEHIND 아님 = AHEAD 또는 ON_TRACK) 상태일 때의 처리
    private void handleOnTrack(Goal goal, GoalDelinquencyState state, LocalDate today) {

        // 반대 상태(연체) 기록은 정상 상태로 전환되는 순간 리셋한다 — 연체 회복 처리.
        state.setBehindSince(null);
        state.setShortTermFired(false);
        state.setLongTermFired(false);

        if (state.getOnTrackSince() == null) {
            state.setOnTrackSince(today);
            return;
        }

        long monthsOnTrack = ChronoUnit.MONTHS.between(state.getOnTrackSince(), today);

        if (monthsOnTrack >= CONSECUTIVE_REPAYMENT_MONTHS && !state.isConsecutiveFired()) {
            creditScoreService.applyEvent(
                    goal.getUserId(),
                    CONSECUTIVE_REPAYMENT_DELTA,
                    CreditScoreReasonCode.CONSECUTIVE_REPAYMENT,
                    "저축목표 '" + goal.getGoalName() + "' 6개월 이상 연속 정상 상환",
                    today.atStartOfDay()
            );
            state.setConsecutiveFired(true);
        }
    }
}
