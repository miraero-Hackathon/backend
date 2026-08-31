package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExpenseSimulationResponse {

    private Integer year;
    private Integer month;
    private Long currentTotalExpense;
    private Long targetTotalExpense;
    private Long totalReductionAmount;
    private List<ExpenseSimulationCategoryResponse> categories;
}
