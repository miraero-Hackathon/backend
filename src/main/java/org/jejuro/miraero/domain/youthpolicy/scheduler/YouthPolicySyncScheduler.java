package org.jejuro.miraero.domain.youthpolicy.scheduler;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.youthpolicy.service.YouthPolicySyncService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class YouthPolicySyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(YouthPolicySyncScheduler.class);

    private final YouthPolicySyncService youthPolicySyncService;

    @Scheduled(cron = "0 10 3 * * *")
    public void syncYouthPolicies() {
        log.info("청년정책 자동 동기화를 시작합니다.");

        try {
            youthPolicySyncService.syncYouthPolicies();
            log.info("청년정책 자동 동기화를 완료했습니다.");
        } catch (Exception exception) {
            log.error("청년정책 자동 동기화에 실패했습니다.", exception);
        }
    }
}
