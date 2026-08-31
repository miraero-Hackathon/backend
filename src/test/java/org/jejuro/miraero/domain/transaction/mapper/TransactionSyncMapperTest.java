package org.jejuro.miraero.domain.transaction.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.jejuro.miraero.domain.transaction.domain.TransactionSyncCommand;
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

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@Transactional
@Rollback
class TransactionSyncMapperTest {

  @Autowired
  private TransactionSyncMapper transactionSyncMapper;

  @Autowired
  private UserMapper userMapper;

  private Long userId;

  // DB에 미리 있는 사용자에 기대면 초기화된 환경에서 FK 위반으로 깨진다. @Rollback이라 매 테스트 후 사라진다.
  @BeforeEach
  void setUp() {
    User user = User.create(
        "거래동기화테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "transaction-sync-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();
  }

  @Test
  @DisplayName("같은 ex_transaction_id로 두 번 upsert해도 오류 없이 갱신된다")
  void upsert_isIdempotent() {
    TransactionSyncCommand command = new TransactionSyncCommand(
        userId,
        null,
        null,
        999301L,
        "WITHDRAWAL",
        5500L,
        3394500L,
        LocalDateTime.of(2026, 8, 1, 9, 12),
        "스타벅스"
    );

    assertEquals(1, transactionSyncMapper.upsert(command));
    transactionSyncMapper.upsert(command);
  }
}
