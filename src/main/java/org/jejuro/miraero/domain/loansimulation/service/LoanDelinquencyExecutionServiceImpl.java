package org.jejuro.miraero.domain.loansimulation.service;

import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class LoanDelinquencyExecutionServiceImpl implements LoanDelinquencyExecutionService {
    private static final Logger log = LoggerFactory.getLogger(LoanDelinquencyExecutionServiceImpl.class);

    private final GoalMapper goalMapper;
    private final LoanDelinquencyExecutor loanDelinquencyExecutor;

    @Override
    public int executeAll(LocalDate executionDate, Long userId) {
        List<Goal> activeGoals = goalMapper.findActiveGoals(userId);

        int processed = 0;

        for (Goal goal : activeGoals) {
            // 한 목표 처리가 실패해도 나머지 목표는 계속 처리한다 (AutoTransferExecutionServiceImpl과 동일한 관례)
            try {
                loanDelinquencyExecutor.execute(goal, executionDate);
                processed++;
            } catch (Exception e) {
                log.error("대출 시뮬레이션 판정 실패 - goalId={}", goal.getGoalId(), e);
            }
        }

        log.info("대출 시뮬레이션 배치 완료 - 대상 {}건, 처리 {}건, 기준일 {}",
                activeGoals.size(), processed, executionDate);

        return processed;
    }
}
