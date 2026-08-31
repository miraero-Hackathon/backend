package org.jejuro.miraero.domain.transaction.dto.response;

import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "현재 기준 지출 분석 대시보드")
public class ExpenseDashboardResponse {
    @ApiModelProperty(value = "분석 기준 연도", example = "2026")
    private Integer year;
    @ApiModelProperty(value = "분석 기준 월", example = "8")
    private Integer month;
    @ApiModelProperty(value = "기준 월 직전 3개월 카테고리별 월평균")
    private CategoryThreeMonthAverageResponse categoryThreeMonthAverages;
    @ApiModelProperty(value = "기준 월과 전월의 카테고리별 지출 변화")
    private List<CategoryMonthChangeResponse> categoryMonthChanges;
}
