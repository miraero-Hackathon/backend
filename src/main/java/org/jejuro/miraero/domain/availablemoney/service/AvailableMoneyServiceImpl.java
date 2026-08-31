package org.jejuro.miraero.domain.availablemoney.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.availablemoney.calculator.AvailableMoneyCalculator;
import org.jejuro.miraero.domain.availablemoney.dto.response.DailyAvailableMoneyResponse;
import org.jejuro.miraero.domain.availablemoney.dto.response.MonthlyAvailableMoneyResponse;
import org.jejuro.miraero.domain.transaction.dto.response.AvailableMoneyExpenseSummary;
import org.jejuro.miraero.domain.transaction.service.TransactionQueryService;
import org.jejuro.miraero.domain.user.service.UserService;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AvailableMoneyServiceImpl implements AvailableMoneyService {

    private final AvailableMoneyCalculator calculator;
    private final TransactionQueryService transactionQueryService;
    // 자동이체 금액을 여유자금에서 차감하는 정책이 다시 적용될 경우 복구한다.
    // private final AutoTransferQueryService autoTransferQueryService;
    private final UserService userService;

    @Override
    public MonthlyAvailableMoneyResponse getMonthlyAvailableMoney(
            Long userId,
            Long goalId
    ) {
        List<LocalDateTime> salaryDateTimes =
                transactionQueryService.getLatestSalaryDateTimes(userId, 3);

        PaydayPeriod period = resolvePaydayPeriod(salaryDateTimes);

        return calculateMonthlyAvailableMoney(
                userId,
                goalId,
                period
        );
    }

    @Override
    public DailyAvailableMoneyResponse getDailyAvailableMoney(
            Long userId,
            Long goalId
    ) {
        List<LocalDateTime> salaryDateTimes =
                transactionQueryService.getLatestSalaryDateTimes(userId, 3);

        PaydayPeriod period = resolvePaydayPeriod(salaryDateTimes);

        MonthlyAvailableMoneyResponse monthly =
                calculateMonthlyAvailableMoney(userId, goalId, period);

        LocalDate businessDate =
                LocalDateTime.now().minusHours(8).toLocalDate();

        long remainingDays =
                ChronoUnit.DAYS.between(
                        businessDate,
                        period.endDate.toLocalDate()
                ) + 1;

        if (remainingDays <= 0) {
            remainingDays = 1;
        }

        Long todayAvailableMoney =
                calculator.calculateDailyAvailableMoney(
                        monthly.getMonthlyAvailableMoney(),
                        remainingDays
                );

        LocalDateTime startDateTime =
                businessDate.atTime(8, 0);

        LocalDateTime endDateTime =
                businessDate.plusDays(1).atTime(8, 0);

        Long todayExpense =
                transactionQueryService.getTodayExpenseSum(
                        userId,
                        startDateTime,
                        endDateTime
                );

        return DailyAvailableMoneyResponse.builder()
                .todayAvailableMoney(todayAvailableMoney)
                .todayExpense(todayExpense)
                .remainingAvailableMoney(
                        calculator.calculateRemainingMoney(
                                todayAvailableMoney,
                                todayExpense
                        )
                )
                .build();
    }

    /**
     * 조회용 메서드와 두 가지가 다르다.
     * 기준일을 밖에서 받아 지난 날짜도 정산할 수 있고, 목표를 구분하지 않고
     * 전체 자동이체를 차감한다(페이스메이커는 특정 목표에 속하지 않으므로).
     */
    @Override
    public Long getRemainingMoneyOf(Long userId, LocalDate businessDate) {
        List<LocalDateTime> salaryDateTimes =
                transactionQueryService.getLatestSalaryDateTimes(userId, 3);

        PaydayPeriod period = resolvePaydayPeriod(salaryDateTimes);

        // 전체 목표 자동이체 금액은 페이스메이커 정산용 여유자금에서도 제외한다.
        // Long totalTransfer =
        //         autoTransferQueryService.getTotalTransferAmount(userId);
        Long totalTransfer = 0L;

        AvailableMoneyExpenseSummary expenseSummary =
                transactionQueryService.getAvailableMoneyExpenseSummary(
                        userId,
                        period.startDate,
                        period.endDate
                );

        Long monthlyAvailableMoney =
                calculator.calculateMonthlyAvailableMoney(
                        userService.getMonthlyIncome(userId),
                        expenseSummary.getFixedExpense(),
                        expenseSummary.getVariableExpense(),
                        totalTransfer,
                        0L
                );

        long remainingDays =
                ChronoUnit.DAYS.between(
                        businessDate,
                        period.endDate.toLocalDate()
                ) + 1;

        if (remainingDays <= 0) {
            remainingDays = 1;
        }

        Long dailyAvailableMoney =
                calculator.calculateDailyAvailableMoney(
                        monthlyAvailableMoney,
                        remainingDays
                );

        Long expense =
                transactionQueryService.getTodayExpenseSum(
                        userId,
                        businessDate.atTime(8, 0),
                        businessDate.plusDays(1).atTime(8, 0)
                );

        return calculator.calculateRemainingMoney(
                dailyAvailableMoney,
                expense
        );
    }

    private PaydayPeriod resolvePaydayPeriod(List<LocalDateTime> salaryDateTimes) {

        if (salaryDateTimes == null || salaryDateTimes.isEmpty()) {
            LocalDate now = LocalDate.now();
            // Fallback: 이번 달 1일 08:00:00 ~ 다음 달 1일 07:59:59
            LocalDateTime startOfMonth = now.withDayOfMonth(1).atTime(8, 0, 0);
            LocalDateTime nextSalaryDate = now.plusMonths(1).withDayOfMonth(1).atTime(8, 0, 0);
            LocalDateTime endOfMonth = nextSalaryDate.minusSeconds(1);

            return new PaydayPeriod(startOfMonth, endOfMonth, nextSalaryDate);
        }

        // 수령일 당일 08:00:00 시각으로 정제
        LocalDateTime lastSalaryDateTime = salaryDateTimes.get(0).toLocalDate().atTime(8, 0, 0);
        LocalDateTime nextSalaryDateTime = calculateNextSalaryDate(salaryDateTimes);

        // 다음 월급일 08:00:00의 1초 전 = 07:59:59
        LocalDateTime periodEndDateTime = nextSalaryDateTime.minusSeconds(1);

        return new PaydayPeriod(lastSalaryDateTime, periodEndDateTime, nextSalaryDateTime);
    }

    private LocalDateTime calculateNextSalaryDate(List<LocalDateTime> salaryDateTimes) {
        LocalDateTime latest = salaryDateTimes.get(0);
        LocalDate latestDate = latest.toLocalDate();

        int originalDay = resolveOriginalPayDay(salaryDateTimes);

        YearMonth nextMonth = YearMonth.from(latestDate).plusMonths(1);
        int targetDay = Math.min(originalDay, nextMonth.lengthOfMonth());
        LocalDate targetDate = nextMonth.atDay(targetDay);

        LocalDate adjustedDate = adjustForWeekend(targetDate);

        // 다음 월급일 아침 08:00:00 지정
        return adjustedDate.atTime(8, 0, 0);
    }

    private int resolveOriginalPayDay(List<LocalDateTime> salaryDateTimes) {
        LocalDate latestDate = salaryDateTimes.get(0).toLocalDate();

        if (salaryDateTimes.size() >= 2) {
            int previousDay = salaryDateTimes.get(1).toLocalDate().getDayOfMonth();
            if (salaryDateTimes.size() == 2 && latestDate.getDayOfMonth() == previousDay) {
                return previousDay;
            }
            if (salaryDateTimes.size() >= 3) {
                int thirdDay = salaryDateTimes.get(2).toLocalDate().getDayOfMonth();
                if (previousDay == thirdDay) {
                    return previousDay;
                }
            }
        }

        if (latestDate.getDayOfWeek() == DayOfWeek.FRIDAY) {
            int day1Later = latestDate.plusDays(1).getDayOfMonth();
            int day2Later = latestDate.plusDays(2).getDayOfMonth();

            if (day1Later % 5 == 0) return day1Later;
            if (day2Later % 5 == 0) return day2Later;
        }

        return latestDate.getDayOfMonth();
    }

    private LocalDate adjustForWeekend(LocalDate date) {
        DayOfWeek dow = date.getDayOfWeek();
        if (dow == DayOfWeek.SATURDAY) return date.minusDays(1);
        if (dow == DayOfWeek.SUNDAY) return date.minusDays(2);
        return date;
    }

    private static class PaydayPeriod {
        final LocalDateTime startDate;
        final LocalDateTime endDate;
        final LocalDateTime nextSalaryDate;
        long elapsedDays;
        long remainingDays;

        PaydayPeriod(
                LocalDateTime startDate,
                LocalDateTime endDate,
                LocalDateTime nextSalaryDate
        ) {
            this.startDate = startDate;
            this.endDate = endDate;
            this.nextSalaryDate = nextSalaryDate;
            LocalDate businessDate =
                    LocalDateTime.now().minusHours(8).toLocalDate();

            this.elapsedDays = ChronoUnit.DAYS.between(
                    startDate.toLocalDate(),
                    businessDate
            );

            this.remainingDays = ChronoUnit.DAYS.between(
                    businessDate,
                    nextSalaryDate.toLocalDate()
            );
        }
    }

    private MonthlyAvailableMoneyResponse calculateMonthlyAvailableMoney(
            Long userId,
            Long goalId,
            PaydayPeriod period
    ) {
        AvailableMoneyExpenseSummary expenseSummary =
                transactionQueryService.getAvailableMoneyExpenseSummary(
                        userId,
                        period.startDate,
                        period.endDate
                );

        Long fixedExpense = expenseSummary.getFixedExpense();
        Long variableExpense = expenseSummary.getVariableExpense();

        Long monthlyIncome = userService.getMonthlyIncome(userId);

        Long targetTransfer = 0L;
        Long otherTransfer = 0L;

        Long availableMoney =
                calculator.calculateMonthlyAvailableMoney(
                        monthlyIncome,
                        fixedExpense,
                        variableExpense,
                        targetTransfer,
                        otherTransfer
                );

        LocalDate businessDate =
                LocalDateTime.now().minusHours(8).toLocalDate();

        long elapsedDays = ChronoUnit.DAYS.between(
                period.startDate.toLocalDate(),
                businessDate
        );

        long remainingDays = ChronoUnit.DAYS.between(
                businessDate,
                period.nextSalaryDate.toLocalDate()
        );

        long periodDays = ChronoUnit.DAYS.between(
                period.startDate.toLocalDate(),
                period.nextSalaryDate.toLocalDate()
        );

        return MonthlyAvailableMoneyResponse.builder()
                .monthlyIncome(monthlyIncome)
                .fixedExpense(fixedExpense)
                .variableExpense(variableExpense)
                .monthlyAvailableMoney(availableMoney)
                .elapsedDays(elapsedDays)
                .remainingDays(remainingDays)
                .periodDays(periodDays)
                .build();
    }
}
