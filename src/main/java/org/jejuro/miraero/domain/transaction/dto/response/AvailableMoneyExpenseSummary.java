package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AvailableMoneyExpenseSummary {

    private Long fixedExpense;
    private Long variableExpense;
}