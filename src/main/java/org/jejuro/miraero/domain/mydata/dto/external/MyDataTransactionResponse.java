package org.jejuro.miraero.domain.mydata.dto.external;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class MyDataTransactionResponse {

  private Long transactionId;
  private Long kbUserId;
  private Long accountId;
  private Long cardId;
  private Long prepaidInstrumentId;
  private String transactionType;
  private Long amount;
  private Long balanceAfter;
  private LocalDateTime transactedAt;
  private String merchantName;
  private String categoryName;
}
