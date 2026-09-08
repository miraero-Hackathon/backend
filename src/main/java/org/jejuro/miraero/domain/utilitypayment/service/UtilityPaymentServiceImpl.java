package org.jejuro.miraero.domain.utilitypayment.service;

import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityPaymentState;
import org.jejuro.miraero.domain.utilitypayment.domain.UtilityType;
import org.jejuro.miraero.domain.utilitypayment.dto.response.UtilityPaymentStatusResponse;
import org.jejuro.miraero.domain.utilitypayment.mapper.UtilityPaymentStateMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UtilityPaymentServiceImpl implements UtilityPaymentService{
    private static final Logger log = LoggerFactory.getLogger(UtilityPaymentServiceImpl.class);

    private final UserMapper userMapper;
    private final UtilityPaymentStateMapper utilityPaymentStateMapper;
    private final UtilityPaymentExecutor utilityPaymentExecutor;

    @Override
    @Transactional(readOnly = true)
    public List<UtilityPaymentStatusResponse> getStatus(Long userId) {

        // 이 유저가 지금까지 판정된 적 있는 항목들만 DB에 있음 (한 번도 안 걸렸으면 아예 없음)
        List<UtilityPaymentState> states = utilityPaymentStateMapper.findByUserId(userId);

        // 항목명(UtilityType) 기준으로 빠르게 찾기 위한 맵으로 변환
        Map<UtilityType, UtilityPaymentState> stateByType = states.stream()
                .collect(Collectors.toMap(UtilityPaymentState::getUtilityType, s -> s));

        // 5개 항목 전부에 대해 응답을 만듦 — 상태가 없는 항목은 "0개월째"로 처리
        return java.util.Arrays.stream(UtilityType.values())
                .map(type -> {
                    UtilityPaymentState state = stateByType.get(type);
                    return UtilityPaymentStatusResponse.of(type, state);
                })
                .collect(Collectors.toList());

    }

    @Override
    public int executeAll(YearMonth targetMonth, Long userId) {
        List<Long> userIds = userId != null ? List.of(userId) : userMapper.findAllUserIds();

        int processed = 0;

        for (Long uid : userIds) {
            for (UtilityType type : UtilityType.values()) {
                try {
                    utilityPaymentExecutor.execute(uid, type, targetMonth);
                    processed++;
                } catch (Exception e) {
                    log.error("통신비/공과금 판정 실패 - userId={}, utilityType={}", uid, type, e);
                }
            }
        }

        log.info("통신비/공과금 배치 완료 - 대상 {}건(유저x항목), 처리 {}건, 기준월 {}",
                userIds.size() * UtilityType.values().length, processed, targetMonth);

        return processed;
    }
}
