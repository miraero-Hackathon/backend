package org.jejuro.miraero.domain.product.scheduler;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.product.service.ProductSyncService;
import org.jejuro.miraero.domain.product.service.ProductSyncServiceImpl;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component
@RequiredArgsConstructor
public class ProductSyncScheduler {

    private final ProductSyncService productSyncService;
    private static final Logger log = LoggerFactory.getLogger(ProductSyncScheduler.class);

    @Scheduled(cron = "0 0 3 * * *")
    public void syncProducts() {

        try {
            productSyncService.syncDepositProducts();
            log.info("예금 상품 동기화 완료");
        } catch (Exception e) {
            log.error("예금 상품 동기화 실패", e);
        }

        try {
            productSyncService.syncSavingProducts();
            log.info("적금 상품 동기화 완료");
        } catch (Exception e) {
            log.error("적금 상품 동기화 실패", e);
        }
    }
}
