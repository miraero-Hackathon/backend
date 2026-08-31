package org.jejuro.miraero.domain.product.dto.response;

import java.math.BigDecimal;
import java.util.List;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
@ApiModel(description = "적금 상품 목록 항목")
public class SavingProductResponse {

    @ApiModelProperty(value = "적금 상품 ID", example = "1")
    private Long savingProductId;
    @ApiModelProperty(value = "금융회사명")
    private String financialInstitutionName;
    @ApiModelProperty(value = "상품명")
    private String productName;
    @ApiModelProperty(value = "최고 우대금리(%)")
    private BigDecimal highestInterestRate;
    @ApiModelProperty(value = "가입 가능 기간(개월)")
    private List<Integer> saveTerms;
    @ApiModelProperty(value = "납입 방식 목록")
    private List<String> reserveTypes;
    @ApiModelProperty(value = "최고 가입 한도(원)")
    private Long maxLimit;
    @ApiModelProperty(value = "가입 방법")
    private String joinMethod;
    @ApiModelProperty(value = "가입 제한 여부")
    private Boolean hasJoinRestriction;
    @ApiModelProperty(value = "우대 조건 존재 여부")
    private Boolean hasSpecialCondition;
}
