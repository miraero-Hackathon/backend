package org.jejuro.miraero.domain.autotransfer.dto.response;

import lombok.Builder;
import lombok.Getter;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;

@Getter
@Builder
public class AutoTransferResponse {
    private Long amount;
    private Integer transferDay;
    private WithdrawalAccountResponse withdrawalAccount;

    public static AutoTransferResponse from(
            AutoTransfer autoTransfer,
            WithdrawalAccountResponse withdrawalAccount
    ){

        return AutoTransferResponse.builder()
                .amount(
                        autoTransfer.getTransferAmount()
                )
                .transferDay(
                        autoTransfer.getTransferDay()
                )
                .withdrawalAccount(
                        withdrawalAccount
                )
                .build();
    }
}

