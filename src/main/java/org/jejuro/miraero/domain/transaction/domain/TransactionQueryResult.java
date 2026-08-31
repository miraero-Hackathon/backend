package org.jejuro.miraero.domain.transaction.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.transaction.dto.response.ExpenseCategoryResponse;
import org.jejuro.miraero.domain.transaction.dto.response.TransactionResponse;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class TransactionQueryResult {

    private Long transactionId;
    private String transactionType;
    private String merchantName;
    private Long amount;
    private Long balanceAfter;
    private LocalDateTime transactedAt;
    private Long categoryId;
    private String categoryName;

    public TransactionResponse toResponse() {
        return TransactionResponse.of(
                transactionId,
                transactionType,
                merchantName,
                amount,
                balanceAfter,
                transactedAt,
                categoryId == null ? null : ExpenseCategoryResponse.of(categoryId, categoryName)
        );
    }
}
