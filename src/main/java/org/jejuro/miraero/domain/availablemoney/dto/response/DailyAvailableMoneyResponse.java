package org.jejuro.miraero.domain.availablemoney.dto.response;


import lombok.Builder;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Getter;

@Getter
@Builder
@ApiModel(description = "목표 일일 가용 금액 정보")
public class DailyAvailableMoneyResponse {
    @ApiModelProperty(value = "오늘 사용 가능한 금액(원)")
    private Long todayAvailableMoney;
    @ApiModelProperty(value = "오늘 지출 금액(원)")
    private Long todayExpense;
    @ApiModelProperty(value = "이번 기간 남은 가용 금액(원)")
    private Long remainingAvailableMoney;
}
