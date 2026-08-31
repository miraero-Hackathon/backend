package org.jejuro.miraero.domain.account.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.goal.domain.AssetType;
import org.jejuro.miraero.domain.goal.domain.Goal;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;
import org.jejuro.miraero.domain.goal.domain.GoalType;
import org.jejuro.miraero.domain.goal.dto.request.GoalAssetRequest;
import org.jejuro.miraero.domain.goal.mapper.GoalAssetMapper;
import org.jejuro.miraero.domain.goal.mapper.GoalMapper;
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
class AccountMapperTest {

  private static final Long EX_ACCOUNT_ID = 999201L;
  private static final String KB_CODE = "004";

  @Autowired
  private AccountMapper accountMapper;

  @Autowired
  private UserMapper userMapper;

  @Autowired
  private ReferenceDataMapper referenceDataMapper;

  @Autowired
  private MoneyBoxMapper moneyBoxMapper;

  @Autowired
  private GoalMapper goalMapper;

  @Autowired
  private GoalAssetMapper goalAssetMapper;

  private Long userId;
  private Long financialInstitutionId;

  // DB에 미리 있는 사용자에 기대면 초기화된 환경에서 FK 위반으로 깨진다. @Rollback이라 매 테스트 후 사라진다.
  @BeforeEach
  void setUp() {
    User user = User.create(
        "계좌테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "account-mapper-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();
    this.financialInstitutionId = referenceDataMapper.findFinancialInstitutionIdByCode(KB_CODE);
  }

  @Test
  @DisplayName("같은 ex_account_id로 두 번 upsert해도 한 건만 남는다")
  void upsert_isIdempotent() {
    accountMapper.upsert(createAccount(3400000L));
    Long firstId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);
    assertNotNull(firstId);

    accountMapper.upsert(createAccount(5000000L));
    Long secondId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertEquals(firstId, secondId);
  }

  @Test
  @DisplayName("소유자가 맞으면 계좌를 조회하고, 다른 사용자면 null을 반환한다")
  void findByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertNotNull(accountMapper.findByIdAndUserId(accountId, userId));
    assertNull(accountMapper.findByIdAndUserId(accountId, userId + 999L));
  }

  @Test
  @DisplayName("소유자가 맞으면 true, 다른 사용자면 false를 반환한다")
  void existsByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    assertTrue(accountMapper.existsByIdAndUserId(accountId, userId));
    assertFalse(accountMapper.existsByIdAndUserId(accountId, userId + 999L));
  }

  @Test
  @DisplayName("사용자의 전체 계좌 목록을 은행명과 함께 조회한다")
  void findAllByUserId_includesInstitutionName() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> accounts = accountMapper.findAllByUserId(userId, null, false);

    assertTrue(accounts.stream().anyMatch(a ->
        "KB 입출금통장".equals(a.getAccountName()) && a.getInstitutionName() != null));
  }

  @Test
  @DisplayName("accountType으로 필터링하면 다른 유형 계좌는 제외된다")
  void findAllByUserId_filtersByAccountType() {
    accountMapper.upsert(createAccount(3400000L));

    java.util.List<AccountResponse> checking = accountMapper.findAllByUserId(userId, "CHECKING", false);
    java.util.List<AccountResponse> savings = accountMapper.findAllByUserId(userId, "SAVINGS", false);

    assertTrue(checking.stream().anyMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
    assertTrue(savings.stream().noneMatch(a -> "KB 입출금통장".equals(a.getAccountName())));
  }

  @Test
  @DisplayName("소유자가 맞으면 은행명 포함 상세를 조회하고, 다른 사용자면 null을 반환한다")
  void findResponseByIdAndUserId_ownershipCheck() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    AccountResponse own = accountMapper.findResponseByIdAndUserId(accountId, userId);
    AccountResponse other = accountMapper.findResponseByIdAndUserId(accountId, userId + 999L);

    assertNotNull(own);
    assertEquals(3400000L, own.getBalance());
    assertNull(other);
  }

  @Test
  @DisplayName("계좌 ID로 은행명을 포함한 상세정보를 조회한다")
  void findResponseById_returnsDetail() {
    accountMapper.upsert(createAccount(3400000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    AccountResponse response = accountMapper.findResponseById(accountId);

    assertNotNull(response);
    assertEquals(3400000L, response.getBalance());
    assertNotNull(response.getInstitutionName());
  }

  @Test
  @DisplayName("저금통이 달린 계좌는 조회 잔액에서 저금통 금액이 빠진다")
  void findResponseById_excludesMoneyBoxBalance() {
    accountMapper.upsert(createAccount(3_400_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId)
        .accountId(accountId)
        .balance(1_000_000L)
        .moneyBoxType(MoneyBoxType.GOAL)
        .build());

    assertEquals(2_400_000L, accountMapper.findResponseById(accountId).getBalance());
    assertEquals(2_400_000L,
        accountMapper.findResponseByIdAndUserId(accountId, userId).getBalance());
  }

  @Test
  @DisplayName("한 계좌에 저금통이 여러 개면 합계만큼 빠진다")
  void findAllByUserId_excludesAllMoneyBoxBalances() {
    accountMapper.upsert(createAccount(3_400_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId).accountId(accountId).balance(1_000_000L)
        .moneyBoxType(MoneyBoxType.GOAL).build());
    moneyBoxMapper.insert(MoneyBox.builder()
        .userId(userId).accountId(accountId).balance(500_000L)
        .moneyBoxType(MoneyBoxType.SAVING).build());

    AccountResponse account = accountMapper.findAllByUserId(userId, null, false).stream()
        .filter(a -> accountId.equals(a.getAccountId()))
        .findFirst()
        .orElseThrow();

    assertEquals(1_900_000L, account.getBalance());
  }

  @Test
  @DisplayName("잔액을 넘는 금액은 차감되지 않는다")
  void decreaseBalance_insufficientAmount_notApplied() {
    accountMapper.upsert(createAccount(10_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    int updated = accountMapper.decreaseBalance(accountId, userId, 20_000L);

    assertEquals(0, updated);
    assertEquals(10_000L, accountMapper.findByIdAndUserId(accountId, userId).getBalance());
  }

  @Test
  @DisplayName("잔액 범위 안이면 차감된다")
  void decreaseBalance_success() {
    accountMapper.upsert(createAccount(10_000L));
    Long accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    int updated = accountMapper.decreaseBalance(accountId, userId, 4_000L);

    assertEquals(1, updated);
    assertEquals(6_000L, accountMapper.findByIdAndUserId(accountId, userId).getBalance());
  }

  @Test
  @DisplayName("excludeGoalLinked면 목표에 연결된 계좌는 빠진다")
  void findAllByUserId_excludeGoalLinked() {
    Long linkedExAccountId = 999202L;
    Long freeExAccountId = 999203L;
    accountMapper.upsert(exAccount(linkedExAccountId, "hash-999202", "1111*****11"));
    accountMapper.upsert(exAccount(freeExAccountId, "hash-999203", "2222*****22"));
    Long linkedAccountId = accountMapper.findAccountIdByExAccountId(linkedExAccountId);
    Long freeAccountId = accountMapper.findAccountIdByExAccountId(freeExAccountId);

    Goal goal = Goal.builder()
        .userId(userId)
        .goalType(GoalType.WEDDING)
        .goalName("결혼 자금")
        .goalAmount(10_000_000L)
        .startAmount(0L)
        .goalDate(LocalDate.of(2027, 1, 1))
        .startDate(LocalDate.of(2026, 1, 1))
        .goalStatus(GoalStatus.ACTIVE)
        .isCollected(false)
        .build();
    goalMapper.save(goal);
    goalAssetMapper.saveAll(goal.getGoalId(), List.of(
        GoalAssetRequest.builder().assetType(AssetType.ACCOUNT).assetId(linkedAccountId).build()
    ));

    List<AccountResponse> free = accountMapper.findAllByUserId(userId, "CHECKING", true);

    assertTrue(free.stream().anyMatch(a -> freeAccountId.equals(a.getAccountId())));
    assertFalse(free.stream().anyMatch(a -> linkedAccountId.equals(a.getAccountId())));
  }

  @Test
  @DisplayName("excludeGoalLinked면 목표 저금통이 올라간 계좌도 빠진다")
  void findAllByUserId_excludeGoalLinked_moneyBoxHostAccount() {
    Long boxHostExAccountId = 999204L;
    Long freeExAccountId = 999205L;
    accountMapper.upsert(exAccount(boxHostExAccountId, "hash-999204", "3333*****33"));
    accountMapper.upsert(exAccount(freeExAccountId, "hash-999205", "4444*****44"));
    Long boxHostAccountId = accountMapper.findAccountIdByExAccountId(boxHostExAccountId);
    Long freeAccountId = accountMapper.findAccountIdByExAccountId(freeExAccountId);

    // 목표 저금통을 boxHostAccount에 만든다 — goal_asset에는 MONEY_BOX만 잡힌다
    MoneyBox goalBox = MoneyBox.builder()
        .userId(userId).accountId(boxHostAccountId).balance(0L)
        .moneyBoxType(MoneyBoxType.GOAL).build();
    moneyBoxMapper.insert(goalBox);

    Goal goal = Goal.builder()
        .userId(userId)
        .goalType(GoalType.EMERGENCY)
        .goalName("비상금")
        .goalAmount(5_000_000L)
        .startAmount(0L)
        .goalDate(LocalDate.of(2027, 1, 1))
        .startDate(LocalDate.of(2026, 1, 1))
        .goalStatus(GoalStatus.ACTIVE)
        .isCollected(false)
        .build();
    goalMapper.save(goal);
    goalAssetMapper.saveAll(goal.getGoalId(), List.of(
        GoalAssetRequest.builder()
            .assetType(AssetType.MONEY_BOX).assetId(goalBox.getMoneyBoxId()).build()
    ));

    List<AccountResponse> free = accountMapper.findAllByUserId(userId, "CHECKING", true);

    assertTrue(free.stream().anyMatch(a -> freeAccountId.equals(a.getAccountId())));
    assertFalse(free.stream().anyMatch(a -> boxHostAccountId.equals(a.getAccountId())));
  }

  private Account exAccount(Long exAccountId, String hash, String masked) {
    return Account.of(
        userId, financialInstitutionId, exAccountId, "CHECKING", "KB 입출금통장",
        new byte[]{1, 2, 3}, hash, masked, 1_000_000L, "ACTIVE",
        LocalDate.of(2020, 1, 1), null, new BigDecimal("0.1000"), null
    );
  }

  private Account createAccount(Long balance) {
    return Account.of(
        userId,
        financialInstitutionId,
        EX_ACCOUNT_ID,
        "CHECKING",
        "KB 입출금통장",
        new byte[]{1, 2, 3},
        "hash-999201",
        "1234*****90",
        balance,
        "ACTIVE",
        LocalDate.of(2020, 1, 1),
        null,
        new BigDecimal("0.1000"),
        null
    );
  }
}
