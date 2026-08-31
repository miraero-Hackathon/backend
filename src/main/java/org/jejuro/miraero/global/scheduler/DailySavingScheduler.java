package org.jejuro.miraero.global.scheduler;

import java.time.LocalDate;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.service.AutoTransferExecutionService;
import org.jejuro.miraero.domain.pacemaker.service.PaceMakerSavingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 08:00에 저금통 적립을 처리한다.
 *
 * 08:00인 이유는 여유자금 계산이 하루 경계를 08:00으로 잡기 때문이다
 * (AvailableMoneyServiceImpl의 now().minusHours(8)). 다른 시각에 돌리면
 * 지출 집계 구간과 어긋난다.
 *
 * 두 작업을 한 스케줄러에 둔 이유는 순서 때문이다. @Scheduled를 같은 시각에
 * 두 개 걸면 실행 순서가 보장되지 않는데, 약정된 목표 자동이체가 먼저 통장
 * 잔액을 가져가고 페이스메이커가 남은 것을 적립해야 한다.
 */
@Component
@RequiredArgsConstructor
public class DailySavingScheduler {

    private static final Logger log = LoggerFactory.getLogger(DailySavingScheduler.class);

    private final AutoTransferExecutionService autoTransferExecutionService;
    private final PaceMakerSavingService paceMakerSavingService;

    @Scheduled(cron = "0 0 8 * * *")
    public void runDailySaving() {
        LocalDate today = LocalDate.now();

        // 오늘이 이체일인 목표 자동이체
        try {
            autoTransferExecutionService.executeAll(today, null);
        } catch (Exception e) {
            log.error("저금통 자동이체 배치 실패", e);
        }

        // 어제(08:00~08:00) 구간이 방금 닫혔으므로 그날 남은 여유자금을 정산한다
        try {
            paceMakerSavingService.saveAll(today.minusDays(1), null);
        } catch (Exception e) {
            log.error("페이스메이커 적립 배치 실패", e);
        }
    }
}
