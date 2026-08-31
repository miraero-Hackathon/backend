package org.jejuro.miraero.domain.account.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountServiceImplTest {

  private static final Long USER_ID = 1L;

  @Mock
  private AccountMapper accountMapper;

  @InjectMocks
  private AccountServiceImpl accountService;

  @Test
  @DisplayName("사용자의 전체 계좌 목록과 잔액 합계를 반환한다")
  void getAccounts_returnsAllAccountsWithTotalBalance() {
    List<AccountResponse> accounts = List.of(
        AccountResponse.builder().accountId(1L).accountType("CHECKING").balance(3_000_000L).build(),
        AccountResponse.builder().accountId(2L).accountType("SAVINGS").balance(5_000_000L).build()
    );
    when(accountMapper.findAllByUserId(USER_ID, null, false)).thenReturn(accounts);

    AccountListResponse response = accountService.getAccounts(USER_ID, null);

    assertEquals(2, response.getAccounts().size());
    assertEquals(8_000_000L, response.getTotalBalance());
  }

  @Test
  @DisplayName("accountType이 주어지면 그대로 매퍼에 전달한다")
  void getAccounts_passesAccountTypeFilter() {
    when(accountMapper.findAllByUserId(USER_ID, "SAVINGS", false)).thenReturn(List.of());

    accountService.getAccounts(USER_ID, "SAVINGS");

    verify(accountMapper).findAllByUserId(USER_ID, "SAVINGS", false);
  }

  @Test
  @DisplayName("허용되지 않은 accountType이면 예외를 던지고 매퍼를 호출하지 않는다")
  void getAccounts_invalidAccountType_throws() {
    assertThrows(BusinessException.class, () -> accountService.getAccounts(USER_ID, "INVALID"));

    verify(accountMapper, never()).findAllByUserId(any(), any(), anyBoolean());
  }

  @Test
  @DisplayName("계좌 ID로 상세정보를 조회한다 (내부용, 소유권 검증 없음)")
  void findAccount_returnsDetail() {
    AccountResponse account = AccountResponse.builder().accountId(1L).balance(500_000L).build();
    when(accountMapper.findResponseById(1L)).thenReturn(account);

    AccountResponse response = accountService.findAccount(1L);

    assertEquals(500_000L, response.getBalance());
  }

  @Test
  @DisplayName("존재하지 않는 계좌면 null을 반환한다 (내부용)")
  void findAccount_notFound_returnsNull() {
    when(accountMapper.findResponseById(999L)).thenReturn(null);

    assertNull(accountService.findAccount(999L));
  }

  @Test
  @DisplayName("소유자가 맞으면 계좌 상세를 반환한다")
  void getAccount_success() {
    AccountResponse account = AccountResponse.builder().accountId(1L).balance(500_000L).build();
    when(accountMapper.findResponseByIdAndUserId(1L, USER_ID)).thenReturn(account);

    AccountResponse response = accountService.getAccount(1L, USER_ID);

    assertEquals(500_000L, response.getBalance());
  }

  @Test
  @DisplayName("내 소유가 아니거나 존재하지 않으면 예외를 던진다")
  void getAccount_notOwned_throws() {
    when(accountMapper.findResponseByIdAndUserId(1L, USER_ID)).thenReturn(null);

    assertThrows(BusinessException.class, () -> accountService.getAccount(1L, USER_ID));
  }
}
