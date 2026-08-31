package org.jejuro.miraero.domain.transaction.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jejuro.miraero.domain.transaction.domain.ExpenseCategoryTargetQueryResult;

@Getter
@AllArgsConstructor
public class ExpenseCategoryTargetResponse {

    private Long categoryId;
    private String categoryName;
    private Long targetAmount;

    public static ExpenseCategoryTargetResponse from(ExpenseCategoryTargetQueryResult result) {
        return new ExpenseCategoryTargetResponse(
                result.getCategoryId(),
                result.getCategoryName(),
                result.getTargetAmount()
        );
    }
}
