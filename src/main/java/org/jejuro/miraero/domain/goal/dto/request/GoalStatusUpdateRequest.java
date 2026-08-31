package org.jejuro.miraero.domain.goal.dto.request;


import lombok.Getter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.NoArgsConstructor;
import org.jejuro.miraero.domain.goal.domain.GoalStatus;

import javax.validation.constraints.NotNull;

@Getter
@NoArgsConstructor
@ApiModel(description = "목표 상태 변경 요청")
public class GoalStatusUpdateRequest {

    @ApiModelProperty(value = "변경할 목표 상태", required = true)
    @NotNull
    private GoalStatus status;
}
