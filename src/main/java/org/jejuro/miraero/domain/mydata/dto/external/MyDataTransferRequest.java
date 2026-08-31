package org.jejuro.miraero.domain.mydata.dto.external;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MyDataTransferRequest {

  private Long kbUserId;
  private Long withdrawalAccountId;
  private Long depositAccountId;
  private Long amount;
  private LocalDateTime transactedAt;
  private String merchantName;
  private String categoryName;
}
