package org.jejuro.miraero.domain.goal.dto.request;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoalUpdateRequest {
    private String goalName;
    private Long goalAmount;
    private Integer goalMonths;
}
