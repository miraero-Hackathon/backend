package org.jejuro.miraero.domain.autotransfer.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WithdrawalAccountResponse {

    private Long accountId;

    private String bankName;

    private String accountNumberMasked;
}