package org.jejuro.miraero.domain.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSimulationCurrentExpense {

    private Long categoryId;
    private String categoryName;
    private Long currentExpense;
}
