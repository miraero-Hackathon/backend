package org.jejuro.miraero.domain.transaction.dto.response;

import java.time.LocalDateTime;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "거래 내역 항목")
public class TransactionResponse {

    @ApiModelProperty(value = "거래 ID", example = "1")
    private Long transactionId;
    @ApiModelProperty(value = "거래 유형", example = "EXPENSE")
    private String transactionType;
    @ApiModelProperty(value = "가맹점 또는 거래처명", example = "스타벅스")
    private String merchantName;
    @ApiModelProperty(value = "거래 금액(원)", example = "5500")
    private Long amount;
    @ApiModelProperty(value = "거래 후 잔액(원)", example = "1250000")
    private Long balanceAfter;
    @ApiModelProperty(value = "거래 일시", example = "2026-08-12T14:30:00")
    private LocalDateTime transactedAt;
    @ApiModelProperty(value = "거래 카테고리")
    private ExpenseCategoryResponse category;

    public static TransactionResponse of(
            Long transactionId,
            String transactionType,
            String merchantName,
            Long amount,
            Long balanceAfter,
            LocalDateTime transactedAt,
            ExpenseCategoryResponse category
    ) {
        return new TransactionResponse(
                transactionId,
                transactionType,
                merchantName,
                amount,
                balanceAfter,
                transactedAt,
                category
        );
    }
}
