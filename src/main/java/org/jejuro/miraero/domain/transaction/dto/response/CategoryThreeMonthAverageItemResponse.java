package org.jejuro.miraero.domain.transaction.dto.response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "카테고리별 최근 3개월 월평균 지출 항목")
public class CategoryThreeMonthAverageItemResponse {

    @ApiModelProperty(value = "카테고리 ID", example = "1")
    private Long categoryId;
    @ApiModelProperty(value = "카테고리명", example = "식비")
    private String categoryName;
    @ApiModelProperty(value = "월평균 지출 금액(원)", example = "280000")
    private Long averageMonthlyAmount;
}
