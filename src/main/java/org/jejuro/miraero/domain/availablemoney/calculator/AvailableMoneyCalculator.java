package org.jejuro.miraero.domain.availablemoney.calculator;

import org.springframework.stereotype.Component;

@Component
public class AvailableMoneyCalculator {

    /**
     * 월 여유자금 계산
     */
    public Long calculateMonthlyAvailableMoney(
        Long monthlyIncome,
        Long fixedExpense,
        Long variableExpense,
        Long targetAutoTransfer,
        Long otherAutoTransfer
    ){
        long income = (monthlyIncome != null) ? monthlyIncome : 0L;
        long fixed = (fixedExpense != null) ? fixedExpense : 0L;
        long variable = (variableExpense != null) ? variableExpense : 0L;
        // 목표 자동이체 금액을 차감하던 기존 정책은 복구 가능하도록 남겨둔다.
        // long target = (targetAutoTransfer != null) ? targetAutoTransfer : 0L;
        // long other = (otherAutoTransfer != null) ? otherAutoTransfer : 0L;

        long result = income - (fixed + variable);
        return Math.max(result, 0L);
    }

    /**
     *  일일 여유자금 계산
     */
    public Long calculateDailyAvailableMoney(
            Long monthlyAvailableMoney,
            long remainingDays
    ){
        if (monthlyAvailableMoney == null || monthlyAvailableMoney <= 0) {
            return 0L;
        }
        long days = (remainingDays <= 0) ? 1 : remainingDays;
        return monthlyAvailableMoney / days;
    }

    /**
     *
     */
    public Long calculateRemainingMoney(
            Long todayAvailableMoney,
            Long todayExpense
    ){
        long available = (todayAvailableMoney != null) ? todayAvailableMoney : 0L;
        long expense = (todayExpense != null) ? todayExpense : 0L;

        return available - expense;
    }
}
