package org.jejuro.miraero.domain.mydata.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.domain.mydata.client.MyDataApiClient;
import org.jejuro.miraero.domain.mydata.dto.external.MyDataTransferRequest;
import org.jejuro.miraero.domain.mydata.exception.MyDataErrorCode;
import org.jejuro.miraero.domain.user.domain.User;
import org.jejuro.miraero.domain.user.mapper.UserMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountTransferServiceImpl implements AccountTransferService {

  private final UserMapper userMapper;
  private final MyDataTokenProvider myDataTokenProvider;
  private final AccountMapper accountMapper;
  private final MyDataApiClient myDataApiClient;

  @Override
  public void transfer(Long userId, Long withdrawalAccountId, Long depositAccountId, Long amount) {
    // 조회는 동기화해둔 로컬 데이터로 되지만 이체는 매번 외부 호출이 필요하다.
    // 토큰이 만료됐으면 재연동을 요구하는 대신 자동으로 재발급받는다.
    String accessToken = myDataTokenProvider.getValidToken(userId);

    User user = userMapper.findById(userId);

    // 목서버는 자기 쪽 계좌 ID(ex_account_id)로만 계좌를 식별한다
    Account withdrawalAccount = findOwnedAccount(withdrawalAccountId, userId);
    Account depositAccount = findOwnedAccount(depositAccountId, userId);

    myDataApiClient.transfer(
        MyDataTransferRequest.builder()
            .kbUserId(user.getKbPayId())
            .withdrawalAccountId(withdrawalAccount.getExAccountId())
            .depositAccountId(depositAccount.getExAccountId())
            .amount(amount)
            .transactedAt(LocalDateTime.now())
            .build(),
        accessToken
    );
  }

  private Account findOwnedAccount(Long accountId, Long userId) {
    Account account = accountMapper.findByIdAndUserId(accountId, userId);

    if (account == null) {
      throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
    }

    return account;
  }
}
