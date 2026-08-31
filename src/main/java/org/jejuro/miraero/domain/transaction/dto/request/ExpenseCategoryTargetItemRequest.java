package org.jejuro.miraero.domain.transaction.dto.request;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "카테고리별 월간 지출 목표 항목")
public class ExpenseCategoryTargetItemRequest {

    @NotNull
    @Positive
    @ApiModelProperty(value = "지출 카테고리 ID", required = true, example = "1")
    private Long categoryId;

    @NotNull
    @PositiveOrZero
    @ApiModelProperty(value = "월간 목표 지출 금액(원)", required = true, example = "300000")
    private Long targetAmount;
}
