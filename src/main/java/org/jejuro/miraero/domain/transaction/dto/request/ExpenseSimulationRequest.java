package org.jejuro.miraero.domain.transaction.dto.request;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "지출 시뮬레이션 요청")
public class ExpenseSimulationRequest {

    @NotNull
    @Min(2000)
    @ApiModelProperty(value = "시뮬레이션 연도", required = true, example = "2026")
    private Integer year;

    @NotNull
    @Min(1)
    @Max(12)
    @ApiModelProperty(value = "시뮬레이션 월(1~12)", required = true, example = "8")
    private Integer month;

    @NotEmpty
    @Valid
    @ApiModelProperty(value = "카테고리별 목표 지출 목록", required = true)
    private List<ExpenseSimulationCategoryRequest> categories;
}
