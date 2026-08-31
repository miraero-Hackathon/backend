package org.jejuro.miraero.domain.availablemoney.calculator;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class AvailableMoneyCalculatorTest {

    private final AvailableMoneyCalculator calculator = new AvailableMoneyCalculator();

    @Test
    void calculateMonthlyAvailableMoney_excludesGoalAutoTransfers() {
        Long result = calculator.calculateMonthlyAvailableMoney(
                3_000_000L,
                1_000_000L,
                500_000L,
                300_000L,
                200_000L
        );

        assertEquals(1_500_000L, result);
    }
}
