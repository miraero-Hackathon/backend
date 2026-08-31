package org.jejuro.miraero.domain.goal.calculator;

import java.time.YearMonth;
import java.time.temporal.ChronoUnit;

import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;
import org.jejuro.miraero.domain.goal.dto.response.GoalPaceResponse;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Component;

/**
 * 목표 페이스 계산.
 *
 * 목표 상세 조회와 마일스톤 리포트가 같은 기준으로 판정해야 하므로 별도 빈으로 둔다.
 * 각자 계산하면 공식이 갈라진다.
 */
@Component
public class GoalPaceCalculator {

    public GoalPaceResponse calculate(
            Goal goal,
            Long currentAmount
    ) {

        long goalMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(goal.getStartDate()),
                YearMonth.from(goal.getGoalDate())
        );

        long elapsedMonths = ChronoUnit.MONTHS.between(
                YearMonth.from(goal.getStartDate()),
                YearMonth.now()
        );

        long requiredMonthly = calculateRequiredMonthly(
                goal.getGoalAmount(),
                goal.getStartAmount(),
                goalMonths
        );

        // 목표 기간이 지나도 기대치가 목표 금액을 넘지 않도록 상한을 둔다
        long expectedAmount =
                goal.getStartAmount()
                        + (requiredMonthly * Math.min(elapsedMonths, goalMonths));

        long differenceAmount =
                (currentAmount == null ? 0L : currentAmount) - expectedAmount;

        PaceStatus status;

        if (differenceAmount > 0) {
            status = PaceStatus.AHEAD;
        } else if (differenceAmount < 0) {
            status = PaceStatus.BEHIND;
        } else {
            status = PaceStatus.ON_TRACK;
        }

        return GoalPaceResponse.builder()
                .expectedAmount(expectedAmount)
                .differenceAmount(Math.abs(differenceAmount))
                .paceStatus(status)
                .build();
    }

    public long calculateRequiredMonthly(
            long goalAmount,
            long startAmount,
            long goalMonths
    ) {

        if (goalMonths <= 0) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (startAmount >= goalAmount) {
            return 0L;
        }

        // 나머지를 버리면 목표에 미달하므로 올림한다
        return (goalAmount - startAmount + goalMonths - 1) / goalMonths;
    }
}
