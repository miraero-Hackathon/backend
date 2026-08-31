package org.jejuro.miraero.domain.autotransfer.service;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;
import org.jejuro.miraero.domain.autotransfer.mapper.SavingHistoryMapper;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * 자동이체 한 건을 처리한다.
 *
 * 별도 빈으로 둔 이유는 트랜잭션 경계 때문이다. 같은 클래스 안에서 호출하면
 * Spring 프록시를 거치지 않아 @Transactional이 적용되지 않는다.
 */
@Component
@RequiredArgsConstructor
public class AutoTransferExecutor {

    private static final Logger log = LoggerFactory.getLogger(AutoTransferExecutor.class);

    private final SavingHistoryMapper savingHistoryMapper;
    private final MoneyBoxMapper moneyBoxMapper;

    /**
     * @return 실제로 적립했으면 true. 이미 실행됐거나 잔액이 부족하면 false
     */
    @Transactional
    public boolean execute(AutoTransferTarget target, LocalDate executionDate) {

        boolean enough = target.getAvailableBalance() >= target.getTransferAmount();

        TransferStatus status = enough
                ? TransferStatus.SUCCESS
                : TransferStatus.FAILED_INSUFFICIENT_FUNDS;

        // 이력을 먼저 남긴다. UNIQUE 제약에 걸리면 이미 오늘 실행된 건이라 적립하지 않는다.
        int recorded = savingHistoryMapper.insertIgnoreDuplicate(
                target.getMoneyBoxId(),
                enough ? target.getTransferAmount() : 0L,
                executionDate,
                status
        );

        if (recorded == 0) {
            log.debug("이미 실행된 자동이체 - moneyBoxId={}, 기준일={}",
                    target.getMoneyBoxId(), executionDate);
            return false;
        }

        if (!enough) {
            log.info("잔액 부족으로 적립 건너뜀 - moneyBoxId={}, 필요 {}, 가용 {}",
                    target.getMoneyBoxId(), target.getTransferAmount(),
                    target.getAvailableBalance());
            return false;
        }

        // 서브 레저라 계좌 잔액은 건드리지 않는다. 저금통에 묶이는 금액만 늘린다.
        moneyBoxMapper.increaseBalance(target.getMoneyBoxId(), target.getTransferAmount());

        return true;
    }
}
