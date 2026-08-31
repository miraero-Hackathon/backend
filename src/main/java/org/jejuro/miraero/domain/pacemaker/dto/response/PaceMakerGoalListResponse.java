package org.jejuro.miraero.domain.pacemaker.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PaceMakerGoalListResponse {

  private List<PaceMakerGoalResponse> goals;
}
