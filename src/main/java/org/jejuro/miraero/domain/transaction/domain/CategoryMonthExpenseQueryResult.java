package org.jejuro.miraero.domain.transaction.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryMonthExpenseQueryResult {

    private Long categoryId;
    private String categoryName;
    private Long previousMonthAmount;
    private Long currentMonthAmount;
}
