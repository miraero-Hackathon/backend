package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerMaxAmountUpdateResponse {

  private Long autoSavingId;
  private Long maxAmount;
}
