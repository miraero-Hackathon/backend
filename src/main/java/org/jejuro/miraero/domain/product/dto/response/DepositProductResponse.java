package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "예금 상품 목록 항목")
public class DepositProductResponse {

    @ApiModelProperty(value = "예금 상품 ID", example = "1")
    private Long depositProductId;
    @ApiModelProperty(value = "금융회사명", example = "국민은행")
    private String financialInstitutionName;
    @ApiModelProperty(value = "상품명", example = "정기예금")
    private String productName;
    @ApiModelProperty(value = "최고 우대금리(%)", example = "3.50")
    private BigDecimal maxInterestRate;
    @ApiModelProperty(value = "가입 가능 기간(개월)", example = "[6, 12, 24]")
    private List<Integer> saveTerms;
    @ApiModelProperty(value = "최고 가입 한도(원)", example = "50000000")
    private Long maxLimit;
    @ApiModelProperty(value = "가입 방법", example = "영업점, 인터넷")
    private String joinMethod;
    @ApiModelProperty(value = "가입 제한 여부", example = "false")
    private Boolean hasJoinRestriction;
    @ApiModelProperty(value = "우대 조건 존재 여부", example = "true")
    private Boolean hasSpecialCondition;
}
