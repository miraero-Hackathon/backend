package org.jejuro.miraero.domain.checkcard.service;

import java.time.YearMonth;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.checkcard.domain.CheckCardUsageState;
import org.jejuro.miraero.domain.checkcard.dto.response.CheckCardUsageStatusResponse;
import org.jejuro.miraero.domain.checkcard.mapper.CheckCardUsageStateMapper;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CheckCardUsageServiceImpl implements CheckCardUsageService {

    private static final Logger log = LoggerFactory.getLogger(CheckCardUsageServiceImpl.class);
    private static final int TOP_CARD_COUNT = 2;

    private final UserMapper userMapper;
    private final CheckCardUsageStateMapper checkCardUsageStateMapper;
    private final CheckCardUsageExecutor checkCardUsageExecutor;

    @Override
    @Transactional(readOnly = true)
    public List<CheckCardUsageStatusResponse> getStatus(Long userId) {
        List<CheckCardUsageState> states = checkCardUsageStateMapper.findByUserId(userId);

        // 연속충족개월수 내림차순 상위 2개만 — "가장 많이(오래) 쓴 카드" 기준
        return states.stream()
                .sorted(Comparator.comparingInt(CheckCardUsageState::getConsecutiveQualifiedMonths).reversed())
                .limit(TOP_CARD_COUNT)
                .map(CheckCardUsageStatusResponse::of)
                .toList();
    }

    @Override
    public int executeAll(YearMonth targetMonth, Long userId) {
        List<Long> userIds = userId != null ? List.of(userId) : userMapper.findAllUserIds();

        int processed = 0;

        for (Long uid : userIds) {
            try {
                checkCardUsageExecutor.execute(uid, targetMonth);
                processed++;
            } catch (Exception e) {
                log.error("체크카드 사용 실적 판정 실패 - userId={}", uid, e);
            }
        }

        log.info("체크카드 사용 실적 배치 완료 - 대상 {}명, 처리 {}건, 기준월 {}",
                userIds.size(), processed, targetMonth);

        return processed;
    }
}
