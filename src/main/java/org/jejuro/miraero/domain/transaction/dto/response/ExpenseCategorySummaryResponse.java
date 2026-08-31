package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ExpenseCategorySummaryResponse {

    private Long categoryId;
    private String categoryName;
    private Long amount;
}