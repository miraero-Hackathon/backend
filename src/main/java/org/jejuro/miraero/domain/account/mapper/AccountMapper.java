package org.jejuro.miraero.domain.account.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.jejuro.miraero.domain.account.domain.Account;
import org.jejuro.miraero.domain.account.dto.response.AccountResponse;

@Mapper
public interface AccountMapper {

  Long findBalanceByIdAndUserIdForUpdate(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId
  );

  // 다른 도메인이 출금계좌 등으로 참조하는 계좌 ID의 소유권을 확인할 때 사용
  Account findByIdAndUserId(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId
  );

  boolean existsByIdAndUserId(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId
  );

  List<AccountResponse> findAllByUserId(
      @Param("userId") Long userId,
      @Param("accountType") String accountType,
      @Param("excludeGoalLinked") boolean excludeGoalLinked
  );

  // 소유권 확인이 끝난 뒤(예: goal_asset 조회) 은행명까지 포함한 상세 조회용
  AccountResponse findResponseById(@Param("accountId") Long accountId);

  // 외부 요청(컨트롤러)에서 계좌 단건 상세를 조회할 때 — 소유권 검증 포함
  AccountResponse findResponseByIdAndUserId(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId
  );

  int increaseBalance(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId,
      @Param("amount") Long amount
  );

  int decreaseBalance(
      @Param("accountId") Long accountId,
      @Param("userId") Long userId,
      @Param("amount") Long amount
  );

  // ex_account_id 기준 upsert — 동기화 재실행 시 중복 없이 갱신
  int upsert(Account account);

  // 외부 계좌 ID로 내부 account_id 조회 (거래 동기화 시 FK 연결용)
  Long findAccountIdByExAccountId(@Param("exAccountId") Long exAccountId);

  List<AccountResponse> findResponsesByIds(
          @Param("accountIds") List<Long> accountIds,
          @Param("userId") Long userId
  );
}
