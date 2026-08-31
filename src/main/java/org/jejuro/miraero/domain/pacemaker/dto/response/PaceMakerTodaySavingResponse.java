package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerTodaySavingResponse {

  private boolean saved;
  private Long amount;
}
