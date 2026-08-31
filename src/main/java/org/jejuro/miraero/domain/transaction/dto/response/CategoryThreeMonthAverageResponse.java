package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "카테고리별 최근 3개월 평균 지출")
public class CategoryThreeMonthAverageResponse {

    @ApiModelProperty(value = "평균 산정 시작 월(YYYY-MM)", example = "2026-05")
    private String startMonth;
    @ApiModelProperty(value = "평균 산정 종료 월(YYYY-MM)", example = "2026-07")
    private String endMonth;
    @ApiModelProperty(value = "카테고리별 월평균 지출 목록")
    private List<CategoryThreeMonthAverageItemResponse> categories;
}
