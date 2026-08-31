package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "적금 금리 옵션")
public class SavingOptionResponse {

    @ApiModelProperty(value = "금리 옵션 ID")
    private Long savingOptionId;
    @ApiModelProperty(value = "금리 유형")
    private String interestRateType;
    @ApiModelProperty(value = "납입 방식")
    private String reserveType;
    @ApiModelProperty(value = "저축 기간(개월)")
    private Integer saveTerm;
    @ApiModelProperty(value = "기본 금리(%)")
    private BigDecimal baseInterestRate;
    @ApiModelProperty(value = "최고 우대금리(%)")
    private BigDecimal maxInterestRate;
}
