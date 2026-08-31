package org.jejuro.miraero.domain.pacemaker.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaceMakerHistoryResponse {

  private String date;
  private String status;
  private Long amount;
  private String description;
}
