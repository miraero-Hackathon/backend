package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "예금 금리 옵션")
public class DepositOptionResponse {

    @ApiModelProperty(value = "금리 옵션 ID")
    private Long depositOptionId;
    @ApiModelProperty(value = "금리 유형", example = "단리")
    private String interestRateType;
    @ApiModelProperty(value = "저축 기간(개월)", example = "12")
    private Integer saveTerm;
    @ApiModelProperty(value = "기본 금리(%)", example = "3.00")
    private BigDecimal baseInterestRate;
    @ApiModelProperty(value = "최고 우대금리(%)", example = "3.50")
    private BigDecimal maxInterestRate;
}
