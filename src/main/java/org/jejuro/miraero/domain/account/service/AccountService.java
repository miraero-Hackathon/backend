package org.jejuro.miraero.domain.account.service;

import org.jejuro.miraero.domain.account.dto.response.AccountListResponse;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;

public interface AccountService {

  AccountListResponse getAccounts(Long userId, String accountType);

  // 끌어쓰기 출처 후보 조회용. true면 이미 목표에 연결된 계좌를 제외한다
  AccountListResponse getAccounts(Long userId, String accountType, boolean excludeGoalLinked);

  // 목표 자산 등 이미 소유권이 검증된 관계를 통해 내부적으로 조회할 때 사용 (userId 검증 없음)
  AccountResponse findAccount(Long accountId);

  // 클라이언트가 직접 accountId를 지정해 조회하는 경로 — 소유권 검증 포함
  AccountResponse getAccount(Long accountId, Long userId);
}
