package org.jejuro.miraero.domain.mydata.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.mydata.client.MyDataApiClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransferRequest;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AccountTransferServiceImplTest {

  private static final Long USER_ID = 1L;
  private static final Long WITHDRAWAL_ACCOUNT_ID = 10L;
  private static final Long DEPOSIT_ACCOUNT_ID = 20L;
  private static final Long WITHDRAWAL_EX_ACCOUNT_ID = 900L;
  private static final Long DEPOSIT_EX_ACCOUNT_ID = 901L;

  @Mock
  private UserMapper userMapper;

  @Mock
  private MyDataTokenProvider myDataTokenProvider;

  @Mock
  private AccountMapper accountMapper;

  @Mock
  private MyDataApiClient myDataApiClient;

  @InjectMocks
  private AccountTransferServiceImpl accountTransferService;

  @Test
  @DisplayName("계좌 소유자의 accessToken과 mock 서버 계좌 ID로 이체를 요청한다")
  void transfer_success() {
    when(myDataTokenProvider.getValidToken(USER_ID)).thenReturn("token");
    when(userMapper.findById(USER_ID)).thenReturn(userWithKbPayId(500L));
    when(accountMapper.findByIdAndUserId(WITHDRAWAL_ACCOUNT_ID, USER_ID))
        .thenReturn(accountWithExId(WITHDRAWAL_EX_ACCOUNT_ID));
    when(accountMapper.findByIdAndUserId(DEPOSIT_ACCOUNT_ID, USER_ID))
        .thenReturn(accountWithExId(DEPOSIT_EX_ACCOUNT_ID));

    accountTransferService.transfer(USER_ID, WITHDRAWAL_ACCOUNT_ID, DEPOSIT_ACCOUNT_ID, 50_000L);

    verify(myDataApiClient).transfer(argThatMatches(), anyString());
  }

  @Test
  @DisplayName("연동 이력이 없으면 재발급도 실패하므로 이체할 수 없다")
  void transfer_notConnected_throws() {
    when(myDataTokenProvider.getValidToken(USER_ID))
        .thenThrow(new BusinessException(MyDataErrorCode.MYDATA_NOT_CONNECTED));

    BusinessException exception = assertThrows(BusinessException.class,
        () -> accountTransferService.transfer(
            USER_ID, WITHDRAWAL_ACCOUNT_ID, DEPOSIT_ACCOUNT_ID, 50_000L));

    assertEquals(MyDataErrorCode.MYDATA_NOT_CONNECTED, exception.getErrorCode());
    verify(myDataApiClient, never()).transfer(any(), anyString());
  }

  private User userWithKbPayId(Long kbPayId) {
    return User.create("테스트", null, null, null, "test@test.com", "hash", kbPayId);
  }

  private Account accountWithExId(Long exAccountId) {
    return Account.of(
        USER_ID, 1L, exAccountId, "CHECKING", "테스트통장",
        new byte[]{1}, "hash", "masked", 1_000_000L, "ACTIVE", null, null, null, null);
  }

  private MyDataTransferRequest argThatMatches() {
    return org.mockito.ArgumentMatchers.argThat(request ->
        request.getWithdrawalAccountId().equals(WITHDRAWAL_EX_ACCOUNT_ID)
            && request.getDepositAccountId().equals(DEPOSIT_EX_ACCOUNT_ID)
            && request.getAmount().equals(50_000L));
  }
}
