package org.jejuro.miraero.global.scheduler;

import java.time.YearMonth;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.checkcard.service.CheckCardUsageService;
import org.jejuro.miraero.domain.utilitypayment.service.UtilityPaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매달 1일 09:00에, 방금 끝난 지난달 기준으로 통신비/공과금·체크카드 판정을 처리한다.
 *
 * DailySavingScheduler(매일 08:00)와 별도로 둔 이유는 판정 기준 단위가 다르기 때문이다.
 * 이 배치는 월 단위로만 결과가 바뀌므로 매일 돌릴 필요가 없다.
 */
@Component
@RequiredArgsConstructor
public class MonthlyCreditScheduler {

    private static final Logger log = LoggerFactory.getLogger(MonthlyCreditScheduler.class);

    private final UtilityPaymentService utilityPaymentService;
    private final CheckCardUsageService checkCardUsageService;

    @Scheduled(cron = "0 0 9 1 * *")
    public void runMonthlyUtilityPaymentCheck() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        try {
            utilityPaymentService.executeAll(lastMonth, null);
        } catch (Exception e) {
            log.error("통신비/공과금 배치 실패", e);
        }
    }

    @Scheduled(cron = "0 0 9 1 * *")
    public void runMonthlyCheckCardUsageCheck() {
        YearMonth lastMonth = YearMonth.now().minusMonths(1);

        try {
            checkCardUsageService.executeAll(lastMonth, null);
        } catch (Exception e) {
            log.error("체크카드 사용 실적 배치 실패", e);
        }
    }
}

