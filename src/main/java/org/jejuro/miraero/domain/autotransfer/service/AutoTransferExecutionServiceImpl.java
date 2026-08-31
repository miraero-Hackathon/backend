package org.jejuro.miraero.domain.autotransfer.service;

import java.time.LocalDate;
import java.util.List;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.autotransfer.mapper.AutoTransferMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AutoTransferExecutionServiceImpl implements AutoTransferExecutionService {

    private static final Logger log =
            LoggerFactory.getLogger(AutoTransferExecutionServiceImpl.class);

    private final AutoTransferMapper autoTransferMapper;
    private final AutoTransferExecutor autoTransferExecutor;

    @Override
    public int executeAll(LocalDate executionDate, Long userId) {
        List<AutoTransferTarget> targets =
                autoTransferMapper.findExecutionTargets(executionDate, userId);

        int executed = 0;

        for (AutoTransferTarget target : targets) {
            // 한 건이 실패해도 나머지는 계속 처리한다
            try {
                if (autoTransferExecutor.execute(target, executionDate)) {
                    executed++;
                }
            } catch (Exception e) {
                log.error("자동이체 실행 실패 - autoTransferId={}",
                        target.getAutoTransferId(), e);
            }
        }

        log.info("자동이체 실행 완료 - 대상 {}건, 적립 {}건, 기준일 {}",
                targets.size(), executed, executionDate);

        return executed;
    }
}
