package org.jejuro.miraero.domain.account.service;

import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;
import org.jejuro.miraero.domain.account.exception.AccountErrorCode;
import org.jejuro.miraero.domain.account.mapper.AccountMapper;
import org.jejuro.miraero.global.exception.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

  // account 테이블 ck_account_type 제약과 동일한 값 집합
  private static final Set<String> VALID_ACCOUNT_TYPES = Set.of(
      "CHECKING", "SAVINGS", "DEPOSIT", "INSTALLMENT", "ISA", "CMA"
  );

  private final AccountMapper accountMapper;

  @Override
  public AccountListResponse getAccounts(Long userId, String accountType) {
    return getAccounts(userId, accountType, false);
  }

  @Override
  public AccountListResponse getAccounts(Long userId, String accountType, boolean excludeGoalLinked) {
    if (accountType != null && !VALID_ACCOUNT_TYPES.contains(accountType)) {
      throw new BusinessException(AccountErrorCode.INVALID_ACCOUNT_TYPE);
    }

    List<AccountResponse> accounts =
        accountMapper.findAllByUserId(userId, accountType, excludeGoalLinked);
    long totalBalance = accounts.stream()
        .mapToLong(AccountResponse::getBalance)
        .sum();

    return AccountListResponse.builder()
        .totalBalance(totalBalance)
        .accounts(accounts)
        .build();
  }

  @Override
  public AccountResponse findAccount(Long accountId) {
    return accountMapper.findResponseById(accountId);
  }

  @Override
  public AccountResponse getAccount(Long accountId, Long userId) {
    AccountResponse account = accountMapper.findResponseByIdAndUserId(accountId, userId);
    if (account == null) {
      throw new BusinessException(AccountErrorCode.ACCOUNT_NOT_FOUND);
    }
    return account;
  }
}
