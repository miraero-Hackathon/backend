package org.jejuro.miraero.domain.goal.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.PaceStatus;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoalPaceResponse {
    private Long expectedAmount;
    private Long differenceAmount;
    private PaceStatus paceStatus;
}
