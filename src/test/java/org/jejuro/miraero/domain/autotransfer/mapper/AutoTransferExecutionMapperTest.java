package org.jejuro.miraero.domain.autotransfer.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransfer;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferStatus;
import org.jejuro.miraero.domain.autotransfer.domain.AutoTransferTarget;
import org.jejuro.miraero.domain.autotransfer.domain.TransferStatus;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
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
class AutoTransferExecutionMapperTest {

  private static final Long EX_ACCOUNT_ID = 999401L;

  @Autowired
  private AutoTransferMapper autoTransferMapper;

  @Autowired
  private SavingHistoryMapper savingHistoryMapper;

  @Autowired
  private MoneyBoxMapper moneyBoxMapper;

  @Autowired
  private AccountMapper accountMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private ReferenceDataMapper referenceDataMapper;

  private Long userId;
  private Long accountId;
  private Long moneyBoxId;

  @BeforeEach
  void setUp() {
    User user = User.create(
        "자동이체테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "auto-transfer-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();

    accountMapper.upsert(Account.of(
        userId,
        referenceDataMapper.findFinancialInstitutionIdByCode("004"),
        EX_ACCOUNT_ID, "CHECKING", "KB 입출금통장",
        new byte[]{1, 2, 3}, "hash-999401", "1234*****90",
        3_000_000L, "ACTIVE", LocalDate.of(2023, 1, 1), null,
        new BigDecimal("0.1000"), null
    ));
    this.accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    MoneyBox moneyBox = MoneyBox.builder()
        .userId(userId)
        .accountId(accountId)
        .balance(0L)
        .moneyBoxType(MoneyBoxType.GOAL)
        .build();
    moneyBoxMapper.insert(moneyBox);
    this.moneyBoxId = moneyBox.getMoneyBoxId();
  }

  @Test
  @DisplayName("이체일이 오늘인 자동이체만 실행 대상으로 조회된다")
  void findExecutionTargets_matchesTransferDay() {
    LocalDate today = LocalDate.of(2026, 8, 12);
    saveAutoTransfer(300_000L, 12);

    List<AutoTransferTarget> targets =
        autoTransferMapper.findExecutionTargets(today, userId);

    assertEquals(1, targets.size());
    assertEquals(moneyBoxId, targets.get(0).getMoneyBoxId());
    assertEquals(300_000L, targets.get(0).getTransferAmount());
  }

  @Test
  @DisplayName("이체일이 오늘이 아니면 조회되지 않는다")
  void findExecutionTargets_otherDay_excluded() {
    saveAutoTransfer(300_000L, 25);

    assertTrue(autoTransferMapper
        .findExecutionTargets(LocalDate.of(2026, 8, 12), userId)
        .isEmpty());
  }

  @Test
  @DisplayName("이체일이 그달 말일보다 크면 말일에 실행된다")
  void findExecutionTargets_dayExceedsMonthEnd_runsOnLastDay() {
    saveAutoTransfer(300_000L, 31);

    // 2026년 2월은 28일까지
    assertEquals(1, autoTransferMapper
        .findExecutionTargets(LocalDate.of(2026, 2, 28), userId).size());

    // 말일이 아닌 날에는 걸리지 않는다
    assertTrue(autoTransferMapper
        .findExecutionTargets(LocalDate.of(2026, 2, 27), userId).isEmpty());
  }

  @Test
  @DisplayName("가용 잔액은 계좌 잔액에서 저금통 합계를 뺀 값이다")
  void findExecutionTargets_availableBalanceExcludesMoneyBox() {
    moneyBoxMapper.increaseBalance(moneyBoxId, 1_000_000L);
    saveAutoTransfer(300_000L, 12);

    List<AutoTransferTarget> targets =
        autoTransferMapper.findExecutionTargets(LocalDate.of(2026, 8, 12), userId);

    // 계좌 300만 - 저금통 100만
    assertEquals(2_000_000L, targets.get(0).getAvailableBalance());
  }

  @Test
  @DisplayName("ACTIVE가 아닌 자동이체는 실행 대상이 아니다")
  void findExecutionTargets_pausedExcluded() {
    AutoTransfer paused = AutoTransfer.builder()
        .withdrawalAccountId(accountId)
        .moneyBoxId(moneyBoxId)
        .maskedDepositAccount("1234*****90")
        .transferAmount(300_000L)
        .transferDay(12)
        .startDate(LocalDate.now())
        .autoTransferStatus(AutoTransferStatus.PAUSED)
        .build();
    autoTransferMapper.save(paused);

    assertTrue(autoTransferMapper
        .findExecutionTargets(LocalDate.of(2026, 8, 12), userId)
        .isEmpty());
  }

  @Test
  @DisplayName("같은 저금통에 같은 날짜로 두 번 기록하면 두 번째는 무시된다")
  void insertIgnoreDuplicate_sameDayTwice_secondIgnored() {
    LocalDate today = LocalDate.of(2026, 8, 12);

    int first = savingHistoryMapper.insertIgnoreDuplicate(
        moneyBoxId, 300_000L, today, TransferStatus.SUCCESS);
    int second = savingHistoryMapper.insertIgnoreDuplicate(
        moneyBoxId, 300_000L, today, TransferStatus.SUCCESS);

    assertEquals(1, first);
    assertEquals(0, second);
  }

  @Test
  @DisplayName("날짜가 다르면 같은 저금통에도 기록된다")
  void insertIgnoreDuplicate_differentDay_recorded() {
    assertEquals(1, savingHistoryMapper.insertIgnoreDuplicate(
        moneyBoxId, 300_000L, LocalDate.of(2026, 8, 12), TransferStatus.SUCCESS));
    assertEquals(1, savingHistoryMapper.insertIgnoreDuplicate(
        moneyBoxId, 300_000L, LocalDate.of(2026, 9, 12), TransferStatus.SUCCESS));
  }

  private void saveAutoTransfer(Long amount, Integer transferDay) {
    autoTransferMapper.save(AutoTransfer.builder()
        .withdrawalAccountId(accountId)
        .moneyBoxId(moneyBoxId)
        .maskedDepositAccount("1234*****90")
        .transferAmount(amount)
        .transferDay(transferDay)
        .startDate(LocalDate.now())
        .autoTransferStatus(AutoTransferStatus.ACTIVE)
        .build());
  }
}
