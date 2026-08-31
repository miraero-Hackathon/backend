package org.jejuro.miraero.domain.pacemaker.mapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBox;
import org.jejuro.miraero.domain.moneybox.domain.MoneyBoxType;
import org.jejuro.miraero.domain.moneybox.mapper.MoneyBoxMapper;
import org.jejuro.miraero.domain.mydata.mapper.ReferenceDataMapper;
import org.jejuro.miraero.domain.pacemaker.domain.AutoSaving;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.config.RootConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.annotation.Transactional;

@SpringJUnitConfig
@ContextConfiguration(classes = RootConfig.class)
@Transactional
@Rollback
class PaceMakerCreateMapperTest {

  private static final Long EX_ACCOUNT_ID = 999501L;

  @Autowired
  private PaceMakerMapper paceMakerMapper;

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
        "페이스메이커테스트", LocalDate.of(2000, 1, 1), "테스트회사", 3_000_000L,
        "pace-maker-test@test.com", "hash", null
    );
    userMapper.save(user);
    this.userId = user.getUserId();

    accountMapper.upsert(Account.of(
        userId,
        referenceDataMapper.findFinancialInstitutionIdByCode("004"),
        EX_ACCOUNT_ID, "CHECKING", "KB 입출금통장",
        new byte[]{1, 2, 3}, "hash-999501", "1234*****90",
        3_000_000L, "ACTIVE", LocalDate.of(2023, 1, 1), null,
        new BigDecimal("0.1000"), null
    ));
    this.accountId = accountMapper.findAccountIdByExAccountId(EX_ACCOUNT_ID);

    MoneyBox moneyBox = MoneyBox.builder()
        .userId(userId)
        .accountId(accountId)
        .balance(0L)
        .moneyBoxType(MoneyBoxType.SAVING)
        .build();
    moneyBoxMapper.insert(moneyBox);
    this.moneyBoxId = moneyBox.getMoneyBoxId();
  }

  @Test
  @DisplayName("페이스메이커를 저장하면 ACTIVE 상태로 조회된다")
  void insertAutoSaving_savedAsActive() {
    paceMakerMapper.insertAutoSaving(userId, moneyBoxId, accountId, 30_000L);

    AutoSaving saved = paceMakerMapper.findByUserId(userId);

    assertNotNull(saved);
    assertEquals(moneyBoxId, saved.getMoneyBoxId());
    assertEquals(accountId, saved.getAccountId());
    assertEquals(30_000L, saved.getMaxAmount());
    assertEquals("ACTIVE", saved.getAutoSavingStatus());
  }

  @Test
  @DisplayName("상한 없이 저장하면 maxAmount는 null이다")
  void insertAutoSaving_nullMaxAmount() {
    paceMakerMapper.insertAutoSaving(userId, moneyBoxId, accountId, null);

    assertNull(paceMakerMapper.findByUserId(userId).getMaxAmount());
  }

  @Test
  @DisplayName("사용자당 하나만 저장할 수 있다")
  void insertAutoSaving_duplicateUser_rejected() {
    paceMakerMapper.insertAutoSaving(userId, moneyBoxId, accountId, null);

    MoneyBox other = MoneyBox.builder()
        .userId(userId).accountId(accountId).balance(0L)
        .moneyBoxType(MoneyBoxType.SAVING).build();
    moneyBoxMapper.insert(other);

    assertThrows(DuplicateKeyException.class,
        () -> paceMakerMapper.insertAutoSaving(
            userId, other.getMoneyBoxId(), accountId, null));
  }
}
