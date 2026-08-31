package org.jejuro.miraero.domain.autotransfer.service;


import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;

public interface AutoTransferService {
    void createMoneyBoxAutoTransfer(
            Long userId,
            Long moneyBoxId,
            Long withdrawalAccountId,
            String maskedDepositAccount,
            AutoTransferCreateRequest request
    );
}
