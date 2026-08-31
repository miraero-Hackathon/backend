package org.jejuro.miraero.domain.transaction.dto.request;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "카테고리별 월간 지출 목표 저장 요청")
public class ExpenseCategoryTargetSaveRequest {

    @NotEmpty
    @Valid
    @ApiModelProperty(value = "저장할 카테고리별 목표 목록", required = true)
    private List<ExpenseCategoryTargetItemRequest> targets;
}
