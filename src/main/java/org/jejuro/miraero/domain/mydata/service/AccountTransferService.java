package org.jejuro.miraero.domain.mydata.service;

public interface AccountTransferService {

  /**
   * 사용자 소유의 두 계좌 사이에 실제 이체를 실행한다.
   * 로컬 잔액 반영은 호출하는 쪽 책임이다 — 이 메서드는 목서버 이체만 담당한다.
   */
  void transfer(Long userId, Long withdrawalAccountId, Long depositAccountId, Long amount);
}
