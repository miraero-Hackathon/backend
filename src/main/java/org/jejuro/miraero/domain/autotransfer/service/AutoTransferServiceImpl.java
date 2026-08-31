package org.jejuro.miraero.domain.autotransfer.service;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferStatus;
import org.jejuro.miraero.domain.autotransfer.dto.request.AutoTransferCreateRequest;
import org.jejuro.miraero.domain.autotransfer.exception.AutoTransferErrorCode;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.jejuro.miraero.global.exception.CommonErrorCode;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;


@Service
@RequiredArgsConstructor
public class AutoTransferServiceImpl implements AutoTransferService {

    private final AutoTransferMapper autoTransferMapper;


    /**
     * 출금계좌는 저금통 소속 계좌와 항상 같다. 저금통은 그 계좌 안의 구획이라
     * 자동이체가 계좌 간 이동이 아니라 같은 계좌 안에서 금액을 묶는 동작이기 때문이다.
     * 계좌 소유권은 저금통 생성 시점에 이미 검증된 값이 넘어온다.
     */
    @Override
    @Transactional
    public void createMoneyBoxAutoTransfer(
            Long userId,
            Long moneyBoxId,
            Long withdrawalAccountId,
            String maskedDepositAccount,
            AutoTransferCreateRequest request
    ){

        if (moneyBoxId == null
                || withdrawalAccountId == null
                || request == null) {
            throw new BusinessException(
                    CommonErrorCode.INVALID_INPUT_VALUE
            );
        }

        if (request.getAmount() == null || request.getAmount() <= 0) {
            throw new BusinessException(
                    AutoTransferErrorCode.INVALID_TRANSFER_AMOUNT
            );
        }

        if (request.getTransferDay() == null
                || request.getTransferDay() < 1
                || request.getTransferDay() > 31) {
            throw new BusinessException(
                    AutoTransferErrorCode.INVALID_TRANSFER_DAY
            );
        }

        AutoTransfer autoTransfer =
                AutoTransfer.builder()
                        .withdrawalAccountId(
                                withdrawalAccountId
                        )
                        .moneyBoxId(
                                moneyBoxId
                        )
                        .maskedDepositAccount(
                                maskedDepositAccount
                        )
                        .transferAmount(
                                request.getAmount()
                        )
                        .transferDay(
                                request.getTransferDay()
                        )
                        .startDate(
                                LocalDate.now()
                        )
                        .autoTransferStatus(
                                AutoTransferStatus.ACTIVE
                        )
                        .build();


        autoTransferMapper.save(
                autoTransfer
        );


    }

}
