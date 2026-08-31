package org.jejuro.miraero.domain.moneybox.service;


import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.dto.request.MoneyBoxCreateRequest;
import org.jejuro.miraero.domain.moneybox.dto.response.MoneyBoxCreateResponse;

public interface MoneyBoxService {
    MoneyBoxCreateResponse createMoneyBox(
            Long userId,
            MoneyBoxCreateRequest request
    );

    /**
     * 저금통 그릇만 만든다. 자동 출금 설정은 호출하는 쪽 책임이다.
     */
    Long createMoneyBox(Long userId, Long accountId, MoneyBoxType moneyBoxType);

    /**
     * 계좌에서 돈이 자동으로 빠져나가는 설정을 붙일 수 있는지 검증한다.
     */
    void validateAutoWithdrawalAccount(Long userId, Long accountId);
}
