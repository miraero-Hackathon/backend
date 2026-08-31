package org.jejuro.miraero.domain.autotransfer.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AutoTransfer {
    private Long autoTransferId;
    private Long withdrawalAccountId;
    private Long depositAccountId;
    private Long moneyBoxId;
    private String depositInstitutionName;
    private Long transferAmount;
    private String maskedDepositAccount;
    private Integer transferDay;
    private LocalDate startDate;
    private LocalDate endDate;
    private AutoTransferStatus autoTransferStatus;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
