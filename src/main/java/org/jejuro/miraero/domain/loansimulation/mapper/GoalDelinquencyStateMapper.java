package org.jejuro.miraero.domain.loansimulation.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.loansimulation.domain.GoalDelinquencyState;

@Mapper
public interface GoalDelinquencyStateMapper {

    GoalDelinquencyState findByGoalId(@Param("goalId") Long goalId);

    // "있으면 갱신, 없으면 새로 생성"을 한 메서드로 처리 (SQL의 ON DUPLICATE KEY UPDATE 사용).
    // insert/update를 따로 안 만든 이유: 이 상태는 항상 "목표 하나에 최신값 하나"만 있으면 되고,
    // 매번 findByGoalId로 있는지 없는지 미리 안 따져도 되게 하려고 (호출부 로직 단순화).
    void upsert(GoalDelinquencyState state);
}
