package org.jejuro.miraero.domain.transaction.domain;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

// 외부(mock-server) 거래 데이터를 로컬 DB에 동기화하기 위한 파라미터 객체.
// accountId/expenseCategoryId는 각각 계좌 매퍼, 기준 데이터 매퍼가 미리 해석해서 넘겨준다는 전제.
@Getter
@AllArgsConstructor
public class TransactionSyncCommand {

  private Long userId;
  private Long accountId;
  private Long expenseCategoryId;
  private Long exTransactionId; // 외부 거래 ID — user_id와 함께 멱등 upsert의 기준이 됨
  private String transactionType;
  private Long amount;
  private Long balanceAfter;
  private LocalDateTime transactedAt;
  private String merchantName;
}
