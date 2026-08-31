package org.jejuro.miraero.domain.goal.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.GoalType;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

/**
 * 매퍼 인터페이스의 @Param 유무와 XML의 파라미터 참조 방식이 어긋나면
 * 컴파일·단위 테스트로는 잡히지 않고 런타임에만 터진다. 실제 DB에 붙여 검증한다.
 */
@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@Transactional
@Rollback
class GoalMapperTest {

  @Autowired
  private GoalMapper goalMapper;

  @Autowired
  private UserMapper userMapper;

  private Long userId;

  @BeforeEach
  void setUp() {
    User user = User.create(
        "목표테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "goal-mapper-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();
  }

  @Test
  @DisplayName("목표를 저장하면 goalId가 채워진다")
  void save_populatesGoalId() {
    Goal goal = createGoal();

    goalMapper.save(goal);

    assertEquals(goal.getGoalId(), goalMapper.findByIdAndUserId(userId, goal.getGoalId()).getGoalId());
  }

  @Test
  @DisplayName("목표 금액·기간을 수정하면 DB에 반영된다")
  void update_appliesChanges() {
    Goal goal = createGoal();
    goalMapper.save(goal);

    goal.update("수정된 목표", 20_000_000L, LocalDate.of(2029, 12, 31));
    int updated = goalMapper.update(goal);

    assertEquals(1, updated);
    Goal found = goalMapper.findByIdAndUserId(userId, goal.getGoalId());
    assertEquals("수정된 목표", found.getGoalName());
    assertEquals(20_000_000L, found.getGoalAmount());
    assertEquals(LocalDate.of(2029, 12, 31), found.getGoalDate());
  }

  @Test
  @DisplayName("다른 사용자의 목표는 수정되지 않는다")
  void update_otherUsersGoal_notApplied() {
    Goal goal = createGoal();
    goalMapper.save(goal);

    Goal forged = Goal.builder()
        .goalId(goal.getGoalId())
        .userId(userId + 999L)
        .goalName("탈취 시도")
        .goalAmount(1L)
        .startAmount(0L)
        .goalDate(LocalDate.of(2030, 1, 31))
        .startDate(LocalDate.now())
        .goalStatus(GoalStatus.ACTIVE)
        .isCollected(false)
        .build();

    assertEquals(0, goalMapper.update(forged));
  }

  private Goal createGoal() {
    return Goal.builder()
        .userId(userId)
        .goalType(GoalType.EMERGENCY)
        .goalName("비상금")
        .goalAmount(5_000_000L)
        .startAmount(0L)
        .goalDate(LocalDate.of(2027, 6, 30))
        .startDate(LocalDate.now())
        .goalStatus(GoalStatus.ACTIVE)
        .isCollected(false)
        .build();
  }
}
