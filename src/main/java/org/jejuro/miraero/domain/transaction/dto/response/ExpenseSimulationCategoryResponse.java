package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSimulationCategoryResponse {

    private Long categoryId;
    private String categoryName;
    private Long currentExpense;
    private Long targetExpense;
    private Long reductionAmount;
}
