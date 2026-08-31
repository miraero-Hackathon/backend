package org.jejuro.miraero.domain.transaction.dto.response;


import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDate;
import java.util.List;

@Getter
@AllArgsConstructor
public class ExpenseSummaryResponse {
    private LocalDate startDate;
    private LocalDate endDate;
    private Long totalExpense;
    private Long dailyAverageExpense;
    private List<ExpenseCategorySummaryResponse> categories;
}
