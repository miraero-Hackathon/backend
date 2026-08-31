package org.jejuro.miraero.domain.pacemaker.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.pacemaker.mapper.PaceMakerMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PaceMakerSavingServiceImpl implements PaceMakerSavingService {

    private static final Logger log =
            LoggerFactory.getLogger(PaceMakerSavingServiceImpl.class);

    private final PaceMakerMapper paceMakerMapper;
    private final PaceMakerSaver paceMakerSaver;

    @Override
    public int saveAll(LocalDate businessDate, Long userId) {
        List<AutoSaving> targets = paceMakerMapper.findActiveAutoSavings(userId);

        int saved = 0;

        for (AutoSaving autoSaving : targets) {
            // 한 건이 실패해도 나머지는 계속 처리한다
            try {
                if (paceMakerSaver.save(autoSaving, businessDate)) {
                    saved++;
                }
            } catch (Exception e) {
                log.error("페이스메이커 적립 실패 - autoSavingId={}",
                        autoSaving.getAutoSavingId(), e);
            }
        }

        log.info("페이스메이커 적립 완료 - 대상 {}건, 적립 {}건, 기준일 {}",
                targets.size(), saved, businessDate);

        return saved;
    }
}
