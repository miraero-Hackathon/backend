package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.availablemoney.service.AvailableMoneyService;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 페이스메이커 적립 한 건을 처리한다.
 *
 * 별도 빈으로 둔 이유는 트랜잭션 경계 때문이다. 같은 클래스 안에서 호출하면
 * Spring 프록시를 거치지 않아 @Transactional이 적용되지 않는다.
 */
@Component
@RequiredArgsConstructor
public class PaceMakerSaver {

    private static final Logger log = LoggerFactory.getLogger(PaceMakerSaver.class);

    private final AvailableMoneyService availableMoneyService;
    private final AccountMapper accountMapper;
    private final SavingHistoryMapper savingHistoryMapper;
    private final MoneyBoxMapper moneyBoxMapper;

    /**
     * @return 실제로 적립했으면 true. 이미 실행됐거나 남은 돈이 없으면 false
     */
    @Transactional
    public boolean save(AutoSaving autoSaving, LocalDate businessDate) {

        Long remaining =
                availableMoneyService.getRemainingMoneyOf(autoSaving.getUserId(), businessDate);

        // 예산을 넘겨 썼으면 적립할 돈이 없다. 이력도 남기지 않는다.
        if (remaining == null || remaining <= 0) {
            log.info("남은 여유자금이 없어 적립 건너뜀 - autoSavingId={}, 기준일={}",
                    autoSaving.getAutoSavingId(), businessDate);
            return false;
        }

        long amount = applyMaxAmount(remaining, autoSaving.getMaxAmount());

        // 여유자금은 소득 기준 계산이라 실제 통장에 그만큼 있는지는 따로 확인해야 한다.
        // 조회 잔액은 이미 저금통 몫이 빠진 값이다.
        AccountResponse account = accountMapper.findResponseById(autoSaving.getAccountId());

        if (account == null || account.getBalance() < amount) {
            log.info("통장 잔액이 부족해 적립 건너뜀 - autoSavingId={}, 필요 {}, 가용 {}",
                    autoSaving.getAutoSavingId(), amount,
                    account == null ? 0 : account.getBalance());

            savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
                    autoSaving.getMoneyBoxId(),
                    autoSaving.getAutoSavingId(),
                    0L,
                    businessDate,
                    TransferStatus.FAILED_INSUFFICIENT_FUNDS
            );
            return false;
        }

        // 상한에 걸려 일부만 적립한 경우를 이력에서 구분할 수 있게 한다
        TransferStatus status = amount < remaining
                ? TransferStatus.PARTIAL_LIMIT
                : TransferStatus.SUCCESS;

        int recorded = savingHistoryMapper.insertPaceMakerIgnoreDuplicate(
                autoSaving.getMoneyBoxId(),
                autoSaving.getAutoSavingId(),
                amount,
                businessDate,
                status
        );

        if (recorded == 0) {
            log.debug("이미 적립된 날짜 - autoSavingId={}, 기준일={}",
                    autoSaving.getAutoSavingId(), businessDate);
            return false;
        }

        // 서브 레저라 계좌 잔액은 건드리지 않는다. 저금통에 묶이는 금액만 늘린다.
        moneyBoxMapper.increaseBalance(autoSaving.getMoneyBoxId(), amount);

        return true;
    }

    private long applyMaxAmount(Long remaining, Long maxAmount) {
        if (maxAmount == null) {
            return remaining;
        }
        return Math.min(remaining, maxAmount);
    }
}
